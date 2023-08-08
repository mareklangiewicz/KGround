package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** No specific IO dispatcher available on JS */
actual val DispatcherIO: CoroutineDispatcher get() = Dispatchers.Default