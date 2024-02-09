package pl.mareklangiewicz.text

import pl.mareklangiewicz.bad.req


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
