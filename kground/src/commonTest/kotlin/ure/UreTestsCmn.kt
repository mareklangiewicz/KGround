package pl.mareklangiewicz.ure

import pl.mareklangiewicz.kground.chk
import pl.mareklangiewicz.kground.getCurrentPlatformKind
import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UreTestsCmn {

    init { "INIT ${this::class.simpleName}".teePP }

    @Test fun t() {
        uspek { testUreCmn() }
        GlobalUSpekContext.branch.assertAllGood()
    }
}

/**
 * Temporary workaround to make sure I notice failed tests in IntelliJ.
 * Without it, I get a green checkmark in IntelliJ on JS and LINUX even if some tests failed, and I have to check logs.
 * In the future I'll have custom mpp runner+logger, so this workaround will be removed.
 */
private fun USpekTree.assertAllGood() {
    if (failed) throw end!!.cause!!
    branches.values.forEach { it.assertAllGood() }
}

private val platform = getCurrentPlatformKind()


// TODO_maybe: Add sth like this to USpek? Or to USpekX?
private inline fun <reified T : Throwable> String.failsWith(crossinline code: () -> Unit) = o {
    assertFailsWith<T>(block = code)
}


fun testUreCmn() {
    testSomeUreCharClasses()
    testUreBasicEmail()
}

@OptIn(UreNonMP::class)
fun testSomeUreCharClasses() {
    onUreClass(name = "chpLower", ure = chpLower,
        match = listOf("a", "b", "x"), // on JS (only!) also matches letters like: "λ", "ξ", etc.
        matchNot = listOf("A", "B", "Z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
        // verbose = true,
    )
    onUreClass(name = "chpUpper", ure = chpUpper,
        match = listOf("A", "B", "X"), // on JS (only!) also matches letters like: "Λ", "Ξ", "Ż", etc.
        matchNot = listOf("a", "b", "z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
    )
    onUreClass(name = "chpAlpha", ure = chpAlpha,
        match = listOf("A", "B", "X", "c", "d"), // on JS (only!) also matches letters like: "ą", "ć", "Λ", "Ξ", "Ż", etc.
        matchNot = listOf("@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
    )
    onUreClass(name = "chpDigit", ure = chpDigit,
        match = listOf("1", "2", "3", "8", "9"),
        matchNot = listOf("A", "b", "c", "@", "#", ":", "-", ")", "¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClass(name = "chpAlnum", ure = chpAlnum,
        match = listOf("A", "B", "X", "c", "d", "1", "2", "8", "9", "0"),
        matchNot = listOf("@", "#", ":", "-", ")", "¥", "₿", "₤", "😈", "ε", "β", "δ", "Λ", "Ξ", "ξ"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClass(name = "chpPunct", ure = chpPunct,
        match = listOf(".", ",", ":", "@", "#"), // on LINUX, it also matches numbers like "2", "3", etc. Why??
        matchNot = listOf("A", "a", "x", "¥", "₿", "₤", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClass(name = "chpBlank", ure = chpBlank,
        match = listOf(" ", "\t"),
        matchNot = listOf("\n", "\r", "\u000B", "A", "a", "x", "¥", "₿", "₤", "2", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )
    onUreClass(name = "chpSpace", ure = chpSpace,
        match = listOf(" ", "\t", "\n", "\r", "\u000B"),
        matchNot = listOf("A", "a", "x", "¥", "₿", "₤", "2", "😈"),
        onPlatforms = listOf("JVM", "LINUX"),
    )


    onUreClass(name = "chpCurrency", ure = chpCurrency,
        match = listOf("¥", "₿", "₤"),
        matchNot = listOf("@", "#", ":", "-", ")", "a", "Y", "😀", "1", "2", "😈"),
    )
    onUreClass(name = "chpLatin", ure = chpLatin,
        match = listOf("a", "B", "Ż", "ó", "ł", "Ź"),
        matchNot = listOf("@", "#", ":", "-", ")", "ε", "β", "δ", "Λ", "Ξ", "ξ", "😈"),
        onPlatforms = listOf("JVM", "JS"),
    )
    onUreClass(name = "chpGreek", ure = chpGreek,
        match = listOf("ε", "β", "δ", "Λ", "Ξ", "ξ"),
        matchNot = listOf("@", "#", ":", "-", ")", "a", "B", "Ż", "ó", "ł", "Ź", "😈"),
        // onPlatforms = listOf("JS"),
    )
    onUreClass(name = "chpExtPict", ure = chpExtPict,
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

private fun itCompiles(ure: Ure, alsoCheckNegation: Boolean = true) = "it compiles" o {
    ure.compile() // will throw if the platform doesn't support it
    if (alsoCheckNegation) ure.not().compile() // will throw if the platform doesn't support it
}

/**
 * Note: It throws different [Throwable] on different platforms.
 * I encountered [SyntaxError] on JS and [InvalidArgumentException] on JVM and LINUX and [PatternSyntaxException] on LINUX.
 */
private fun itDoesNotCompile(ure: Ure) = "it does not compile".failsWith<Throwable> { ure.compile() }

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

fun testUreBasicEmail() {
    "On ureBasicEmail" o {
        "assert IR as expected" o {
            // This assertion is kinda lame (expecting exact impl/ir / cementing impl),
            // but it's useful for me now as documentation and to track if sth changes.
            ureBasicEmail.toIR().str eq """^(?<user>[\w.]+)@(?<domain>(?:[\w\-]+\.)+[\w\-]{2,4})$"""
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

