@file:Suppress("SpellCheckingInspection")

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import kotlin.jvm.JvmInline
import kotlin.reflect.*
import kotlin.text.RegexOption.*

/**
 * Multiplatform Kotlin Frontend / DSL for regular expressions. Actual regular expressions are used like IR
 * (intermediate representation) just to compile it to standard kotlin.text.Regex,
 * but the developer is using nice DSL to build regular expressions instead of writing them by hand.
 *
 * Reference links to RE engines/backends docs, etc.:
 * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 * https://docs.oracle.com/javase/tutorial/essential/regex/quant.html
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/RegExp
 * https://www.w3schools.com/jsref/jsref_obj_regexp.asp
 * https://www.regular-expressions.info/ (comprehensive information, notes about different implementations)
 * https://regexr.com/
 * https://regex101.com/ (nice but closed source)
 */



/** UreIR is the traditional regular expression - no human should read - kind of "intermediate representation" */
@JvmInline
value class UreIR @DelicateApi internal constructor(val str: String) {
    override fun toString(): String = str
}

@OptIn(DelicateApi::class) private val String.asUreIR get() = UreIR(this)

fun ure(vararg opts: RegexOption, init: UreProduct.() -> Unit) = ure(enable = opts.toSet(), disable = emptySet(), init)

// TODO_later: maybe remove options here? and always use Ure.withOptions? (check in practice first)
fun ure(enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet(), init: UreProduct.() -> Unit) =
    if (enable.isEmpty() && disable.isEmpty()) UreProduct().apply(init)
    else UreProduct().apply(init).withOptions(enable, disable)

@SecondaryApi("Use Ure.withOptions", ReplaceWith("content.withOptions(enable, disable)"))
fun ureWithOptions(content: Ure, enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet()) =
    UreChangeOptionsGroup(content, enable, disable)

// TODO_someday_maybe: maybe remove name and opts here? and always use Ure.with...? (check in practice first)
fun ure(name: String, vararg opts: RegexOption, init: UreProduct.() -> Unit) =
    if (opts.isEmpty()) UreProduct().apply(init).withName(name)
    else UreProduct().apply(init).withOptionsEnabled(*opts).withName(name)

@SecondaryApi("Use Ure.withName", ReplaceWith("content.withName(name"))
fun ureWithName(name: String, content: Ure) = UreNamedGroup(content, name)

fun Ure.withName(name: String?) = if (name == null) this else UreNamedGroup(this, name)
fun Ure.withOptions(enable: Set<RegexOption> = emptySet(), disable: Set<RegexOption> = emptySet()) =
    UreChangeOptionsGroup(this, enable, disable)

fun Ure.withOptionsEnabled(vararg options: RegexOption) = withOptions(enable = options.toSet())
fun Ure.withOptionsDisabled(vararg options: RegexOption) = withOptions(disable = options.toSet())

fun Ure.withWordBoundaries(boundaryBefore: Boolean = true, boundaryAfter: Boolean = true) =
    if (!boundaryBefore && !boundaryAfter) this else ure {
        if (boundaryBefore) 1 of bBOWord
        1 of this@withWordBoundaries // it should flatten if this is UreProduct (see UreProduct.toIR()) TODO_later: doublecheck
        if (boundaryAfter) 1 of bEOWord
    }

sealed interface Ure {

    fun toIR(): UreIR

    /**
     * Optionally wraps in non-capturing group before generating IR, so it's safe to use with quantifiers, unions, etc
     * Wrapping is done only when needed. For example concatenation(product) with more than one element is wrapped.
     * (UreProduct with 0 elements also is wrapped - so f. e. external UreQuantifier only catches empty product)
     */
    fun toClosedIR(): UreIR

    /**
     * It sets MULTILINE by default.
     * Also I decided NOT to use DO_MATCHES_ALL by default. Lets keep "any" as single line matcher.
     * Lets use explicit ureAnyLine and/or ureWhateva utils instead of changing "any" meaning freely.
     * Lets assume in all normal val/fun ureSth.. that DOT_MATCHES_ALL is disabled
     * (so we don't enable/disable it all the time "just to make sure")
     */
    fun compile(vararg options: RegexOption) = compileMultiLine(*options)

    fun compileMultiLine(vararg options: RegexOption) = Regex(toIR().str, setOf(MULTILINE) + options)

