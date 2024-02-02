@file:Suppress("SpellCheckingInspection")

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
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


// region [Ure Core Classes]

/** IR is the traditional regular expression - no human should read - kind of "intermediate representation" */
@JvmInline
value class IR @DelicateApi internal constructor(val str: String) {
    override fun toString(): String = str
}

sealed interface Ure {

    fun toIR(): IR

    /**
     * Optionally wraps in a non-capturing group before generating IR, so it's safe to use with quantifiers, alternations, etc.
     * Wrapping is done only when needed. For example, [UreConcatenation] with more than one element is wrapped.
     * (UreConcatenation with zero elements also is wrapped - so f. e. external UreQuantif only catches empty concatenation)
     */
    fun toClosedIR(): IR

    /**
     * It sets MULTILINE by default.
     * Also, I decided NOT to use DOT_MATCHES_ALL by default. Let's keep the ".": [chAnyInLine] as single line matcher.
     * Let's use explicit [chAnyAtAll] instead of changing the "." meaning all the time.
     * IMPORTANT:
     * We assume in all normal val/fun ureSth... That DOT_MATCHES_ALL is DISABLED, and MULTILINE is ENABLED,
     * so we don't have to enable/disable it all the time "just to make sure".
     */
    @OptIn(DelicateApi::class, NotPortableApi::class)
    fun compile() = compileWithOptions(MULTILINE)

    @DelicateApi("Usually code using Ure assumes default options, so changing options can create hard to find issues.")
    @NotPortableApi("Some options work only on some platforms. Check docs for each used platform.")
    fun compileWithOptions(vararg options: RegexOption) = Regex(toIR().str, options.toSet())
}

/** https://www.regular-expressions.info/brackets.html */
sealed interface UreNonCapturing: Ure

/** https://www.regular-expressions.info/brackets.html */
sealed interface UreCapturing: Ure

/** https://www.regular-expressions.info/brackets.html */
sealed interface UreNumbered: UreCapturing

/**
 * Named group is also automatically numbered (in most implementations),
 * but different regex implementations can number them differently.
 * So better not to mix [UreNamedGroup] with [UreNumberedGroup] in one [Ure],
 * but choose one way of capturing. See "Numbers for Named Capturing Groups" here:
 * https://www.regular-expressions.info/named.html
 */
sealed interface UreNamed: UreNumbered

/**
 * The first way it matches becomes the only way (backtracking info gets removed if there was any).
 * So no trying if it could eat more or less chars (in particular place) when sth later failed.
 * [UreAtomic] only means that this [Ure] is known to be atomic. Some others can also in practice be atomic.
 * https://www.regular-expressions.info/atomic.html
 */
sealed interface UreAtomic: UreNonCapturing

/** https://www.regular-expressions.info/anchors.html */
sealed interface UreAnchor: UreAtomic

/**
 * Also known as a character set: https://www.regular-expressions.info/charclass.html
 * Note:
 *   It can sometimes match more than one char technically.
 *   For example, most (all?) emoji "code points" take two "code units" (16b chars).
 *   Such 32b encoding is also called "surrogate pair".
 */
sealed interface UreCharClass: UreAtomic {
    fun toIRInCharClass(): IR
}

