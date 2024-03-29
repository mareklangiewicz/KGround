@file:Suppress("UnstableApiUsage")

//gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    if (File("../DepsKt").exists()) {
        logger.warn("Including local build ../DepsKt")
        includeBuild("../DepsKt")
    }
}

plugins {
    id("pl.mareklangiewicz.deps.settings") version "0.2.93" // https://plugins.gradle.org/search?term=mareklangiewicz
    id("com.gradle.enterprise") version "3.16.2" // https://docs.gradle.com/enterprise/gradle-plugin/
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("GITHUB_ACTIONS") == "true")
        publishOnFailure()
    }
}

rootProject.name = "KommandLine"

include(":kommandline")
include(":kommandsamples")
include(":kommandjupyter")

val kgroundDir = File("../KGround/kground")

val kgroundLocal = kgroundDir.exists()
// val kgroundLocal = false

if (kgroundLocal) {
    logger.warn("Adding local kground module.")
    include(":kground")
    project(":kground").projectDir = kgroundDir
}
