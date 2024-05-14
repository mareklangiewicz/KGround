package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.regex.*
import kotlin.jvm.JvmInline
import kotlin.reflect.*
import kotlin.text.Regex.Companion.escapeReplacement
import pl.mareklangiewicz.bad.req

fun Ure.matchEntireOrNull(input: CharSequence) = compile().matchEntireOrNull(input)

fun Ure.matchEntireOrThrow(input: CharSequence) = compile().matchEntireOrThrow(input)

fun Ure.matchAtOrNull(input: CharSequence, index: Int) = compile().matchAtOrNull(input, index)

fun Ure.matchAtOrThrow(input: CharSequence, index: Int) = compile().matchAtOrThrow(input, index)

fun Ure.findFirstOrNull(input: CharSequence, startIndex: Int = 0) = compile().findFirstOrNull(input, startIndex)

fun Ure.findFirst(input: CharSequence, startIndex: Int = 0) = compile().findFirst(input, startIndex)

fun Ure.findSingle(input: CharSequence, startIndex: Int = 0) = compile().findSingle(input, startIndex)

fun Ure.findSingleWithOverlap(input: CharSequence, startIndex: Int = 0) =
  compile().findSingleWithOverlap(input, startIndex)

fun Ure.findAll(input: CharSequence, startIndex: Int = 0) = compile().findAll(input, startIndex)

fun Ure.findAllWithOverlap(input: CharSequence, startIndex: Int = 0) = compile().findAllWithOverlap(input, startIndex)


// Note: UReplacement depends only on Regex and not Ure, but I want it here in pl.mareklangiewicz.ure package,
// because it will almost always be used with Ure replacements.
@Suppress("FunctionName")
@JvmInline
value class UReplacement private constructor(val raw: String) {
  companion object {
    /** Escapes provided replacement string with [escapeReplacement] so it's always treated literally. */
    fun Literal(literalReplacement: String) = UReplacement(escapeReplacement(literalReplacement))
    /**
     * Replacements have different rules/special constructs than regular expressions.
     * Group references are done with $nr or ${name} instead of \nr or \k<name> as in regexes
     * Also there is \ escaping special constructs in "replacement syntax".
     * See [Regex.replaceFirst] and [Regex.escapeReplacement] kdoc for more details.
     */
    @DelicateApi("Special constructs for replacement string. Can be really complex in practice - layers of escaping.")
    fun Advanced(rawReplacement: String) = UReplacement(rawReplacement)

    val Empty = Literal("")

    /** Usually doesn't make sense to replace with the same string, but can be useful for debugging or experimenting */
    val Same = Group(0)

    @OptIn(DelicateApi::class)
    fun Group(nr: Int) = Advanced("\$$nr") // TODO_someday: chk if nr is not strange

    @OptIn(DelicateApi::class)
    fun Group(name: String) = Advanced("\${$name}") // TODO_someday: chk if name doesn't have forbidden chars
  }

  @ExperimentalApi("TODO: Implement correct checks for all edge cases (with less false positives)")
  operator fun plus(that: UReplacement) = UReplacement(raw + that.raw).also {
    req(raw.last() !in "$\\") { "Concatenation with first part ending with \"${raw.last()}\" is not safe." }
    req(!raw.last().isDigit() || !that.raw.first().isDigit()) { "Concatenation with numbers in the middle is not safe." }
  }
}


fun Ure.replaceFirstOrNone(input: CharSequence, replacement: UReplacement) =
  compile().replaceFirstOrNone(input, replacement)

fun Ure.replaceAll(input: CharSequence, replacement: UReplacement) = compile().replaceAll(input, replacement)

fun Ure.replaceAll(input: CharSequence, transform: (MatchResult) -> UReplacement): String =
  compile().replaceAll(input, transform)

fun Ure.replaceSingle(input: CharSequence, replacement: UReplacement) = compile().replaceSingle(input, replacement)


fun CharSequence.matchEntireOrNull(ure: Ure) = ure.matchEntireOrNull(this)

fun CharSequence.matchEntireOrThrow(ure: Ure) = ure.matchEntireOrThrow(this)

fun CharSequence.matchAtOrNull(ure: Ure, index: Int) = ure.matchAtOrNull(this, index)

fun CharSequence.matchAtOrThrow(ure: Ure, index: Int) = ure.matchAtOrThrow(this, index)

fun CharSequence.findFirstOrNull(ure: Ure, startIndex: Int = 0) = ure.findFirstOrNull(this, startIndex)

fun CharSequence.findFirst(ure: Ure, startIndex: Int = 0) = ure.findFirst(this, startIndex)

fun CharSequence.findSingle(ure: Ure, startIndex: Int = 0) = ure.findSingle(this, startIndex)

fun CharSequence.findSingleWithOverlap(ure: Ure, startIndex: Int = 0) = ure.findSingleWithOverlap(this, startIndex)

fun CharSequence.findAll(ure: Ure, startIndex: Int = 0) = ure.findAll(this, startIndex)

fun CharSequence.findAllWithOverlap(ure: Ure, startIndex: Int = 0) = ure.findAllWithOverlap(this, startIndex)

fun CharSequence.replaceFirstOrNone(ure: Ure, replacement: UReplacement) = ure.replaceFirstOrNone(this, replacement)

fun CharSequence.replaceAll(ure: Ure, replacement: UReplacement) = ure.replaceAll(this, replacement)

fun CharSequence.replaceAll(ure: Ure, transform: (MatchResult) -> UReplacement): String =
  ure.replaceAll(this, transform)

fun CharSequence.replaceSingle(ure: Ure, replacement: UReplacement) = ure.replaceSingle(this, replacement)



@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
operator fun MatchResult.get(name: String) =
  namedValues[name] ?: bad { "Group named \"$name\" not found in MatchResult." }

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
operator fun MatchResult.getValue(thisObj: Any?, property: KProperty<*>) = get(property.name)

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
val MatchResult.named
  get() = groups as? MatchNamedGroupCollection
    ?: throw UnsupportedOperationException("Retrieving groups by name is not supported on this platform.")

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
val MatchResult.namedValues: Map<String, String?> get() = MatchNamedValues(named)

@JvmInline
value class MatchNamedValues internal constructor(private val groups: MatchNamedGroupCollection) :
  Map<String, String?> {
  override val entries: Set<Map.Entry<String, String?>> get() = bad { "Operation not implemented." }
  override val keys: Set<String> get() = bad { "Operation not implemented." }
  override val size: Int get() = groups.size
  override val values: Collection<String?> get() = groups.map { it?.value }

  override fun isEmpty(): Boolean = groups.isEmpty()
  override fun get(key: String): String? = groups[key]?.value
  override fun containsValue(value: String?): Boolean = bad { "Operation not implemented." }
  override fun containsKey(key: String): Boolean = bad { "Operation not implemented." }
}

