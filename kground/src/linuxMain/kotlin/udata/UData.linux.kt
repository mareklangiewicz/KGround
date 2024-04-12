@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.udata

import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.w

actual inline fun Float.str(maxLength: Int, maxIndicator: String, precision: Int): String {
  ulog.w("No Float formatting on K/N implemented yet. Using just toString()")
  return toString()
}

actual inline fun Double.str(maxLength: Int, maxIndicator: String, precision: Int): String {
  ulog.w("No Double formatting on K/N implemented yet. Using just toString()")
  return toString()
}