@JvmInline
value class UreConcatenation internal constructor(val tokens: MutableList<Ure> = mutableListOf()) : UreNonCapturing {
    // TODO_someday: make tokens publicly List<Ure>, when kotlin have this feature:
    // https://youtrack.jetbrains.com/issue/KT-14663/Support-having-a-public-and-a-private-type-for-the-same-property

    private val debugWithClosedTokens: Boolean get() = false
        // In case of difficult issues: try to temporarily change it to true see if matching changes somehow.

    override fun toIR(): IR = when (tokens.size) {
        0 -> "".asIR
        else -> tokens.joinToString("") {
            if ((it is UreAlternation) or (it is UreWithRawIR) or debugWithClosedTokens) it.toClosedIR().str else it.toIR().str
        }.asIR
    }

    override fun toClosedIR() = when (tokens.size) {
        1 -> tokens[0].toClosedIR()
        else -> this.groupNonCapt().toIR() // In case 0, we also want to wrap it in groupNonCapt!
        // To avoid issues when outside operator captures something else instead of empty concatenation.
        // I decided NOT to throw IllegalStateError in case 0, so we can always monitor IR in half-baked UREs.
        // (Like when creating UREs with some @Composable UI)
    }

    // Can't decide if this syntax is better in case of "1 of ..."; let's leave it for now.
    // TODO_later: rethink syntax when context receivers become multiplatform.
    //   Maybe somehow force '+' in other cases too, but I don't want to force some syntax with additional parentheses.
    operator fun Ure.unaryPlus() { tokens.add(this) }

    class UreX internal constructor(val times: IntRange, val reluctant: Boolean, val possessive: Boolean)

    fun x(times: IntRange, reluctant: Boolean = false, possessive: Boolean = false) = UreX(times, reluctant, possessive)
    fun x(times: Int) = x(times..times)

    infix fun UreX.of(ure: Ure) {
        tokens.add(ure.times(times, reluctant, possessive))
    }

    infix fun UreX.of(init: UreConcatenation.() -> Unit) {
        this of ure(init = init)
    }

    infix fun IntRange.of(ure: Ure) = x(this) of ure
    infix fun Int.of(ure: Ure) = x(this) of ure
    infix fun IntRange.of(init: UreConcatenation.() -> Unit) = x(this) of init
    infix fun Int.of(init: UreConcatenation.() -> Unit) = x(this) of init
}

data class UreAlternation internal constructor(val first: Ure, val second: Ure) : UreNonCapturing {
    override fun toIR() = "${first.toClosedIR()}|${second.toClosedIR()}".asIR
    override fun toClosedIR() = this.groupNonCapt().toIR()
}

sealed interface UreGroup : Ure {
    val content: Ure
    private val contentIR get() = content.toIR()
    val typeIR: IR // it's not full IR but just the part that signifies the type of group

    override fun toIR(): IR = "($typeIR$contentIR)".asIR
    // it looks like all possible typeIR prefixes cannot be confused with first contentIR characters.
    // (meaning: RE designers thought about it, so I don't have to be extra careful here.)

    override fun toClosedIR() = toIR() // group is always "closed" - has parentheses outside
}

data class UreNamedGroup internal constructor(override val content: Ure, val name: String) : UreGroup, UreNamed {
    override val typeIR get() = "?<$name>".asIR
}

@JvmInline
value class UreNonCapturingGroup internal constructor(override val content: Ure) : UreGroup, UreNonCapturing {
    override val typeIR get() = "?:".asIR
}

@JvmInline
value class UreNumberedGroup internal constructor(override val content: Ure) : UreGroup, UreNumbered {
    override val typeIR get() = "".asIR
}


/** https://www.regular-expressions.info/atomic.html */
@JvmInline
value class UreAtomicGroup internal constructor(override val content: Ure) : UreGroup, UreAtomic {
    override val typeIR get() = "?>".asIR
}

