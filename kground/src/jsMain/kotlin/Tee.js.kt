package pl.mareklangiewicz.kground

import kotlinx.browser.*
import kotlin.js.*

actual fun getCurrentTimeMs(): Long = Date().getMilliseconds().toLong()

// FIXME_later: The format should be user-friendly and short. And similar to other platforms (the same??)
actual fun getCurrentTimeStr(): String = getCurrentTimeMs().toString()


// FIXME_someday: maybe other name - saying sth more about platform? browser/node/??
actual fun getCurrentThreadName(): String = "JS Thread"


actual fun getCurrentPlatformName(): String = window.navigator.userAgent.let { "JS Agent $it" }

// FIXME_someday: real path on node.js platform? something more appropriate in browser too? rename? write kdoc?
actual fun getCurrentAbsolutePath(): String = window.document.location.toString()
