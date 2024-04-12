@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

actual inline fun Float.str(maxLength: Int, maxIndicator: String, precision: Int): String =
  asDynamic().toFixed(precision) as String

actual inline fun Double.str(maxLength: Int, maxIndicator: String, precision: Int): String =
  asDynamic().toFixed(precision) as String
