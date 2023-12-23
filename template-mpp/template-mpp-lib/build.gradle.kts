@file:Suppress("UnusedVariable", "unused")

import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.android.build.api.dsl.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
    plugAll(
        plugs.KotlinMulti,
        plugs.Compose,
        plugs.MavenPublish,
        plugs.Signing,
    )
    plug(plugs.AndroLibNoVer) apply false // will be applied conditionally depending on LibSettings
}

// workaround for crazy gradle bugs like this one or similar:
// https://youtrack.jetbrains.com/issue/KT-43500/KJS-IR-Failed-to-resolve-Kotlin-library-on-attempting-to-resolve-compileOnly-transitive-dependency-from-direct-dependency
repositories { maven(repos.composeJbDev) }


defaultBuildTemplateForFullMppLib {
// workaround for crazy gradle bugs like this one or similar:
// https://youtrack.jetbrains.com/issue/KT-43500/KJS-IR-Failed-to-resolve-Kotlin-library-on-attempting-to-resolve-compileOnly-transitive-dependency-from-direct-dependency
    implementation(KotlinX.coroutines_core)
}

// region [Full MPP Lib Build Template]

fun Project.defaultBuildTemplateForFullMppLib(
    details: LibDetails = rootExtLibDetails,
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
    if (details.settings.withAndro) {
        apply(plugin = plugs.AndroLibNoVer.group) // group is actually id for plugins
        // TODO_later: try to move the rest of andro config from below here
    }
    defaultBuildTemplateForComposeMppLib(
        details = details,
        ignoreAndroConfig = true, // andro configured below
        addCommonMainDependencies = addCommonMainDependencies,
    )

    if (details.settings.withAndro) {
        extensions.configure<LibraryExtension> {
            defaultAndroLib(ignoreCompose = true) // compose mpp configured already
        }

        // this is "single platform way" / "android way" to declare deps,
        // it would be more "correct" to configure everything "mpp way" (android deps too),
        // but it's more important to reuse andro related functions like "fun defaultAndroDeps"
        // (trust me future Marek: I've tried this already :) )
        dependencies {
            // ignoreCompose because we have compose configured mpp way already.
            defaultAndroDeps(details.settings, ignoreCompose = true)
            defaultAndroTestDeps(details.settings, ignoreCompose = true)
        }
    }
}

// endregion [Full MPP Lib Build Template]


// region [Kotlin Module Build Template]

fun RepositoryHandler.addRepos(settings: LibReposSettings) = with(settings) {
    if (withMavenLocal) mavenLocal()
    if (withMavenCentral) mavenCentral()
    if (withGradle) gradlePluginPortal()
    if (withGoogle) google()
    if (withKotlinx) maven(repos.kotlinx)
    if (withKotlinxHtml) maven(repos.kotlinxHtml)
    if (withComposeJbDev) maven(repos.composeJbDev)
    if (withComposeCompilerAxDev) maven(repos.composeCompilerAxDev)
    if (withKtorEap) maven(repos.ktorEap)
    if (withJitpack) maven(repos.jitpack)
}

fun TaskCollection<Task>.defaultKotlinCompileOptions(
    jvmTargetVer: String? = vers.JvmDefaultVer,
    renderInternalDiagnosticNames: Boolean = false,
) = withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTargetVer?.let { jvmTarget = it }
        if (renderInternalDiagnosticNames) freeCompilerArgs = freeCompilerArgs + "-Xrender-internal-diagnostic-names"
        // useful, for example, to suppress some errors when accessing internal code from some library, like:
        // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
    }
}

fun TaskCollection<Task>.defaultTestsOptions(
    printStandardStreams: Boolean = true,
    printStackTraces: Boolean = true,
    onJvmUseJUnitPlatform: Boolean = true,
) = withType<AbstractTestTask>().configureEach {
    testLogging {
        showStandardStreams = printStandardStreams
        showStackTraces = printStackTraces
    }
    if (onJvmUseJUnitPlatform) (this as? Test)?.useJUnitPlatform()
}

