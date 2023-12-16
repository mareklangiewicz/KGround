@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templatempp

actual fun helloPlatform() = "Hello JVM World! (kotlin: ${KotlinVersion.CURRENT})".also { println(it) }