package pl.mareklangiewicz.kground

import kotlinx.browser.*
import org.w3c.dom.Window
import kotlin.js.*

actual fun getCurrentTimeMs(): Long = Date().getMilliseconds().toLong()

// FIXME_later: The format should be user-friendly and short. And similar to other platforms (the same??)
actual fun getCurrentTimeStr(): String = getCurrentTimeMs().toString()


// FIXME_someday: maybe other name - saying sth more about platform? browser/node/??
actual fun getCurrentThreadName(): String = "JS Thread"


actual fun getCurrentPlatformName(): String =
    windowOrNull?.navigator?.userAgent?.let { "JS Agent $it" } ?: "JS Node probably"

// FIXME_someday: real path on node.js platform? something more appropriate in browser too? rename? write kdoc?
actual fun getCurrentAbsolutePath(): String =
    windowOrNull?.document?.location?.toString() ?: js("process.cwd()")

val windowOrNull: Window? get() = if (js("typeof window !== 'undefined'")) window else null