package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*

actual val DefaultDispatcherForIO: CoroutineDispatcher get() = Dispatchers.IO
