package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.findSingle
import pl.mareklangiewicz.uspek.*
import kotlin.text.RegexOption.*

const val example1 = "aBcDe\naBcDe\nABCDE"

fun testSomeUreBasicStuff() {

    // some example very basic sanity tests
    "chDot compiled pattern is quoted dot" o { chDot.compile().pattern eq "\\." }
    "chAnyInLine compiled pattern just a dot" o { chAnyInLine.compile().pattern eq "." }

    testUreWithDifferentOptions()
    testUreQuantifAndAtomic()
    // TODO("Test some quantifiers (is UreQuantif.toClosedIR() correct?)")
    // TODO NOW

    testUreBasicEmail()
}

fun testUreQuantifAndAtomic() {
    // TODO NOW (which platforms support atomic, which possessive quantifiers, etc)
}


@OptIn(DelicateApi::class, NotPortableApi::class)
fun testUreWithDifferentOptions() {

    "ureLineBreak matches line breaks" o { ureLineBreak.compile().findAll(example1).count() eq 2 }

    "chAnyInLine does NOT match line breaks" o { chAnyInLine.compile().findAll(example1).count() eq example1.length - 2 }

    "chAnyAtAll does match every character" o { chAnyAtAll.compile().findAll(example1).count() eq example1.length }

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
        "reBOLaBcD matches example1 twice" o { reBOLaBcD.findAll(example1).count() eq 2 }
        "reBcDeEOL matches example1 twice" o { reBcDeEOL.findAll(example1).count() eq 2 }
    }
    "On compile with empty options" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions()
        val reBcDeEOL = ureBcDeEOL.compileWithOptions()
        "reBOLaBcD gets compiled with no options" o { reBOLaBcD.options eq setOf() }
        "reBcDeEOL gets compiled with no options" o { reBcDeEOL.options eq setOf() }
        "reBOLaBcD matches example1 once at very start" o { reBOLaBcD.findSingle(example1).range eq 0..3 }
        "reBcDeEOL does NOT match example1 anywhere" o { reBcDeEOL.findAll(example1).count() eq 0 }
    }
    "On compile with ignore case" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE)
        "reBOLaBcD gets compiled with only ignore case option" o { reBOLaBcD.options eq setOf(IGNORE_CASE) }
        "reBcDeEOL gets compiled with only ignore case option" o { reBcDeEOL.options eq setOf(IGNORE_CASE) }
        "reBOLaBcD matches example1 once at very start" o { reBOLaBcD.findSingle(example1).range eq 0..3 }
        "reBcDeEOL matches example1 once at very end" o { reBcDeEOL.findSingle(example1).range eq 13..16 }
    }
    "On compile with ignore case and multiline" o {
        val reBOLaBcD = ureBOLaBcD.compileWithOptions(IGNORE_CASE, MULTILINE)
        val reBcDeEOL = ureBcDeEOL.compileWithOptions(IGNORE_CASE, MULTILINE)
        "reBOLaBcD gets compiled with both options" o { reBOLaBcD.options eq setOf(IGNORE_CASE, MULTILINE) }
        "reBcDeEOL gets compiled with both options" o { reBcDeEOL.options eq setOf(IGNORE_CASE, MULTILINE) }
        "reBOLaBcD matches example1 three times" o { reBOLaBcD.findAll(example1).count() eq 3 }
        "reBcDeEOL matches example1 three times" o { reBcDeEOL.findAll(example1).count() eq 3 }
    }

}

@OptIn(DelicateApi::class)
fun testUreBasicEmail() {
    "On ureBasicEmail" o {
        "assert IR as expected" o {
            // This assertion is kinda lame (expecting exact impl/ir / cementing impl),
            // but it's useful for me now as documentation and to track if sth changes.
            ureBasicEmail.toIR().str eq """\b(?<user>[\w.\-]+)\b@\b(?<domain>(?:[\w\-]+\.)+[\w\-]{2,16})\b"""
        }
        testUreEmail(ureBasicEmail)
    }
}

private fun testUreEmail(ureEmail: Ure) {
    val ureEmailIR = ureEmail.toIR()
    val ureEmailRegex = ureEmail.compile()
    // println("ure:\n$ureEmail")
    // println("ureIR:\n$ureEmailIR")
    // println("regex:\n$ureEmailRegex")
    testRegexWithEmail(ureEmailRegex, "marek.langiewicz@gmail.com", "marek.langiewicz", "gmail.com")
    testRegexWithEmail(ureEmailRegex, "langara@wp.pl", "langara", "wp.pl")
    testRegexWithEmail(ureEmailRegex, "a.b.c@d.e.f.hhh", "a.b.c", "d.e.f.hhh")
    testRegexWithIncorrectEmail(ureEmailRegex, "a.b.cd.e.f.hhh")
    testRegexWithIncorrectEmail(ureEmailRegex, "a@b@c")
}

private fun testRegexWithEmail(regex: Regex, email: String, expectedUser: String, expectedDomain: String) {
    "for email: $email" o {
        "it matches" o { regex.matches(email) eq true }
        "for match result" o {
            val result = regex.matchEntire(email)!!
            val groups = result.groups
            "it captures expected user name: $expectedUser" o { groups["user"]!!.value eq expectedUser }
            "it captures expected domain: $expectedDomain" o { groups["domain"]!!.value eq expectedDomain }
        }
    }
}

private fun testRegexWithIncorrectEmail(regex: Regex, email: String) {
    "for incorrect email: $email" o {
        "it does not match" o { regex.matches(email) eq false }
        "match result is null" o { regex.matchEntire(email) eq null }
    }
}

