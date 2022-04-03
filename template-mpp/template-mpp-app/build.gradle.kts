import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.dsl.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
    kotlin("multiplatform") version vers.kotlin
}

defaultBuildTemplateForMppApp(
    appMainPackage = "pl.mareklangiewicz.hello",
    withNativeLinux64 = true,
    details = libs.TemplateMPP,
) {
    implementation(project(":template-mpp-lib"))
}

// example stuff in addition to defaultBuildTemplate...
repositories { maven(repos.kotlinxHtml) }

// example stuff in addition to defaultBuildTemplate...
kotlin {
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(deps.kotlinxHtmlJs)
            }
        }
    }
}



// region [Kotlin Module Build Template]

fun TaskCollection<Task>.defaultKotlinCompileOptions(
    jvmTargetVer: String = vers.defaultJvm,
    requiresOptIn: Boolean = true
) = withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = jvmTargetVer
        if (requiresOptIn) freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

// endregion [Kotlin Module Build Template]


// region [MPP Module Build Template]

/** Only for very standard small libs. In most cases it's better to not use this function. */
fun Project.defaultBuildTemplateForMppLib(
    withJvm: Boolean = true,
    withJs: Boolean = true,
    withNativeLinux64: Boolean = false,
    details: LibDetails = libs.Unknown,
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {}
) {
    repositories { defaultRepos() }
    defaultGroupAndVerAndDescription(details)
    kotlin { allDefault(withJvm, withJs, withNativeLinux64, addCommonMainDependencies) }
    tasks.defaultKotlinCompileOptions()
    tasks.defaultTestsOptions()
    if (plugins.hasPlugin("maven-publish")) {
        defaultPublishing(details)
        if (plugins.hasPlugin("signing")) defaultSigning()
        else println("MPP Module ${name}: signing disabled")
    }
    else println("MPP Module ${name}: publishing (and signing) disabled")
}

/** Only for very standard small libs. In most cases it's better to not use this function. */
@Suppress("UNUSED_VARIABLE")
fun KotlinMultiplatformExtension.allDefault(
    withJvm: Boolean = true,
    withJs: Boolean = true,
    withNativeLinux64: Boolean = false,
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {}
) {
    if (withJvm) jvm()
    if (withJs) jsDefault()
    if (withNativeLinux64) linuxX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                addCommonMainDependencies()
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(deps.uspekx)
            }
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

// TODO: check if this region can be merged into MPP Module Build Template
// region [MPP App Build Template]

fun Project.defaultBuildTemplateForMppApp(
    appMainPackage: String,
    appMainFun: String = "main",
    withJvm: Boolean = true,
    withJs: Boolean = true,
    withNativeLinux64: Boolean = false,
    details: LibDetails = libs.Unknown,
    addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {}
) {
    // TODO NOW: withCompose
    defaultBuildTemplateForMppLib(withJvm, withJs, withNativeLinux64, details, addCommonMainDependencies)
    kotlin {
        if (withJvm) jvm {
            println("MPP App ${project.name}: Generating general jvm executables with kotlin multiplatform plugin is not supported (without compose).")
            // TODO_someday: Will they support multiplatform way of declaring jvm app?
            //binaries.executable()
        }
        if (withJs) js(IR) {
            binaries.executable()
        }
        if (withNativeLinux64) linuxX64 {
            binaries {
                executable {
                    entryPoint = "$appMainPackage.$appMainFun"
                }
            }
        }
    }
}

// endregion [MPP App Build Template]