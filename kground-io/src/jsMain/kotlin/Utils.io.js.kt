package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*
import pl.mareklangiewicz.bad.*


actual fun getEnv(name: String): String? = js("globalThis.process.env[name]") as String?

/** No specific IO dispatcher available on JS */
actual fun getDefaultDispatcherIO(): CoroutineDispatcher = Dispatchers.Default

/** Currently no default Okio FileSystem available on JS. TODO_maybe: detect okio-nodefilesystem artifact? */
actual fun getDefaultFS(): UFileSys = bad { "JS doesn't have default Okio FileSystem" }

