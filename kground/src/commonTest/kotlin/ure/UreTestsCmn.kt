package pl.mareklangiewicz.ure

import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UreTestsCmn {

    init { "INIT ${this::class.simpleName}".teePP }

    @Test fun t() {
        uspek { testUreCmn() }
        GlobalUSpekContext.branch.assertAllGood()
    }
}

fun testUreCmn() {
    testSomeUreBasicStuff()
    testSomeUreCharClasses()
}
