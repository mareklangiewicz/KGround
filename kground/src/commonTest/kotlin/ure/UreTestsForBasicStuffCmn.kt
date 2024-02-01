package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
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


@OptIn(DelicateApi::class)
fun testUreWithDifferentOptions() {

    "ureLineBreak matches line breaks" o { ureLineBreak.compile().findAll(example1).count() eq 2 }

    "chAnyInLine does NOT match line breaks" o { chAnyInLine.compile().findAll(example1).count() eq example1.length - 2 }

    "chAnyAtAll does match every character" o { chAnyAtAll.compile().findAll(example1).count() eq example1.length }

    val ureBOLaBcD = atBOLine then ureText("aBcD")
    val ureBcDeEOL = ureText("BcDe") then atEOLine

    "examples constructed as expected" o {
        ureBOLaBcD.toIR() eq IR("^aBcD")
        ureBcDeEOL.toIR() eq IR("BcDe\$")
    }

    "by default it gets compiled with just multiline option" o {
        val re = ureBOLaBcD.compile()
        re.options eq setOf(MULTILINE)
    }

    // TODO NOW: continue
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

