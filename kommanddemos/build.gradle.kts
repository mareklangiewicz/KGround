import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version vers.kotlin
}

group = "pl.mareklangiewicz"
version = "0.0.01"

repositories {
    mavenCentral()
}

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
