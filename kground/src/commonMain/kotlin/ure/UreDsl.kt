@file:Suppress("SpellCheckingInspection")
@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ure.core.*

// region [Ure Basic Stuff]

const val MAX = Int.MAX_VALUE

fun ure(name: String? = null, init: UreConcatenation.() -> Unit) =
  UreConcatenation().apply(init).withName(name) // when name is null, the withName doesn't wrap ure at all.

@DelicateApi("Usually code using Ure assumes default options, so changing options can create hard to find issues.")
@NotPortableApi("Some options work only on some platforms. Check docs for each used platform.")
@SecondaryApi("Use Ure.withOptions", ReplaceWith("content.withOptions(enable, disable)"))
fun ureWithOptions(content: Ure, enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet()) =
  UreChangeOptionsGroup(content, enable, disable)

@SecondaryApi("Use Ure.withName", ReplaceWith("content.withName(name"))
fun ureWithName(name: String, content: Ure) = UreNamedGroup(content, name)

fun Ure.withName(name: String?) = if (name == null) this else UreNamedGroup(this, name)

@DelicateApi("Usually code using Ure assumes default options, so changing options can create hard to find issues.")
@NotPortableApi("Some options work only on some platforms. Check docs for each used platform.")
fun Ure.withOptions(enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet()) =
  UreChangeOptionsGroup(this, enable, disable)

@DelicateApi("Usually code using Ure assumes default options, so changing options can create hard to find issues.")
@NotPortableApi("Some options work only on some platforms. Check docs for each used platform.")
fun Ure.withOptionsEnabled(vararg options: RegexOption) = withOptions(enable = options.toSet())

@DelicateApi("Usually code using Ure assumes default options, so changing options can create hard to find issues.")
@NotPortableApi("Some options work only on some platforms. Check docs for each used platform.")
fun Ure.withOptionsDisabled(vararg options: RegexOption) = withOptions(disable = options.toSet())

/**
 * Changes regex options ([RegexOption]) from this point ahead. Very problematic construct.
 * It is much safer to use [withOptions] instead of [ureWithOptionsAhead].
 * Or even safer not to change options at all, so all [Ure]s are interpreted the same way.
 */
@DelicateApi("Makes the whole Ure very difficult to analyze.", ReplaceWith("withOptions"))
@SecondaryApi("Use Ure.withOptions", ReplaceWith("withOptions"))
@NotPortableApi("Does NOT even compile (Ure.compile) on JS.", ReplaceWith("withOptions"))
fun ureWithOptionsAhead(enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet()) =
  UreChangeOptionsAhead(enable, disable)

fun Ure.withWordBoundaries(boundaryBefore: Boolean = true, boundaryAfter: Boolean = true) =
  withBoundaries(atWordBoundary.takeIf { boundaryBefore }, atWordBoundary.takeIf { boundaryAfter })

@SecondaryApi @NotPortableApi fun Ure.withBOEOWordBoundaries(
  boundaryBefore: Boolean = true,
  boundaryAfter: Boolean = true,
) = withBoundaries(atBOWord.takeIf { boundaryBefore }, atEOWord.takeIf { boundaryAfter })

fun Ure.withBoundaries(boundaryBefore: Ure? = null, boundaryAfter: Ure? = null) =
  if (boundaryBefore == null && boundaryAfter == null) this else ure {
    boundaryBefore?.let { +it }
    +this@withBoundaries // it will flatten if this is UreConcatenation (see UreConcatenation.toIR())
    boundaryAfter?.let { +it }
  }

infix fun Ure.or(that: Ure) = UreAlternation(this, that)
infix fun Ure.then(that: Ure) = UreConcatenation(MutLO(this, that))
// Do not rename "then" to "and". The "and" would suggest sth more like a special lookahead/lookbehind group

