package pl.mareklangiewicz.kground.io

import kotlinx.coroutines.*
import okio.FileSystem
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.ulog.*
import kotlin.coroutines.*

interface WithFS { val fs: FileSystem }

interface KGroundIO: KGround, WithFS

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
open class KGroundIOCtx(override val fs: FileSystem, ulog: ULog): KGroundCtx(ulog), KGroundIO {
    companion object Key : AbstractCoroutineContextKey<KGroundCtx, KGroundIOCtx>(KGroundCtx, { it as? KGroundIOCtx })
}

/**
 * @param name set to non-null to add new [CoroutineName] to context
 * @param fs set to non-null to set specific [FileSystem] in context,
 * when null it tries to use one from current (parent) [KGroundIOCtx],
 * and defaults to [DefaultOkioFileSystemOrErr] if no [FileSystem] found.
 * @param ulog set to non-null to change the [ULog],
 * when null it tries to use one from current (parent) [KGroundCtx],
 * and defaults to [ULogPrintLn] if no [ULog] found.
 */
suspend fun <T> withKGroundIOCtx(
    name: String? = null,
    fs: FileSystem? = null,
    ulog: ULog? = null,
    block: suspend CoroutineScope.() -> T,
): T = withContext(
    KGroundIOCtx(
        fs ?: coroutineContext[KGroundIOCtx]?.fs ?: DefaultOkioFileSystemOrErr,
        ulog ?: coroutineContext[KGroundCtx]?.ulog ?: ULogPrintLn()
    ) plusIfNN name?.let(::CoroutineName),
    block
)
