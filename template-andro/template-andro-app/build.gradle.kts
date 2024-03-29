@file:Suppress("DEPRECATION", "unused", "UnusedVariable")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
    plugAll(plugs.AndroAppNoVer, plugs.KotlinAndro, plugs.MavenPublish, plugs.Signing)
}

defaultBuildTemplateForAndroApp()

// besides default dependencies declared by fun defaultBuildTemplateForAndroApp
dependencies { implementation(project(":template-andro-lib")) }


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and LibDetails
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...


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
    suppressComposeCheckKotlinVer: Ver? = null,
) = withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTargetVer?.let { jvmTarget = it }
        if (renderInternalDiagnosticNames) freeCompilerArgs = freeCompilerArgs + "-Xrender-internal-diagnostic-names"
        // useful, for example, to suppress some errors when accessing internal code from some library, like:
        // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
        suppressComposeCheckKotlinVer?.ver?.let {
            freeCompilerArgs = freeCompilerArgs + "-P" + "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=$it"
        }
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

fun CommonExtension<*, *, *, *, *, *>.defaultCompileOptions(
    jvmVer: String = vers.JvmDefaultVer,
) = compileOptions {
    sourceCompatibility(jvmVer)
    targetCompatibility(jvmVer)
}

fun CommonExtension<*, *, *, *, *, *>.defaultComposeStuff(withComposeCompiler: Dep? = null) {
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

fun CommonExtension<*, *, *, *, *, *>.defaultPackagingOptions() = packaging {
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
        defaultAndroDeps(details.settings)
        defaultAndroTestDeps(details.settings)
        add("debugImplementation", AndroidX.Tracing.ktx) // https://github.com/android/android-test/issues/1755
        addAndroDependencies()
    }
    configurations.checkVerSync()
    tasks.defaultKotlinCompileOptions(
        details.settings.withJvmVer ?: error("No JVM version in settings."),
        suppressComposeCheckKotlinVer = details.settings.compose?.withComposeCompilerAllowWrongKotlinVer,
    )
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
    details.settings.compose?.takeIf { !ignoreCompose }?.let { defaultComposeStuff(it.withComposeCompiler) }
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