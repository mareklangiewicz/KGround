import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.mareklangiewicz.defaults.*

plugins {
    kotlin("jvm")
}

repositories { defaultRepos() }
defaultGroupAndVerAndDescription(libs.KommandLine)

dependencies {
    implementation(project(":kommandline"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}