// Provide artifacts information requited by Maven Central
fun MavenPublication.defaultPOM(lib: LibDetails) = pom {
    name put lib.name
    description put lib.description
    url put lib.githubUrl

    licenses {
        license {
            name put lib.licenceName
            url put lib.licenceUrl
        }
    }
    developers {
        developer {
            id put lib.authorId
            name put lib.authorName
            email put lib.authorEmail
        }
    }
    scm { url put lib.githubUrl }
}

/** See also: root project template-mpp: addDefaultStuffFromSystemEnvs */
fun Project.defaultSigning(
    keyId: String = rootExtString["signing.keyId"],
    key: String = rootExtReadFileUtf8TryOrNull("signing.keyFile") ?: rootExtString["signing.key"],
    password: String = rootExtString["signing.password"],
) = extensions.configure<SigningExtension> {
    useInMemoryPgpKeys(keyId, key, password)
    sign(extensions.getByType<PublishingExtension>().publications)
}

fun Project.defaultPublishing(
    lib: LibDetails,
    readmeFile: File = File(rootDir, "README.md"),
    withSignErrorWorkaround: Boolean = true,
    withPublishingPrintln: Boolean = false, // FIXME_later: enabling brakes gradle android publications
) {

    val readmeJavadocJar by tasks.registering(Jar::class) {
        from(readmeFile) // TODO_maybe: use dokka to create real docs? (but it's not even java..)
        archiveClassifier put "javadoc"
    }

    extensions.configure<PublishingExtension> {

        // We have at least two cases:
        // 1. With plug.KotlinMulti it creates publications automatically (so no need to create here)
        // 2. With plug.KotlinJvm it does not create publications (so we have to create it manually)
        if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            publications.create<MavenPublication>("jvm") {
                from(components["kotlin"])
            }
        }

        publications.withType<MavenPublication> {
            artifact(readmeJavadocJar)
            // Adding javadoc artifact generates warnings like:
            // Execution optimizations have been disabled for task ':uspek:signJvmPublication'
            // (UPDATE: now it's errors - see workaround below)
            // It looks like a bug in kotlin multiplatform plugin:
            // https://youtrack.jetbrains.com/issue/KT-46466
            // FIXME_someday: Watch the issue.
            // If it's a bug in kotlin multiplatform then remove this comment when it's fixed.
            // Some related bug reports:
            // https://youtrack.jetbrains.com/issue/KT-47936
            // https://github.com/gradle/gradle/issues/17043

            defaultPOM(lib)
        }
    }
    if (withSignErrorWorkaround) tasks.withSignErrorWorkaround() //very much related to comments above too
    if (withPublishingPrintln) tasks.withPublishingPrintln()
}

/*
Hacky workaround for gradle error with signing+publishing on gradle 8.1-rc-1:

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':template-mpp-lib:signJvmPublication' (type 'Sign').
  - Gradle detected a problem with the following location: '/home/marek/code/kotlin/DepsKt/template-mpp/template-mpp-lib/build/libs/template-mpp-lib-0.0.02-javadoc.jar.asc'.

    Reason: Task ':template-mpp-lib:publishJsPublicationToMavenLocal' uses this output of task ':template-mpp-lib:signJvmPublication' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed.

    Possible solutions:
      1. Declare task ':template-mpp-lib:signJvmPublication' as an input of ':template-mpp-lib:publishJsPublicationToMavenLocal'.
      2. Declare an explicit dependency on ':template-mpp-lib:signJvmPublication' from ':template-mpp-lib:publishJsPublicationToMavenLocal' using Task#dependsOn.
      3. Declare an explicit dependency on ':template-mpp-lib:signJvmPublication' from ':template-mpp-lib:publishJsPublicationToMavenLocal' using Task#mustRunAfter.

    Please refer to https://docs.gradle.org/8.1-rc-1/userguide/validation_problems.html#implicit_dependency for more details about this problem.

 */
fun TaskContainer.withSignErrorWorkaround() =
    withType<AbstractPublishToMaven>().configureEach { dependsOn(withType<Sign>()) }

