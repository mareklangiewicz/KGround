package pl.mareklangiewicz.ulog.hack

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import pl.mareklangiewicz.bad.reqNN
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogLevel


/**
 * Hacky ulog impl with cache, before redesign after we have real ulog in context parameters.
 *
 * TODO: track: https://github.com/Kotlin/KEEP/issues/367
 *
 * logging should be thread-safe (uses thread-safe [tryEmit]), but.. toLogLine, ulogCache, etc.. redesign later
 *
 * TODO_later: add some cool universal logger like this to ULog.kt, but rethink carefully.
 * BTW This dirty impl is only fast solution for my current limited needs:
 * no saved timestamps, loglevels, immediate .toString, ...
 */
class UHackySharedFlowLog(
  var minLevel: ULogLevel = ULogLevel.INFO,
  val replayCacheSize: Int = 16384,
  val alsoPrintLn: Boolean = true,
  val toLogLine: (ULogLevel, Any?) -> String = { level, data -> "L ${level.symbol} ${data.str()}" },
) : ULog {

  /** TODO_later: analyze thread-safety */
  val flow = MutableSharedFlow<String>(replayCacheSize, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override operator fun invoke(level: ULogLevel, data: Any?) {
    if (level >= minLevel) {
      val line = toLogLine(level, data)
      flow.tryEmit(line)
      if (alsoPrintLn) println(line)
    }
  }
}

/** This global var is especially hacky and will be removed when we have context parameters */
@Deprecated("Use val log = implictx<ULog>()")
var ulog: ULog =
  UHackySharedFlowLog { level, data -> "Deprecated L ${level.symbol} ${data.str(maxLength = 512)}" }
  // UHackySharedFlowLog { level, data -> "L ${level.symbol} ${getCurrentTimeStr()} ${data.str(maxLength = 128)}" }
  // Note: getting current time makes it a bit slower, so it shouldn't be the default.

/** TODO_later: make sure getting snapshot from replayCache is thread-safe */
@Deprecated("Use val log = implictx<UHackySharedFlowLog>()")
val ulogCache: List<String>? get() = (ulog as? UHackySharedFlowLog)?.flow?.replayCache

@Deprecated("Use val log = implictx<UHackySharedFlowLog>()")
var ulogHackyMinLevel: ULogLevel?
  get() = (ulog as? UHackySharedFlowLog)?.minLevel
  set(value) { (ulog as? UHackySharedFlowLog)?.minLevel = value.reqNN() }