data class UreChangeOptionsGroup @DelicateApi @NotPortableApi internal constructor(
    override val content: Ure,
    val enable: Set<RegexOption> = emptySet(),
    val disable: Set<RegexOption> = emptySet(),
) : UreGroup, UreNonCapturing {
    init {
        req((enable intersect disable).isEmpty()) { "Can not enable and disable the same option at the same time" }
        req(enable.isNotEmpty() || disable.isNotEmpty()) { "No options provided" }
    }

    override val typeIR get() = "?${enable.ir}-${disable.ir}:".asIR // TODO_later: check if either set can be empty

    @Suppress("GrazieInspection")
    private val RegexOption.code
        get() = when (this) {
            // Note: Kotlin stdlib RegexOption will probably evolve, so I'll enable more options here in the future.
            // See also: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
            IGNORE_CASE -> "i"
            MULTILINE -> "m"
            // LITERAL -> bad { "Looks like not even supported by kotlin stdlib RegexOption in common code." }
            // UNIX_LINES -> "d" // bad { "Looks like not even supported by kotlin stdlib RegexOption in common code." }
            // COMMENTS -> "x" // bad { "not really supported... maybe in UreIR, but I wouldn't use it." }
            // DOT_MATCHES_ALL -> "s" // bad { "Looks like not even supported by kotlin stdlib RegexOption in common code." }
            // CANON_EQ -> bad { "Looks like not even supported by kotlin stdlib RegexOption." }
            // UNICODE_CASE -> "u" // bad { "Looks like not even supported by kotlin stdlib RegexOption in common code." }
            else -> bad { "RegexOption: $this is not supported." }
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
) : UreGroup, UreAnchor {
    override val typeIR
        get() = when (ahead to positive) {
            true to true -> "?="
            true to false -> "?!"
            false to true -> "?<="
            false to false -> "?<!"
            else -> error("Impossible case")
        }.asIR
    @OptIn(DelicateApi::class, NotPortableApi::class)
    operator fun not() = UreLookGroup(content, ahead, !positive)
}


data class UreGroupRef internal constructor(val nr: Int? = null, val name: String? = null) : UreAtomic {
    init {
        nr == null || name == null || error("Can not reference capturing group by both nr ($nr) and name ($name)")
        nr == null && name == null && error("Either nr or name has to be provided for the group reference")
    }

    override fun toIR(): IR = if (nr != null) "\\$nr".asIR else "\\k<$name>".asIR
    override fun toClosedIR(): IR = toIR()
}

/**
 * By default, it's "greedy" - tries to match as many "times" as possible, but backs off one by one when about to fail.
 * @param times - Uses shorter notation when appropriate, like: 0..1 -> "?"; 0..MAX -> "*"; 1..MAX -> "+"
 * @param reluctant - Tries to eat as little "times" as possible. Opposite to default "greedy" behavior.
 * @param possessive - It's like more greedy than default greedy. Never backtracks - fails instead. Just as [UreAtomicGroup].
 */
data class UreQuantifier internal constructor(
    val content: Ure,
    val times: IntRange,
    val reluctant: Boolean = false,
    val possessive: Boolean = false,
) : UreNonCapturing {
    init {
        reluctant && possessive && error("UreQuantif can't be reluctant and possessive at the same time")
    }

    val greedy get() = !reluctant && !possessive
    override fun toIR(): IR {
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
        }.asIR
        val suffixIR = when {
            reluctant -> "?"
            possessive -> "+"
            else -> ""
        }.asIR
        return "${content.toClosedIR()}$timesIR$suffixIR".asIR
    }
    // override fun toClosedIR(): IR = this.groupNonCapt().toIR()
    override fun toClosedIR() = toIR()
        // TODO: I think it's correct, but should be analyzed more carefully (and write some tests!).
}

/**
 * Represents exactly one character (code point in Unicode). Will be automatically escaped if needed.
 * @param str can contain more than one jvm char in cases when one codepoint in utf16 takes more than one char,
 * but it does not accept regexes representing special characters, like "\\t", or "\\n" - use single backslash,
 * so kotlin compiler changes "\n" into actual newline character, etc;
 * UreCharExact.toIR() will recreate necessary regex (like \n or \x{h...h}) for weird characters.
 */
