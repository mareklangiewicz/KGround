package pl.mareklangiewicz.kground.io

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmInline
import kotlinx.coroutines.CoroutineScope
import okio.Path
import okio.Path.Companion.toPath
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.uctx.UCtx
import pl.mareklangiewicz.uctx.uctx


@JvmInline
value class UWorkDir(val dir: Path) : UCtx {
  companion object Key : CoroutineContext.Key<UWorkDir>
  override val key: CoroutineContext.Key<*> get() = Key
}

suspend inline fun localUWorkDir(): UWorkDir =
  localUWorkDirOrNull() ?: bad { "No UWorkDir provided in coroutine context." }

suspend inline fun localUWorkDirOrNull(): UWorkDir? = coroutineContext[UWorkDir]

suspend inline fun <R> cd(dir: Path, noinline block: suspend CoroutineScope.() -> R): R {
  val p = if (dir.isAbsolute) dir else localUWorkDir().dir / dir
  return uctx(UWorkDir(localUFileSys().canonicalize(p)), block = block)
}

suspend inline fun <R> cd(dir: String, noinline block: suspend CoroutineScope.() -> R): R = cd(dir.toPath(), block)
