package pl.mareklangiewicz.kground.tee

import java.nio.file.*
import pl.mareklangiewicz.udata.strf

actual fun getCurrentThreadName(): String = Thread.currentThread().name

actual fun getCurrentPlatformName(): String = System.getProperty("os.name").let { "JVM $it" }

actual fun getCurrentAbsolutePath(): String = Paths.get("").toAbsolutePath().strf


actual inline fun <R> synchronizedMaybe(lock: Any, block: () -> R): R = synchronized(lock, block)