@JvmInline value class UreCharExact @DelicateApi internal constructor(val str: String) : UreCharClass {
    init {
        req(str.isNotEmpty()) { "Empty char point." }
        req(str.isSingleCharacter) { "Looks like more than one char point." }
    }
    override fun toClosedIR() = toIR()
    override fun toIR(): IR = toIR(str[0].isMeta)
    override fun toIRInCharClass(): IR = toIR(str[0].isMetaInCharClass)

    @OptIn(ExperimentalStdlibApi::class)
    private fun toIR(justQuote: Boolean): IR = when {
        justQuote -> "\\$str"
        str == "\t" -> "\\t" // tab
        str == "\n" -> "\\n" // newline
        str == "\r" -> "\\r" // carriage-return
        str == "\u000C" -> "\\f" // form-feed
        str == "\u0007" -> "\\a" // alert bell
        str == "\u001B" -> "\\e" // escape
        // TODO_someday: what with control characters? \cx
        str.isSafeExactCharInRegex -> str

        else -> "\\x{${str[0].code.toHexString()}}".also {
            req(str.length == 1) { "Looks like surrogate pair. Unfortunately not supported yet." }
            // (waiting for better codepoints support in kotlin common stdlib)
            // https://youtrack.jetbrains.com/issue/KT-23251/Extend-Unicode-support-in-Kotlin-common
        }
    }.asIR

    companion object {
        private val reAnyAtAll = Regex("[\\s\\S]") // for performance, also cannot use chAnyAtAll in initialization
        private val String.isSingleCharacter get() = reAnyAtAll.matches(this)
        private val String.isSafeExactCharInRegex get() = length == 1
        // FIXME This is temporary and very wrong implementation.
        //   (but at least we use it only when we already know it is single character and not meta char)
        //   It should accept some multichar points, it shoud actually check if it's save to put in regex as-is.
        //   (waiting for better codepoints support in kotlin common stdlib)
        //   https://youtrack.jetbrains.com/issue/KT-23251/Extend-Unicode-support-in-Kotlin-common
    }
}

private val Char.isMeta get() = this in "\\[].^\$?*+{}|()" // https://www.regular-expressions.info/characters.html

private val Char.isMetaInCharClass get() = this in "\\[]^-" // https://www.regular-expressions.info/charclass.html

/**
 * https://www.regular-expressions.info/anchors.html
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#bounds
 */
@JvmInline value class UreAnchorPreDef @NotPortableApi internal constructor(val name: Char) : UreAnchor {
    init { req(name.isNameOfAnchorPreDef) { "Incorrect name of predefined anchor: $name" } }
    override fun toIR(): IR = if (name in "^$") "$name".asIR else "\\$name".asIR
    override fun toClosedIR(): IR = toIR()

    @OptIn(NotPortableApi::class) operator fun not() =
        if (name in "bB") UreAnchorPreDef(name.switchCase()) else bad { "The anchor: $name can't be negated." }

    companion object {
        private val Char.isNameOfAnchorPreDef get() = this in "^\$bBAGZz"
    }
}


/**
 * Also known as shorthand character set
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#predef
 * https://www.regular-expressions.info/shorthand.html
 */
@JvmInline value class UreCharClassPreDef @DelicateApi internal constructor(val name: Char) : UreCharClass {
    init { req(name.isNameOfPreDefCC) { "Incorrect name of predefined character class: $name" } }
    override fun toIR(): IR = if (name == '.') "$name".asIR else "\\$name".asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = toIR()

    @OptIn(DelicateApi::class) operator fun not() =
        if (name == '.') bad { "The chAnyInLine can't be negated." }
        else UreCharClassPreDef(name.switchCase())

    companion object {
        private val Char.isNameOfPreDefCC get() = this in ".dDhHsSvVwW"
    }
}


// TODO: test complex unions and intersections like [abc[^def]], [abc&&[c-x]], etc

data class UreCharClassUnion internal constructor(val tokens: List<UreCharClass>, val positive: Boolean = true) : UreCharClass {
    init { req(tokens.isNotEmpty()) { "No tokens in UreCharClassUnion." } }
    override fun toIR(): IR = if (tokens.size == 1 && positive) tokens[0].toIR()
        else tokens.joinToString("", if (positive) "[" else "[^", "]") { it.toIRInCharClass().str }.asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = tokens.joinToString("", if (positive) "" else "[^", if (positive) "" else "]") { it.toIRInCharClass().str }.asIR
        // TODO: I don't wrap in [] here (when positive) to see if it works,
        //  but make sure to analyze all cases and write unit tests!!
    operator fun not() = UreCharClassUnion(tokens, !positive)
}

