import com.android.build.api.dsl.*
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
    plugAll(
        plugs.KotlinMulti,
        plugs.Compose,
        plugs.AndroAppNoVer,
    )
}

// workaround for crazy gradle bugs like this one or simillar:
// https://youtrack.jetbrains.com/issue/KT-43500/KJS-IR-Failed-to-resolve-Kotlin-library-on-attempting-to-resolve-compileOnly-transitive-dependency-from-direct-dependency
repositories { maven(repos.composeJbDev) }

defaultBuildTemplateForFullMppApp {
    implementation(project(":template-mpp-lib"))
    // implementation("pl.mareklangiewicz:template-mpp-lib:0.0.20")
}

// region [Full MPP App Build Template]

fun Project.defaultBuildTemplateForFullMppApp(
    details: LibDetails = rootExtLibDetails,
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
    defaultBuildTemplateForComposeMppApp(
        details = details,
        ignoreAndroConfig = true, // andro configured below?? FIXME NOW: really? how it is in compose-multiplatform-template?
        addCommonMainDependencies = addCommonMainDependencies
    )

    extensions.configure<ApplicationExtension> {
        defaultAndroApp(ignoreCompose = true) // compose mpp already configured above
        // FIXME NOW: are these dependencies at the right level?? I guess I have to follow mpp style here for deps...
        dependencies {
            defaultAndroDeps() // withCompose == false because we have compose from compose-multiplatform already

            // FIXME NOW: use defaultStuff
            api("androidx.activity:activity-compose:1.8.1")
            api("androidx.appcompat:appcompat:1.6.1")
            api("androidx.core:core-ktx:1.12.0")
        }
    }

}

// endregion [Full MPP App Build Template]


// region [Kotlin Module Build Template]

fun RepositoryHandler.addRepos(settings: LibReposSettings) = with(settings) {
    if (withMavenLocal) mavenLocal()
    if (withMavenCentral) mavenCentral()
    if (withGradle) gradlePluginPortal()
    if (withGoogle) google()
    if (withKotlinx) maven(repos.kotlinx)
    if (withKotlinxHtml) maven(repos.kotlinxHtml)
    if (withComposeJbDev) maven(repos.composeJbDev)
    if (withComposeCompilerAndroidxDev) maven(repos.composeCompilerAndroidxDev)
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
        require(ignoreAndroConfig) { "defaultBuildTemplateForMppLib can not configure android stuff (besides just adding target)" }
        require(ignoreAndroPublish || it.publishNoVariants) { "defaultBuildTemplateForMppLib can not publish android stuff YET" }
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

// region [MPP App Build Template]

fun Project.defaultBuildTemplateForBasicMppApp(
    appMainPackage: String,
    appMainFun: String = "main",
    details: LibDetails = rootExtLibDetails,
    ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
    ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
    ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
    defaultBuildTemplateForBasicMppLib(
        details = details,
        ignoreCompose = ignoreCompose,
        ignoreAndroTarget = ignoreAndroTarget,
        ignoreAndroConfig = ignoreAndroConfig,
        ignoreAndroPublish = true,
        addCommonMainDependencies = addCommonMainDependencies
    )
    extensions.configure<KotlinMultiplatformExtension> {
        if (details.settings.withJvm) jvm {
            println("MPP App ${project.name}: Generating general jvm executables with kotlin multiplatform plugin is not supported (without compose).")
            // TODO_someday: Will they support multiplatform way of declaring jvm app?
            // binaries.executable()
            // UPDATE:TODO_later: analyze experimental: mainRun {  } it doesn't work yet (compilation fails) even though IDE recognizes it
            // for now workaround is: kotlin { jvm { withJava() } }; application { mainClass.set("...") }
            // but I don't want to include such old dsl in this default fun.
            // see also:
            // https://youtrack.jetbrains.com/issue/KT-45038
            // https://youtrack.jetbrains.com/issue/KT-31424
        }
        if (details.settings.withJs) js(IR) {
            binaries.executable()
        }
        if (details.settings.withNativeLinux64) linuxX64 {
            binaries {
                executable {
                    entryPoint = "$appMainPackage.$appMainFun"
                }
            }
        }
    }
}

// endregion [MPP App Build Template]

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
    withComposeCompilerVer?.let {
        val cc = AndroidX.Compose.Compiler.compiler.withVer(it)
        extensions.configure<ComposeExtension> { kotlinCompilerPlugin.set(cc.mvn) }
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
                    if (withComposeWebCore) implementation(compose.html.core)
                    if (withComposeWebSvg) implementation(compose.html.svg)
                }
            }
            val jsTest by getting {
                dependencies {
                    if (withComposeTestWebUtils) implementation(compose.html.testUtils)
                }
            }
        }
    }
}

// endregion [Compose MPP Module Build Template]

// region [Compose MPP App Build Template]

/** Only for very standard compose mpp apps. In most cases it's better to not use this function. */
fun Project.defaultBuildTemplateForComposeMppApp(
    details: LibDetails = rootExtLibDetails,
    ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
    ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
    defaultBuildTemplateForComposeMppLib(
        details = details,
        ignoreAndroTarget = ignoreAndroTarget,
        ignoreAndroConfig = ignoreAndroConfig,
        ignoreAndroPublish = true,
        addCommonMainDependencies = addCommonMainDependencies,
    )
    extensions.configure<KotlinMultiplatformExtension> {
        if (details.settings.withJs) js(IR) {
            binaries.executable()
        }
        if (details.settings.withNativeLinux64) linuxX64 {
            binaries {
                executable {
                    entryPoint = "${details.appMainPackage}.${details.appMainFun}"
                }
            }
        }
    }
    if (details.settings.withJvm) {
        compose.desktop {
            application {
                mainClass = "${details.appMainPackage}.${details.appMainClass}"
                nativeDistributions {
                    targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
                    packageName = details.name
                    packageVersion = details.version.ver
                    description = details.description
                }
            }
        }
    }
}

