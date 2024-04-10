package pl.mareklangiewicz.ulog.hack

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
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
  val minLevel: ULogLevel = ULogLevel.INFO,
  val replayCacheSize: Int = 16384,
  val alsoPrintLn: Boolean = true,
  val toLogLine: (ULogLevel, Any?) -> String = { level, data -> "kg ${level.symbol} ${data.toString().take(64)}" },
  // FIXME_later: Move UStr functions from UWidgets to KGround,
  // make sure string truncation/limit sets "..." at the end
  // (when actually truncated; truncated version together with "..." should always have 64 chars!);
  // then use it here instead of simple .toString().take(64)
) : ULog {

  /** TODO_later: analyze thread-safety */
  val flow = MutableSharedFlow<String>(replayCacheSize, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override operator fun invoke(level: ULogLevel, data: Any?) {
    if (level >= minLevel) {
      val str = toLogLine(level, data)
      flow.tryEmit(str)
      if (alsoPrintLn) println(str)
    }
  }
}

/** This global var is especially hacky and will be removed when we have context parameters */
var ulog: ULog = UHackySharedFlowLog()

/** TODO_later: make sure getting snapshot from replayCache is thread-safe */
val ulogCache: List<String>? get() = (ulog as? UHackySharedFlowLog)?.flow?.replayCache
