package pl.mareklangiewicz.kground

import kotlinx.coroutines.*
import pl.mareklangiewicz.ulog.*
import kotlin.coroutines.*

interface WithULog { val ulog: ULog }

interface KGround: WithULog

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
open class KGroundCtx(override val ulog: ULog): KGround, CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<KGroundCtx>
    override val key: CoroutineContext.Key<*> get() = Key
    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? = getPolymorphicElement(key)
    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext = minusPolymorphicKey(key)
}

infix fun CoroutineContext.plusIfNN(c: CoroutineContext?) = when (c) { null -> this; else -> this + c }

/**
 * @param name set to non-null to add new [CoroutineName] to context
 * @param ulog set to non-null to change the [ULog],
 * when null it tries to use one from current (parent) [KGroundCtx],
 * and defaults to [ULogPrintLn] if no [ULog] found.
 */
suspend fun <T> withKGroundCtx(
    name: String? = null,
    ulog: ULog? = null,
    block: suspend CoroutineScope.() -> T,
): T = withContext(
    KGroundCtx(
        ulog ?: coroutineContext[KGroundCtx]?.ulog ?: ULogPrintLn()
    ) plusIfNN name?.let(::CoroutineName),
    block
)