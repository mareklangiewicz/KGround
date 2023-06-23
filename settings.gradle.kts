@file:Suppress("UnstableApiUsage")

import okio.Path.Companion.toOkioPath
import pl.mareklangiewicz.deps.Langiewicz
import pl.mareklangiewicz.evts.*
import pl.mareklangiewicz.utils.includeAndSubstituteBuild

//gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
//    includeBuild("../DepsKt")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("pl.mareklangiewicz.deps.settings") version "0.2.41" // https://plugins.gradle.org/search?term=mareklangiewicz
    id("com.gradle.enterprise") version "3.13.4" // https://docs.gradle.com/enterprise/gradle-plugin/
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


// FIXME_someday: doesn't really work. And seems like kotlin.mpp.import.enableKgpDependencyResolution=true doesn't help
//   https://youtrack.jetbrains.com/issue/KT-52172/Multiplatform-Support-composite-builds
//includeAndSubstituteBuild("../UPue", Langiewicz.upue.mvn, ":upue")
