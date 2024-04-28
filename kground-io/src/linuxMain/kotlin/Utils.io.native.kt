@file:OptIn(ExperimentalNativeApi::class)

package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import kotlin.experimental.*
import okio.*
import platform.posix.getenv
import kotlinx.cinterop.toKString

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getSysEnv(name: String): String? = getenv(name)?.toKString()

// TODO_later see expect fun
actual fun getSysProp(name: String): String? = when (name) {
  "os.name" -> Platform.osFamily.name
  "os.arch" -> null // TODO
  else -> null
}

// TODO_maybe: "NATIVE-Android", "NATIVE-Ubuntu" ?? Check platform detection in okio. But goal is pretty stable types.
actual fun getSysPlatformType(): String? = "NATIVE-Linux"

actual fun getSysDispatcherForIO(): CoroutineDispatcher = Dispatchers.IO

actual fun getSysUFileSys(): UFileSys = UNativeSysFileSys()

private class UNativeSysFileSys : UFileSys(SYSTEM) {
  // TODO_someday: improve getSysProp above so these paths are also better than null on native
  override val pathToUserHome: Path? = getSysPathToUserHome()
  override val pathToUserTmp: Path? = getSysPathToUserTmp()

  // TODO_someday: getSysPathToSysTmp() should not do getSysProp("java.io.tmpdir") but some new MPP key instead.
  override val pathToSysTmp: Path? = getSysPathToSysTmp()
}
