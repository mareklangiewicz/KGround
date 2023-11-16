@file:OptIn(ExperimentalNativeApi::class)

package pl.mareklangiewicz.kground

import kotlin.experimental.*
import kotlin.native.concurrent.*
import kotlin.system.*

@Suppress("DEPRECATION")
actual fun getCurrentTimeMs(): Long = getTimeMillis()

// FIXME_later: The format should be user-friendly and short. And similar to other platforms (the same??)
actual fun getCurrentTimeStr(): String = getCurrentTimeMs().toString()

@OptIn(ObsoleteWorkersApi::class)
actual fun getCurrentThreadName(): String = Worker.current.name

actual fun getCurrentPlatformName(): String = Platform.osFamily.name
actual fun getCurrentAbsolutePath(): String = TODO()
