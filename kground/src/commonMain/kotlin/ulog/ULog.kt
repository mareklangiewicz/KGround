package pl.mareklangiewicz.ulog

import pl.mareklangiewicz.ulog.ULogLevel.*
import kotlin.coroutines.*

/**
 * NONE should always be ignored (not logged)
 * QUIET should usually be ignored
 * VERBOSE ... ASSERT as in android (also the same ordinal as android numeric priority, and same char symbol)
 * ASSERT can crash the system/app (meaning unsupported or fatal error)
 */
enum class ULogLevel(val symbol: Char) {
  NONE('N'),
  QUIET('Q'),
  VERBOSE('V'),
  DEBUG('D'),
  INFO('I'),
  WARN('W'),
  ERROR('E'),
  ASSERT('A'),
}

fun interface ULog {
  operator fun invoke(level: ULogLevel, data: Any?)
}

interface WithULog {
  val ulog: ULog
}

fun ULog.d(data: Any?) = this(DEBUG, data)
fun ULog.i(data: Any?) = this(INFO, data)
fun ULog.w(data: Any?) = this(WARN, data)
fun ULog.e(data: Any?) = this(ERROR, data)
// No convenience methods for other levels on purpose.
// Those levels are only for special cases and shouldn't be overused.
// I don't want any tags and/or exceptions here in API, any tags/keys/etc can be passed inside data.
// I want MINIMALISTIC API here, and to promote single arg functions that compose better with UPue.

class ULogPrintLn(private val prefix: String = "ulog", private val separator: String = " ") : ULog {
  override operator fun invoke(level: ULogLevel, data: Any?) {
    if (level.ordinal > 1) println("$prefix$separator${level.symbol}$separator$data")
  }
}

class DataWithContext(val data: Any?, val context: CoroutineContext)

/**
 * structural log - receiving side can check the job hierarchy, coroutine name, etc.
 * (it technically could also cancel by throwing [kotlinx.coroutines.CancellationException], but that's hacky)
 */
suspend fun ULog.slog(level: ULogLevel, data: Any?) = this(level, DataWithContext(data, coroutineContext))
suspend fun ULog.sd(data: Any?) = slog(DEBUG, data)
suspend fun ULog.si(data: Any?) = slog(INFO, data)
suspend fun ULog.sw(data: Any?) = slog(WARN, data)
suspend fun ULog.se(data: Any?) = slog(ERROR, data)
