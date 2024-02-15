package pl.mareklangiewicz.ure.core


import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.text.*
import pl.mareklangiewicz.ure.*
import kotlin.jvm.JvmInline
import kotlin.text.RegexOption.*



/** IR is the traditional regular expression - no human should read - kind of "intermediate representation" */
@JvmInline
value class IR @DelicateApi internal constructor(val str: String) {
    override fun toString(): String = str
}


@OptIn(DelicateApi::class) private val String.asIR get() = IR(this)

/**
 * General info about Ure (Micro Regular Expressions):
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
@NotPortableApi("Does NOT even compile (Ure.compile) on JS.")
value class UreAtomicGroup internal constructor(override val content: Ure) : UreGroup, UreAtomic {
    override val typeIR get() = "?>".asIR
}

sealed class UreChangeOptions @DelicateApi @NotPortableApi protected constructor(
): UreNonCapturing {
    abstract val enable: Set<RegexOption>
    abstract val disable: Set<RegexOption>

    // run it in init of final class
    protected fun reqCorrectOptions() {
        req((enable intersect disable).isEmpty()) { "Can not enable and disable the same option at the same time" }
        req(enable.isNotEmpty() || disable.isNotEmpty()) { "No options provided" }
    }

    @Suppress("GrazieInspection")
    protected val RegexOption.code
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

    private val oec get() = enable.code
    private val odc get() = disable.code.let { if (it.isEmpty()) it else "-$it" }
    private val Set<RegexOption>.code get() = joinToString("") { it.code }

    protected val optionsCode get() = "$oec$odc"
}

data class UreChangeOptionsGroup @DelicateApi @NotPortableApi internal constructor(
    override val content: Ure,
    override val enable: Set<RegexOption> = emptySet(),
    override val disable: Set<RegexOption> = emptySet(),
) : UreChangeOptions(), UreGroup {
    init { reqCorrectOptions() }
    override val typeIR get() = "?$optionsCode:".asIR
}

/**
 * Changes regex options ([RegexOption]) from this point ahead. Very problematic construct.
 * It is much safer to use [UreChangeOptionsGroup] instead of [UreChangeOptionsAhead].
 * Or even safer not to change options at all, so all [Ure]s are interpreted the same way.
 */
@DelicateApi("Makes the whole Ure very difficult to analyze.", ReplaceWith("UreChangingOptionsGroup"))
@SecondaryApi("Use UreChangingOptionsGroup", ReplaceWith("UreChangingOptionsGroup"))
@NotPortableApi("Does NOT even compile (Ure.compile) on JS.", ReplaceWith("UreChangingOptionsGroup"))
data class UreChangeOptionsAhead internal constructor(
    override val enable: Set<RegexOption> = emptySet(),
    override val disable: Set<RegexOption> = emptySet(),
) : UreChangeOptions(), UreNonCapturing {
    init { reqCorrectOptions() }
    override fun toClosedIR(): IR = toIR()
    override fun toIR(): IR = "(?$optionsCode)".asIR
}


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
            else -> bad { "Impossible case" }
        }.asIR
    @OptIn(DelicateApi::class, NotPortableApi::class)
    operator fun not() = UreLookGroup(content, ahead, !positive)
}


data class UreGroupRef internal constructor(val nr: Int? = null, val name: String? = null) : UreAtomic {
    init {
        nr == null || name == null || bad { "Can not reference capturing group by both nr ($nr) and name ($name)" }
        nr == null && name == null && bad { "Either nr or name has to be provided for the group reference" }
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
        reluctant && possessive && bad { "UreQuantifier can't be reluctant and possessive at the same time" }
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
                MAX -> "{${times.first},}" // Note: skipping min is not implicit 0, it's an incorrect syntax.
                else -> "{${times.first},${times.last}}"
            }
        }.asIR
        val suffixIR = when {
            reluctant -> "?"
            possessive -> "+"
            greedy -> ""
            else -> bad { "impossible" }
        }.asIR
        return "${content.toClosedIR()}$timesIR$suffixIR".asIR
    }
    override fun toClosedIR() = this.groupNonCapt().toIR()
        // has to be wrapped, because stacking quantifiers doesn't compile in different cases, especially on JS
        // (see TestUreQuantifiersEtc.kt: "dangling quantifiers", etc.)
        // TODO_someday: Optimize: carefully multiply min and max when content is also UreQuantifier
}

