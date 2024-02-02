package pl.mareklangiewicz.ure

import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.uspek.*
import kotlin.test.assertFailsWith


internal val platform = getCurrentPlatformKind()


// TODO_maybe: Add sth like this to USpek? Or to USpekX?
internal inline fun <reified T : Throwable> String.failsWith(crossinline code: () -> Unit) = o {
    assertFailsWith<T>(block = code)
}


internal fun itCompiles(ure: Ure, alsoCheckNegation: Boolean = true) = "it compiles" o {
    ure.compile() // will throw if the platform doesn't support it
    if (alsoCheckNegation) ure.not().compile() // will throw if the platform doesn't support it
}

/**
 * Note: It throws different [Throwable] on different platforms.
 * I encountered [SyntaxError] on JS and [InvalidArgumentException] on JVM and LINUX and [PatternSyntaxException] on LINUX.
 */
internal fun itDoesNotCompile(ure: Ure) = "it does not compile".failsWith<Throwable> { ure.compile() }

/**
 * Temporary workaround to make sure I notice failed tests in IntelliJ.
 * Without it, I get a green checkmark in IntelliJ on JS and LINUX even if some tests failed, and I have to check logs.
 * In the future I'll have custom mpp runner+logger, so this workaround will be removed.
 */
internal fun USpekTree.assertAllGood() {
    if (failed) throw end!!.cause!!
    branches.values.forEach { it.assertAllGood() }
}

