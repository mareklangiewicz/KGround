package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*


actual fun getEnv(name: String): String? = System.getenv(name)


actual fun getDefaultDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

actual fun getDefaultFS(): UFileSys = UFileSys(FileSystem.SYSTEM)