// TODO_later: analyze if some special kotlin progression/range would fit here better
data class UreCharClassRange @NotPortableApi constructor(val from: UreCharClass, val to: UreCharClass, val positive: Boolean = true) : UreCharClass {
    private val neg = if (positive) "" else "^"
    override fun toClosedIR(): IR = toIR()
    override fun toIR(): IR = "[${toIRInCharClass()}]".asIR
    override fun toIRInCharClass(): IR = "$neg${from.toIRInCharClass()}-${to.toIRInCharClass()}".asIR
        // TODO: I don't wrap in [] here to see if it works, but make sure to analyze all cases and write unit tests!!
    @OptIn(NotPortableApi::class)
    operator fun not() = UreCharClassRange(from, to, !positive)
}

// TODO NOW: test it!
data class UreCharClassIntersect internal constructor(val tokens: List<UreCharClass>, val positive: Boolean = true) : UreCharClass {
    override fun toIR(): IR = tokens.joinToString("&&", if (positive) "[" else "[^", "]") { it.toIRInCharClass().str }.asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = toIR()
        // FIXME_later: maybe I can sometimes drop [] wrapping, but first analyze all cases and write unit tests.
    operator fun not() = UreCharClassIntersect(tokens, !positive)
}

// TODO_later: make it not portable and opt in when using "constructor" that checks if prop is known and portable.
data class UreCharClassProp @NotPortableApi internal constructor(val prop: String, val positive: Boolean = true) : UreCharClass {
    override fun toIR(): IR = "\\${if (positive) "p" else "P"}{$prop}".asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = toIR()
    @OptIn(NotPortableApi::class)
    operator fun not() = UreCharClassProp(prop, !positive)
}

/** Dirty way to inject whole regexes fast. Avoid if possible. */
@JvmInline value class UreWithRawIR @DelicateApi @NotPortableApi internal constructor(val ir: IR) : Ure {
    override fun toIR(): IR = ir
    override fun toClosedIR(): IR = if (isClosed) ir else this.groupNonCapt().toIR()
    private val isClosed get() = when {
        ir.str.length == 1 -> true
        ir.str.length == 2 && ir.str[0] == '\\' -> true
        else -> false
    }
    // TODO_someday: analyze more carefully and drop grouping when actually not needed.
}

@JvmInline value class UreQuote @NotPortableApi internal constructor(val str: String) : UreAtomic {
    override fun toClosedIR(): IR = toIR()
    override fun toIR() = "\\Q$str\\E".asIR
}

/** Could be implemented as [UreConcatenation] of each character, but it's better to have a smaller tree. */
@JvmInline value class UreText internal constructor(val str: String) : UreAtomic {
    override fun toClosedIR(): IR = this.groupNonCapt().toIR()
    override fun toIR() = str.map { if (it.isMeta) "\\$it" else "$it" }.joinToString("").asIR
}

// endregion [Ure Core Classes]


// region [Ure Core DSL fun]

const val MAX = Int.MAX_VALUE

@OptIn(DelicateApi::class) private val String.asIR get() = IR(this)

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

fun Ure.withWordBoundaries(boundaryBefore: Boolean = true, boundaryAfter: Boolean = true) =
    withBoundaries(atWordBoundary.takeIf { boundaryBefore }, atWordBoundary.takeIf { boundaryAfter })

@SecondaryApi @NotPortableApi fun Ure.withBOEOWordBoundaries(boundaryBefore: Boolean = true, boundaryAfter: Boolean = true) =
    withBoundaries(atBOWord.takeIf { boundaryBefore }, atEOWord.takeIf { boundaryAfter })

fun Ure.withBoundaries(boundaryBefore: Ure? = null, boundaryAfter: Ure? = null) =
    if (boundaryBefore == null && boundaryAfter == null) this else ure {
        boundaryBefore?.let { + it }
        + this@withBoundaries // it should flatten if this is UreConcatenation (see UreConcatenation.toIR()) TODO_later: doublecheck
        boundaryAfter?.let { + it }
    }

