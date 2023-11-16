@file:OptIn(ExperimentalNativeApi::class)

package pl.mareklangiewicz.kground

import kotlinx.cinterop.*
import kotlinx.cinterop.nativeHeap.alloc
import platform.posix.*
import kotlin.experimental.*
import kotlin.native.concurrent.*
import kotlin.system.*


import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.posix.getcwd
import platform.posix.size_t
import platform.posix.uint8_tVar

@Suppress("DEPRECATION")
actual fun getCurrentTimeMs(): Long = getTimeMillis()

// FIXME_later: The format should be user-friendly and short. And similar to other platforms (the same??)
actual fun getCurrentTimeStr(): String = getCurrentTimeMs().toString()

// See KT-60932 KT-54702
@OptIn(ObsoleteWorkersApi::class)
actual fun getCurrentThreadName(): String = Worker.current.name

actual fun getCurrentPlatformName(): String = Platform.osFamily.name

actual fun getCurrentAbsolutePath(): String = getCurrentDirectoryFromGPTAdvice()


@OptIn(ExperimentalForeignApi::class)
private fun getCurrentDirectoryFromGPTAdvice(): String {
    val bufferLength = 4096 // Adjust buffer length as needed
    return "FIXME (linux getcwd)"
/*
    val buffer = nativeHeap.alloc<uint8_tVar>(bufferLength)
    getcwd(buffer, bufferLength.convert())
    val currentPath = buffer.toKString()
    nativeHeap.free(buffer)
    return currentPath
*/
}