/**
 * Represents exactly one character (code point in Unicode). Will be automatically escaped if needed.
 * @param str can contain more than one jvm char in cases when one codepoint in utf16 takes more than one char,
 * but it does not accept regexes representing special characters, like "\\t", or "\\n" - use single backslash,
 * so kotlin compiler changes "\n" into actual newline character, etc;
 * UreCharExact.toIR() will recreate necessary regex (like \n or \x{hhhhh}) for weird characters.
 * Only surrogate pair case is not portable. It compiles to \x{hhhhh} which works only on JVM.
 * Note: On JS there is \u{hhhhh} syntax instead of \x{hhhhh},
 * but I really don't want to create different IR for different platforms.
 * It all should be the same common implementation, except actual regex matching (which is outside Ure).
 * (usecases like: tool on website creating IR to copy&paste to different places)
 */
@JvmInline value class UreCharExact @NotPortableApi internal constructor(val str: String) : UreCharClass {
    init {
        req(str.isNotEmpty()) { "Empty char point." }
        req(str.isSingleUnicodeCharacter) { "Looks like more than one char point." }
    }
    override fun toClosedIR() = toIR()
    override fun toIR(): IR = toIR(str[0].isMeta)
    override fun toIRInCharClass(): IR = toIR(str[0].isMetaInCharClass)

    @OptIn(ExperimentalStdlibApi::class)
    private fun toIR(justQuote: Boolean): IR = when {
        justQuote -> "\\$str" // below, we know it's not meta-like character in this context
        str == "\t" -> "\\t" // tab
        str == "\n" -> "\\n" // newline
        str == "\r" -> "\\r" // carriage-return
        str == "\u000C" -> "\\f" // form-feed
        str == "\u0007" -> "\\a" // alert bell
        str == "\u001B" -> "\\e" // escape
        // Note: other ascii control chars are encoded below as "\\x$hex" which is fine.
        str.length == 1 && str[0].isAsciiPrintable -> str
            // TODO_someday: make sure all ascii printable are fine (we already checked it's not meta in this context).
        else -> {
            val p = str.toSingleCodePoint()
            when {
                p < 0x100 -> "\\x${p.toUByte().toHexString()}" // ascii control chars are also represented this way
                p < 0x10000 -> "\\u${p.toUShort().toHexString()}"
                else -> "\\x{${p.toHexString(HexFormat { number { removeLeadingZeros = true } })}}"
            }
        }
    }.asIR

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


data class UreCharClassUnion @NotPortableApi internal constructor(val tokens: List<UreCharClass>, val positive: Boolean = true) : UreCharClass {
    init { req(tokens.isNotEmpty()) { "No tokens in UreCharClassUnion." } }
    override fun toIR(): IR = if (tokens.size == 1 && positive) tokens[0].toIR()
        else tokens.joinToString("", if (positive) "[" else "[^", "]") { it.toIRInCharClass().str }.asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = tokens.joinToString("", if (positive) "" else "[^", if (positive) "" else "]") { it.toIRInCharClass().str }.asIR
    @OptIn(NotPortableApi::class)
    operator fun not() = UreCharClassUnion(tokens, !positive)
}

// TODO_later: analyze if some special kotlin progression/range would fit here better
data class UreCharClassRange @NotPortableApi constructor(val from: UreCharClass, val to: UreCharClass, val positive: Boolean = true) : UreCharClass {
    override fun toClosedIR(): IR = toIR()
    override fun toIR(): IR = "[$content]".asIR
    override fun toIRInCharClass(): IR = if (positive) content.asIR else toIR()
    private val neg get() = if (positive) "" else "^"
    private val content get() = "$neg${from.toIRInCharClass()}-${to.toIRInCharClass()}"
    @OptIn(NotPortableApi::class)
    operator fun not() = UreCharClassRange(from, to, !positive)
}

/**
 * This class is not only not-portable, but also VERY DELICATE.
 * Please always write unit tests to make sure it behaves as expected on platforms you're using.
 * There are weird inconsistencies when regex engines interpret intersections of unions, negated intersections, etc etc.
 * Some are described here: https://www.regular-expressions.info/charclassintersect.html
 * Some are reproduced in fun testUreCharClasses in TestUreCharClasses.cmn.kt
 * Usual workaround for weird behavior is to wrap some parts in additional chOfAny(token).
 */
data class UreCharClassIntersect @NotPortableApi @DelicateApi internal constructor(val tokens: List<UreCharClass>, val positive: Boolean = true) : UreCharClass {
    override fun toIR(): IR = tokens.joinToString("&&", if (positive) "[" else "[^", "]") { it.toIRInCharClass().str }.asIR
    override fun toClosedIR(): IR = toIR()
    override fun toIRInCharClass(): IR = toIR() // this class is delicate enough, so let's not try to drop brackets here
    @NotPortableApi @DelicateApi
    operator fun not() = UreCharClassIntersect(tokens, !positive)
}

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

