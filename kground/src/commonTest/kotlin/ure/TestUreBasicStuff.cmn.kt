package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*

const val exampleABCDEx3 = "aBcDe\naBcDe\nABCDE"

val ureBOLaBcD = atBOLine then ureText("aBcD")

val ureBcDeEOL = ureText("BcDe") then atEOLine


fun testUreBasicStuff() {
    testUreBasicSanity()
    testUreWithNames()
    testUreWithOptions()
    testUreQuantifAndAtomic()
    // TODO("Test some quantifiers (is UreQuantif.toClosedIR() correct?)")
    // TODO NOW

    testUreBasicEmail()
}

@OptIn(DelicateApi::class)
fun testUreBasicSanity() {
    // some example very basic sanity tests
    "chDot compiled pattern is quoted dot" o { chDot.compile().pattern chkEq "\\." }
    "chAnyInLine compiled pattern just a dot" o { chAnyInLine.compile().pattern chkEq "." }
    "ureLineBreak matches line breaks" o { ureLineBreak.compile().findAll(exampleABCDEx3).count() chkEq 2 }
    "chAnyInLine does NOT match line breaks" o { chAnyInLine.compile().findAll(exampleABCDEx3).count() chkEq exampleABCDEx3.length - 2 }
    "chAnyAtAll does match every character" o { chAnyAtAll.compile().findAll(exampleABCDEx3).count() chkEq exampleABCDEx3.length }
    "example ure s constructed as expected" o {
        ureBOLaBcD.toIR().str chkEq "^aBcD"
        ureBcDeEOL.toIR().str chkEq "BcDe\$"
    }
}


fun testUreQuantifAndAtomic() {
    // TODO NOW (which platforms support atomic, which possessive quantifiers, etc)
}


@OptIn(DelicateApi::class)
fun testUreBasicEmail() {
    "On ureBasicEmail" o {
        "assert IR as expected" o {
            // This assertion is kinda lame (expecting exact impl/ir / cementing impl),
            // but it's useful for me now as documentation and to track if sth changes.
            ureBasicEmail.toIR().str chkEq """\b(?<user>[\w.\-]+)\b@\b(?<domain>(?:[\w\-]+\.)+[\w\-]{2,16})\b"""
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
        "it matches" o { regex.matches(email).chkTrue() }
        "for match result" o {
            val result = regex.matchEntire(email)!!
            val groups = result.groups
            "it captures expected user name: $expectedUser" o { groups["user"]!!.value chkEq expectedUser }
            "it captures expected domain: $expectedDomain" o { groups["domain"]!!.value chkEq expectedDomain }
        }
    }
}

private fun testRegexWithIncorrectEmail(regex: Regex, email: String) {
    "for incorrect email: $email" o {
        "it does not match" o { regex.matches(email).chkFalse() }
        "match result is null" o { regex.matchEntire(email).chkNull() }
    }
}