    fun compileSingleLine(vararg options: RegexOption) = Regex(toIR().str, options.toSet())
        .also { check(MULTILINE !in options) }

    // TODO_later: experiment with dropping U (Micro) prefix in classes nested in Ure when I have some examples working.
    // I'm leaving it for now to have more unique names and less clashes, but design is not final.
}

infix fun Ure.or(that: Ure) = UreUnion(this, that)
infix fun Ure.then(that: Ure) = UreProduct(mutableListOf(this, that))
// Do not rename "then" to "and". The "and" is more like special lookahead/lookbehind group


@JvmInline
value class UreProduct internal constructor(val product: MutableList<Ure> = mutableListOf()) : Ure {

    override fun toIR(): UreIR = when (product.size) {
        0 -> "".asUreIR
        1 -> product[0].toIR()
        else -> product.joinToString("") { if (it is UreProduct) it.toIR().str else it.toClosedIR().str }.asUreIR
    }

    override fun toClosedIR() = when (product.size) {
        1 -> product[0].toClosedIR()
        else -> this.groupNonCapt().toIR() // in 0 case we also want ncapt!
        // To avoid issues when outside operator captures something else instead of empty product.
        // I decided NOT to throw IllegalStateError in 0 case so we can always monitor IR in half-baked UREs.
        // (like when creating UREs with some compose UI)
    }

    // Can't decide if this syntax is better in case of "1 of ..."; let's leave it for now.
    // TODO_later: rethink syntax when context receivers become multiplatform.
    //   Maybe somehow force '+' in other cases too, but I don't want to force syntax with additional parentheses.
    operator fun Ure.unaryPlus() { product.add(this) }

    class UreX internal constructor(val times: IntRange, val reluctant: Boolean, val possessive: Boolean)

    fun x(times: IntRange, reluctant: Boolean = false, possessive: Boolean = false) = UreX(times, reluctant, possessive)
    fun x(times: Int) = x(times..times)
    infix fun UreX.of(ure: Ure) {
        product.add(quantify(ure, times, reluctant, possessive))
    }

    infix fun UreX.of(init: UreProduct.() -> Unit) {
        product.add(quantify(times, reluctant, possessive, init))
    }

    infix fun IntRange.of(ure: Ure) = x(this) of ure
    infix fun Int.of(ure: Ure) = x(this) of ure
    infix fun IntRange.of(init: UreProduct.() -> Unit) = x(this) of init
    infix fun Int.of(init: UreProduct.() -> Unit) = x(this) of init
}

data class UreUnion internal constructor(val first: Ure, val second: Ure) : Ure {
    override fun toIR() = "${first.toClosedIR()}|${second.toClosedIR()}".asUreIR
    override fun toClosedIR() = this.groupNonCapt().toIR()
}

sealed interface UreGroup : Ure {
    val content: Ure
    private val contentIR get() = content.toIR()
    val typeIR: UreIR // it's not full IR but just the part that signifies the type of group

    override fun toIR(): UreIR = "($typeIR$contentIR)".asUreIR
    // looks like all possible typeIR prefixes can not be confused with first contentIR characters.
    // (meaning: RE designers thought about it, so I don't have to be extra careful here)

    override fun toClosedIR() = toIR() // group is always "closed" - has parentheses outside
}

data class UreNamedGroup internal constructor(override val content: Ure, val name: String) : UreGroup {
    override val typeIR get() = "?<$name>".asUreIR
}

@JvmInline
value class UreNonCaptGroup internal constructor(override val content: Ure) : UreGroup {
    override val typeIR get() = "?:".asUreIR
}

@JvmInline
value class UreCaptGroup internal constructor(override val content: Ure) : UreGroup {
    override val typeIR get() = "".asUreIR
}

