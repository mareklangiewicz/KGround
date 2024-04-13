@file:Suppress("unused")

package pl.mareklangiewicz.udata

/*
 * These cryptic short fun names like lng, flt, lngr, (str too) are kind of experiment.
 * Experiment with treating some opinionated set of extensions/utils as DSL/keywords,
 * that should be short and memorized by user (instead of long and descriptive).
 * I'm very much aware it's against any normal coding convention. :)
 */

import kotlin.math.roundToInt
import kotlin.math.roundToLong
import pl.mareklangiewicz.bad.bad


inline val Number.int get() = toInt()
inline val Number.lng get() = toLong()
inline val Number.sht get() = toShort()
inline val Number.dbl get() = toDouble()
inline val Number.flt get() = toFloat()

inline val Float.intr get() = roundToInt()
inline val Float.lngr get() = roundToLong()
inline val Double.intr get() = roundToInt()
inline val Double.lngr get() = roundToLong()

inline val Any?.unt get() = Unit


inline val Number.numIsReal get() = when (this) {
  is Float -> true
  is Double -> true
  else -> false
}

inline val Number.numIsInteger get() = !numIsReal

/** Note: unsigned integers don't extend [Number] */
inline val Number.memBytes: Int get() = when (this) {
  is Byte -> Byte.SIZE_BYTES
  is Short -> Short.SIZE_BYTES
  is Int -> Int.SIZE_BYTES
  is Long -> Long.SIZE_BYTES
  is Float -> Float.SIZE_BYTES
  is Double -> Double.SIZE_BYTES
  else -> bad { "unknown number type" }
}

/** Note: unsigned integers don't extend [Number] */
inline val Number.numMinVal: Number get() = when (this) {
  is Byte -> Byte.MIN_VALUE
  is Short -> Short.MIN_VALUE
  is Int -> Int.MIN_VALUE
  is Long -> Long.MIN_VALUE
  is Float -> Float.MIN_VALUE
  is Double -> Double.MIN_VALUE
  else -> bad { "unknown number type" }
}

/** Note: unsigned integers don't extend [Number] */
inline val Number.numMaxVal: Number get() = when (this) {
  is Byte -> Byte.MAX_VALUE
  is Short -> Short.MAX_VALUE
  is Int -> Int.MAX_VALUE
  is Long -> Long.MAX_VALUE
  is Float -> Float.MAX_VALUE
  is Double -> Double.MAX_VALUE
  else -> bad { "unknown number type" }
}
