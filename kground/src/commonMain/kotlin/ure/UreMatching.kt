package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.regex.*
import kotlin.jvm.JvmInline
import kotlin.reflect.*


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

fun Ure.replaceFirstOrNone(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = compile().replaceFirstOrNone(input, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun Ure.replaceAll(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = compile().replaceAll(input, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun Ure.replaceAll(input: CharSequence, transform: (MatchResult) -> CharSequence): String =
    compile().replaceAll(input, transform)

fun Ure.replaceSingle(
    input: CharSequence,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = compile().replaceSingle(input, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)


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

fun CharSequence.replaceFirstOrNone(
    ure: Ure,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = ure.replaceFirstOrNone(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun CharSequence.replaceAll(
    ure: Ure,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = ure.replaceAll(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)

fun CharSequence.replaceAll(ure: Ure, transform: (MatchResult) -> CharSequence): String =
    ure.replaceAll(this, transform)

fun CharSequence.replaceSingle(
    ure: Ure,
    replacement: String,
    vararg useNamedArgs: Unit,
    escapeGroupRefs: Boolean = false,
    allowGroupRefs: Boolean = false,
) = ure.replaceSingle(this, replacement, escapeGroupRefs = escapeGroupRefs, allowGroupRefs = allowGroupRefs)



@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
operator fun MatchResult.get(name: String) = namedValues[name] ?: bad { "Group named \"$name\" not found in MatchResult." }

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
operator fun MatchResult.getValue(thisObj: Any?, property: KProperty<*>) = get(property.name)

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
val MatchResult.named get() = groups as? MatchNamedGroupCollection
    ?: throw UnsupportedOperationException("Retrieving groups by name is not supported on this platform.")

@NotPortableApi("Not guaranteed to work on all platforms.") // but it is currently working on platforms I unit-test.
val MatchResult.namedValues: Map<String, String?> get() = MatchNamedValues(named)

@JvmInline
value class MatchNamedValues internal constructor(private val groups: MatchNamedGroupCollection): Map<String, String?> {
    override val entries: Set<Map.Entry<String, String?>> get() = bad { "Operation not implemented." }
    override val keys: Set<String> get() = bad { "Operation not implemented." }
    override val size: Int get() = groups.size
    override val values: Collection<String?> get() = groups.map { it?.value }

    override fun isEmpty(): Boolean = groups.isEmpty()
    override fun get(key: String): String? = groups[key]?.value
    override fun containsValue(value: String?): Boolean = bad { "Operation not implemented." }
    override fun containsKey(key: String): Boolean = bad { "Operation not implemented." }
}

