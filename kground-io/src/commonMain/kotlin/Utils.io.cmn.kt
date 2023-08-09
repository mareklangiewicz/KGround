package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*


expect val DefaultDispatcherForIO: CoroutineDispatcher

suspend fun <T> withIO(
    dispatcherForIO: CoroutineDispatcher = DefaultDispatcherForIO,
    block: suspend CoroutineScope.() -> T,
) = withContext(dispatcherForIO, block)
