package pl.mareklangiewicz.kground.io

import kotlin.coroutines.*
import kotlinx.coroutines.*
import okio.FileSystem
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.ulog.*

@Deprecated("")
interface WithFS {
  val fs: FileSystem
}

@Deprecated("")
interface WithKGroundIO : WithKGround, WithFS

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
open class KGroundIOCtx(override val fs: FileSystem, ulog: ULog, usubmit: USubmit) : KGroundCtx(ulog, usubmit),
  WithKGroundIO {
  companion object Key : AbstractCoroutineContextKey<KGroundCtx, KGroundIOCtx>(KGroundCtx, { it as? KGroundIOCtx })
}

/**
 * @param name set to non-null to add new [CoroutineName] to context
 * @param fs set to non-null to set specific [FileSystem] in context,
 * @param ulog set to non-null to set specific [ULog] in context,
 * @param usubmit set to non-null to set specific [USubmit] in context,
 */
@Deprecated("")
suspend fun <T> withKGroundIOCtx(
  name: String? = null,
  fs: FileSystem? = null,
  ulog: ULog? = null,
  usubmit: USubmit? = null,
  block: suspend CoroutineScope.() -> T,
): T = withContext(
  KGroundIOCtx(
    fs ?: coroutineContext[KGroundIOCtx]?.fs ?: DefaultOkioFileSystemOrErr,
    ulog ?: coroutineContext[KGroundCtx]?.ulog ?: pl.mareklangiewicz.ulog.hack.ulog,
    usubmit ?: coroutineContext[KGroundCtx]?.usubmit ?: USubmitNotSupportedErr(),
  ) plusIfNN name?.let(::CoroutineName),
  block,
)
