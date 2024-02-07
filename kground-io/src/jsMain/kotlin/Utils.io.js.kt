package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*
import pl.mareklangiewicz.bad.*

/** No specific IO dispatcher available on JS */
actual val DefaultDispatcherForIO: CoroutineDispatcher get() = Dispatchers.Default

actual val DefaultOkioFileSystemOrErr: FileSystem = bad { "JS doesn't have default Okio FileSystem" }

