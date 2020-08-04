import pl.mareklangiewicz.SourceFunExtension
import pl.mareklangiewicz.SourceFunPlugin
import pl.mareklangiewicz.SourceRegexTask
import pl.mareklangiewicz.Def

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.72"
    `maven-publish`
}
repositories {
    mavenCentral()
}
group = "com.github.langara.kommandline"
version = "0.0.1"

apply<SourceFunPlugin>()

configure<SourceFunExtension> {
    defs.add(Def("funTask1", "src", "funTempOut", {
        println(file.absolutePath)
    }))
}

tasks.register<SourceRegexTask>("regexExperiment") {
    source("regexTempSrc")
    outputDir = file("regexTempOut")
    match = ".*"
    replace = "XXX"
    doLast {
        println("fjkdslj")
    }
}

kotlin {
    jvm()
    js {
//        browser {}
        nodejs {}
    }
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
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}