package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*


expect val DefaultDispatcherForIO: CoroutineDispatcher

expect val DefaultOkioFileSystemOrErr: FileSystem

suspend fun <T> withIO(
  dispatcherForIO: CoroutineDispatcher = DefaultDispatcherForIO,
  block: suspend CoroutineScope.() -> T,
) = withContext(dispatcherForIO, block)
