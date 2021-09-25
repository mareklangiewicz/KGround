plugins {
    kotlin("multiplatform") version "1.5.30"
    `maven-publish`
}
repositories {
    mavenCentral()
}
group = "com.github.langara.kommandline"
version = "0.0.02"

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
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
//        js().compilations["main"].defaultSourceSet {
//            dependencies {
//                implementation(kotlin("stdlib-js"))
//            }
//        }
//        js().compilations["test"].defaultSourceSet {
//            dependencies {
//                implementation(kotlin("test-js"))
//            }
//        }
    }
}