package pl.mareklangiewicz.kground

import pl.mareklangiewicz.bad.*
import kotlin.text.Regex.Companion.escapeReplacement


// region [Char Related Stuff]

// Note: waiting for better codepoints support in kotlin common stdlib
// https://youtrack.jetbrains.com/issue/KT-23251/Extend-Unicode-support-in-Kotlin-common

/** also known as leading-surrogate */
val Char.isSurrogateHigh: Boolean get() =
    this >= Char.MIN_HIGH_SURROGATE && this < Char.MAX_HIGH_SURROGATE + 1

/** also known as trailing-surrogate */
val Char.isSurrogateLow: Boolean get() =
    this >= Char.MIN_LOW_SURROGATE && this < Char.MAX_LOW_SURROGATE + 1

val Char.isSurrogate: Boolean get() =
    this >= Char.MIN_SURROGATE && this < Char.MAX_SURROGATE + 1

val Char.isAscii: Boolean get() = code in 0..127
val Char.isAsciiExtended: Boolean get() = code in 128..255
val Char.isAsciiControl: Boolean get() = code in 0..31 || code == 127
val Char.isAsciiPrintable: Boolean get() = code in 32..126


fun Char.switchCase() = if (isLowerCase()) uppercaseChar() else lowercaseChar()

// endregion [Char Related Stuff]

val String.isSingleSurrogatePair get() = length == 2 && get(0).isSurrogateHigh && get(1).isSurrogateLow

val String.isSingleUnicodeCharacter get() = length == 1 && !get(0).isSurrogate || isSingleSurrogatePair

fun CharSequence.toSingleCodePoint(): Int {
    val ch = first() // will throw if it's empty
    req(length < 3) { "Contains more than single code point." }
    if (length == 1) {
        req(!ch.isSurrogateHigh) { "Contains only first (high) part of surrogate pair." }
        req(!ch.isSurrogateLow) { "Contains only second (low) part of surrogate pair." }
        return ch.code
    }
    // length == 2
    val cl = this[1]
    req(ch.isSurrogateHigh) { "Incorrect first part of surrogate pair." }
    req(cl.isSurrogateLow) { "Incorrect second part of surrogate pair." }
    return ((ch - Char.MIN_HIGH_SURROGATE) shl 10) + (cl - Char.MIN_LOW_SURROGATE) + 0x10000
}

fun String.removeReqPrefix(prefix: CharSequence): String {
    req(startsWith(prefix)) { "Can not find prefix: $prefix" }
    return removePrefix(prefix)
}

fun String.removeReqSuffix(suffix: CharSequence): String {
    req(endsWith(suffix)) { "Can not find suffix: $suffix" }
    return removeSuffix(suffix)
}

/** More explicit name for stdlib [Regex.matchEntire] */
fun Regex.matchEntireOrNull(input: CharSequence): MatchResult? = matchEntire(input)

fun Regex.matchEntireOrThrow(input: CharSequence): MatchResult? =
    matchEntireOrNull(input).reqNN { "this regex: \"$this\" does not match entire input" }

/** More explicit name for stdlib [Regex.matchAt] */
fun Regex.matchAtOrNull(input: CharSequence, index: Int): MatchResult? = matchAt(input, index)

fun Regex.matchAtOrThrow(input: CharSequence, index: Int): MatchResult =
    matchAtOrNull(input, index).reqNN { "this regex: \"$this\" does not match input at index: $index" }

/** More explicit name for stdlib [Regex.find] */
fun Regex.findFirstOrNull(input: CharSequence, startIndex: Int = 0): MatchResult? = find(input, startIndex)

/** @throws BadArgErr if not found */
fun Regex.findFirst(input: CharSequence, startIndex: Int = 0): MatchResult =
    findFirstOrNull(input, startIndex).reqNN { "this regex: \"$this\" is nowhere in input" }

/**
 * @throws BadArgErr if not found or found more than one
 * Does NOT check for overlapping matches.
 */
fun Regex.findSingle(input: CharSequence, startIndex: Int = 0): MatchResult =
    findFirst(input, startIndex).also {
        val second = findFirstOrNull(input, it.range.last + 1)
        second.reqNull { "this regex: \"$this\" has been found second time at idx: ${second!!.range.first}" }
    }

/** @throws BadArgErr if not found or found more than one. Even if the second one overlaps with the first one. */
fun Regex.findSingleWithOverlap(input: CharSequence, startIndex: Int = 0): MatchResult =
    findFirst(input, startIndex).also {
        val second = findFirstOrNull(input, it.range.first + 1)
        second.reqNull { "this regex: \"$this\" has been found second time at idx: ${second!!.range.first}" }
    }

/**
 * Note: Even with this overlapping version, the first match starting at particular position wins,
 * (no additional results starting from the same place). It's like the whole thing is always "atomic".
 * Actual atomic/possessive/reluctant constructs matter INSIDE every single matching, not across MatchResults.
 */
