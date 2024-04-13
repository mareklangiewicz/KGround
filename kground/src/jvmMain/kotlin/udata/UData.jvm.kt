@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

actual inline fun Number.str(
  vararg useNamedArgs: Unit,
  maxLength: Int,
  maxIndicator: String,
  precision: Int,
): String = when {
  this is Float || this is Double -> "%.${precision}f".format(this)
  else -> toString()
}.str(maxLength = maxLength, maxIndicator = maxIndicator)
