package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.chk
import pl.mareklangiewicz.uspek.*

@OptIn(NotPortableApi::class, DelicateApi::class)
fun testSomeUreCharClasses() {
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
            itCompiles(ure)
            itMatchesCorrectChars(ure, match, matchNot, verbose)
        }
        else itDoesNotCompile(ure)
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

private fun itMatchesCorrectChars(
    ure: Ure,
    match: List<String>,
    matchNot: List<String>,
    verbose: Boolean = false,
) = "it matches correct chars" o {
    testUreMatchesAll(ure, *match.toTypedArray(), verbose = verbose)
    testUreMatchesNone(ure, *matchNot.toTypedArray(), verbose = verbose)
    testUreMatchesAll(!ure, *matchNot.toTypedArray(), verbose = verbose)
    testUreMatchesNone(!ure, *match.toTypedArray(), verbose = verbose)
}

fun testUreMatchesAll(ure: Ure, vararg examples: String, verbose: Boolean = false) {
    val re = ure.compile()
    for (e in examples)
        if (verbose) "matches $e" o { chk(re.matches(e)) { "$re does not match $e" } }
        else chk(re.matches(e)) { "$re doesn't match $e" }
}

fun testUreMatchesNone(ure: Ure, vararg examples: String, verbose: Boolean = false) {
    val re = ure.compile()
    for (e in examples)
        if (verbose) "does not match $e" o { chk(!re.matches(e)) { "$re matches $e" } }
        else chk(!re.matches(e)) { "$re matches $e" }
}

