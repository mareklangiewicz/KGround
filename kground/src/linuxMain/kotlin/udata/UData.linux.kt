@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.e


actual inline fun Number.str(
  vararg useNamedArgs: Unit,
  maxLength: Int,
  maxIndicator: String,
  precision: Int,
): String = when(this) { // FIXME_later
  is Float -> toString().also { ulog.e("No Float formatting on K/N implemented yet. Using toString()") }
  is Double -> toString().also { ulog.e("No Double formatting on K/N implemented yet. Using toString()") }
  else -> toString()
}.str(maxLength = maxLength, maxIndicator = maxIndicator)