fun TaskContainer.withPublishingPrintln() = withType<AbstractPublishToMaven>().configureEach {
    val coordinates = publication.run { "$groupId:$artifactId:$version" }
    when (this) {
        is PublishToMavenRepository -> doFirst {
            println("Publishing $coordinates to ${repository.url}")
        }
        is PublishToMavenLocal -> doFirst {
            val localRepo = System.getenv("HOME")!! + "/.m2/repository"
            val localPath = localRepo + publication.run { "/$groupId/$artifactId".replace('.', '/') }
            println("Publishing $coordinates to $localPath")
        }
    }
}

// endregion [Kotlin Module Build Template]

// region [MPP Module Build Template]

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed. see [allDefault] kdoc for details.
 */
fun Project.defaultBuildTemplateForBasicMppLib(
    details: LibDetails = rootExtLibDetails,
    ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
    ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
    ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
    require(ignoreCompose || details.settings.compose == null) { "defaultBuildTemplateForMppLib can not configure compose stuff" }
    details.settings.andro?.let {
        require(ignoreAndroConfig) { "defaultBuildTemplateForBasicMppLib can not configure android stuff (besides just adding target)" }
        require(ignoreAndroPublish || it.publishNoVariants) { "defaultBuildTemplateForBasicMppLib can not publish android stuff YET" }
    }
    repositories { addRepos(details.settings.repos) }
    defaultGroupAndVerAndDescription(details)
    extensions.configure<KotlinMultiplatformExtension> {
        allDefault(
            settings = details.settings,
            ignoreCompose = ignoreCompose,
            ignoreAndroTarget = ignoreAndroTarget,
            ignoreAndroConfig = ignoreAndroConfig,
            ignoreAndroPublish = ignoreAndroPublish,
            addCommonMainDependencies = addCommonMainDependencies,
        )
    }
    configurations.checkVerSync()
    tasks.defaultKotlinCompileOptions(details.settings.withJvmVer)
    tasks.defaultTestsOptions(onJvmUseJUnitPlatform = details.settings.withTestJUnit5)
    if (plugins.hasPlugin("maven-publish")) {
        defaultPublishing(details)
        if (plugins.hasPlugin("signing")) defaultSigning()
        else println("MPP Module ${name}: signing disabled")
    } else println("MPP Module ${name}: publishing (and signing) disabled")
}

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed because we want to inject this code also to such build files,
 * where plugins for compose and/or android are not applied at all, so compose/android stuff should be explicitly ignored,
 * and then configured right after this call, using code from another special region (region using compose and/or andro plugin stuff).
 * Also kmp andro publishing is in the middle of big changes, so let's not support it yet, and let's wait for more clarity regarding:
 * https://youtrack.jetbrains.com/issue/KT-61575/Publishing-a-KMP-library-handles-Android-target-inconsistently-requiring-an-explicit-publishLibraryVariants-call-to-publish
 * https://youtrack.jetbrains.com/issue/KT-60623/Deprecate-publishAllLibraryVariants-in-kotlin-android
 */
fun KotlinMultiplatformExtension.allDefault(
    settings: LibSettings,
    ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
    ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
    ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) = with(settings) {
    require(ignoreCompose || compose == null) { "allDefault can not configure compose stuff" }
    andro?.let {
        require(ignoreAndroConfig) { "allDefault can not configure android stuff (besides just adding target)" }
        require(ignoreAndroPublish || it.publishNoVariants) { "allDefault can not publish android stuff YET" }
    }
    if (withJvm) jvm()
    if (withJs) jsDefault()
    if (withNativeLinux64) linuxX64()
    if (withAndro && !ignoreAndroTarget) androidTarget {
        // TODO_someday some kmp andro publishing. See kdoc above why not yet.
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                if (withKotlinxHtml) implementation(KotlinX.html)
                addCommonMainDependencies()
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                if (withTestUSpekX) implementation(Langiewicz.uspekx)
            }
        }
        if (withJvm) {
            val jvmTest by getting {
                dependencies {
                    if (withTestJUnit4) implementation(JUnit.junit)
                    if (withTestJUnit5) implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
                    if (withTestUSpekX) {
                        implementation(Langiewicz.uspekx)
                        if (withTestJUnit4) implementation(Langiewicz.uspekx_junit4)
                        if (withTestJUnit5) implementation(Langiewicz.uspekx_junit5)
                    }
                    if (withTestGoogleTruth) implementation(Com.Google.Truth.truth)
                    if (withTestMockitoKotlin) implementation(Org.Mockito.Kotlin.mockito_kotlin)
                }
            }
        }
        if (withNativeLinux64) {
            val linuxX64Main by getting
            val linuxX64Test by getting
        }
    }
}


