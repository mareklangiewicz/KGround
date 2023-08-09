package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*

/** No specific IO dispatcher available on JS */
actual val DefaultDispatcherForIO: CoroutineDispatcher get() = Dispatchers.Default

