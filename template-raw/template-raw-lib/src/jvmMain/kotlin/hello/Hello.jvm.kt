@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templatefull

actual fun helloPlatform() = "Hello JVM World! (kotlin: ${KotlinVersion.CURRENT})".also { println(it) }
