@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

import kotlin.math.roundToInt
import kotlin.math.roundToLong

/*
 * Micro string representations of common data types.
 * Doesn't have to be unique, doesn't have to be maximally precise.
 * Have to be consistent, short. Used mostly for logging (so short), but also in tests, so str representation
 * should be consistent (almost never changing).
 * Try not to change defaults, user should rely on generated strings being "short enough" / "micro".
 * TODO_someday: use context parameters when available.
 *
 * Intention is for every "app" / "consumer lib" to have internal val Any?.str: String = when {...}
 * with conversions/defaults selected/adjusted for specific project/module and it's types.
 * BTW: when logging source code lines, set maxLength = 128 locally/manually
 * (128 is nice limit because I try to format all my code with .editorconfig: max_line_length = 120)
 */


inline val STR_DEFAULT_MAX_LENGTH get() = 32
inline val STR_DEFAULT_MAX_INDICATOR get() = ".."
inline val STR_DEFAULT_REAL_NUMBER_PRECISION get() = 2

// very short defaults mostly for logging/debugging a lot of data structures
inline val STR_DEFAULT_TRUE get() = "T"
inline val STR_DEFAULT_FALSE get() = "F"
inline val STR_DEFAULT_NULL get() = "n"
inline val STR_DEFAULT_NOT_NULL get() = "nn"

inline fun CharSequence.str(
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
): String = if (length > maxLength) substring(0, maxLength - maxIndicator.length) + maxIndicator else this.toString()

expect inline fun Float.str(
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
  precision: Int = STR_DEFAULT_REAL_NUMBER_PRECISION,
): String

expect inline fun Double.str(
  maxLength: Int = STR_DEFAULT_MAX_LENGTH,
  maxIndicator: String = STR_DEFAULT_MAX_INDICATOR,
  precision: Int = STR_DEFAULT_REAL_NUMBER_PRECISION,
): String


inline fun Boolean?.str(
  strTrue: String = STR_DEFAULT_TRUE,
  strFalse: String = STR_DEFAULT_FALSE,
  strNull: String = STR_DEFAULT_NULL,
) = when (this) {
  true -> strTrue
  false -> strFalse
  null -> strNull
}

inline fun Any?.strIfNullOrNot(
  strNotNull: String = STR_DEFAULT_NOT_NULL,
  strNull: String = STR_DEFAULT_NULL,
): String =
  if (this != null) strNotNull else strNull


inline val Number.int get() = toInt()
inline val Number.lng get() = toLong()
inline val Number.dbl get() = toDouble()
inline val Number.flt get() = toFloat()

inline val Float.intr get() = roundToInt()
inline val Float.lngr get() = roundToLong()
inline val Double.intr get() = roundToInt()
inline val Double.lngr get() = roundToLong()

inline val Any?.unit get() = Unit

inline fun <reified E> mutableListOfNulls(size: Int) = MutableList<E?>(size) { null }
