@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.bad

import kotlin.apply
import kotlin.collections.isNotEmpty
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


/**
 * Why introducing own strange exceptions? To easily spot when my own exceptions happen in large codebases.
 * I'm using bad/hacky practices (like funny shortcut names) intentionally to avoid name clashes with other code.
 * These exceptions still inherit from std "Illegal(State/Argument)" so can still be handled as usual.
 */
interface BadErr

open class BadStateErr(override val message: String? = null, override val cause: Throwable? = null): IllegalStateException(), BadErr
open class BadArgErr(override val message: String? = null, override val cause: Throwable? = null): IllegalArgumentException(), BadErr

inline fun bad(lazyMessage: () -> String = { "this is bad" }): Nothing = throw BadStateErr(lazyMessage())
inline fun badArg(lazyMessage: () -> String = { "this arg is bad" }): Nothing = throw BadArgErr(lazyMessage())

@OptIn(ExperimentalContracts::class)
inline fun chk(value: Boolean, lazyMessage: () -> String = { "this is bad" }) {
    contract { returns() implies value }
    value || bad(lazyMessage)
}

@OptIn(ExperimentalContracts::class)
inline fun req(value: Boolean, lazyMessage: () -> String = { "this arg is bad" }) {
    contract { returns() implies value }
    value || badArg(lazyMessage)
}

inline fun <T: Any> T?.chkNN(lazyMessage: () -> String = { "this null is bad" }): T = this ?: bad(lazyMessage)
inline fun <T: Any> T?.reqNN(lazyMessage: () -> String = { "this null arg is bad" }): T = this ?: badArg(lazyMessage)

open class NotEqStateErr(val exp: Any?, val act: Any?, message: String? = null): BadStateErr(message)

open class NotEqArgErr(val exp: Any?, val act: Any?, message: String? = null): BadArgErr(message)


open class NotSameStateErr(val exp: Any?, val act: Any?, message: String? = null): BadStateErr(message)

open class NotSameArgErr(val exp: Any?, val act: Any?, message: String? = null): BadArgErr(message)

inline fun <T> T.chkEq(exp: Any?, lazyMessage: () -> String): T =
    apply { this == exp || throw NotEqStateErr(exp, this, lazyMessage()) }

inline fun <T> T.reqEq(exp: Any?, lazyMessage: () -> String): T =
    apply { this == exp || throw NotEqArgErr(exp, this, lazyMessage()) }

inline fun <T> T.chkSame(exp: Any?, lazyMessage: () -> String): T =
    apply { this === exp || throw NotSameStateErr(exp, this, lazyMessage()) }

inline fun <T> T.reqSame(exp: Any?, lazyMessage: () -> String): T =
    apply { this === exp || throw NotEqArgErr(exp, this, lazyMessage()) }

inline infix fun <T> T.chkEq(exp: Any?): T = chkEq(exp) { "bad $this != $exp" }
inline infix fun <T> T.reqEq(exp: Any?): T = reqEq(exp) { "bad arg $this != $exp" }

inline infix fun <T> T.chkSame(exp: Any?): T = chkSame(exp) { "bad $this !== $exp" }
inline infix fun <T> T.reqSame(exp: Any?): T = reqSame(exp) { "bad arg $this !== $exp" }

@OptIn(ExperimentalContracts::class)
inline fun Any?.chkNull(lazyMessage: () -> String = { "this non-null is bad" }): Nothing? {
    contract { returns() implies (this@chkNull == null) }
    this == null || throw NotSameStateErr(null, this, lazyMessage())
    return null
}

@OptIn(ExperimentalContracts::class)
inline fun Any?.reqNull(lazyMessage: () -> String = { "this non-null arg is bad" }): Nothing? {
    contract { returns() implies (this@reqNull == null) }
    this == null || throw NotSameArgErr(null, this, lazyMessage())
    return null
}

inline fun Boolean?.chkTrue(lazyMessage: () -> String = { "this is not true: $this" }): Boolean =
    chkSame(true, lazyMessage)!!

inline fun Boolean?.reqTrue(lazyMessage: () -> String = { "this arg is not true: $this" }): Boolean =
    reqSame(true, lazyMessage)!!

inline fun Boolean?.chkFalse(lazyMessage: () -> String = { "this is not false: $this" }): Boolean =
    chkSame(false, lazyMessage)!!

inline fun Boolean?.reqFalse(lazyMessage: () -> String = { "this arg is not false: $this" }): Boolean =
    reqSame(false, lazyMessage)!!


inline fun <reified T: Throwable> chkThrows(
    expectation: (T) -> Boolean = { true },
    lazyMessage: () -> String = { "code does not throw expected ${T::class}" },
    code: () -> Any?,
): T = try { code(); null } catch (throwable: Throwable) {
    throwable is T && expectation(throwable) || throw BadStateErr(lazyMessage(), cause = throwable)
    throwable
} ?: bad(lazyMessage)

inline fun <reified T: Throwable> reqThrows(
    expectation: (T) -> Boolean = { true },
    lazyMessage: () -> String = { "code arg does not throw expected ${T::class}" },
    code: () -> Any?,
): T = try { code(); null } catch (throwable: Throwable) {
    throwable is T && expectation(throwable) || throw BadArgErr(lazyMessage(), cause = throwable)
    throwable
} ?: badArg(lazyMessage)



inline fun <T: Collection<*>> T.chkEmpty(lazyMessage: () -> String = { "this not empty is bad" }): T =
    apply { isEmpty() || throw BadStateErr(lazyMessage()) }

inline fun <T: Collection<*>> T.reqEmpty(lazyMessage: () -> String = { "this not empty arg is bad" }): T =
    apply { isEmpty() || throw BadArgErr(lazyMessage()) }

inline fun <T: Collection<*>> T.chkEmptyVerbose(): T =
    apply { isEmpty() || throw BadStateErr("this not empty collection is bad: $this") }

inline fun <T: Collection<*>> T.reqEmptyVerbose(): T =
    apply { isEmpty() || throw BadArgErr("this not empty arg collection is bad: $this") }

inline fun <T: Collection<*>> T.chkNotEmpty(lazyMessage: () -> String = { "this empty is bad" }): T =
    apply { isNotEmpty() || throw BadStateErr(lazyMessage()) }

inline fun <T: Collection<*>> T.reqNotEmpty(lazyMessage: () -> String = { "this empty arg is bad" }): T =
    apply { isNotEmpty() || throw BadArgErr(lazyMessage()) }

