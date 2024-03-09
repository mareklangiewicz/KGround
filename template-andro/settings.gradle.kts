@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    // includeBuild("../../DepsKt")
}

plugins {
    id("pl.mareklangiewicz.deps.settings") version "0.2.91" // https://plugins.gradle.org/search?term=mareklangiewicz
}

rootProject.name = "template-andro"
include(":template-andro-lib")
include(":template-andro-app")
