@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.templatefull

actual fun helloPlatform(): String = "Hello Native Linux 64 World!".also { println(it) }
