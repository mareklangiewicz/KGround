package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*
import kotlin.text.RegexOption.*


@OptIn(DelicateApi::class, NotPortableApi::class, SecondaryApi::class)
fun testUreWithDifferentOptions() {

    "ureLineBreak matches line breaks" o { ureLineBreak.compile().findAll(exampleABCDEx3).count() eq 2 }

    "chAnyInLine does NOT match line breaks" o { chAnyInLine.compile().findAll(exampleABCDEx3).count() eq exampleABCDEx3.length - 2 }

    "chAnyAtAll does match every character" o { chAnyAtAll.compile().findAll(exampleABCDEx3).count() eq exampleABCDEx3.length }

    val ureBOLaBcD = atBOLine then ureText("aBcD")
    val ureBcDeEOL = ureText("BcDe") then atEOLine

    "example ure s constructed as expected" o {
        ureBOLaBcD.toIR() eq IR("^aBcD")
        ureBcDeEOL.toIR() eq IR("BcDe\$")
    }

    "On compile with default options" o {
        val reBOLaBcD = ureBOLaBcD.compile()
        val reBcDeEOL = ureBcDeEOL.compile()
        "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options eq setOf(MULTILINE) }
        "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options eq setOf(MULTILINE) }
        "reBOLaBcD matches exampleABCDEx3 twice" o { reBOLaBcD.findAll(exampleABCDEx3).count() eq 2 }
        "reBcDeEOL matches exampleABCDEx3 twice" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 2 }
    }
    "On compile with empty options" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions()
        val reBcDeEOL = ureBcDeEOL.compileWithOptions()
        "reBOLaBcD gets compiled with no options" o { reBOLaBcD.options eq setOf() }
        "reBcDeEOL gets compiled with no options" o { reBcDeEOL.options eq setOf() }
        "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range eq 0..3 }
        "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 0 }
    }
    "On compile with ignore case" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE)
        "reBOLaBcD gets compiled with only ignore case option" o { reBOLaBcD.options eq setOf(IGNORE_CASE) }
        "reBcDeEOL gets compiled with only ignore case option" o { reBcDeEOL.options eq setOf(IGNORE_CASE) }
        "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range eq 0..3 }
        "reBcDeEOL matches exampleABCDEx3 once at very end" o { reBcDeEOL.findSingle(exampleABCDEx3).range eq 13..16 }
    }
    "On compile with ignore case and multiline" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE, MULTILINE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE, MULTILINE)
        "reBOLaBcD gets compiled with both options" o { reBOLaBcD.options eq setOf(IGNORE_CASE, MULTILINE) }
        "reBcDeEOL gets compiled with both options" o { reBcDeEOL.options eq setOf(IGNORE_CASE, MULTILINE) }
        "reBOLaBcD matches exampleABCDEx3 three times" o { reBOLaBcD.findAll(exampleABCDEx3).count() eq 3 }
        "reBcDeEOL matches exampleABCDEx3 three times" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 3 }
    }
    "On example ure s wrapped in withOptionsEnabled IGNORE_CASE on $platform" o {
        if (platform == "JS") itDoesNotCompile(ureBOLaBcD.withOptionsEnabled(IGNORE_CASE))
        else "On compile normally" o {
            val reBOLaBcD = ureBOLaBcD.withOptionsEnabled(IGNORE_CASE).compile()
            val reBcDeEOL = ureBcDeEOL.withOptionsEnabled(IGNORE_CASE).compile()
            "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options eq setOf(MULTILINE) }
            "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options eq setOf(MULTILINE) }
            "reBOLaBcD matches exampleABCDEx3 three times" o { reBOLaBcD.findAll(exampleABCDEx3).count() eq 3 }
            "reBcDeEOL matches exampleABCDEx3 three times" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 3 }
        }
    }
    "On example ure s wrapped in withOptionsDisabled MULTILINE on $platform" o {
        if (platform == "JS") itDoesNotCompile(ureBOLaBcD.withOptionsDisabled(MULTILINE))
        else "On compile normally" o {
            val reBOLaBcD = ureBOLaBcD.withOptionsDisabled(MULTILINE).compile()
            val reBcDeEOL = ureBcDeEOL.withOptionsDisabled(MULTILINE).compile()
            "reBOLaBcD gets compiled with only multiline option" o { reBOLaBcD.options eq setOf(MULTILINE) }
            "reBcDeEOL gets compiled with only multiline option" o { reBcDeEOL.options eq setOf(MULTILINE) }
            "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range eq 0..3 }
            "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 0 }
        }
    }
    "On example ure s prepended with weird ureWithOptionsAhead disable MULTILINE on $platform" o {
        if (platform == "JS") itDoesNotCompile(ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBOLaBcD)
        else "On compile normally" o {
            val reBOLaBcD = (ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBOLaBcD).compile()
            val reBcDeEOL = (ureWithOptionsAhead(disable = setOf(MULTILINE)) then ureBcDeEOL).compile()

            // TODO_someday: Investigate if it's a bug in stdlib
            // look like they have mutable field that changes during parsing RE and the (?-m) removes MULTILINE from it.
            "reBOLaBcD INCORRECTLY reports being compiled with no options" o { reBOLaBcD.options eq setOf() }
            "reBcDeEOL INCORRECTLY reports being compiled with no options" o { reBcDeEOL.options eq setOf() }

            "reBOLaBcD matches exampleABCDEx3 once at very start" o { reBOLaBcD.findSingle(exampleABCDEx3).range eq 0..3 }
            "reBcDeEOL does NOT match exampleABCDEx3 anywhere" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 0 }
        }
    }
    "On example ure s appended with weird ureWithOptionsAhead disable MULTILINE on $platform" o {
        if (platform == "JS") itDoesNotCompile(ureBOLaBcD then ureWithOptionsAhead(disable = setOf(MULTILINE)))
        else "On compile normally" o {
            val reBOLaBcD = (ureBOLaBcD then ureWithOptionsAhead(disable = setOf(MULTILINE))).compile()
            val reBcDeEOL = (ureBcDeEOL then ureWithOptionsAhead(disable = setOf(MULTILINE))).compile()

            // TODO_someday: Investigate if it's a bug in stdlib
            // look like they have mutable field that changes during parsing RE and the (?-m) removes MULTILINE from it.
            "reBOLaBcD INCORRECTLY reports being compiled with no options" o { reBOLaBcD.options eq setOf() }
            "reBcDeEOL INCORRECTLY reports being compiled with no options" o { reBcDeEOL.options eq setOf() }

            // appended (?-m) should not change the behavior at all; it would if it was BEFORE the rest of RE
            "reBOLaBcD matches exampleABCDEx3 twice" o { reBOLaBcD.findAll(exampleABCDEx3).count() eq 2 }
            "reBcDeEOL matches exampleABCDEx3 twice" o { reBcDeEOL.findAll(exampleABCDEx3).count() eq 2 }
        }
    }

}

