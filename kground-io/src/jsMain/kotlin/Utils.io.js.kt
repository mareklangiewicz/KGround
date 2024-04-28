package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.windowOrNull


actual fun getSysEnv(name: String): String? = js("globalThis.process.env[name]") as String?

// TODO_later see expect fun
actual fun getSysProp(name: String): String? = when (name) {
  // FIXME_later: do sth better for os.name?? see platform detecion in okio
  "os.name" -> windowOrNull?.navigator?.userAgent?.let { "JS Agent $it" } ?: "JS Node probably"
  "os.arch" -> null // TODO
  else -> null
}

// TODO_maybe: "JS-Node", "JS-..." ?? See platform detection in okio first. But we want pretty stable types.
actual fun getSysPlatformType(): String? = "JS"

/** No specific IO dispatcher available on JS */
actual fun getSysDispatcherForIO(): CoroutineDispatcher = Dispatchers.Default

/** Currently no default Okio FileSystem available on JS. TODO_maybe: detect okio-nodefilesystem artifact? */
actual fun getSysUFileSys(): UFileSys = bad { "JS doesn't have default Okio FileSystem" }

