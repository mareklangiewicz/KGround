package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*

const val exampleABCDEx3 = "aBcDe\naBcDe\nABCDE"

val ureBOLaBcD = atBOLine then ureText("aBcD")

val ureBcDeEOL = ureText("BcDe") then atEOLine


fun testSomeUreBasicStuff() {
    testSomeUreBasicSanity()
    testSomeUreWithName()
    testUreWithDifferentOptions()
    testUreQuantifAndAtomic()
    // TODO("Test some quantifiers (is UreQuantif.toClosedIR() correct?)")
    // TODO NOW

    testUreBasicEmail()
}

@OptIn(DelicateApi::class)
fun testSomeUreBasicSanity() {
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

// Note: I will access matched groups in these tests, both by name and by number. This kind of mixed approach is not
//   recommended because indexing can be inconsistent between regex implementations when named groups are present.
//   I do it just to test in practice how currently platforms under kotlin mpp are indexing groups in such edge cases.
//   See "Numbers for Named Capturing Groups" here: https://www.regular-expressions.info/named.html
@OptIn(DelicateApi::class, NotPortableApi::class)
fun testSomeUreWithName() {

    "Sanity check for ure without named groups" o {
        val found = ureBOLaBcD.compile().findAll(exampleABCDEx3).toList()

        "found twice in correct places" o {
            found.size chkEq 2
            found[0].value chkEq found[1].value chkEq "aBcD"
            found[0].range chkEq 0..3
            found[1].range chkEq 6..9
        }
        "result .groups and .named returns the same object on $platform" o {
            found[0].groups chkSame found[0].named
            found[1].groups chkSame found[1].named
        }
    }

    "On wrapping ure s in withName" o {
        val ure1 = ureBOLaBcD.withName("nm1")
        val ure2 = ureBcDeEOL.withName("nm2")
        val ure3 = ure("nm3") { // convenient way to wrap withName when building concatenation
            + ure1
            0..MAX of chAnyAtAll
            + ure2
        }
        val ure4 = ure1 or ure2 // no additional name added for whole ure4
        "constructed as expected" o {
            ure1.toIR() chkEq IR("(?<nm1>^aBcD)")
            ure2.toIR() chkEq IR("(?<nm2>BcDe$)")
            ure3.toIR() chkEq IR("(?<nm3>(?<nm1>^aBcD)[\\s\\S]*(?<nm2>BcDe$))")
            ure4.toIR() chkEq IR("(?<nm1>^aBcD)|(?<nm2>BcDe$)")
        }
        "On compile" o {
            val re1 = ure1.compile()
            val re2 = ure2.compile()
            val re3 = ure3.compile()
            val re4 = ure4.compile()
            "On matching re1" o {
                val found: List<MatchResult> = re1.findAll(exampleABCDEx3).toList()
                "found twice in correct places" o {
                    found.size chkEq 2
                    found[0].value chkEq found[1].value chkEq "aBcD"
                    found[0].range chkEq 0..3
                    found[1].range chkEq 6..9
                }
                "captured groups by first result as expected" o {
                    val gs = found[0].named chkSame found[0].groups
                    gs.size chkEq 2 // note: gs[0] is always an entire match
                    "numbered and named groups are eq" o { gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"] }
                    // can't access range, because MatchGroup.range is not available on JS (so not in common)
                    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-match-group/range.html
                }
                "captured groups by second result as expected" o {
                    val gs = found[1].named chkSame found[1].groups
                    gs.size chkEq 2 // note: gs[0] is always an entire match
                    "numbered and named groups are eq" o { gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"] }
                }
            }
            "On matching re2" o {
                val found: List<MatchResult> = re2.findAll(exampleABCDEx3).toList()
                "found twice in correct places" o {
                    found.size chkEq 2
                    found[0].value chkEq found[1].value chkEq "BcDe"
                    found[0].range chkEq 1..4
                    found[1].range chkEq 7..10
                }
                "captured groups by first result as expected" o {
                    val gs = found[0].named chkSame found[0].groups
                    gs.size chkEq 2 // note: gs[0] is always an entire match
                    "numbered and named groups are eq" o { gs[0].chkNN() chkEq gs[1] chkEq gs["nm2"] }
                }
                "captured groups by second result as expected" o {
                    val gs = found[1].named chkSame found[1].groups
                    gs.size chkEq 2 // note: gs[0] is always an entire match
                    "numbered and named groups are eq" o { gs[0].chkNN() chkEq gs[1] chkEq gs["nm2"] }
                }
            }
            "On matching re3" o {
                val found: List<MatchResult> = re3.findAll(exampleABCDEx3).toList()
                "found once in correct place" o {
                    val result = found.single()
                    result.value chkEq "aBcDe\naBcDe"
                    result.range chkEq 0..10
                    "captured groups as expected" o {
                        val gs = result.named chkEq result.groups
                        gs.size chkEq 4 // note: gs[0] is always an entire match
                        "numbered and named groups are correct" o {
                            gs[0].chkNN() chkEq gs[1] chkEq gs["nm3"]
                            gs[2].chkNN() chkEq gs["nm1"]
                            gs[3].chkNN() chkEq gs["nm2"]
                        }
                        "access named groups values with property delegations" o {
                            val nm1 by result
                            val nm2 by result
                            val nm3 by result
                            nm1 chkEq gs["nm1"]!!.value
                            nm2 chkEq gs["nm2"]!!.value
                            nm3 chkEq gs["nm3"]!!.value
                        }
                    }
                }
            }
            "On matching re4" o {
                val found: List<MatchResult> = re4.findAll(exampleABCDEx3).toList()
                "found twice in correct places" o {
                    found.size chkEq 2
                    found[0].value chkEq found[1].value chkEq "aBcD"
                    found[0].range chkEq 0..3
                    found[1].range chkEq 6..9
                }
                "captured groups by first result as expected" o {
                    val gs = found[0].named chkSame found[0].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"]
                        gs[0]!!.value chkEq "aBcD"
                        gs[2].chkNull() // so the group for second alternative is in groups collection, but it is null
                        gs["nm2"].chkNull() // same when accessing via name (available, but null)
                    }
                }
                "captured groups by second result as expected" o {
                    val gs = found[1].named chkSame found[1].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"]
                        gs[0]!!.value chkEq "aBcD" // this is the second aBcD in the whole exampleABCDEx3
                        gs[2].chkNull() // so the group for second alternative is in groups collection, but it is null
                        gs["nm2"].chkNull() // same when accessing via name (available, but null)
                    }
                }
            }
            "On matching re4 with overlap" o {
                val found: List<MatchResult> = re4.findAllWithOverlap(exampleABCDEx3).toList()
                "found 4 times in correct places" o {
                    found.size chkEq 4
                    found[0].value chkEq found[2].value chkEq "aBcD"
                    found[1].value chkEq found[3].value chkEq "BcDe"
                    found[0].range chkEq 0..3
                    found[1].range chkEq 1..4
                    found[2].range chkEq 6..9
                    found[3].range chkEq 7..10
                }
                "captured groups by first result as expected" o {
                    val gs = found[0].named chkSame found[0].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"]
                        gs[0]!!.value chkEq "aBcD"
                        gs[2].chkNull() // so the group for second alternative is in groups collection, but it is null
                        gs["nm2"].chkNull() // same when accessing via name (available, but null)
                    }
                }
                "captured groups by second result as expected" o {
                    val gs = found[1].named chkSame found[1].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[2] chkEq gs["nm2"]
                        gs[0]!!.value chkEq "BcDe"
                        gs[1].chkNull() // so the group for first alternative is in groups collection, but it is null
                        gs["nm1"].chkNull() // same when accessing via name (available, but null)
                    }
                }
                "captured groups by third result as expected" o { // just like first
                    val gs = found[2].named chkSame found[2].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[1] chkEq gs["nm1"]
                        gs[0]!!.value chkEq "aBcD" // this is the second aBcD in the whole exampleABCDEx3
                        gs[2].chkNull() // so the group for second alternative is in groups collection, but it is null
                        gs["nm2"].chkNull() // same when accessing via name (available, but null)
                    }
                }
                "captured groups by fourth result as expected" o { // just like second
                    val gs = found[3].named chkSame found[3].groups
                    gs.size chkEq 3 // note: gs[0] is always an entire match
                    "numbered and named groups are as expected" o {
                        gs[0].chkNN() chkEq gs[2] chkEq gs["nm2"]
                        gs[0]!!.value chkEq "BcDe"
                        gs[1].chkNull() // so the group for first alternative is in groups collection, but it is null
                        gs["nm1"].chkNull() // same when accessing via name (available, but null)
                    }
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

