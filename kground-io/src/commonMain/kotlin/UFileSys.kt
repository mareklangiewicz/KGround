package pl.mareklangiewicz.kground.io

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import okio.FileSystem
import okio.ForwardingFileSystem
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.uctx.UCtx


open class UFileSys(system: FileSystem) : UCtx, ForwardingFileSystem(system) {
  companion object Key : CoroutineContext.Key<UFileSys>
  override val key: CoroutineContext.Key<*> get() = Key

  // TODO_someday_maybe: add some additional "micro file system" stuff here?
  // (but not val lineEnd: It should always be "\n" except for very specific cases handled manually/separately)
  // (also not var currentDir/Path/CWD: It should probably have separate immutable context element)
}
suspend inline fun <reified T: UFileSys> implictx(): T =
  coroutineContext[UFileSys] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

