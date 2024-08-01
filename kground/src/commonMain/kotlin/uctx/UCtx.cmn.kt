package pl.mareklangiewicz.uctx

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import pl.mareklangiewicz.kground.plusIfNN

// TODO_later: try some generic class to wrap any type, but careful with keys/generics/equalities/instances etc.
// class UCtx<V>(val value: V) : CoroutineContext.Element {
//   override val key: CoroutineContext.Key<*> = ???
// }


/**
 * Each [UCtx] implementation should do sth like:
 * fun interface ULog: UCtx {
 *   operator fun invoke(level: ULogLevel, data: Any?)
 *   companion object Key : CoroutineContext.Key<ULog>
 *   override val key: CoroutineContext.Key<*> get() = Key
 * }
 * @see [pl.mareklangiewicz.ulog.ULog]
 * @see [pl.mareklangiewicz.ulog.localULog]
 * @see [pl.mareklangiewicz.ulog.localULogAs]
 */
interface UCtx : CoroutineContext.Element

suspend inline fun <R> uctx(
  context: CoroutineContext = EmptyCoroutineContext,
  name: String? = null,
  noinline block: suspend CoroutineScope.() -> R,
): R = withContext(context plusIfNN name?.let(::CoroutineName), block)
