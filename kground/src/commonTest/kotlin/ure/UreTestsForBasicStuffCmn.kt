package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*

const val exampleABCDEx3 = "aBcDe\naBcDe\nABCDE"

val ureBOLaBcD = atBOLine then ureText("aBcD")

val ureBcDeEOL = ureText("BcDe") then atEOLine


fun testSomeUreBasicStuff() {
    testSomeUreSanity()
    testSomeUreWithName()
    testUreWithDifferentOptions()
    testUreQuantifAndAtomic()
    // TODO("Test some quantifiers (is UreQuantif.toClosedIR() correct?)")
    // TODO NOW

    testUreBasicEmail()
}

@OptIn(DelicateApi::class)
fun testSomeUreSanity() {
    // some example very basic sanity tests
    "chDot compiled pattern is quoted dot" o { chDot.compile().pattern chkEq "\\." }
    "chAnyInLine compiled pattern just a dot" o { chAnyInLine.compile().pattern chkEq "." }
    "ureLineBreak matches line breaks" o { ureLineBreak.compile().findAll(exampleABCDEx3).count() chkEq 2 }
    "chAnyInLine does NOT match line breaks" o { chAnyInLine.compile().findAll(exampleABCDEx3).count() chkEq exampleABCDEx3.length - 2 }
    "chAnyAtAll does match every character" o { chAnyAtAll.compile().findAll(exampleABCDEx3).count() chkEq exampleABCDEx3.length }
    "example ure s constructed as expected" o {
        ureBOLaBcD.toIR() chkEq IR("^aBcD")
        ureBcDeEOL.toIR() chkEq IR("BcDe\$")
    }
}

@OptIn(DelicateApi::class, NotPortableApi::class)
fun testSomeUreWithName() {
    "On wrapping ure s in withName" o {
        val ure1 = ureBOLaBcD.withName("ure1")
        val ure2 = ureBcDeEOL.withName("ure2")
        val ure3 = ure("ure3") { // convenient way to wrap withName when building concatenation
            + ure1
            0..MAX of chAnyAtAll
            + ure2
        }
        "constructed as expected" o {
            ure1.toIR() chkEq IR("(?<ure1>^aBcD)")
            ure2.toIR() chkEq IR("(?<ure2>BcDe$)")
            ure3.toIR() chkEq IR("(?<ure3>(?<ure1>^aBcD)[\\s\\S]*(?<ure2>BcDe$))")
        }
        "On compile" o {
            val re1 = ure1.compile()
            val re2 = ure2.compile()
            val re3 = ure3.compile()
            "On matching re1" o {
                // reminder: exampleABCDEx3 = "aBcDe\naBcDe\nABCDE"
                val found: List<MatchResult> = re1.findAll(exampleABCDEx3).toList()
                "found twice in correct places" o {
                    found.size chkEq 2
                    found[0].value chkEq found[1].value chkEq "aBcD"
                    found[0].range chkEq 0..3
                    found[1].range chkEq 6..9
                }
            }
        }
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