fun Regex.findAllWithOverlap(input: CharSequence, startIndex: Int = 0): Sequence<MatchResult> {
    req(startIndex in 0..input.length) { "startIdx: $startIndex is not in bounds: 0..${input.length}" }
    return generateSequence({ find(input, startIndex) },  { find(input, it.range.first + 1) })
}



private fun String.escapeGroupRefsIf(escapeGroupRefs: Boolean): String =
    if (escapeGroupRefs) escapeReplacement(this) else this

@Suppress("RegExpRedundantEscape") // not redundant on JS
private val reGroupRefInReplacement = Regex("\\$(\\d+|\\{\\w+\\})")
private fun String.reqNoGroupRefsIf(reqNoGroupRefs: Boolean): String {
    reqNoGroupRefs || return this
    var idx = 0
    // it's faster and simpler to go through simple cases manually,
    // and use regex only when it's possible to be group ref at some idx
    while (idx < length) {
        val ch = get(idx)
        if (ch == '\\') { idx += 2; continue }
        if (ch != '$') { idx ++; continue }
        if (idx == length - 1) break // $ as last char is fine
        matchAtOrNull(reGroupRefInReplacement, idx).reqNull { "Looks like there is a group reference at idx: $idx" }
        idx ++
    }
    return this
}


/**
 * Improved and more explicit version of stdlib [Regex.replaceFirst] with optional escaping group references.
 * By default, it doesn't even allow group references in replacement to avoid hard to debug issues.
 * @throws [BadArgErr] when [allowGroupRefs] is false and some group reference is found in the replacement.
 * Note: [escapeGroupRefs] to true makes [allowGroupRefs] irrelevant because it checks already escaped replacement.
 */
fun Regex.replaceFirstOrNone(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
): String = replaceFirst(input, replacement.escapeGroupRefsIf(escapeGroupRefs).reqNoGroupRefsIf(!allowGroupRefs))

/**
 * Improved and more explicit version of stdlib [Regex.replace] with optional escaping group references.
 * By default, it doesn't even allow group references in replacement to avoid hard to debug issues.
 * @throws [BadArgErr] when [allowGroupRefs] is false and some group reference is found in the replacement.
 * Note: [escapeGroupRefs] to true makes [allowGroupRefs] irrelevant because it checks already escaped replacement.
 * Note: No overlapping here - searching with overlap wouldn't have much sense during replacing.
 */
fun Regex.replaceAll(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
): String = replace(input, replacement.escapeGroupRefsIf(escapeGroupRefs).reqNoGroupRefsIf(!allowGroupRefs))

/**
 * More explicit name for stdlib [Regex.replace] with custom [transform] fun.
 * No [Regex.escapeReplacement] called here. User has to do it manually inside [transform] if needed.
 * No overlapping here - searching with overlap wouldn't have much sense during replacing.
 */
fun Regex.replaceAll(input: CharSequence, transform: (MatchResult) -> CharSequence): String =
    replace(input, transform)

/** @throws BadArgErr if not found or found more than one */
fun Regex.replaceSingle(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
): String = findSingle(input).range.let { // already throws when more than one or none
    when {
        // Fast literal replacement (ignoring group refs), no need for actual escaping in this case.
        escapeGroupRefs -> input.replaceRange(it, replacement).toString()
        // Also fast literal replacement, but after making sure no group refs there.
        !allowGroupRefs -> input.replaceRange(it, replacement.reqNoGroupRefsIf(true)).toString()
        // Slower replacement with group references support.
        else -> replaceFirstOrNone(input, replacement, allowGroupRefs = true)
    }
}


fun CharSequence.matchEntireOrNull(re: Regex) = re.matchEntireOrNull(this)

fun CharSequence.matchEntireOrThrow(re: Regex) = re.matchEntireOrThrow(this)

fun CharSequence.matchAtOrNull(re: Regex, index: Int) = re.matchAtOrNull(this, index)

fun CharSequence.matchAtOrThrow(re: Regex, index: Int) = re.matchAtOrThrow(this, index)

fun CharSequence.findFirstOrNull(re: Regex, startIndex: Int = 0) = re.findFirstOrNull(this, startIndex)

fun CharSequence.findFirst(re: Regex, startIndex: Int = 0) = re.findFirst(this, startIndex)

fun CharSequence.findSingle(re: Regex, startIndex: Int = 0) = re.findSingle(this, startIndex)

fun CharSequence.findSingleWithOverlap(re: Regex, startIndex: Int = 0) = re.findSingleWithOverlap(this, startIndex)

fun CharSequence.findAll(re: Regex, startIndex: Int = 0) = re.findAll(this, startIndex)

fun CharSequence.findAllWithOverlap(re: Regex, startIndex: Int = 0) = re.findAllWithOverlap(this, startIndex)

fun CharSequence.replaceFirstOrNone(
    re: Regex,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = re.replaceFirstOrNone(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun CharSequence.replaceAll(
    re: Regex,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = re.replaceAll(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun CharSequence.replaceAll(re: Regex, transform: (MatchResult) -> CharSequence): String =
    re.replaceAll(this, transform)

fun CharSequence.replaceSingle(
    re: Regex,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = re.replaceSingle(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)
