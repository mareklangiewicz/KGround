package pl.mareklangiewicz.templatempp

import android.util.*

actual fun helloPlatform() = "Hello Andro World! (kotlin: ${KotlinVersion.CURRENT})".also { Log.i("hello", it) }