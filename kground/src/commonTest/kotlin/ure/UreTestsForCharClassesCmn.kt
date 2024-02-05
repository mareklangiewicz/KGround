@file:OptIn(DelicateApi::class, NotPortableApi::class)

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.uspek.*

fun testSomeUreCharClasses() {
    testSomeBasicCharClassIRs()
    testSomeUreCharClassPairsEtc()
}

fun testSomeBasicCharClassIRs() {

    "On some already created UreCharExact vals" o {
        "chSlash str is just slash" o { chSlash.str chkEq "/" }
        "chSlash IR is just slash" o { chSlash.toIR() chkEq IR("/") }
        "chBackSlash str is just backslash" o { chBackSlash.str chkEq "\\" }
        "chBackSlash IR is quoted" o { chBackSlash.toIR() chkEq IR("\\\\") }
        "chTab str is just actual tab char code 9" o { chTab.str chkEq "\t" }
        "chTab IR is special t quoted" o { chTab.toIR() chkEq IR("\\t") }
        "chAlert str is just actual alert char code 7" o { chTab.str.single().code chkEq 9 }
        "chAlert IR is special a quoted" o { chAlert.toIR() chkEq IR("\\a") }
    }

    "On fun ch constructing UreCharExact" o {
        "fails with more than single unicode character".failsWith<BadArgErr> { ch("ab") }
        "works correctly with single backslash" o {
            val ure = ch("\\") // using the version with string arg to be analogous to next tests with surrogate pairs
            ure.str chkEq "\\" // it is remembering just one char - no quoting here yet
            ure.toIR() chkEq IR("\\\\") // it is quoting (if necessary) when generating IR
        }
        "On copyright character" o {
            val copyRight = "\u00a9"
            "two ways of encoding in kotlin src result in the same single utf16 char" o {
                copyRight.length chkEq 1
                copyRight[0].isSurrogate.chkFalse()
                "©" chkEq copyRight
            }
            "On ch with copyright" o {
                val ure = ch(copyRight)
                val re = ure.toIR()
                "created correctly" o {
                    ure.str chkEq copyRight // it is remembering just the copyright character - no quoting here
                    re chkEq IR("\\xa9") // it is using \xhh notation for IR (only two digits)
                }
                onUreClass(
                    name = "copy right",
                    ure = ure,
                    match = listOf(copyRight),
                    matchNot = listOf("a", "0", " ", "⛔", "✅", "❌"),
                )
            }
        }
        "On bitcoin symbol" o {
            val bitcoin = "\u20bf"
            "two ways of encoding in kotlin src result in the same single utf16 char" o {
                bitcoin.length chkEq 1
                bitcoin[0].isSurrogate.chkFalse()
                "₿" chkEq bitcoin
            }
            "On ch with bitcoin" o {
                val ure = ch(bitcoin)
                val re = ure.toIR()
                "created correctly" o {
                    ure.str chkEq bitcoin // it is remembering just the bitcoin character - no quoting here yet
                    re chkEq IR("\\u20bf") // it is using \uhhhh notation for IR
                }
                onUreClass(
                    name = "bitcoin",
                    ure = ure,
                    match = listOf(bitcoin),
                    matchNot = listOf("a", "0", " ", "⛔", "✅", "❌"),
                )
            }
        }
        "On The Devil character surrogate" o {
            val devil = "\uD83D\uDE08"
            "two ways of encoding in kotlin src result in the same surrogate pair" o {
                devil.length chkEq 2
                devil[0].isSurrogateHigh.chkTrue()
                devil[1].isSurrogateLow.chkTrue()
                "😈" chkEq devil
            }
            "On ch with The Devil" o {
                val ure = ch(devil)
                val re = ure.toIR()
                "created correctly" o {
                    ure.str chkEq devil // it is remembering just the stop sign character - no quoting here yet
                    re chkEq IR("\\x{1f608}") // it is using \x{hhhhh} notation for IR
                }
                onUreClass(
                    name = "devil",
                    ure = ure,
                    match = listOf(devil),
                    matchNot = listOf("a", "0", " ", "⛔", "✅", "❌", "🛑"),
                    onPlatforms = listOf("JVM"),
                )
            }
        }
    }
}

