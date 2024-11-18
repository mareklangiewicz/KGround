@file:Suppress("NOTHING_TO_INLINE", "unused")

package pl.mareklangiewicz.udata

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineName
import pl.mareklangiewicz.ulog.ULogEntry

/*
 * These cryptic short fun names like unt, tru, fls, strf (str too) are kind of experiment.
 * Experiment with treating some opinionated set of extensions/utils as DSL/keywords,
 * that should be short and memorized by user (instead of long and descriptive).
 * I'm very much aware it's against any normal coding convention. :)
 */

inline val Any?.unt get() = Unit
inline val Any?.tru get() = true
inline val Any?.fls get() = false

/** Full default string representation, as opposed to shortening str(...) flavors */
inline val Any.strf get() = toString()
// mostly to have same prefix as str(..), and not to have to READ and write so many parentheses everywhere

/** Full default string representation if not null. Null becomes empty string. */
inline val Any?.strfoe get() = this?.strf ?: ""
/** Full default string representation if not null. Null becomes short default representation "n". */
inline val Any?.strfon get() = this?.strf ?: STR_DEFAULT_NULL

/*
 * Micro string representations of common data types.
 * Doesn't have to be unique, doesn't have to be maximally precise.
 * Have to be consistent, short. Used mostly for logging (so short), but also in tests, so str representation
 * should be consistent (almost never changing).
 * Try not to change defaults, user should rely on generated strings being "short enough" / "micro".
 * TODO_someday: use context parameters when available.
 *
 * Intention is for every "app" / "consumer lib" to have own internal val Any?.str: String = when {...}
 * with conversions/defaults selected/adjusted for specific project/module and it's types.
 * BTW: when logging source code lines, set maxLength = 128 locally/manually
 * (128 is nice limit because it's common to set .editorconfig: max_line_length = 120)
 */


inline val STR_DEFAULT_MAX_LENGTH get() = 32
inline val STR_DEFAULT_MAX_INDICATOR get() = "\u2026" // so default is single unicode char representing ellipsis
inline val STR_DEFAULT_REAL_NUMBER_PRECISION get() = 2

// very short defaults especially nice for logging a lot of data structures with flags
inline val STR_DEFAULT_TRUE get() = "T"
inline val STR_DEFAULT_FALSE get() = "F"
inline val STR_DEFAULT_NULL get() = "n"
inline val STR_DEFAULT_NOT_NULL get() = "nn"

inline fun CharSequence.str(
  vararg useNamedArgs: Unit,
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
): String = if (length > maxLength) substring(0, maxLength - maxIndicator.length) + maxIndicator else strf

expect inline fun Number.str(
  vararg useNamedArgs: Unit,
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
  precision: Int = STR_DEFAULT_REAL_NUMBER_PRECISION,
): String

inline fun Boolean?.str(
  vararg useNamedArgs: Unit,
  strTrue: String = STR_DEFAULT_TRUE,
  strFalse: String = STR_DEFAULT_FALSE,
  strNull: String = STR_DEFAULT_NULL,
) = when (this) {
  true -> strTrue
  false -> strFalse
  null -> strNull
}

fun ULogEntry.str(
  startTime: ComparableTimeMark? = null,
  vararg useNamedArgs: Unit,
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
): String {
  // FIXME: this is fast temporary implementation. implement something more versatile/parametrized
  //   maybe even optional logging of job hierarchy?? nah...
  val name = context?.get(CoroutineName)?.name
  val elapsed: Duration? = if (startTime == null || time == null) null else time - startTime
  return lONN(elapsed, name, data.str(maxLength = maxLength, maxIndicator = maxIndicator))
    .joinToString(" ") // FIXME_later: joined str can be longer than maxLength
}


inline fun Any?.str(
  vararg useNamedArgs: Unit,
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
  precision: Int = STR_DEFAULT_REAL_NUMBER_PRECISION,
  strTrue: String = STR_DEFAULT_TRUE,
  strFalse: String = STR_DEFAULT_FALSE,
  strNull: String = STR_DEFAULT_NULL,
) = when (this) {
  is CharSequence -> str(maxLength = maxLength, maxIndicator = maxIndicator)
  is Number -> str(maxLength = maxLength, maxIndicator = maxIndicator, precision = precision)
  is Boolean? -> str(strTrue = strTrue, strFalse = strFalse, strNull = strNull)
  is ULogEntry -> str(maxLength = maxLength, maxIndicator = maxIndicator)
  else -> strf.str(maxLength = maxLength, maxIndicator = maxIndicator)
}


inline fun Any?.strIfNullOrNot(
  vararg useNamedArgs: Unit,
  strNotNull: String = STR_DEFAULT_NOT_NULL,
  strNull: String = STR_DEFAULT_NULL,
): String = if (this != null) strNotNull else strNull



/*
 * Some shortcuts for most common lists/maps creation.
 * Might be changed/removed when we finally get proper collections literals in kotlin.
 * Naming:
 * Common prefix "l", so I don't pollute global namespace more than I have to.
 * Then upper letters, so it's clearer it's a shortcut (and less similar to USpek "o" and "so").
 * Concrete LinkedHashMap return type, so it's more useful most of the times in practice.
 * Note: lMO(..) / linkedMapOf(..) / LinkedHashMap is mutable.
 */

inline fun <T> lO(vararg elements: T): List<T> = listOf(*elements)
inline fun <T> lONN(vararg elements: T?): List<T> = listOfNotNull(*elements)

inline fun <T> lOMutN(size: Int) = MutableList<T?>(size) { null }

inline fun <K, V> lMO(vararg pairs: Pair<K, V>): LinkedHashMap<K, V> = linkedMapOf(*pairs)

@Suppress("UNCHECKED_CAST")
inline fun <K, V: Any> lMONN(vararg pairs: Pair<K, V?>): LinkedHashMap<K, V> =
  lMO(*pairs.mapNotNull { it.takeIf { it.second != null } as Pair<K, V>? }.tta)

inline val <reified T> Collection<T>.tta get() = toTypedArray()
