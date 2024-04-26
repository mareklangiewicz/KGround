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


// TODO NOW: try to use it all

@JvmInline
value class UCWD(val path: Path) : UCtx {
  companion object Key : CoroutineContext.Key<UCWD>
  override val key: CoroutineContext.Key<*> get() = Key
}

suspend inline fun implictx(): UCWD = coroutineContext[UCWD] ?: bad { "No UCWD provided in coroutine context." }

suspend inline fun <R> cd(path: Path, noinline block: suspend CoroutineScope.() -> R): R {
  val p = if (path.isAbsolute) path else implictx().path / path
  return uctx(UCWD(implictx<UFileSys>().canonicalize(p)), block = block)
}

suspend inline fun <R> cd(path: String, noinline block: suspend CoroutineScope.() -> R): R = cd(path.toPath(), block)
