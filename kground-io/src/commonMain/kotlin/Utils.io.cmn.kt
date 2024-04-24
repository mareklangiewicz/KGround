package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*

// TODO: probably move this kind of stuff to USys

expect val DefaultDispatcherForIO: CoroutineDispatcher

expect val DefaultOkioFileSystemOrErr: FileSystem

@Deprecated("Use USys")
suspend fun <T> withIO(
  dispatcherForIO: CoroutineDispatcher = DefaultDispatcherForIO,
  block: suspend CoroutineScope.() -> T,
) = withContext(dispatcherForIO, block)