// endregion [Compose MPP App Build Template]



// region [Andro Common Build Template]

fun DependencyHandler.defaultAndroDeps(
    configuration: String = "implementation",
    withCompose: Boolean = false,
    withMDC: Boolean = false,
) {
    addAll(
        configuration,
        AndroidX.Core.ktx,
        AndroidX.AppCompat.appcompat,
        AndroidX.Lifecycle.compiler,
        AndroidX.Lifecycle.runtime_ktx,
    )
    if (withCompose) {
        addAllWithVer(
            configuration,
            Vers.ComposeAndro,
            AndroidX.Compose.Ui.ui,
            AndroidX.Compose.Ui.tooling,
            AndroidX.Compose.Ui.tooling_preview,
            AndroidX.Compose.Material.material,
        )
        addAll(
            configuration,
            AndroidX.Activity.compose,
            AndroidX.Compose.Material3.material3,
        )
    }
    if (withMDC) add(configuration, Com.Google.Android.Material.material)
}

fun DependencyHandler.defaultAndroTestDeps(
    configuration: String = "testImplementation",
    withCompose: Boolean = false,
) {
    addAll(
        configuration,
        Kotlin.test_junit.withVer(Vers.Kotlin),
        JUnit.junit, // FIXME_someday: when will android move to JUnit5?
        Langiewicz.uspekx_junit4,
        AndroidX.Test.Espresso.core,
        Com.Google.Truth.truth,
        AndroidX.Test.rules,
        AndroidX.Test.runner,
        AndroidX.Test.Ext.truth,
        AndroidX.Test.Ext.junit,
        Org.Mockito.Kotlin.mockito_kotlin,
    )
    if (withCompose) addAllWithVer(
        configuration,
        vers.ComposeAndro,
        AndroidX.Compose.Ui.test,
        AndroidX.Compose.Ui.test_junit4,
        AndroidX.Compose.Ui.test_manifest,
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

fun CommonExtension<*, *, *, *, *>.defaultComposeStuff(withComposeCompilerVer: Ver? = Vers.ComposeCompiler) {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = withComposeCompilerVer?.ver
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

// region [Andro App Build Template]

fun Project.defaultBuildTemplateForAndroApp(
    details: LibDetails = rootExtLibDetails,
    addAndroDependencies: DependencyHandler.() -> Unit = {},
) {
    val andro = details.settings.andro ?: error("No andro settings.")
    require(!andro.publishAllVariants) { "Only single app variant can be published" }
    val variant = andro.publishVariant.takeIf { andro.publishOneVariant }
    repositories { addRepos(details.settings.repos) }
    extensions.configure<ApplicationExtension> {
        defaultAndroApp(details)
        variant?.let { defaultAndroAppPublishVariant(it) }
    }
    dependencies {
        defaultAndroDeps(withCompose = details.settings.withCompose, withMDC = andro.withMDC)
        defaultAndroTestDeps(withCompose = details.settings.withCompose)
        add("debugImplementation", AndroidX.Tracing.ktx) // https://github.com/android/android-test/issues/1755
        addAndroDependencies()
    }
    configurations.checkVerSync()
    tasks.defaultKotlinCompileOptions(details.settings.withJvmVer!!)
    defaultGroupAndVerAndDescription(details)
    variant?.let {
        defaultPublishingOfAndroApp(details, it)
        defaultSigning()
    }
}

fun ApplicationExtension.defaultAndroApp(
    details: LibDetails = rootExtLibDetails,
    ignoreCompose: Boolean = false,
) {
    val andro = details.settings.andro ?: error("No andro settings.")
    compileSdk = andro.sdkCompile
    defaultCompileOptions(details.settings.withJvmVer ?: error("No JVM version in settings."))
    defaultDefaultConfig(details)
    defaultBuildTypes()
    details.settings.compose?.takeIf { !ignoreCompose }?.let { defaultComposeStuff(it.withComposeCompilerVer) }
    defaultPackagingOptions()
}

fun ApplicationExtension.defaultDefaultConfig(details: LibDetails) = defaultConfig {
    val asettings = details.settings.andro ?: error("No andro settings.")
    applicationId = details.appId
    namespace = details.namespace
    targetSdk = asettings.sdkTarget
    minSdk = asettings.sdkMin
    versionCode = details.appVerCode
    versionName = details.appVerName
    testInstrumentationRunner = asettings.withTestRunner
}

fun ApplicationExtension.defaultBuildTypes() = buildTypes { release { isMinifyEnabled = false } }

fun ApplicationExtension.defaultAndroAppPublishVariant(
    variant: String = "debug",
    publishAPK: Boolean = true,
    publishAAB: Boolean = false,
) {
    require(!publishAAB || !publishAPK) { "Either APK or AAB can be published, but not both." }
    publishing { singleVariant(variant) { if (publishAPK) publishApk() } }
}

// endregion [Andro App Build Template]
