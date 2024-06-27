@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templatebasic

actual fun helloPlatform(): String = "Hello JS World! (kotlin: ${KotlinVersion.CURRENT})".also { println(it) }
