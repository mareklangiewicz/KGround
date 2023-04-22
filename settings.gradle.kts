@file:Suppress("UnstableApiUsage")

import okio.Path.Companion.toOkioPath
import pl.mareklangiewicz.evts.*

//gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
    includeBuild("../DepsKt")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("pl.mareklangiewicz.deps.settings") version "0.2.34"
}

rootProject.name = "KommandLine"

include(":kommandline")
include(":kommanddemos")

//includeAndSubstituteBuild("../USpek", Deps.uspekx, ":uspekx")