// TODO: test it!
data class UreChangeOptionsGroup internal constructor(
    override val content: Ure,
    val enable: Set<RegexOption> = emptySet(),
    val disable: Set<RegexOption> = emptySet(),
) : UreGroup {
    init {
        require((enable intersect disable).isEmpty()) { "Can not enable and disable the same option at the same time" }
        require(enable.isNotEmpty() || disable.isNotEmpty()) { "No options provided" }
    }

    override val typeIR get() = "?${enable.ir}-${disable.ir}:".asUreIR // TODO_later: check if either set can be empty

    // TODO: review flags we support; support non-multi-platform, but add annotations and docs for different platforms.
    private val RegexOption.code
        get() = when (this) {
            IGNORE_CASE -> "i"
            MULTILINE -> "m"
            else -> error("Only multiplatform regex options are supported for now.")
//            LITERAL -> TODO()
//            UNIX_LINES -> "d"
//            COMMENTS -> "x" // but not really supported... maybe in UreRawIR, but I wouldn't use it
//            DOT_MATCHES_ALL -> "s" // s means - treat all as a single line (so dot matches terminators too)
//            CANON_EQ -> TODO()
//            // TODO_someday "u" is not supported (unicode case)
        }

    private val Set<RegexOption>.ir get() = joinToString("") { it.code }
}
// TODO_someday: there are also similar "groups" without content (see Pattern.java), add support for it (content nullable?)


// Note: For delicate/not portable reasons, see
//   https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Lookbehind_assertion#description
//   search: "This behavior is reasonable...Therefore, it starts... Regexes in some other languages forbid..."
data class UreLookGroup @DelicateApi @NotPortableApi internal constructor(
    override val content: Ure,
    val ahead: Boolean = true,
    val positive: Boolean = true,
) : UreGroup {
    override val typeIR
        get() = when (ahead to positive) {
            true to true -> "?="
            true to false -> "?!"
            false to true -> "?<="
            false to false -> "?<!"
            else -> error("Impossible case")
        }.asUreIR
}


// TODO_someday: "independent" non-capturing group - what is that? (see Pattern.java)

data class UreGroupRef internal constructor(val nr: Int? = null, val name: String? = null) : Ure {
    init {
        nr == null || name == null || error("Can not reference capturing group by both nr ($nr) and name ($name)")
        nr == null && name == null && error("Either nr or name has to be provided for the group reference")
    }

    override fun toIR(): UreIR = if (nr != null) "\\$nr".asUreIR else "\\k<$name>".asUreIR
    override fun toClosedIR(): UreIR = toIR()
}

const val MAX = Int.MAX_VALUE

/**
 * By default it's "greedy" - tries to match as many "times" as possible. But backs off one by one if it would fail.
 * @param reluctant - Tries to eat as little "times" as possible. Opposite to default "greedy" behavior.
 * @param possessive - It's like more greedy than default greedy. Never backs off - fails instead.
 */
data class UreQuantifier internal constructor(
    val content: Ure,
    val times: IntRange,
    val reluctant: Boolean = false,
    val possessive: Boolean = false,
) : Ure {
    init {
        reluctant && possessive && error("Quantifier can't be reluctant and possessive at the same time")
    }

    val greedy get() = !reluctant && !possessive
    override fun toIR(): UreIR {
        val timesIR = when (times) {
            1..1 -> return content.toIR()
            0..1 -> "?"
            0..MAX -> "*"
            1..MAX -> "+"
            else -> when (times.last) {
                times.first -> "{${times.first}}"
                MAX -> "{${times.first},}"
                else -> "{${times.first},${times.last}}"
            }
        }.asUreIR
        val suffixIR = when {
            reluctant -> "?"
            possessive -> "+"
            else -> ""
        }.asUreIR
        return "${content.toClosedIR()}$timesIR$suffixIR".asUreIR
    }
    //    override fun toClosedIR(): UreIR = ncapt(this).toIR()
    override fun toClosedIR() = toIR()
    // TODO_later: I think it's correct, but should be analyzed more carefully (and write tests!).
}

@JvmInline
value class UreChar @DelicateApi internal constructor(val ir: UreIR) : Ure {
    // TODO_maybe: separate sealed class for specials etc. We should never ask user to manually provide UreIR
    init {
        // TODO NOW: check if ir correctly represent full "char-like" regex.
        //   (escaped chars, or surrogate pairs (two code units representing one code point), are also "char-like")
    }
    override fun toIR(): UreIR = ir
    @OptIn(DelicateApi::class)
    override fun toClosedIR(): UreIR = ir(ir).toClosedIR()
}

