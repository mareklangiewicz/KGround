package pl.mareklangiewicz.kgroundx.io

import kotlinx.coroutines.*
import okio.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.ulog.*
import kotlin.coroutines.*
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.uctx.UCtx


open class USys(val fs: FileSystem, val cli: CLI) : UCtx {
  companion object Key : CoroutineContext.Key<USys>
  override val key: CoroutineContext.Key<*> get() = Key
}
suspend inline fun <reified T: USys> implictx(): T =
  coroutineContext[USys] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

@Deprecated("")
interface WithCLI {
  val cli: CLI
}

@Deprecated("")
interface WithKGroundXIO : WithKGroundIO, WithCLI

/**
 * Sth like this be used as a context receiver when Kotlin supports it.
 *
 * For now just pass it inside [coroutineContext] when in coroutine.
 * or as "kg" parameter - when in blocking world,
 *
 * Warning: I use experimental polymorphic keys as in kdoc of [AbstractCoroutineContextKey]
 * Warning: Make sure all derived classes also follow this experimental API rules correctly.
 */
@OptIn(ExperimentalStdlibApi::class)
@Deprecated("")
open class KGroundXIOCtx(override val cli: CLI, fs: FileSystem, ulog: ULog, usubmit: USubmit) :
  KGroundIOCtx(fs, ulog, usubmit), WithKGroundXIO {
  companion object Key :
    AbstractCoroutineContextKey<KGroundIOCtx, KGroundXIOCtx>(KGroundIOCtx, { it as? KGroundXIOCtx })
}

/**
 * FIXME NOW: update this whole kdoc
 * @param name set to non-null to add new [CoroutineName] to context
 * @param cli set to non-null to set specific [CLI] in context,
 * when null it tries to use one from current(parent) [KGroundXIOCtx],
 * and defaults to [CLI.SYS] if no [CLI] found.
 * @param fs set to non-null to set specific [FileSystem] in context,
 * when null it tries to use one from current (parent) [KGroundIOCtx],
 * and defaults to [DefaultOkioFileSystemOrErr] if no [FileSystem] found.
 * @param ulog set to non-null to change the [ULog],
 * when null it tries to use one from current (parent) [KGroundCtx],
 * and defaults to [pl.mareklangiewicz.ulog.hack.ulog] if no [ULog] found.
 */
@Deprecated("")
suspend fun <T> withKGroundXIOCtx(
  name: String? = null,
  cli: CLI? = null,
  fs: FileSystem? = null,
  ulog: ULog? = null,
  usubmit: USubmit? = null,
  block: suspend CoroutineScope.() -> T,
): T = withContext(
  KGroundXIOCtx(
    cli ?: coroutineContext[KGroundXIOCtx]?.cli ?: CLI.SYS,
    fs ?: coroutineContext[KGroundIOCtx]?.fs ?: DefaultOkioFileSystemOrErr,
    ulog ?: coroutineContext[KGroundCtx]?.ulog ?: pl.mareklangiewicz.ulog.hack.ulog,
    usubmit ?: coroutineContext[KGroundCtx]?.usubmit ?: USubmitNotSupportedErr(),
  ) plusIfNN name?.let(::CoroutineName),
  block,
)
