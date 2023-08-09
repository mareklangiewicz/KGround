package pl.mareklangiewicz.kground

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


/**
 * Why introducing own strange exceptions? To easily spot when my own exceptions happen in large codebases.
 * I'm using bad/hacky practices (like funny shortcut names) intentionally to avoid name clashes with other code.
 * These exceptions still override std "Illegal(State/Argument)" so can still be handled as usual.
 */
interface BadErr

class BadStateErr(override val message: String? = null, override val cause: Throwable? = null): IllegalStateException(), BadErr
class BadArgErr(override val message: String? = null, override val cause: Throwable? = null): IllegalArgumentException(), BadErr

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