// TODO: can I do something like: chars: Set<UreChar> ?? some error checking for wrong chars?
data class UreCharSet internal constructor(val chars: Set<String>, val positive: Boolean = true) : Ure {
    override fun toClosedIR(): UreIR = toIR()
    override fun toIR(): UreIR = chars
        .joinToString("", if (positive) "[" else "[^", "]") {
            if (it in setOf("]", "\\", "-")) "\\$it" else it
        }
        .asUreIR
}

// TODO_later: more complicated combinations of char classes
// TODO_later: analyze if some special kotlin progression/range would fit here better
data class UreCharRange internal constructor(val from: String, val to: String, val positive: Boolean = true) : Ure {
    private val neg = if (positive) "" else "^"
    override fun toIR(): UreIR = "[$neg$from-$to]".asUreIR
    override fun toClosedIR(): UreIR = toIR()
}

data class UreCharProp @NotPortableApi internal constructor(val prop: String, val positive: Boolean = true) : Ure {
    override fun toClosedIR(): UreIR = toIR()
    override fun toIR(): UreIR =  "\\${if (positive) "p" else "P"}{$prop}".asUreIR
}

@JvmInline
/** "Dirty way to inject whole strings fast.") */
// TODO_someday_maybe: think what would be better.
// Maybe still ask user for string, but validate and transform to the actual UreProduct of UreChar's
value class UreRawIR @DelicateApi internal constructor(val ir: UreIR) : Ure {

    override fun toIR(): UreIR = ir
    override fun toClosedIR(): UreIR = if (isClosed) ir else this.groupNonCapt().toIR()
    private val isClosed get() = when {
        ir.str.length == 1 -> true
        ir.str.length == 2 && ir.str[0] == '\\' -> true
        else -> false
    }
    // TODO_someday: analyze more carefully and drop grouping when actually not needed.
}

@JvmInline
value class UreQuote internal constructor(val str: String) : Ure {
    override fun toIR() = "\\Q$str\\E".asUreIR
    override fun toClosedIR(): UreIR = toIR()
}

// It only returns NotPortableApi ure if argument was already NotPortableApi (UreCharProp).
@OptIn(NotPortableApi::class, SecondaryApi::class, DelicateApi::class)
operator fun Ure.not(): Ure = when (this) {
    is UreChar -> when (this) {
        chWord -> chNonWord
        chNonWord -> chWord
        chDigit -> chNonDigit
        chNonDigit -> chDigit
        chSpace -> chNonSpace
        chNonSpace -> chSpace
        else -> oneCharNotOf(ir.str)
        // TODO: check if particular ir is appropriate for such wrapping
        // TODO_later: other special cases?
    }
    is UreRawIR -> when (this) {
        bchWord -> bchWordNot
        bchWordNot -> bchWord
        else -> error("This UreRawIR can not be negated")
    }

    is UreCharRange -> UreCharRange(from, to, !positive)
    is UreCharSet -> UreCharSet(chars, !positive)
    is UreCharProp -> UreCharProp(prop, !positive)
    is UreGroup -> when (this) {
        is UreLookGroup -> UreLookGroup(content, ahead, !positive)
        else -> error("Unsupported UreGroup for negation: ${this::class.simpleName}")
    }

    is UreGroupRef -> error("UreGroupRef can not be negated")
    is UreProduct -> error("UreProduct can not be negated")
    is UreQuantifier -> error("UreQuantifier can not be negated")
    is UreQuote -> error("UreQuote can not be negated")
    is UreUnion -> error("UreUnion can not be negated")
    else -> error("Unexpected Ure type: ${this::class.simpleName}")
    // had to add "else" branch because Android Studio 2021.2.1 canary 7 complains..
    // TODO_later: Remove "else" when newer AS stops complaining
}
// TODO_later: experiment more with different operators overloading (after impl some working examples)
//  especially indexed access operators and invoke operators..

@DelicateApi fun ir(ir: UreIR) = UreRawIR(ir)
@DelicateApi fun ir(str: String) = ir(UreIR(str))
@DelicateApi fun ch(ir: UreIR) = UreChar(ir)
@OptIn(DelicateApi::class) fun ch(str: String) = ch(UreIR(str)) // TODO check for the wrong strings in UreChar.init.
fun ch(chr: Char) = ch(chr.toString())


// Ure constants matching one char (special chars; common categories). All names start with ch.
// Note from experience: It's really more important to have a common prefix than to be a bit shorter.


