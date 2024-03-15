@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*

@OptIn(DelicateApi::class)
fun psAllFull() = ps("-Af") // BTW ps -e is the same as ps -A

/**
 * [linux man](https://man7.org/linux/man-pages/man1/ps.1.html)
 * FIXME_later: This is very rudimentary implementation. Reimplement later with proper class and typed options.
 */
@DelicateApi fun ps(vararg args: String) = kommand("ps", *args)
