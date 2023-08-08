package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val DispatcherIO: CoroutineDispatcher get() = Dispatchers.IO