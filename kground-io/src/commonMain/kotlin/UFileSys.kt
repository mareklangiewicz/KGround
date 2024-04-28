package pl.mareklangiewicz.kground.io

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.uctx.UCtx


open class UFileSys(delegate: FileSystem) : UCtx, ForwardingFileSystem(delegate) {
  companion object Key : CoroutineContext.Key<UFileSys>
  override val key: CoroutineContext.Key<*> get() = Key

  // TODO_someday_maybe: add some additional "micro file system" stuff here?
  // (but not val lineEnd: It should always be "\n" except for very specific cases handled manually/separately)
  // (also not var currentDir/Path/CWD: It should probably have separate immutable context element)

  open val pathToUserHome: Path? = null
  open val pathToUserTmp: Path? = null
  open val pathToSysTmp: Path? = null
}

val UFileSys.pathToSomeTmpOrHome get() = listOfNotNull(pathToUserTmp, pathToSysTmp, pathToUserHome).first()

/**
 * Just some convention I like; additional "tmp" in name is there to emphasize that
 * this file content is temporary, and can be easily replaced by some kommand/sample/etc.
 */
val UFileSys.pathToTmpNotes get() = pathToSomeTmpOrHome / "tmp.notes"



suspend inline fun <reified T: UFileSys> implictx(): T =
  coroutineContext[UFileSys] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

