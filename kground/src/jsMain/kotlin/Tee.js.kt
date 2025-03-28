package pl.mareklangiewicz.kground.tee

import kotlinx.browser.*
import org.w3c.dom.Window
import kotlin.js.*
import pl.mareklangiewicz.udata.strf

// FIXME_someday: maybe other name - saying sth more about platform? browser/node/??
actual fun getCurrentThreadName(): String = "JS Thread"


actual fun getCurrentPlatformName(): String =
  windowOrNull?.navigator?.userAgent?.let { "JS Agent $it" } ?: "JS Node probably"

// FIXME_someday: real path on node.js platform? something more appropriate in browser too? rename? write kdoc?
actual fun getCurrentAbsolutePath(): String =
  windowOrNull?.document?.location?.strf ?: js("process.cwd()")


actual inline fun <R> synchronizedMaybe(lock: Any, block: () -> R): R = block()

val windowOrNull: Window? get() = if (js("typeof window !== 'undefined'")) window else null