fun KotlinMultiplatformExtension.jsDefault(
    withBrowser: Boolean = true,
    withNode: Boolean = false,
    testWithChrome: Boolean = true,
    testHeadless: Boolean = true,
) {
    js(IR) {
        if (withBrowser) browser {
            testTask {
                useKarma {
                    when (testWithChrome to testHeadless) {
                        true to true -> useChromeHeadless()
                        true to false -> useChrome()
                    }
                }
            }
        }
        if (withNode) nodejs()
    }
}

// endregion [MPP Module Build Template]

// region [Compose MPP Module Build Template]

/** Only for very standard compose mpp libs. In most cases, it's better to not use this function. */
@OptIn(ExperimentalComposeLibrary::class)
fun Project.defaultBuildTemplateForComposeMppLib(
    details: LibDetails = rootExtLibDetails,
    ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
    ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) = with(details.settings.compose ?: error("Compose settings not set.")) {
    withComposeCompiler?.let {
        extensions.configure<ComposeExtension> { kotlinCompilerPlugin.set(it.mvn) }
    }
    if (withComposeTestUiJUnit5)
        logger.warn("Compose UI Tests with JUnit5 are not supported yet! Configuring JUnit5 anyway.")
    defaultBuildTemplateForBasicMppLib(
        details = details,
        ignoreCompose = true,
        ignoreAndroTarget = ignoreAndroTarget,
        ignoreAndroConfig = ignoreAndroConfig,
        ignoreAndroPublish = ignoreAndroPublish,
        addCommonMainDependencies = addCommonMainDependencies,
    )
    extensions.configure<KotlinMultiplatformExtension> {
        allDefaultSourceSetsForCompose(details.settings)
    }
}


/**
 * Normal fun KotlinMultiplatformExtension.allDefault ignores compose stuff,
 * because it's also used for libs without compose plugin.
 * This one does the rest, so it has to be called additionally for compose libs, after .allDefault */
@OptIn(ExperimentalComposeLibrary::class)
fun KotlinMultiplatformExtension.allDefaultSourceSetsForCompose(
    settings: LibSettings,
) = with(settings.compose ?: error("Compose settings not set.")) {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                if (withComposeUi) {
                    implementation(compose.ui)
                }
                if (withComposeFoundation) implementation(compose.foundation)
                if (withComposeFullAnimation) {
                    implementation(compose.animation)
                    implementation(compose.animationGraphics)
                }
                if (withComposeMaterial2) implementation(compose.material)
                if (withComposeMaterial3) implementation(compose.material3)
            }
        }
        if (settings.withJvm) {
            val jvmMain by getting {
                dependencies {
                    if (withComposeUi) {
                        implementation(compose.uiTooling)
                        implementation(compose.preview)
                    }
                    if (withComposeMaterialIconsExtended) implementation(compose.materialIconsExtended)
                    if (withComposeDesktop) {
                        implementation(compose.desktop.common)
                        implementation(compose.desktop.currentOs)
                    }
                    if (withComposeDesktopComponents) {
                        implementation(compose.desktop.components.splitPane)
                    }
                }
            }
            val jvmTest by getting {
                dependencies {
                    @Suppress("DEPRECATION")
                    if (withComposeTestUiJUnit4) implementation(compose.uiTestJUnit4)
                }
            }
        }
        if (settings.withJs) {
            val jsMain by getting {
                dependencies {
                    implementation(compose.runtime)
                    if (withComposeHtmlCore) implementation(compose.html.core)
                    if (withComposeHtmlSvg) implementation(compose.html.svg)
                }
            }
            val jsTest by getting {
                dependencies {
                    if (withComposeTestHtmlUtils) implementation(compose.html.testUtils)
                }
            }
        }
    }
}

