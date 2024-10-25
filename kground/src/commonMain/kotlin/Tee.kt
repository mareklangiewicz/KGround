package pl.mareklangiewicz.kground.tee

import kotlin.time.Duration
import kotlin.time.TimeSource
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.*

/**

Extensions to log any value (mostly for debugging) and returning the same value.
(So it can be "injected" inside any expression without breaking logic)
all extensions here have prefix ".tee" (tee like cli command)
It also can be thought of as ".also" stdlib extension,
but with additional default "batteries included" - mostly for debugging.

This implementation is temporary.
The final implementation will use context receivers (when they are multiplatform)
and will be correctly composable with the rest of ULog.

 */


fun <T> T.tee(
  into: ULog = TeeDefaultLog,
  withCurrentThread: Boolean = true,
  withCurrentTime: Boolean = true,
  withCurrentPlatform: Boolean = false,
  withCurrentPath: Boolean = false,
  withValue: Boolean = true,
): T {
  val p1 = if (withCurrentThread) " [${getCurrentThreadName().str().padEnd(32)}]" else ""
  val p2 = if (withCurrentTime) " [${getRunningTimeMs()}]" else ""
  val p3 = if (withCurrentPlatform) " [${getCurrentPlatformName().str().padEnd(32)}]" else ""
  val p4 = if (withCurrentPath) " [${getCurrentAbsolutePath()}]" else ""
  val p5 = if (withValue) " $this" else ""
  into.i("$p1$p2$p3$p4$p5")
  return this
}

val String.tee: String get() = tee()

// Some example shortcuts, usually users would create their own versions of .teeSomething
val <T> T.teeP: T get() = tee(withCurrentPlatform = true)
val <T> T.teePP: T get() = tee(withCurrentPlatform = true, withCurrentPath = true)

private val TeeDefaultLog = ULogPrintLn(prefix = "tee")


// FIXME: move it all to better places (but still in kground repo); refactor/redesign
//   (I need it all in kground right now, so this is temporary implementation)
//   UPDATE: Think what should be in kground-io

private val TeeStartTimeMark = TimeSource.Monotonic.markNow()

@Deprecated("")
fun getStartTimeMark() = TeeStartTimeMark
@Deprecated("")
fun getRunningTime(): Duration = TimeSource.Monotonic.markNow() - getStartTimeMark()
@Deprecated("")
fun getRunningTimeMs(): Long = getRunningTime().inWholeMilliseconds
@Deprecated("")
expect fun getCurrentThreadName(): String
@Deprecated("")
expect fun getCurrentPlatformName(): String
@Deprecated("")
expect fun getCurrentAbsolutePath(): String

/** JVM, JS, LINUX, ... */
@Deprecated("")
fun getCurrentPlatformKind(): String = getCurrentPlatformName().takeWhile { it.isLetter() }.uppercase()


@DelicateApi
expect inline fun <R> synchronizedMaybe(lock: Any, block: () -> R): R
