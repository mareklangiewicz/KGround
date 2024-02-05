@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.kground

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

