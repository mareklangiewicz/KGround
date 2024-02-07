package pl.mareklangiewicz.ure

import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.uspek.*
import kotlin.test.assertFailsWith


internal val platform = getCurrentPlatformKind()


// TODO_maybe: Add sth like this to USpek? Or to USpekX?
inline fun <reified T : Throwable> String.failsWith(crossinline code: () -> Unit) = o {
    assertFailsWith<T>(block = code)
}


fun testUreCompiles(ure: Ure, alsoCheckNegation: Boolean = true) = "compiles" o {
    ure.compile() // will throw if the platform doesn't support it
    if (alsoCheckNegation) ure.not().compile() // will throw if the platform doesn't support it
}

/**
 * Note: It throws different [Throwable] on different platforms.
 * I encountered [SyntaxError] on JS and [InvalidArgumentException] on JVM and LINUX and [PatternSyntaxException] on LINUX.
 */
fun testUreDoesNotCompile(ure: Ure) = "does NOT compile".failsWith<Throwable> { ure.compile() }



fun testUreMatchesCorrectChars(
    ure: Ure,
    match: List<String>,
    matchNot: List<String>,
    verbose: Boolean = false,
) = "matches correct chars" o {
    testUreMatchesAll(ure, *match.toTypedArray(), verbose = verbose)
    testUreMatchesNone(ure, *matchNot.toTypedArray(), verbose = verbose)
    testUreMatchesAll(!ure, *matchNot.toTypedArray(), verbose = verbose)
    testUreMatchesNone(!ure, *match.toTypedArray(), verbose = verbose)
}

fun testUreMatchesAll(ure: Ure, vararg examples: String, verbose: Boolean = false) {
    val re = ure.compile()
    for (e in examples)
        if (verbose) "matches $e" o { chk(re.matches(e)) { "$re does not match $e" } }
        else chk(re.matches(e)) { "$re doesn't match $e" }
}

fun testUreMatchesNone(ure: Ure, vararg examples: String, verbose: Boolean = false) {
    val re = ure.compile()
    for (e in examples)
        if (verbose) "does not match $e" o { chk(!re.matches(e)) { "$re matches $e" } }
        else chk(!re.matches(e)) { "$re matches $e" }
}

/**
 * Temporary workaround to make sure I notice failed tests in IntelliJ.
 * Without it, I get a green checkmark in IntelliJ on JS and LINUX even if some tests failed, and I have to check logs.
 * In the future I'll have custom mpp runner+logger, so this workaround will be removed.
 */
internal fun USpekTree.assertAllGood() {
    if (failed) throw end!!.cause!!
    branches.values.forEach { it.assertAllGood() }
}

