package pl.mareklangiewicz.ure

import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*
import kotlin.test.Test
import kotlin.test.assertFailsWith


internal val platform = getCurrentPlatformKind()


// TODO_maybe: Add sth like this to USpek? Or to USpekX?
internal inline fun <reified T : Throwable> String.failsWith(crossinline code: () -> Unit) = o {
    assertFailsWith<T>(block = code)
}


class UreTestsCmn {

    init { "INIT ${this::class.simpleName}".teePP }

    @Test fun t() {
        uspek { testUreCmn() }
        GlobalUSpekContext.branch.assertAllGood()
    }
}

/**
 * Temporary workaround to make sure I notice failed tests in IntelliJ.
 * Without it, I get a green checkmark in IntelliJ on JS and LINUX even if some tests failed, and I have to check logs.
 * In the future I'll have custom mpp runner+logger, so this workaround will be removed.
 */
private fun USpekTree.assertAllGood() {
    if (failed) throw end!!.cause!!
    branches.values.forEach { it.assertAllGood() }
}

fun testUreCmn() {
    testSomeUreCharClasses()
    testSomeUreBasicStuff()
}
