package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*

actual val DefaultDispatcherForIO: CoroutineDispatcher get() = Dispatchers.IO

actual val DefaultOkioFileSystemOrErr: FileSystem = FileSystem.SYSTEM
