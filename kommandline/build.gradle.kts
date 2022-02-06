plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

repositories { defaultRepos() }

defaultGroupAndVer(Deps.kommandLine)

//apply<SourceFunPlugin>()
//
//configure<SourceFunExtension> {
//    +Def("funTask1", "src", "funTempOut") { println(file.absolutePath) }
//    +Def("funTask2", "gradle", "funTempOut") { println(file.absolutePath) }
//}
//
//tasks.register<SourceRegexTask>("regexExperiment") {
//    source("regexTempSrc")
//    outputDir = file("regexTempOut")
//    match = ".*"
//    replace = "XXX"
//    doLast {
//        println("fjkdslj")
//    }
//}

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
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(Deps.junit5engine)
                implementation(Deps.uspekx)
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
