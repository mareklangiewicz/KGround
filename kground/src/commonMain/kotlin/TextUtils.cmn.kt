package pl.mareklangiewicz.kground


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


/** @throws BadArgErr if not found or found more than one */
fun Regex.findSingle(input: CharSequence, startIndex: Int = 0): MatchResult {
    val r1 = find(input, startIndex).reqNN { "this regex: \"$this\" is nowhere in input" }
    val r2 = find(input, r1.range.last + 1)
    r2.reqNull { "this regex: \"$this\" has been found second time at idx: ${r2!!.range.first}" }
    return r1
}

/** @throws BadArgErr if not found or found more than one */
fun Regex.replaceSingle(input: CharSequence, replacement: CharSequence, startIndex: Int = 0): CharSequence =
    input.replaceRange(findSingle(input, startIndex).range, replacement)

fun Regex.findAllWithOverlap(input: CharSequence, startIndex: Int = 0): Sequence<MatchResult> {
    req(startIndex in 0..input.length) { "startIdx: $startIndex is not in bounds: 0..${input.length}" }
    return generateSequence({ find(input, startIndex) },  { find(input, it.range.first + 1) })
}


fun CharSequence.replace(re: Regex, transform: (MatchResult) -> CharSequence) = re.replace(this, transform)
fun CharSequence.replace(re: Regex, replacement: String): String = re.replace(this, replacement)
fun CharSequence.replaceFirst(re: Regex, replacement: String): String = re.replaceFirst(this, replacement)
fun CharSequence.findAll(re: Regex, startIndex: Int = 0) = re.findAll(this, startIndex)
fun CharSequence.findAllWithOverlap(re: Regex, startIndex: Int = 0) = re.findAllWithOverlap(this, startIndex)
fun CharSequence.find(re: Regex, startIndex: Int = 0) = re.find(this, startIndex)
fun CharSequence.matchEntire(re: Regex) = re.matchEntire(this)
