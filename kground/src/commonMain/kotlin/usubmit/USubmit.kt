@file:Suppress("unused")

package pl.mareklangiewicz.usubmit

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.uctx.UCtx

/**
 * I want to keep stable basic USubmit contract using Any?: "USubmit.invoke(data: Any?): Any?".
 * It's a "hole" in type system for good reasons. I don't want to force any conventions/types globally.
 * See XDModel.kt file [pl.mareklangiewicz.usubmit.xd.XD] for "funny" proposed experimental data model,
 * and for more explanations in kdoc comments.
 */
fun interface USubmit : UCtx {
  suspend operator fun invoke(data: Any?): Any?
  companion object Key : CoroutineContext.Key<USubmit>
  override val key: CoroutineContext.Key<*> get() = Key
}
suspend inline fun <reified T: USubmit> implictxOrNull(): T? = coroutineContext[USubmit] as? T

suspend inline fun <reified T: USubmit> implictx(): T =
  implictxOrNull() ?: bad { "No ${T::class.simpleName} provided in coroutine context." }
