@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

actual inline fun Number.str(
  vararg useNamedArgs: Unit,
  maxLength: Int,
  maxIndicator: String,
  precision: Int,
): String = (asDynamic().toFixed(precision) as String).str(maxLength = maxLength, maxIndicator = maxIndicator)