fun testSomeUreCharClassPairsEtc() {
    onUreClassPair("chLower", "chPLower", chLower, chPLower,
        match = listOf("a", "b", "x"), // on JS (only!) also matches letters like: "λ", "ξ", etc.
        matchNot = listOf("A", "B", "Z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
        // verbose = true,
    )
    onUreClassPair("chUpper", "chPUpper", chUpper, chPUpper,
        match = listOf("A", "B", "X"), // on JS (only!) also matches letters like: "Λ", "Ξ", "Ż", etc.
        matchNot = listOf("a", "b", "z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
    )
    onUreClassPair("chAlpha", "chPAlpha", chAlpha, chPAlpha,
        match = listOf("A", "B", "X", "c", "d"), // on JS (only!) also matches letters like: "ą", "ć", "Λ", "Ξ", "Ż", etc.
        matchNot = listOf("@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
    )
    onUreClassPair("chDigit", "chPDigit", chDigit, chPDigit,
        match = listOf("1", "2", "3", "8", "9"),
        matchNot = listOf("A", "b", "c", "@", "#", ":", "-", ")", "¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClassPair("chHexDigit", "chPHexDigit", chHexDigit, chPHexDigit,
        match = listOf("1", "2", "3", "8", "9", "a", "A", "c", "E", "F"),
        matchNot = listOf("@", "#", ":", "-", ")", "¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClassPair("chAlnum", "chPAlnum", chAlnum, chPAlnum,
        match = listOf("A", "B", "X", "c", "d", "1", "2", "8", "9", "0"),
        matchNot = listOf("@", "#", ":", "-", ")", "¥", "₿", "₤", "😈", "ε", "β", "δ", "Λ", "Ξ", "ξ"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClassPair("chPunct", "chPPunct", chPunct, chPPunct,
        match = listOf(".", ",", ":", "@", "#"), // on LINUX, it also matches numbers like "2", "3", etc. Why??
        matchNot = listOf("A", "a", "x", "¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClassPair("chGraph", "chPGraph", chGraph, chPGraph,
        match = listOf(".", ",", ":", "@", "#", "2", "3", "a", "B"),
        matchNot = listOf("¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    // TODO_later: chPPrint is broken on LINUX. Doesn't match anything I tried. Report it, but for now it's deprecated.
    // onUreClassPair("chPrint", "chPPrint", chPrint, chPPrint,
    //     match = listOf(".", ",", ":", "@", "#", "2", "3", "a", "B", " "),
    //     matchNot = listOf("¥", "₿", "₤", "😈", "\t"),
    //     onPlatforms = listOf("JVM", "LINUX"),
    // )
    // So let's just test portable version for now.
    onUreClass("chPrint", chPrint,
        match = listOf(".", ",", ":", "@", "#", "2", "3", "a", "B", " "),
        matchNot = listOf("¥", "₿", "₤", "😈", "\t"),
    )
    onUreClassPair("chWhiteSpaceInLine", "chPBlank", chWhiteSpaceInLine, chPBlank,
        match = listOf(" ", "\t"),
        matchNot = listOf("\n", "\r", "\u000B", "A", "a", "x", "¥", "₿", "₤", "2", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClassPair("chSpace", "chPSpace", chWhiteSpace, chPWhiteSpace,
        match = listOf(" ", "\t", "\n", "\r", "\u000B"),
        matchNot = listOf("A", "a", "x", "¥", "₿", "₤", "2", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClass(name = "chPCurrency", ure = chPCurrency,
        match = listOf("¥", "₿", "₤"),
        matchNot = listOf("@", "#", ":", "-", ")", "a", "Y", "😀", "1", "2", "😈"),
    )
    onUreClass(name = "chPLatin", ure = chPLatin,
        match = listOf("a", "B", "Ż", "ó", "ł", "Ź"),
        matchNot = listOf("@", "#", ":", "-", ")", "ε", "β", "δ", "Λ", "Ξ", "ξ", "😈"),
        onPlatforms = listOf("JVM", "JS"),
    )
    onUreClass(name = "chPGreek", ure = chPGreek,
        match = listOf("ε", "β", "δ", "Λ", "Ξ", "ξ"),
        matchNot = listOf("@", "#", ":", "-", ")", "a", "B", "Ż", "ó", "ł", "Ź", "😈"),
        onPlatforms = listOf("JVM", "JS"),
    )
    onUreClass(name = "chPExtPict", ure = chPExtPict,
        match = listOf("😀", "🫠", "🥶", "😈"),
        matchNot = listOf("@", "#", ":", "-", ")", "a", "b", "X", "Y", "1", "2"),
        onPlatforms = listOf("JS"),
    )
}

// Warning: Sibling calls have to have different names, so USpek tree can differentiate branches.
private fun onUreClass(
    name: String,
    ure: Ure,
    match: List<String>,
    matchNot: List<String>,
    onPlatforms: List<String> = listOf("JVM", "JS", "LINUX"),
    verbose: Boolean = false,
) {
    "On ure class $name on $platform" o {
        if (platform in onPlatforms) {
            testUreCompiles(ure)
            testUreMatchesCorrectChars(ure, match, matchNot, verbose)
        }
        else testUreDoesNotCompile(ure)
    }
}

// Warning: Sibling calls have to have different names, so USpek tree can differentiate branches.
private fun onUreClassPair(
    namePortable: String,
    namePlatform: String,
    urePortable: Ure,
    urePlatform: Ure,
    match: List<String>,
    matchNot: List<String>,
    onPlatforms: List<String> = listOf("JVM", "JS", "LINUX"),
    verbose: Boolean = false,
) {
    "On ure class pair $namePortable and $namePlatform" o {
        onUreClass(namePortable, urePortable, match, matchNot, verbose = verbose)
        onUreClass(namePlatform, urePlatform, match, matchNot, onPlatforms, verbose)
    }
}

