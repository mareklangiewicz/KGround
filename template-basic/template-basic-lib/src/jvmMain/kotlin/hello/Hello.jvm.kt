@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templatebasic

actual fun helloPlatform() = "Hello JVM World! (kotlin: ${KotlinVersion.CURRENT})".also { println(it) }
