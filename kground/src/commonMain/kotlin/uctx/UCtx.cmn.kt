package pl.mareklangiewicz.uctx

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

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
 * suspend inline fun <reified T: ULog> implictx(): T =
 *   coroutineContext[ULog] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }
 */
interface UCtx : CoroutineContext.Element

suspend inline fun <R> uctx(vararg elems: UCtx, noinline block: suspend CoroutineScope.() -> R) =
  withContext(elems.fold<UCtx, CoroutineContext>(EmptyCoroutineContext) { a, b -> a + b }, block)

