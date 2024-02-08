package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*
import kotlin.text.RegexOption.*


@OptIn(DelicateApi::class, NotPortableApi::class, SecondaryApi::class)
fun testUreWithOptions() {

    "On compile with default options" o {
        val reBOLaBcD = ureBOLaBcD.compile()
        val reBcDeEOL = ureBcDeEOL.compile()
        "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options chkEq setOf(MULTILINE) }
        "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options chkEq setOf(MULTILINE) }
        "reBOLaBcD matches exampleABCDEx3 twice" o { reBOLaBcD.findAll(exampleABCDEx3).count() chkEq 2 }
        "reBcDeEOL matches exampleABCDEx3 twice" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 2 }
    }
    "On compile with empty options" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions()
        val reBcDeEOL = ureBcDeEOL.compileWithOptions()
        "reBOLaBcD gets compiled with no options" o { reBOLaBcD.options.chkEmpty() }
        "reBcDeEOL gets compiled with no options" o { reBcDeEOL.options.chkEmpty() }
        "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range chkEq 0..3 }
        "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 0 }
    }
    "On compile with ignore case" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE)
        "reBOLaBcD gets compiled with only ignore case option" o { reBOLaBcD.options chkEq setOf(IGNORE_CASE) }
        "reBcDeEOL gets compiled with only ignore case option" o { reBcDeEOL.options chkEq setOf(IGNORE_CASE) }
        "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range chkEq 0..3 }
        "reBcDeEOL matches exampleABCDEx3 once at very end" o { reBcDeEOL.findSingle(exampleABCDEx3).range chkEq 13..16 }
    }
    "On compile with ignore case and multiline" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE, MULTILINE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE, MULTILINE)
        "reBOLaBcD gets compiled with both options" o { reBOLaBcD.options chkEq setOf(IGNORE_CASE, MULTILINE) }
        "reBcDeEOL gets compiled with both options" o { reBcDeEOL.options chkEq setOf(IGNORE_CASE, MULTILINE) }
        "reBOLaBcD matches exampleABCDEx3 three times" o { reBOLaBcD.findAll(exampleABCDEx3).count() chkEq 3 }
        "reBcDeEOL matches exampleABCDEx3 three times" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 3 }
    }
    "On example ure s wrapped in withOptionsEnabled IGNORE_CASE on $platform" o {
        if (platform == "JS") testUreDoesNotCompile(ureBOLaBcD.withOptionsEnabled(IGNORE_CASE))
        else "On compile normally" o {
            val reBOLaBcD = ureBOLaBcD.withOptionsEnabled(IGNORE_CASE).compile()
            val reBcDeEOL = ureBcDeEOL.withOptionsEnabled(IGNORE_CASE).compile()
            "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options chkEq setOf(MULTILINE) }
            "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options chkEq setOf(MULTILINE) }
            "reBOLaBcD matches exampleABCDEx3 three times" o { reBOLaBcD.findAll(exampleABCDEx3).count() chkEq 3 }
            "reBcDeEOL matches exampleABCDEx3 three times" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 3 }
        }
    }
    "On example ure s wrapped in withOptionsDisabled MULTILINE on $platform" o {
        if (platform == "JS") testUreDoesNotCompile(ureBOLaBcD.withOptionsDisabled(MULTILINE))
        else "On compile normally" o {
            val reBOLaBcD = ureBOLaBcD.withOptionsDisabled(MULTILINE).compile()
            val reBcDeEOL = ureBcDeEOL.withOptionsDisabled(MULTILINE).compile()
            "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options chkEq setOf(MULTILINE) }
            "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options chkEq setOf(MULTILINE) }
            "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range chkEq 0..3 }
            "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 0 }
        }
    }
    "On example ure s prepended with weird ureWithOptionsAhead disable MULTILINE on $platform" o {
        if (platform == "JS") testUreDoesNotCompile(ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBOLaBcD)
        else "On compile normally" o {
            val reBOLaBcD = (ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBOLaBcD).compile()
            val reBcDeEOL = (ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBcDeEOL).compile()

            // TODO_later: Track if it's a bug in stdlib
            // look like they have mutable field that changes during parsing RE and the (?-m) removes MULTILINE from it.
            // see my bug report: https://youtrack.jetbrains.com/issue/KT-65531/Regex-content-can-change-Regex.options
            // also see: UreTestsCmd:testStdLibRegexIssue
            "reBOLaBcD INCORRECTLY reports being compiled with no options" o { reBOLaBcD.options.chkEmpty() }
            "reBcDeEOL INCORRECTLY reports being compiled with no options" o { reBcDeEOL.options.chkEmpty() }

            "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range chkEq 0..3 }
            "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 0 }
        }
    }
    "On example ure s appended with weird ureWithOptionsAhead disable MULTILINE on $platform" o {
        if (platform == "JS") testUreDoesNotCompile(ureBOLaBcD then ureWithOptionsAhead(disable = setOf(MULTILINE)))
        else "On compile normally" o {
            val reBOLaBcD = (ureBOLaBcD then ureWithOptionsAhead(disable = setOf(MULTILINE))).compile()
            val reBcDeEOL = (ureBcDeEOL then ureWithOptionsAhead(disable = setOf(MULTILINE))).compile()

            // TODO_someday: Investigate if it's a bug in stdlib
            // look like they have mutable field that changes during parsing RE and the (?-m) removes MULTILINE from it.
            "reBOLaBcD INCORRECTLY reports being compiled with no options" o { reBOLaBcD.options.chkEmpty() }
            "reBcDeEOL INCORRECTLY reports being compiled with no options" o { reBcDeEOL.options.chkEmpty() }

            // appended (?-m) should not change the behavior at all; it would if it was BEFORE the rest of RE
            "reBOLaBcD matches exampleABCDEx3 twice" o { reBOLaBcD.findAll(exampleABCDEx3).count() chkEq 2 }
            "reBcDeEOL matches exampleABCDEx3 twice" o { reBcDeEOL.findAll(exampleABCDEx3).count() chkEq 2 }
        }
    }
    "On inserting weird ureWithOptionsAhead in the middle of UreConcatenation" o {
        val ure = ure {
            + ureBcDeEOL // should not ignore case while matching this
            + ureLineBreak
            + ureWithOptionsAhead(enable = setOf(IGNORE_CASE))
            + ureBOLaBcD // should ignore case while matching this
        }
        if (platform == "JS") testUreDoesNotCompile(ure)
        else "On compile normally" o {
            val re = ure.compile()
            "compiled pattern is as expected" o { re.pattern chkEq """BcDe$(?:\r?\n)(?i)^aBcD""" }
            "re matches exampleABCDEx3 one time at 1" o { re.findSingle(exampleABCDEx3).range.start chkEq 1 }
            "re matches exampleABCDEx3 one time at 7 when starting at 2" o { re.findSingle(exampleABCDEx3, 2).range.start chkEq 7 }

        }
    }

}