infix fun Ure.or(that: Ure) = UreAlternation(this, that)
infix fun Ure.then(that: Ure) = UreConcatenation(mutableListOf(this, that))
// Do not rename "then" to "and". The "and" would suggest sth more like a special lookahead/lookbehind group


operator fun Ure.not(): Ure = when (this) {
    is UreWithRawIR -> when (this) {
        // TODO_someday: Can I negate some common raw ures?
        else -> error("This UreWithRawIR can not be negated")
    }
    is UreCharExact -> bad { "UreCharExact can not be negated" }
    is UreAnchorPreDef -> !this
    is UreCharClassPreDef -> !this
    is UreCharClassRange -> !this
    is UreCharClassUnion -> !this
    is UreCharClassIntersect -> !this
    is UreCharClassProp -> !this
    is UreGroup -> when (this) {
        is UreLookGroup -> !this
        else -> error("Unsupported UreGroup for negation: ${this::class.simpleName}")
    }

    is UreGroupRef -> error("UreGroupRef can not be negated")
    is UreConcatenation -> error("UreConcatenation can not be negated")
    is UreQuantifier -> error("UreQuantifier can not be negated")
    is UreQuote -> error("UreQuote can not be negated")
    is UreText -> error("UreText can not be negated")
    is UreAlternation -> error("UreAlternation can not be negated")
}
// TODO_someday: experiment more with different operators overloading,
//  especially indexed access operators and invoke operators.

@NotPortableApi @DelicateApi fun ureRaw(ir: IR) = UreWithRawIR(ir)
@NotPortableApi @DelicateApi fun ureRaw(str: String) = ureRaw(str.asIR)

/** Wraps the [text] with \Q...\E, so it's interpreted as exact text to match (no chars treated as special). */
@NotPortableApi fun ureQuote(text: String) = UreQuote(text)

/** Similar to [ureQuote] but quotes each character (which could be treated as special) separately with backslash */
fun ureText(text: String) = UreText(text)

// endregion [Ure Core DSL fun]


// region [Ure Character Related Stuff]

@OptIn(DelicateApi::class) fun ch(str: String) = UreCharExact(str)

fun ch(chr: Char) = ch(chr.toString())

@DelicateApi fun chPreDef(name: Char) = UreCharClassPreDef(name)


// Ure constants matching one char (special chars; common categories). All names start with ch.
// Note from experience: It's really more important to have a common prefix than to be a bit shorter.


// just private shortcuts
@OptIn(DelicateApi::class) private inline val String.ce get() = ch(this)
@OptIn(DelicateApi::class) private inline val Char.cpd get() = chPreDef(this)

val chSlash = "/".ce
val chBackSlash = "\\".ce

val chTab = "\t".ce
val chLF = "\n".ce
val chCR = "\r".ce
val chFF = "\u000C".ce
val chAlert = "\u0007".ce
val chEsc = "\u001B".ce

/** [a-z] */
val chLower = oneCharOf('a'..'z')
/** [A-Z] */
val chUpper = oneCharOf('A'..'Z')
/** [a-zA-Z] */
val chAlpha = oneCharOf(chLower, chUpper) // Note: "chLower or chUpper" is worse, because UreAlternation can't be negated.

/** Same as [0-9] */
val chDigit = 'd'.cpd

/** Same as [0-9] */
val chHexDigit = oneCharOf(chDigit, oneCharOf('a'..'f'), oneCharOf('A'..'F'))

/** Same as [a-zA-Z0-9] */
val chAlnum = oneCharOf(chAlpha, chDigit)

val chPunct = oneCharOfExact("""!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~""")

val chGraph = oneCharOf(chAlnum, chPunct)

val chSpace = " ".ce
val chWhiteSpace = 's'.cpd
val chWhiteSpaceInLine = oneCharOf(chSpace, chTab) // Note: "chSpace or chTab" is worse, because UreAlternation can't be negated.