@OptIn(NotPortableApi::class, DelicateApi::class) // not portable only if the receiver was already not portable.
operator fun Ure.not(): Ure = when (this) {
  is UreWithRawIR -> when (this) {
    // TODO_someday: Can I negate some common raw ures?
    else -> bad { "This UreWithRawIR can not be negated" }
  }
  is UreCharExact -> !chOfAny(this)
  is UreAnchorPreDef -> !this
  is UreCharClassPreDef -> !this
  is UreCharClassRange -> !this
  is UreCharClassUnion -> !this
  is UreCharClassIntersect -> !this
  is UreCharClassProp -> !this
  is UreGroup -> when (this) {
    is UreLookGroup -> !this
    else -> bad { "Unsupported UreGroup for negation: ${this::class.simpleName}" }
  }

  is UreGroupRef -> bad { "UreGroupRef can not be negated" }
  is UreConcatenation -> bad { "UreConcatenation can not be negated" }
  is UreQuantifier -> bad { "UreQuantifier can not be negated" }
  is UreQuote -> bad { "UreQuote can not be negated" }
  is UreText -> bad { "UreText can not be negated" }
  is UreAlternation -> bad { "UreAlternation can not be negated" }
  is UreChangeOptions -> bad { "UreChangeOptions can not be negated" }
}
// TODO_someday: experiment more with different operators overloading,
//  especially indexed access operators and invoke operators.

@NotPortableApi @DelicateApi fun ureRaw(ir: IR) = UreWithRawIR(ir)
@NotPortableApi @DelicateApi fun ureRaw(str: String) = ureRaw(IR(str))

/** Wraps the [text] with \Q...\E, so it's interpreted as exact text to match (no chars treated as special). */
@NotPortableApi fun ureQuote(text: String) = UreQuote(text)

/** Similar to [ureQuote] but quotes each character (which could be treated as special) separately with backslash */
fun ureText(text: String) = UreText(text)

// endregion [Ure Basic Stuff]

// region [Ure Character Related Stuff]

@NotPortableApi("Only surrogate pairs compiling is not portable (IR with \\x{hhhhh})", ReplaceWith("ch(chr: Char)"))
fun ch(str: String) = UreCharExact(str)

// this one is portable, because it never ends up being a surrogate pair.
@OptIn(NotPortableApi::class) fun ch(chr: Char) = ch(chr.strf)

@DelicateApi fun chPreDef(name: Char) = UreCharClassPreDef(name)

// Ure constants matching one char (special chars; common categories). All names start with ch.
// Note from experience: It's really more important to have a common prefix than to be a bit shorter.

// just private shortcuts
@OptIn(DelicateApi::class) private inline val Char.ce get() = ch(this)
@OptIn(DelicateApi::class) private inline val Char.cpd get() = chPreDef(this)

val chSlash = '/'.ce
val chBackSlash = '\\'.ce

val chTab = '\t'.ce
val chLF = '\n'.ce
val chCR = '\r'.ce
val chFF = '\u000C'.ce
val chAlert = '\u0007'.ce
val chEsc = '\u001B'.ce

/** [a-z] */
val chLower = chOf('a'..'z')

/** [A-Z] */
val chUpper = chOf('A'..'Z')

/** [a-zA-Z] */
@OptIn(NotPortableApi::class)
val chAlpha = chOfAny(chLower, chUpper) // Note: "chLower or chUpper" is worse, because UreAlternation can't be negated.

/** Same as [0-9] */
val chDigit = 'd'.cpd

/** Same as [0-9a-fA-F] */
@OptIn(NotPortableApi::class)
val chHexDigit = chOfAny(chDigit, chOf('a'..'f'), chOf('A'..'F'))

/** Same as [a-zA-Z0-9] */
@OptIn(NotPortableApi::class)
val chAlnum = chOfAny(chAlpha, chDigit)

val chPunct = chOfAnyExact("""!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~""".toL)

@OptIn(NotPortableApi::class)
val chGraph = chOfAny(chAlnum, chPunct)

val chSpace = ' '.ce
val chWhiteSpace = 's'.cpd

@OptIn(NotPortableApi::class)
val chWhiteSpaceInLine =
  chOfAny(chSpace, chTab) // Note: "chSpace or chTab" is worse, because UreAlternation can't be negated.

/** Basic printable characters. Only normal space. No emojis, etc. */
@OptIn(NotPortableApi::class)
val chPrint = chOfAny(chGraph, chSpace)

/** Same as [^0-9] */
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chDigit"))
val chNonDigit = 'D'.cpd

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWhiteSpace"))
val chNonWhiteSpace = 'S'.cpd

