package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*


@DelicateApi("Very basic email Ure. It will not match many strange but correct emails. Can also match some incorrect.")
val ureBasicEmail = ure {
    + atWordBoundary // so the first user char is not dot nor dash
    + ure("user") { 1..MAX of chWordOrDotOrDash }
    + atWordBoundary // so the last user char is not dot nor dash
    + ch('@')
    + atWordBoundary // so the first domain char is not dot nor dash
    + ure("domain") {
        1..MAX of {
            1..MAX of chWordOrDash
            + chDot // so the domain has at least one dot
        }
        2..16 of chWordOrDash
    }
    + atWordBoundary // so the last domain char is not dash
}

fun ureIdent(chStart: Ure = chWordStart, withWordBoundaries: Boolean = true, allowDashesInside: Boolean = false) = ure {
    + chStart
    0..MAX of chWord
    if (allowDashesInside) 0..MAX of ure {
        + chDash
        1..MAX of chWord
    }
}.withWordBoundaries(withWordBoundaries, withWordBoundaries)

fun ureChain(
    element: Ure,
    separator: Ure = chWhiteSpaceInLine,
    times: IntRange = 1..MAX,
    reluctant: Boolean = false,
    possessive: Boolean = false,
): Ure = when (times.first) {
    0 ->
        @OptIn(DelicateApi::class, NotPortableApi::class)
        if (times.last <= 0) ureRaw("") // It should always match 0 chars. TODO unit tests with unions/quantifiers.
        else ure { x(0..1, reluctant, possessive) of ureChain(element, separator, 1..times.last, reluctant, possessive) }
    else -> ure {
        + element
        val last = if (times.last == MAX) MAX else times.last - 1
        x(0..last, reluctant, possessive) of ure {
            + separator
            + element
        }
    }
}

/** Warning: Be careful (catastrophic backtracking: https://www.regular-expressions.info/catastrophic.html) */
fun ureWhateva(reluctant: Boolean = true, inLine: Boolean = false) =
    ure { x(0..MAX, reluctant = reluctant) of if (inLine) chAnyInLine else chAnyAtAll }

/** Warning: Be careful (catastrophic backtracking: https://www.regular-expressions.info/catastrophic.html) */
fun ureWhatevaInLine(reluctant: Boolean = true) = ureWhateva(reluctant, inLine = true)

fun ureBlankStartOfLine() = ure {
    + atBOLine
    0..MAX of chWhiteSpaceInLine
}

fun ureBlankRestOfLine(withOptLineBreak: Boolean = true) = ure {
    0..MAX of chWhiteSpaceInLine
    + atEOLine
    if (withOptLineBreak) x(0..1, possessive = true) of ureLineBreak
}

fun ureLineWithContent(content: Ure, withOptLineBreak: Boolean = true) = ure {
    + ureBlankStartOfLine()
    + content
    + ureBlankRestOfLine(withOptLineBreak)
}

fun ureLineWithContentFragments(vararg contentFragment: Ure, withOptLineBreak: Boolean = true) = ure {
    + ureBlankStartOfLine()
    + ureWhateva(inLine = true)
    for (fragment in contentFragment) {
        + fragment
        + ureWhateva(inLine = true)
    }
    + ureBlankRestOfLine(withOptLineBreak)
}

fun ureAnyLine(withOptLineBreak: Boolean = true) = ureLineWithContent(ureWhateva(inLine = true), withOptLineBreak)

fun Ure.withOptSpacesAround(inLine: Boolean = false, allowBefore: Boolean = true, allowAfter: Boolean = true) =
    if (!allowBefore && !allowAfter) this else ure {
        val s = if (inLine) chWhiteSpaceInLine else chWhiteSpace
        if (allowBefore) 0..MAX of s
        + this@withOptSpacesAround // it should flatten if this is UreProduct (see UreProduct.toIR()) TODO_later: doublecheck
        if (allowAfter) 0..MAX of s
    }

fun Ure.withOptSpacesAroundInLine(allowBefore: Boolean = true, allowAfter: Boolean = true) =
    withOptSpacesAround(inLine = true, allowBefore, allowAfter)

fun Ure.withOptWhatevaAround(
    reluctant: Boolean = true,
    inLine: Boolean = false,
    allowBefore: Boolean = true,
    allowAfter: Boolean = true
) = if (!allowBefore && !allowAfter) this else ure {
    if (allowBefore) + ureWhateva(reluctant, inLine)
    + this@withOptWhatevaAround // it should flatten if this is UreProduct (see UreProduct.toIR()) TODO_later: doublecheck
    if (allowAfter) + ureWhateva(reluctant, inLine)
}

fun Ure.withOptWhatevaAroundInLine(reluctant: Boolean = true, allowBefore: Boolean = true, allowAfter: Boolean = true) =
    withOptWhatevaAround(reluctant, inLine = true, allowBefore, allowAfter)

fun Ure.commentedOut(inLine: Boolean = false, traditional: Boolean = true, kdoc: Boolean = false) = ure {
    req(inLine || traditional) { "Non traditional comments are only single line" }
    req(!kdoc || traditional) { "Non traditional comments can't be used as kdoc" }
    + when {
        kdoc -> ureText("/**")
        traditional -> ureText("/*")
        else -> ureText("//")
    }
    + this@commentedOut.withOptSpacesAround(inLine)
    if (traditional) + ureText("*/")
}

@OptIn(SecondaryApi::class) @DelicateApi @NotPortableApi
fun Ure.notCommentedOut(traditional: Boolean = true, maxSpacesBehind: Int = 100) = ure {
    + ureLookBehind(positive = false) {
        + if (traditional) ureText("/*") else ureText("//")
        0..maxSpacesBehind of if (traditional) chWhiteSpace else chWhiteSpaceInLine
        // Cannot use MAX - look-behind implementation complains (throws) (JVM)
    }
    + this@notCommentedOut
    if (traditional) + ureLookAhead(positive = false) {
        0..MAX of chWhiteSpace
        + ureText("*/")
    }
}

fun ureCommentLine(content: Ure = ureWhateva(inLine = true), traditional: Boolean = true, kdoc: Boolean = false) =
    ureLineWithContent(content.commentedOut(inLine = true, traditional, kdoc))

fun ureLineWithEndingComment(comment: Ure) =
    ureLineWithContent(ureWhateva(inLine = true) then comment.commentedOut(inLine = true, traditional = false))


// TODO_someday: support named regions without the exact name repeated at the end
//   (as an optional flag, but leave unchanged requirement by default!)
fun ureRegion(content: Ure, regionName: Ure? = null) = ure {
    + ureCommentLine(ureKeywordAndOptArg("region", regionName), traditional = false)
    + content
    + ureCommentLine(ureKeywordAndOptArg("endregion", regionName), traditional = false)
}

// by "special" we mean region with label wrapped in squared brackets
// the promise is: all special regions with some label should contain exactly the same content (synced)
fun ureWithSpecialRegion(regionLabel: String) = ure {
    + ureWhateva().withName("before")
    + ureRegion(ureWhateva(), ureText("[$regionLabel]")).withName("region")
    + ureWhateva(reluctant = false).withName("after")
}


fun ureKeywordAndOptArg(keyword: String, arg: Ure? = null, separator: Ure = chWhiteSpaceInLine.timesMin(1)) =
    ureKeywordAndOptArg(ureText(keyword).withWordBoundaries(), arg, separator)

fun ureKeywordAndOptArg(
    keyword: Ure,
    arg: Ure? = null,
    separator: Ure = chWhiteSpaceInLine.timesMin(1),
) = ure {
    + keyword
    arg?.let {
        + separator
        + it
    }
}

