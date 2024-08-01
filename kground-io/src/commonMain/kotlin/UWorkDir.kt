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

suspend inline fun <R> cd(dir: String, noinline block: suspend CoroutineScope.() -> R): R = cd(dir.pth, block)



// Alternative names. I can't decide now which convention makes better "DSL" (someday I'll choose which to deprecate)

suspend inline fun localDir(): UWorkDir = localUWorkDir()

suspend inline fun localDirOrNull(): UWorkDir? = localUWorkDirOrNull()

suspend inline fun <R> changeDir(dir: Path, noinline block: suspend CoroutineScope.() -> R): R = cd(dir, block)

suspend inline fun <R> changeDir(dir: String, noinline block: suspend CoroutineScope.() -> R): R = cd(dir, block)
