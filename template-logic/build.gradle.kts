plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:2.4.20-Beta1")
    implementation("com.android.tools.build:gradle:9.3.0-rc02")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.12.0-beta01")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.37.0")
    implementation("pl.mareklangiewicz.deps:DepsKt:0.4.22")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
