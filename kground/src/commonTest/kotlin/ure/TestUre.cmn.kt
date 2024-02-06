package pl.mareklangiewicz.ure

import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUreCmn {

    init { "INIT ${this::class.simpleName}".teePP }

    @Test fun t() {
        uspek { testUreCmn() }
        GlobalUSpekContext.branch.assertAllGood()
    }
}

fun testUreCmn() {
    testStdLibRegexIssueJvm()
    testUreBasicStuff()
    testUreCharClasses()
}

/** https://youtrack.jetbrains.com/issue/KT-65531/Regex-content-can-change-Regex.options */
fun testStdLibRegexIssueJvm() {
    if (platform == "JVM") "test std lib regex issue on JVM".failsWith<Error> {
        val pattern = "\\s+(?m)$" // the original in stdlib was: "\\s+$" (I changed it to reproduce the issue in stdlib)
        val regex1 = Regex(pattern, RegexOption.IGNORE_CASE)
        assertEquals(pattern, regex1.pattern)
        assertEquals(setOf(RegexOption.IGNORE_CASE), regex1.options)

        val options2 = setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        val regex2 = Regex(pattern, options2)
        assertEquals(options2, regex2.options)
    }
}
