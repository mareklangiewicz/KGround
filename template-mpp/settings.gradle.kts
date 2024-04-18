@file:Suppress("UnstableApiUsage")

// gradle.logSomeEventsToFile(rootProjectPath / "my.gradle.log")

pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
  // includeBuild("../../DepsKt")
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.2.98" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.enterprise") version "3.17.2" // https://docs.gradle.com/enterprise/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { // careful with publishing fails especially from my machine (privacy)
      @Suppress("SimplifyBooleanWithConstants")
      true &&
        it.buildResult.failures.isNotEmpty() &&
        // it.buildResult.failures.isEmpty() &&
        System.getenv("GITHUB_ACTIONS") == "true" &&
        // System.getenv("GITHUB_ACTIONS") != "true" &&
        true
        // false
    }
  }
}

rootProject.name = "template-mpp"

include(":template-mpp-lib")
include(":template-mpp-app")
