package pl.mareklangiewicz.kground

import java.nio.file.*
import java.util.*
import pl.mareklangiewicz.udata.strf

actual fun getCurrentTimeMs(): Long = System.currentTimeMillis()

// The format should be user-friendly and short
actual fun getCurrentTimeStr(): String = getCurrentTimeMs().let { String.format(Locale.US, "%tT:%tL", it, it) }

actual fun getCurrentThreadName(): String = Thread.currentThread().name

actual fun getCurrentPlatformName(): String = System.getProperty("os.name").let { "JVM $it" }

actual fun getCurrentAbsolutePath(): String = Paths.get("").toAbsolutePath().strf


actual inline fun <R> synchronizedMaybe(lock: Any, block: () -> R): R = synchronized(lock, block)