// endregion [Compose MPP Module Build Template]


// region [Andro Common Build Template]

/** @param ignoreCompose Should be set to true if compose mpp is configured instead of compose andro */
fun DependencyHandler.defaultAndroDeps(
    settings: LibSettings,
    ignoreCompose: Boolean = false,
    configuration: String = "implementation",
) {
    val andro = settings.andro ?: error("No andro settings.")
    addAll(
        configuration,
        AndroidX.Core.ktx,
        AndroidX.AppCompat.appcompat.takeIf { andro.withAppCompat },
        AndroidX.Activity.compose.takeIf { andro.withActivityCompose }, // this should not depend on ignoreCompose!
        AndroidX.Lifecycle.compiler.takeIf { andro.withLifecycle },
        AndroidX.Lifecycle.runtime_ktx.takeIf { andro.withLifecycle },
        // TODO_someday_maybe: more lifecycle related stuff by default (viewmodel, compose)?
        Com.Google.Android.Material.material.takeIf { andro.withMDC },
    )
    if (!ignoreCompose && settings.withCompose) {
        val compose = settings.compose!!
        addAllWithVer(
            configuration,
            Vers.ComposeAndro,
            AndroidX.Compose.Ui.ui,
            AndroidX.Compose.Ui.tooling,
            AndroidX.Compose.Ui.tooling_preview,
            AndroidX.Compose.Material.material.takeIf { compose.withComposeMaterial2 },
        )
        addAll(
            configuration,
            AndroidX.Compose.Material3.material3.takeIf { compose.withComposeMaterial3 },
        )
    }
}

/** @param ignoreCompose Should be set to true if compose mpp is configured instead of compose andro */
fun DependencyHandler.defaultAndroTestDeps(
    settings: LibSettings,
    ignoreCompose: Boolean = false,
    configuration: String = "testImplementation",
) {
    val andro = settings.andro ?: error("No andro settings.")
    addAll(
        configuration,
        AndroidX.Test.Espresso.core.takeIf { andro.withTestEspresso },
        Com.Google.Truth.truth.takeIf { settings.withTestGoogleTruth },
        AndroidX.Test.rules,
        AndroidX.Test.runner,
        AndroidX.Test.Ext.truth.takeIf { settings.withTestGoogleTruth },
        Org.Mockito.Kotlin.mockito_kotlin.takeIf { settings.withTestMockitoKotlin },
    )

    if (settings.withTestJUnit4) {
        addAll(
            configuration,
            Kotlin.test_junit.withVer(Vers.Kotlin),
            JUnit.junit,
            Langiewicz.uspekx_junit4.takeIf { settings.withTestUSpekX },
            AndroidX.Test.Ext.junit_ktx,
        )
    }
    // android doesn't fully support JUnit5, but adding deps anyway to be able to write JUnit5 dependent code
    if (settings.withTestJUnit5) {
        addAll(
            configuration,
            Kotlin.test_junit5.withVer(Vers.Kotlin),
            Org.JUnit.Jupiter.junit_jupiter_api,
            Org.JUnit.Jupiter.junit_jupiter_engine,
            Langiewicz.uspekx_junit5.takeIf { settings.withTestUSpekX },
        )
    }

    if (!ignoreCompose && settings.withCompose) addAllWithVer(
        configuration,
        vers.ComposeAndro,
        AndroidX.Compose.Ui.test,
        AndroidX.Compose.Ui.test_manifest,
        AndroidX.Compose.Ui.test_junit4.takeIf { settings.withTestJUnit4 },
    )
}

fun MutableSet<String>.defaultAndroExcludedResources() = addAll(
    listOf(
        "**/*.md",
        "**/attach_hotspot_windows.dll",
        "META-INF/licenses/**",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/kotlinx_coroutines_core.version",
    )
)

fun CommonExtension<*, *, *, *, *>.defaultCompileOptions(
    jvmVer: String = vers.JvmDefaultVer,
) = compileOptions {
    sourceCompatibility(jvmVer)
    targetCompatibility(jvmVer)
}