val chDash = '-'.ce

/**
 * Matches only the actual dot "." character.
 * Check [chAnyInLine] if you want regex that matches any character in line (represented by "." IR),
 * Check [chAnyAtAll] if you want regex that matches any character at all.
 */
val chDot = '.'.ce

/**
 * Warning: Careful with timesXXX (catastrophic backtracking: https://www.regular-expressions.info/catastrophic.html)
 * Note: We have [RegexOption.MULTILINE] enabled by default, so the '.' matches only "in line".
 * See [Ure.compile] kdoc for details.
 */
val chAnyInLine = '.'.cpd

/**
 * [\s\S] It is a portable and fast way to match any character at all.
 * Warning: Careful with timesXXX (catastrophic backtracking: https://www.regular-expressions.info/catastrophic.html)
 */
@OptIn(NotPortableApi::class)
val chAnyAtAll = chOfAny(chWhiteSpace, !chWhiteSpace) // should work everywhere and should be fast.
// Note: following impl would not work on JS: ureIR("(?s:.)")
//   see details: https://www.regular-expressions.info/dot.html

/** Same as [a-zA-Z0-9_] */
val chWord = 'w'.cpd

/** The first character of a "word" normally can't be a digit, but still can be '_', so: [a-zA-Z_] */
@OptIn(NotPortableApi::class)
val chWordFirst = chOfAny(chAlpha, ch('_'))

/** Same as [^a-zA-Z0-9_] */
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWord"))
val chNonWord = 'W'.cpd

@OptIn(NotPortableApi::class)
val chWordOrDot = chOfAny(chWord, chDot)

@OptIn(NotPortableApi::class)
val chWordOrDash = chOfAny(chWord, chDash) // also hints (when typing chWo) that chWord doesn't match dash.
@OptIn(NotPortableApi::class)
val chWordOrDotOrDash = chOfAny(chWord, chDot, chDash)

// Note: All these different flavors of "word-like" classes seem unnecessary/not-micro-enough,
//   but let's keep them because I suspect I will reuse them a lot in practice.
//   I'll also keep negative versions as a hint for user that negation (operator) does the same.

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWordOrDot"))
val chNonWordNorDot = !chWordOrDot

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWordOrDash"))
val chNonWordNorDash = !chWordOrDash

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWordOrDotOrDash"))
val chNonWordNorDotNorDash = !chWordOrDotOrDash

/**
 * Predefined char set with some property.
 * It uses regex like \p{Latin} or \P{Emoji} etc.
 *
 * Warning: different platforms support different character properties/classes.
 *   https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 *   https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Unicode_character_class_escape
 *
 * Note: It can sometimes match more than one char technically.
 *   For example, most (all?) emoji "code points" take two "code units" (16b chars).
 *   Such 32b encoding is also called "surrogate pair".
 *
 * Note: Kotlin/JS: RegExp objects under the hood are constructed with the "u" flag,
 *   that enables unicode features in regular expressions (and makes the syntax stricter).
 *   https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/RegExp/unicode
 */
@NotPortableApi("Different platforms support different character properties/classes")
fun chProp(prop: String, positive: Boolean = true) = UreCharClassProp(prop, positive)

@NotPortableApi
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chProp(prop)"))
fun chpPropNot(prop: String) = !chProp(prop)

// Some of the more popular char props available on (probably) all platforms:

/**
 * Warning: It works differently on JS than on other platforms. Check [chLower] for a basic portable version.
 * On JS, it is more correct because matches letters like: "ε", "ł", "ź"
 */
@NotPortableApi("It works differently on JS than on other platforms. Check chLower for a basic portable version.")
val chPLower = chProp("Lower")

/**
 * Warning: It works differently on JS than on other platforms. Check [chUpper] for a basic portable version.
 * On JS, it is more correct because matches letters like: "Λ", "Ξ", "Ł", "Ź"
 */
@NotPortableApi("It works differently on JS than on other platforms. Check chUpper for a basic portable version.")
val chPUpper = chProp("Upper")