// just private shortcuts
@OptIn(DelicateApi::class) private val String.ch get() = ch(this)
@OptIn(DelicateApi::class) private val String.raw get() = ir(this)

val chBackSlash = "\\\\".ch

fun chUniCode(name: String) = "\\N{$name}".ch

val chTab = "\\t".ch
val chLF = "\\n".ch
val chCR = "\\r".ch
val chFF = "\\f".ch
val chAlert = "\\a".ch
val chEsc = "\\e".ch

val chDot = "\\.".ch
val chAny = ".".ch
val chAnyMultiLine = "(?s:.)".ch

val chDigit = "\\d".ch

val chSpace = "\\s".ch
val chSpaceInLine = " ".ch or chTab

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chDigit"))
val chNonDigit = "\\D".ch

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chSpace"))
val chNonSpace = "\\S".ch



/** Same as [a-zA-Z0-9_] */
val chWord = "\\w".ch

/** Same as [^a-zA-Z0-9_] */
val chNonWord = "\\W".ch

fun chWord(orDot: Boolean = false, orHyphen: Boolean = false) = oneCharOf(
    "\\w" + if (orDot) "." else "" + if (orHyphen) "\\-" else ""
)

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWord(orDot = norDot, orHyphen = norHyphen"))
fun chNonWord(norDot: Boolean = false, norHyphen: Boolean = false) = !chWord(orDot = norDot, orHyphen = norHyphen)
// Let it stay as a hint for user that negation (operator) does the same.

val chWordOrDot = chWord(orHyphen = true)
val chWordOrHyphen = chWord(orHyphen = true) // also hints (when typing chWo) that chWord doesn't match hyphen.
val chWordOrDotOrHyphen = chWord(orDot = true, orHyphen = true)

// Note: All these different flavors of "word-like" classes seem unnecessary/not-micro-enough,
//   but let's keep them because I suspect I will reuse them a lot in practice.
//   I'll also keep negative versions as a hint for user that negation (operator) does the same.

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWordOrDot"))
val chNonWordNorDot = !chWordOrDot

@Deprecated("Use operator fun Ure.not()", ReplaceWith("!chWordOrHyphen"))
val chNonWordNorHyphen = !chWordOrHyphen

@Deprecated("Use operator fun Ure.not()", ReplaceWith("!chWordOrDotOrHyphen"))
val chNonWordNorDotNorHyphen = !chWordOrDotOrHyphen



val chaz = oneCharOfRange("a", "z")
val chAZ = oneCharOfRange("A", "Z")
val chazAZ = chaz or chAZ

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
fun chp(prop: String) = UreCharProp(prop, positive = true)

@NotPortableApi
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chp(prop)"))
fun chpNot(prop: String) = !chp(prop)


// Some of the more popular char props available on (probably) all platforms:

/**
 * Warning: It works differently on JS than on other platforms.
 * On JS, it is more correct because matches letters like: "ε", "ł", "ź"
 */
@NotPortableApi("It works differently on JS than on other platforms.")
val chpLower = chp("Lower")

/**
 * Warning: It works differently on JS than on other platforms.
 * On JS, it is more correct because matches letters like: "Λ", "Ξ", "Ł", "Ź"
 */
@NotPortableApi("It works differently on JS than on other platforms.")
val chpUpper = chp("Upper")

/**
 * Warning: It works differently on JS than on other platforms.
 * On JS, it is more correct because matches letters like: "Λ", "Ξ", "Ł", "Ź", "λ", "ξ", "ł", "ź"
 */
@NotPortableApi("It works differently on JS than on other platforms.")
val chpAlpha = chp("Alpha")

/** Warning: Currently does NOT compile (Ure.compile) on JS. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS.")
val chpDigit = chp("Digit")

/** Warning: Currently does NOT compile (Ure.compile) on JS. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS.")
val chpAlnum = chp("Alnum")

/**
 * Warning: Currently does NOT compile (Ure.compile) on JS.
 * Warning: On LINUX it also somehow matches numbers, like "2", "3", etc. Why??
 */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS.")
val chpPunct = chp("Punct")

/** Warning: Currently does NOT compile (Ure.compile) on JS. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS.")
val chpBlank = chp("Blank")

/** Warning: Currently does NOT compile (Ure.compile) on JS. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on JS.")
val chpSpace = chp("Space")

@OptIn(NotPortableApi::class)
val chpCurrency = chp("Sc")

/**
 * Warning: Currently it compiles (Ure.compile) only on JS.
 * Note: I guess this one is pretty good class to match actual emojis.
 *   Others like chp("Emoji") or chp("Emoji_Presentation") match/don't match weird characters.
 *   https://unicode.org/reports/tr51/#Emoji_Properties
 */
@NotPortableApi("Currently it compiles (Ure.compile) only on JS.")
val chpExtPict = chp("ExtPict")

/** Warning: Currently does NOT compile (Ure.compile) on LINUX. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on LINUX.")
val chpLatin = chp("sc=Latin")

/** Warning: Currently does NOT compile (Ure.compile) on LINUX. */
@NotPortableApi("Currently does NOT compile (Ure.compile) on LINUX.")
val chpGreek = chp("sc=Greek")



// boundaries (b...)

val bBOLine = "^".raw
val bEOLine = "$".raw
val bBOInput = "\\A".raw
val bEOInput = "\\z".raw
val bEOPreviousMatch = "\\G".raw

val bchWord = "\\b".raw
val bchWordNot = "\\B".raw // Calling it "non-word boundary" is wrong. It's a negation of bchWord, so "not (word boundary)"

@OptIn(NotPortableApi::class, DelicateApi::class)
val bBOWord = bchWord then chWord.lookAhead() // emulating sth like in vim: "\<"
@OptIn(NotPortableApi::class, DelicateApi::class)
val bEOWord = bchWord then chWord.lookBehind() // emulating sth like in vim: "\>"

/** Any Unicode linebreak sequence, is equivalent to \u000D\u000A|[\u000A\u000B\u000C\u000D\u0085\u2028\u2029] */
val ureLineBreak = "\\R".raw

@DelicateApi
fun control(x: String) = "\\c$x".ch // FIXME_later: what exactly is this?? (see std Pattern.java)
fun oneCharOf(vararg chars: String) = UreCharSet(chars.toSet()) // TODO_maybe: Use UreChar as vararg type
fun oneCharNotOf(vararg chars: String) = UreCharSet(chars.toSet(), positive = false) // TODO_maybe: as above
fun oneCharOfRange(from: String, to: String) = UreCharRange(from, to)
fun oneCharNotOfRange(from: String, to: String) = UreCharRange(from, to, positive = false)


fun Ure.group(capture: Boolean = true, name: String? = null) = when {
    name != null -> {
        require(capture) { "Named group is always capturing." }
        withName(name)
    }
    capture -> UreCaptGroup(this)
    else -> groupNonCapt()
}

fun Ure.groupNonCapt() = UreNonCaptGroup(this)

@OptIn(NotPortableApi::class, DelicateApi::class) // lookAhead should be safe; lookBehind is delicate/non-portable.
fun Ure.lookAhead(positive: Boolean = true) = UreLookGroup(this, true, positive)

@DelicateApi("Can be suprisingly slow for some ures. Or even can throw on some platforms, when looking behind for non-fixed length ure.")
@NotPortableApi("Behavior can differ on different platforms. Read docs for each used platform.")
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Lookbehind_assertion#description
fun Ure.lookBehind(positive: Boolean = true) = UreLookGroup(this, false, positive)

@OptIn(NotPortableApi::class, DelicateApi::class) // lookAhead should be safe; lookBehind is delicate/non-portable.
@SecondaryApi("Use Ure.lookAhead", ReplaceWith("ure(init = init).lookAhead(positive)"))
fun ureLookAhead(positive: Boolean = true, init: UreProduct.() -> Unit) = ure(init = init).lookAhead(positive)

@DelicateApi("Can be suprisingly slow for some ures. Or even can throw on some platforms, when looking behind for non-fixed length ure.")
@NotPortableApi("Behavior can differ on different platforms. Read docs for each used platform.")
@SecondaryApi("Use Ure.lookBehind", ReplaceWith("ure(init = init).lookBehind(positive)"))
fun ureLookBehind(positive: Boolean = true, init: UreProduct.() -> Unit) = ure(init = init).lookBehind(positive)


/**
 * By default, it's "greedy" - tries to match as many "times" as possible. But backs off one by one if it fails.
 * @param reluctant - Tries to eat as little "times" as possible. Opposite to default "greedy" behavior.
 * @param possessive - It's like more greedy than default greedy. Never backs off - fails instead.
 */
fun Ure.timesMinMax(min: Int, max: Int, reluctant: Boolean = false, possessive: Boolean = false) =
    if (min == 1 && max == 1) this else UreQuantifier(this, min..max, reluctant, possessive)

fun Ure.timesMin(min: Int, reluctant: Boolean = false, possessive: Boolean = false) =
    UreQuantifier(this, min..MAX, reluctant, possessive)

fun Ure.timesMax(max: Int, reluctant: Boolean = false, possessive: Boolean = false) =
    UreQuantifier(this, 0..max, reluctant, possessive)

fun Ure.times(exactly: Int) = UreQuantifier(this, exactly..exactly)

@Deprecated("Let's try to use .times instead")
fun quantify(
    content: Ure,
    times: IntRange,
    reluctant: Boolean = false,
    possessive: Boolean = false,
) = if (times == 1..1) content else UreQuantifier(content, times, reluctant, possessive)

@Deprecated("Let's try to use .times instead")
fun quantify(
    times: IntRange,
    reluctant: Boolean = false,
    possessive: Boolean = false,
    init: UreProduct.() -> Unit,
) = quantify(ure(init = init), times, reluctant, possessive)

fun ureRef(nr: Int? = null, name: String? = null) = UreGroupRef(nr, name)

fun ureQuote(string: String) = UreQuote(string)

fun CharSequence.replace(ure: Ure, transform: (MatchResult) -> CharSequence) = ure.compile().replace(this, transform)
fun CharSequence.replace(ure: Ure, replacement: String): String = ure.compile().replace(this, replacement)
fun CharSequence.replaceFirst(ure: Ure, replacement: String): String = ure.compile().replaceFirst(this, replacement)
fun CharSequence.findAll(ure: Ure, startIndex: Int = 0) = ure.compile().findAll(this, startIndex)
fun CharSequence.find(ure: Ure, startIndex: Int = 0) = ure.compile().find(this, startIndex)
fun CharSequence.matchEntire(ure: Ure) = ure.compile().matchEntire(this)

@NotPortableApi("Not all platforms support retreiving groups by name.")
operator fun MatchResult.get(name: String) = namedValues[name] ?: error("Group named \"$name\" not found in MatchResult.")

@NotPortableApi("Not all platforms support retreiving groups by name.")
operator fun MatchResult.getValue(thisObj: Any?, property: KProperty<*>) = get(property.name)

// FIXME_someday: this is hack, but I can't reliably get named groups from MatchResult (at least in multiplatform)
// TRACK: https://youtrack.jetbrains.com/issue/KT-51908
// see also:
//    https://youtrack.jetbrains.com/issue/KT-41890
//    https://youtrack.jetbrains.com/issue/KT-29241/Unable-to-use-named-Regex-groups-on-JDK-11
//    https://youtrack.jetbrains.com/issue/KT-20865/Retrieving-groups-by-name-is-not-supported-on-Java-9-even-with-kotlin-stdlib-jre8-in-the-classpath
//    https://github.com/JetBrains/kotlin/commit/9c4c1ed557a889bf57c754b81f4897a0d8405b0d
@NotPortableApi("Not all platforms support retreiving groups by name.")
val MatchResult.named get() = groups as? MatchNamedGroupCollection
    ?: throw UnsupportedOperationException("Retrieving groups by name is not supported on this platform.")

@NotPortableApi("Not all platforms support retreiving groups by name.")
val MatchResult.namedValues: Map<String, String?> get() = MatchNamedValues(named)

@JvmInline
value class MatchNamedValues internal constructor(private val groups: MatchNamedGroupCollection): Map<String, String?> {
    override val entries: Set<Map.Entry<String, String?>> get() = error("Operation not implemented.")
    override val keys: Set<String> get() = error("Operation not implemented.")
    override val size: Int get() = groups.size
    override val values: Collection<String?> get() = groups.map { it?.value }

    override fun isEmpty(): Boolean = groups.isEmpty()
    override fun get(key: String): String? = groups[key]?.value
    override fun containsValue(value: String?): Boolean = error("Operation not implemented.")
    override fun containsKey(key: String): Boolean = error("Operation not implemented.")
}