package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.*


actual fun getSysEnv(name: String): String? = System.getenv(name)

actual fun getSysProp(name: String): String? = System.getProperty(name)

// TODO_maybe: "JVM-Android", "JVM-Linux" ?? See platform detection in okio first. But we want pretty stable types.
actual fun getSysPlatformType(): String? = "JVM"

actual fun getSysDispatcherForIO(): CoroutineDispatcher = Dispatchers.IO

actual fun getSysUFileSys(): UFileSys = UJvmSysFileSys()

private class UJvmSysFileSys : UFileSys(SYSTEM) {
  override val pathToUserHome: Path? = getSysPathToUserHome()
  override val pathToUserTmp: Path? = getSysPathToUserTmp()
  override val pathToSysTmp: Path? = getSysPathToSysTmp()
}
