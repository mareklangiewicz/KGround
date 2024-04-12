@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

actual inline fun Float.str(maxLength: Int, maxIndicator: String, precision: Int): String =
  "%.${precision}f".format(this).str(maxLength, maxIndicator)

actual inline fun Double.str(maxLength: Int, maxIndicator: String, precision: Int): String =
  "%.${precision}f".format(this).str(maxLength, maxIndicator)
