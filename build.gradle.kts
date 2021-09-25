plugins {
    kotlin("multiplatform") version Vers.kotlin
    `maven-publish`
}
repositories {
    mavenCentral()
    maven(Repos.jitpack)
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
        val commonMain by getting
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
                implementation(Deps.uspek)
            }
        }
    }
}