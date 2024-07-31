package pl.mareklangiewicz.ulog

import kotlin.coroutines.*
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.uctx.UCtx
import pl.mareklangiewicz.ulog.ULogLevel.*

/**
 * NONE should always be ignored (not logged)
 * BABBLE should usually be ignored
 * VERBOSE ... ASSERT as in android (also the same ordinal as android numeric priority, and same char symbol)
 * ASSERT can crash the system/app (meaning unsupported or fatal error)
 */
enum class ULogLevel(val symbol: Char) {
  NONE('N'),
  BABBLE('B'),
  VERBOSE('V'),
  DEBUG('D'),
  INFO('I'),
  WARN('W'),
  ERROR('E'),
  ASSERT('A'),
}

fun interface ULog: UCtx {
  operator fun invoke(level: ULogLevel, data: Any?)
  companion object Key : CoroutineContext.Key<ULog>
  override val key: CoroutineContext.Key<*> get() = Key
}

suspend inline fun <reified T: ULog> localULogAs(): T =
  localULogAsOrNull<T>() ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

suspend inline fun <reified T: ULog> localULogAsOrNull(): T? = coroutineContext[ULog] as? T

suspend inline fun localULog(): ULog = localULogOrNull() ?: bad { "No ULog provided in coroutine context." }

suspend inline fun localULogOrNull(): ULog? = coroutineContext[ULog]

@Deprecated("")
interface WithULog {
  val ulog: ULog
}


fun ULog.d(data: Any?) = this(DEBUG, data)
fun ULog.i(data: Any?) = this(INFO, data)
fun ULog.w(data: Any?) = this(WARN, data)
fun ULog.e(data: Any?) = this(ERROR, data)

@DelicateApi fun ULog.none(data: Any?) = this(NONE, data)
@DelicateApi fun ULog.babble(data: Any?) = this(BABBLE, data)
@DelicateApi fun ULog.verbose(data: Any?) = this(VERBOSE, data)
@DelicateApi fun ULog.assert(data: Any?) = this(ASSERT, data)

@Deprecated("Use babble", ReplaceWith("babble"))
@DelicateApi fun ULog.quiet(data: Any?) = babble(data)
// quiet is confusing name because in gradle it means sth like my LogLevel.ASSERT

// DelicateApi annotations are for levels are only for special cases and shouldn't be overused.
// I don't want any tags and/or exceptions here in API, any tags/keys/etc can be passed inside data.
// I want MINIMALISTIC API here, and to promote single arg functions that compose better with UPue.

class ULogPrintLn(
  val minLevel: ULogLevel = DEBUG,
  val prefix: String = "L",
  val separator: String = " ",
) : ULog {
  override operator fun invoke(level: ULogLevel, data: Any?) {
    if (level >= minLevel) println("$prefix$separator${level.symbol}$separator$data")
  }
}

class ULogEntry(val data: Any?, val context: CoroutineContext? = null, val time: ComparableTimeMark? = null)

/**
 * full log - receiving side can check the job hierarchy, coroutine name, time mark, etc.
 * (it technically could also cancel by throwing [kotlinx.coroutines.CancellationException], but that's hacky)
 * Warning: It allocates memory, and it checks time mark by default. So it's slower than normal log.
 * Don't use it in time critical code (like animations etc.). Use normal log without allocations.
 * (and configure some fast logger - it might for example check time less often, not on each log)
 * Generally we support marking time on both sides. Here when ULog user want to make sure time is logged/saved;
 * but also some specific ULog impl can decide to capture current time even if log messages don't carry time.
 */
suspend fun ULog.full(level: ULogLevel, data: Any?, time: ComparableTimeMark = TimeSource.Monotonic.markNow()) =
  this(level, ULogEntry(data, coroutineContext, time))
suspend fun ULog.fd(data: Any?) = full(DEBUG, data)
suspend fun ULog.fi(data: Any?) = full(INFO, data)
suspend fun ULog.fw(data: Any?) = full(WARN, data)
suspend fun ULog.fe(data: Any?) = full(ERROR, data)


fun ULog.timed(level: ULogLevel, data: Any?, time: ComparableTimeMark = TimeSource.Monotonic.markNow()) =
  this(level, ULogEntry(data, null, time))
fun ULog.td(data: Any?) = timed(DEBUG, data)
fun ULog.ti(data: Any?) = timed(INFO, data)
fun ULog.tw(data: Any?) = timed(WARN, data)
fun ULog.te(data: Any?) = timed(ERROR, data)