/** Basic printable characters. Only normal space. No emojis, etc. */
val chPrint = oneCharOf(chGraph, chSpace)

/** Same as [^0-9] */
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chDigit"))
val chNonDigit = 'D'.cpd

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWhiteSpace"))
val chNonWhiteSpace = 'S'.cpd

val chDash = "-".ce

/**
 * Matches only the actual dot "." character.
 * Check [chAnyInLine] if you want regex that matches any character in line (represented by "." IR),
 * Check [chAnyAtAll] if you want regex that matches any character at all.
 */
val chDot = ".".ce

val chAnyInLine = '.'.cpd

/** [\s\S] It is a portable and fast way to match any character at all. */
val chAnyAtAll = oneCharOf(chWhiteSpace, !chWhiteSpace) // should work everywhere and should be fast.
// Note: following impl would not work on JS: ureIR("(?s:.)")
//   see details: https://www.regular-expressions.info/dot.html



/** Same as [a-zA-Z0-9_] */
val chWord = 'w'.cpd

/** Same as [^a-zA-Z0-9_] */
@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!chWord"))
val chNonWord = 'W'.cpd

val chWordOrDot = oneCharOf(chWord, chDot)
val chWordOrDash = oneCharOf(chWord, chDash) // also hints (when typing chWo) that chWord doesn't match dash.
val chWordOrDotOrDash = oneCharOf(chWord, chDot, chDash)

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


fun oneCharOf(charClasses: List<UreCharClass>) = UreCharClassUnion(charClasses)

fun oneCharOf(vararg charClasses: UreCharClass?) = oneCharOf(charClasses.toList().filterNotNull())

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!oneCharOf(charClasses)"))
fun oneCharNotOf(vararg charClasses: UreCharClass?) = !oneCharOf(*charClasses)

@OptIn(DelicateApi::class)
fun oneCharOfExact(exactChars: List<Char>) = oneCharOf(exactChars.map(::ch))

@OptIn(DelicateApi::class)
fun oneCharOfExact(exactChars: String) = oneCharOfExact(exactChars.toList())

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!oneCharOfExact(charClasses)"))
fun oneCharNotOfExact(exactChars: String) = !oneCharOfExact(exactChars)

@NotPortableApi
fun oneCharOfRange(from: UreCharClass, to: UreCharClass) = UreCharClassRange(from, to)

@OptIn(NotPortableApi::class)
fun oneCharOf(range: CharRange) = oneCharOfRange(ch(range.start), ch(range.endInclusive))

@SecondaryApi("Use operator fun Ure.not()", ReplaceWith("!oneCharOf(range)"))
fun oneCharNotOf(range: CharRange) = !oneCharOf(range)

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
fun Ure.groupAtomic() = UreAtomicGroup(this)

@OptIn(NotPortableApi::class, DelicateApi::class) // lookAhead should be safe; lookBehind is delicate/non-portable.
fun Ure.lookAhead(positive: Boolean = true) = UreLookGroup(this, true, positive)

@DelicateApi("Can be suprisingly slow for some ures. Or even can throw on some platforms, when looking behind for non-fixed length ure.")
@NotPortableApi("Behavior can differ on different platforms. Read docs for each used platform.")
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Lookbehind_assertion#description
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


@Deprecated("Let's try to use .times instead", ReplaceWith("content.times(times, reluctant, possessive)"))
fun quantify(content: Ure, times: IntRange, reluctant: Boolean = false, possessive: Boolean = false) =
    content.times(times, reluctant, possessive)

@Deprecated("Let's try to use .times instead", ReplaceWith("ure(init = init).times(times, reluctant, possessive)"))
fun quantify(times: IntRange, reluctant: Boolean = false, possessive: Boolean = false, init: UreConcatenation.() -> Unit) =
    ure(init = init).times(times, reluctant, possessive)

// endregion [Ure Quantifiers Related Stuff]


// region [Ure Match Related Stuff]

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

// endregion [Ure Match Related Stuff]