/**
 * Warning: It works differently on JS than on other platforms. Check [chAlpha] for a basic portable version.
 * On JS, it is more correct because matches letters like: "Λ", "Ξ", "Ł", "Ź", "λ", "ξ", "ł", "ź"
 */
@NotPortableApi("It works differently on JS than on other platforms. Check chAlpha for a basic portable version.")
val chPAlpha = chProp("Alpha")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chDigit] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chDigit for a basic portable version.")
val chPDigit = chProp("Digit")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chHexDigit] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chHexDigit for a basic portable version.")
val chPHexDigit = chProp("XDigit")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chAlnum] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chAlnum for a basic portable version.")
val chPAlnum = chProp("Alnum")

/**
 * Warning: Currently does NOT compile (Ure.compile) on JS. Check [chPunct] for a basic portable version.
 * Warning: On LINUX it also somehow matches digits. Why??
 */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chPunct for a basic portable version.")
@DelicateApi("On LINUX it also somehow matches digits. Why??") // TODO_later: report this?
val chPPunct = chProp("Punct")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chGraph] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chGraph for a basic portable version.")
val chPGraph = chProp("Graph")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Also broken on LINUX. Check [chPrint] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chPrint for a basic portable version.")
@DelicateApi("On LINUX it somehow doesn't match ANYTHING I tried. Why??") // TODO_later: report this?
@Deprecated("It looks like it's really broken on LINUX so don't use it.", ReplaceWith("chPrint"))
val chPPrint = chProp("Print")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chWhiteSpaceInLine] for a basic portable version.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chWhiteSpaceInLine for a basic portable version.")
val chPBlank = chProp("Blank")

/** Warning: Currently does NOT compile (Ure.compile) on JS. Check [chWhiteSpace] for basic portable verion.*/
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS. Check chWhiteSpace for basic portable verion.")
val chPWhiteSpace = chProp("Space")

@OptIn(NotPortableApi::class)
val chPCurrency = chProp("Sc")

/**
 * Warning: Currently it compiles (Ure.compile) only on JS.
 * Note: I guess this one is pretty good class to match actual emojis.
 *   Others like chProp("Emoji") or chProp("Emoji_Presentation") match/don't match weird characters.
 *   https://unicode.org/reports/tr51/#Emoji_Properties
 */
@NotPortableApi("Currently it compiles (Ure.compile) only on JS.")
val chPExtPict = chProp("ExtPict")

/** Warning: Currently does NOT compile (Ure.compile) on LINUX. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on LINUX.")
val chPLatin = chProp("sc=Latin")

/** Warning: Currently does NOT compile (Ure.compile) on LINUX. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on LINUX.")
val chPGreek = chProp("sc=Greek")

@NotPortableApi @DelicateApi fun chCtrl(letter: Char) = ureRaw("\\c$letter").also { req(letter in 'A'..'Z') }
// https://www.regular-expressions.info/nonprint.html

@NotPortableApi("Some unions do NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfAny(charClasses: List<UreCharClass>) = UreCharClassUnion(charClasses)

@NotPortableApi("Some unions do NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfAny(vararg charClasses: UreCharClass?) = chOfAny(charClasses.toList().filterNotNull())

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chOfAny(charClasses)"))
@NotPortableApi("Some unions do NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfNotAny(vararg charClasses: UreCharClass?) = !chOfAny(*charClasses)

@OptIn(DelicateApi::class, NotPortableApi::class) // simple union like this works on all platforms.
fun chOfAnyExact(exactChars: List<Char>) = chOfAny(exactChars.map(::ch))

@OptIn(DelicateApi::class)
fun chOfAnyExact(vararg exactChars: Char?) = chOfAnyExact(exactChars.toL.filterNotNull())

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chOfAnyExact(exactChars)"))
fun chOfNotAnyExact(exactChars: List<Char>) = !chOfAnyExact(exactChars)

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chOfAnyExact(*exactChars)"))
fun chOfNotAnyExact(vararg exactChars: Char?) = !chOfAnyExact(*exactChars)

@NotPortableApi
fun chOfRange(from: UreCharClass, to: UreCharClass) = UreCharClassRange(from, to)

@OptIn(NotPortableApi::class)
fun chOf(range: CharRange) = chOfRange(ch(range.start), ch(range.endInclusive))

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chOf(range)"))
fun chOfNot(range: CharRange) = !chOf(range)

/**
 * This is not only not-portable, but also VERY DELICATE.
 * Please always write unit tests to make sure it behaves as expected on platforms you're using.
 * There are weird inconsistencies when regex engines interpret intersections of unions, negated intersections, etc etc.
 * Some are described here: https://www.regular-expressions.info/charclassintersect.html
 * Some are reproduced in fun testUreCharClasses in TestUreCharClasses.cmn.kt
 * Usual workaround for weird behavior is to wrap some parts in additional chOfAny(token).
 */
