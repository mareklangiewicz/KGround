@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templateraw

actual fun helloPlatform() = "Hello JVM World! (kotlin: ${KotlinVersion.CURRENT})".also { println(it) }
