package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*
import platform.posix.getenv
import kotlinx.cinterop.toKString

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getEnv(name: String): String? = getenv(name)?.toKString()

actual fun getDefaultDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

actual fun getDefaultFS(): UFileSys = UFileSys(FileSystem.SYSTEM)