fun CommonExtension<*, *, *, *, *>.defaultComposeStuff(withComposeCompiler: Dep? = null) {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = withComposeCompiler?.run {
            require(group == AndroidX.Compose.Compiler.compiler.group) {
                "Wrong compiler group: $group. Only AndroidX compose compilers are supported on android (without mpp)."
            }
            ver?.ver ?: error("Compose compiler without version provided: $this")
        }
    }
}

fun CommonExtension<*, *, *, *, *>.defaultPackagingOptions() = packaging {
    resources.excludes.defaultAndroExcludedResources()
}

/** Use template-andro/build.gradle.kts:fun defaultAndroLibPublishAllVariants() to create component with name "default". */
fun Project.defaultPublishingOfAndroLib(
    lib: LibDetails,
    componentName: String = "default",
) {
    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications.register<MavenPublication>(componentName) {
                from(components[componentName])
                defaultPOM(lib)
            }
        }
    }
}

fun Project.defaultPublishingOfAndroApp(
    lib: LibDetails,
    componentName: String = "release",
) = defaultPublishingOfAndroLib(lib, componentName)


// endregion [Andro Common Build Template]

// region [Andro Lib Build Template]

fun Project.defaultBuildTemplateForAndroLib(
    details: LibDetails = rootExtLibDetails,
    addAndroMainDependencies: DependencyHandler.() -> Unit = {},
) {
    val andro = details.settings.andro ?: error("No andro settings.")
    repositories { addRepos(details.settings.repos) }
    extensions.configure<LibraryExtension> {
        defaultAndroLib(details)
        if (andro.publishAllVariants) defaultAndroLibPublishAllVariants()
        if (andro.publishOneVariant) defaultAndroLibPublishVariant(andro.publishVariant)
    }
    dependencies {
        defaultAndroDeps(details.settings)
        defaultAndroTestDeps(details.settings)
        add("debugImplementation", AndroidX.Tracing.ktx) // https://github.com/android/android-test/issues/1755
        addAndroMainDependencies()
    }
    configurations.checkVerSync()
    tasks.defaultKotlinCompileOptions(details.settings.withJvmVer ?: error("No JVM version in settings."))
    defaultGroupAndVerAndDescription(details)
    if (andro.publishAllVariants) defaultPublishingOfAndroLib(details, "default")
    if (andro.publishOneVariant) defaultPublishingOfAndroLib(details, andro.publishVariant)
    if (!andro.publishNoVariants) defaultSigning()
}

fun LibraryExtension.defaultAndroLib(
    details: LibDetails = rootExtLibDetails,
    ignoreCompose: Boolean = false,
) {
    val andro = details.settings.andro ?: error("No andro settings.")
    compileSdk = andro.sdkCompile
    defaultCompileOptions(details.settings.withJvmVer!!)
    defaultDefaultConfig(details)
    defaultBuildTypes()
    details.settings.compose?.takeIf { !ignoreCompose }?.let { defaultComposeStuff(it.withComposeCompiler) }
    defaultPackagingOptions()
}

fun LibraryExtension.defaultDefaultConfig(details: LibDetails) = defaultConfig {
    val asettings = details.settings.andro ?: error("No andro settings.")
    namespace = details.namespace
    minSdk = asettings.sdkMin
    testInstrumentationRunner = asettings.withTestRunner
}

fun LibraryExtension.defaultBuildTypes() = buildTypes { release { isMinifyEnabled = false } }

fun LibraryExtension.defaultAndroLibPublishVariant(
    variant: String = "debug",
    withSources: Boolean = true,
    withJavadoc: Boolean = false,
) {
    publishing {
        singleVariant(variant) {
            if (withSources) withSourcesJar()
            if (withJavadoc) withJavadocJar()
        }
    }
}

fun LibraryExtension.defaultAndroLibPublishAllVariants(
    withSources: Boolean = true,
    withJavadoc: Boolean = false,
) {
    publishing {
        multipleVariants {
            allVariants()
            if (withSources) withSourcesJar()
            if (withJavadoc) withJavadocJar()
        }
    }
}

// endregion [Andro Lib Build Template]