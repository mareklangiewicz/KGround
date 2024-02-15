package pl.mareklangiewicz.ure

import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.regex.bad.chkMatchEntire
import pl.mareklangiewicz.regex.bad.chkNotMatchEntire
import pl.mareklangiewicz.uspek.*


internal val platform = getCurrentPlatformKind()


// TODO_maybe: Add sth like this to USpekX?
inline fun <reified T : Throwable> String.oThrows(
    crossinline expectation: (T) -> Boolean = { true },
    crossinline code: () -> Unit
) = o { chkThrows<T>(expectation) { code() } }


/**
 * Extension functions with prefix "tst" are similar to "chk", but in test sources,
 * and they use uspek and add some (usually small) subtree to the current uspek tree.
 * All functions with "chk" prefix are in main sources and unaware of uspek.
 * The tst prefixed extensions usually use some chk prefixed checks inside.
 */

fun Ure.tstCompiles(
    vararg useNamedArgs: Unit,
    alsoCheckNegation: Boolean = true,
) = "compiles" o {
    compile() // will throw if the platform doesn't support it
    if (alsoCheckNegation)
        not().compile() // will throw if the platform doesn't support it or Ure.not() doesn't support it
}

/**
 * Note: It throws different [Throwable] on different platforms.
 * I encountered [SyntaxError] on JS and [InvalidArgumentException] on JVM and LINUX and [PatternSyntaxException] on LINUX.
 */
fun Ure.tstDoesNotCompile() = "does NOT compile".oThrows<Throwable>({
    println("fake debug ulog on $platform: $it")
    // FIXME_later: use ULog and KGroundCtx, when ready (level: DEBUG)
    // (throwables here are very interesting and I definitely want to have it logged, but in the right place)
    true
}) { compile() }

fun Ure.tstCompilesOnlyOn(platforms: List<String>, vararg useNamedArgs: Unit, alsoCheckNegation: Boolean = true) {
    if (platform in platforms) tstCompiles(alsoCheckNegation = alsoCheckNegation) else tstDoesNotCompile()
}


fun Ure.tstMatchCorrectChars(
    match: List<String>,
    matchNot: List<String>,
    vararg useNamedArgs: Unit,
    alsoCheckNegation: Boolean = true,
    verbose: Boolean = false,
) = "matches correct chars" o {
    tstMatchAll(*match.toTypedArray(), verbose = verbose)
    tstMatchNone(*matchNot.toTypedArray(), verbose = verbose)
    if (alsoCheckNegation) {
        not().tstMatchAll(*matchNot.toTypedArray(), verbose = verbose)
        not().tstMatchNone(*match.toTypedArray(), verbose = verbose)
    }
}

fun Ure.tstMatchAll(vararg examples: String, verbose: Boolean = false) {
    val re = compile()
    for (e in examples)
        if (verbose) "matches $e" o { re.chkMatchEntire(e) }
        else re.chkMatchEntire(e)
}

fun Ure.tstMatchNone(vararg examples: String, verbose: Boolean = false) {
    val re = compile()
    for (e in examples)
        if (verbose) "does not match $e" o { re.chkNotMatchEntire(e) }
        else re.chkNotMatchEntire(e)
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

