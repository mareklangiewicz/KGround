package pl.mareklangiewicz.ure

import pl.mareklangiewicz.bad.NotEqStateErr
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*
import kotlin.test.Test

class TestUreCmn {

    init { "INIT ${this::class.simpleName}".teePP }

    @Test fun t() {
        uspek { testUreCmn() }
        GlobalUSpekContext.branch.assertAllGood()
    }
}

fun testUreCmn() = "On testUreCmn on $platform" o {
    testStdLibRegexIssueJvm()
    testUreBasicStuff()
    testUreCharClasses()
    testUreQuantifiersAndAtomicGroups()
    testUreCommonStuff()
}

/** https://youtrack.jetbrains.com/issue/KT-65531/Regex-content-can-change-Regex.options */
fun testStdLibRegexIssueJvm() {
    if (platform == "JVM") "test std lib regex issue on JVM".oThrows<NotEqStateErr> {
        val pattern = "\\s+(?m)$" // the original in stdlib was: "\\s+$" (I changed it to reproduce the issue in stdlib)
        val regex1 = Regex(pattern, RegexOption.IGNORE_CASE)
        regex1.pattern chkEq pattern
        regex1.options chkEq setOf(RegexOption.IGNORE_CASE)

        val options2 = setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        val regex2 = Regex(pattern, options2)
        regex2.options chkEq options2
    }
}