@DelicateApi("Very delicate! Expect inconsistent matching behavior between platforms. Always write unit tests.")
@NotPortableApi("Does NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfAll(charClasses: List<UreCharClass>) = UreCharClassIntersect(charClasses)

/**
 * This is not only not-portable, but also VERY DELICATE.
 * Please always write unit tests to make sure it behaves as expected on platforms you're using.
 * There are weird inconsistencies when regex engines interpret intersections of unions, negated intersections, etc etc.
 * Some are described here: https://www.regular-expressions.info/charclassintersect.html
 * Some are reproduced in fun testUreCharClasses in TestUreCharClasses.cmn.kt
 * Usual workaround for weird behavior is to wrap some parts in additional chOfAny(token).
 */
@DelicateApi("Very delicate! Expect inconsistent matching behavior between platforms. Always write unit tests.")
@NotPortableApi("Does NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfAll(vararg charClasses: UreCharClass?) = chOfAll(charClasses.toL.filterNotNull())

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chOfAll(charClasses)"))
@DelicateApi("Very delicate! Expect inconsistent matching behavior between platforms. Always write unit tests.")
@NotPortableApi("Does NOT compile on JS. Kotlin/JS uses 'unicode'(u) mode but not 'unicodeSets'(v) mode.")
fun chOfNotAll(vararg charClasses: UreCharClass?) = !chOfAll(*charClasses)

// endregion [Ure Character Related Stuff]

// region [Ure Anchors Related Stuff]

/**
 * https://www.regular-expressions.info/anchors.html
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#bounds
 */
@NotPortableApi fun at(anchorName: Char) = UreAnchorPreDef(anchorName)

@OptIn(NotPortableApi::class) private inline val Char.at get() = at(this)

val atBOLine = '^'.at
val atEOLine = '$'.at

@NotPortableApi val atBOInput = 'A'.at
@NotPortableApi val atEOInputWithoutLineBreak = 'Z'.at
@NotPortableApi val atEOInput = 'z'.at

@NotPortableApi val atEOPrevMatch = 'G'.at

val atWordBoundary = 'b'.at

@SecondaryApi("Use !atWordBoundary") val atNotWordBounday = 'B'.at
// Calling it "non-word boundary" is wrong. It's the opposite to the word boundary, so "not (word boundary)"

@SecondaryApi("Usually atWordBoundary is also good, and have simpler construction (no lookAhead).")
val atBOWord = atWordBoundary then chWord.lookAhead() // emulating sth like in Vim or GNU: "\<"

@OptIn(DelicateApi::class) @NotPortableApi
@SecondaryApi("Usually just atWordBoundary is also good, and have simpler construction (no lookBehind).")
val atEOWord = atWordBoundary then chWord.lookBehind() // emulating sth like in Vim or GNU: "\>"

@OptIn(DelicateApi::class, NotPortableApi::class)
val ureLineBreakBasic = ureRaw("\\r?\\n")

/** On modern JVM it should be equivalent to ureRaw("\u000D\u000A|[\u000A\u000B\u000C\u000D\u0085\u2028\u2029]") */
@DelicateApi("Can work differently not only on different platforms, but even on different JVM versions.")
@NotPortableApi("Not supported on JS. Works differently on different platforms or even JVM versions.")
val ureLineBreakAdvanced = ureRaw("\\R")
// See also the "Line Breaks" there:
//   https://www.regular-expressions.info/nonprint.html
//   https://www.regular-expressions.info/dot.html

val ureLineBreak = ureLineBreakBasic

// endregion [Ure Anchors Related Stuff]

// region [Ure Groups Related Stuff]

fun Ure.group(capture: Boolean = true, name: String? = null) = when {
  name != null -> {
    req(capture) { "Named group is always capturing." }
    withName(name)
  }
  capture -> UreNumberedGroup(this)
  else -> groupNonCapt()
}

fun Ure.groupNonCapt() = UreNonCapturingGroup(this)

/** https://www.regular-expressions.info/atomic.html */
@NotPortableApi("Does NOT even compile (Ure.compile) on JS.")
fun Ure.groupAtomic() = UreAtomicGroup(this)

@OptIn(NotPortableApi::class, DelicateApi::class) // lookAhead should be safe; lookBehind is delicate/non-portable.
fun Ure.lookAhead(positive: Boolean = true) = UreLookGroup(this, true, positive)

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Lookbehind_assertion#description
@DelicateApi("Can be suprisingly slow for some ures. Or even can throw on some platforms, when looking behind for non-fixed length ure.")
@NotPortableApi("Behavior can differ on different platforms. Read docs for each used platform.")
fun Ure.lookBehind(positive: Boolean = true) = UreLookGroup(this, false, positive)

@OptIn(NotPortableApi::class, DelicateApi::class) // lookAhead should be safe; lookBehind is delicate/non-portable.
@SecondaryApi("Use Ure.lookAhead", ReplaceWith("ure(init = init).lookAhead(positive)"))
fun ureLookAhead(positive: Boolean = true, init: UreConcatenation.() -> Unit) = ure(init = init).lookAhead(positive)

@DelicateApi("Can be suprisingly slow for some ures. Or even can throw on some platforms, when looking behind for non-fixed length ure.")
@NotPortableApi("Behavior can differ on different platforms. Read docs for each used platform.")
@SecondaryApi("Use Ure.lookBehind", ReplaceWith("ure(init = init).lookBehind(positive)"))
fun ureLookBehind(positive: Boolean = true, init: UreConcatenation.() -> Unit) = ure(init = init).lookBehind(positive)

fun ureRef(nr: Int? = null, name: String? = null) = UreGroupRef(nr, name)

// endregion [Ure Groups Related Stuff]

// region [Ure Quantifiers Related Stuff]

fun Ure.times(exactly: Int) = UreQuantifier(this, exactly..exactly)

/**
 * By default, it's "greedy" - tries to match as many "times" as possible. But back off one by one if it fails.
 * @param reluctant - Tries to eat as little "times" as possible. Opposite to default "greedy" behavior.
 * @param possessive - It's like more greedy than default greedy. Never backtracks - fails instead. Just as [UreAtomicGroup]
 */
fun Ure.times(times: IntRange, reluctant: Boolean = false, possessive: Boolean = false) =
  if (times.start == 1 && times.endInclusive == 1) this else UreQuantifier(this, times, reluctant, possessive)

fun Ure.timesMinMax(min: Int, max: Int, reluctant: Boolean = false, possessive: Boolean = false) =
  times(min..max, reluctant, possessive)

fun Ure.timesMin(min: Int, reluctant: Boolean = false, possessive: Boolean = false) =
  timesMinMax(min, MAX, reluctant, possessive)

fun Ure.timesMax(max: Int, reluctant: Boolean = false, possessive: Boolean = false) =
  timesMinMax(0, max, reluctant, possessive)

fun Ure.timesAny(reluctant: Boolean = false, possessive: Boolean = false) =
  times(0..MAX, reluctant, possessive)

@Deprecated("Let's try to use .times instead", ReplaceWith("content.times(times, reluctant, possessive)"))
fun quantify(content: Ure, times: IntRange, reluctant: Boolean = false, possessive: Boolean = false) =
  content.times(times, reluctant, possessive)

@Deprecated("Let's try to use .times instead", ReplaceWith("ure(init = init).times(times, reluctant, possessive)"))
fun quantify(
  times: IntRange,
  reluctant: Boolean = false,
  possessive: Boolean = false,
  init: UreConcatenation.() -> Unit,
) = ure(init = init).times(times, reluctant, possessive)

// endregion [Ure Quantifiers Related Stuff]
