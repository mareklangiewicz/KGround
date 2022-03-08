import pl.mareklangiewicz.defaults.*

plugins {
    kotlin("multiplatform") version vers.kotlin
    id("maven-publish")
}

repositories { defaultRepos() }

defaultGroupAndVer(deps.kommandLine)

kotlin {
    jvm()
//    js {
////        browser {}
//        nodejs {}
//    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("script-runtime"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(deps.uspekx)
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(deps.junit5engine)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions { jvmTarget = "16" }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        showStandardStreams = true
        showStackTraces = true
    }
}
