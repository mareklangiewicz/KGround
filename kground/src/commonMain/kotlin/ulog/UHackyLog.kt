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
 * logging is thread-safe (uses thread-safe [tryEmit])
 *
 * TODO_later: add some cool universal logger like this to ULog.kt, but rethink carefully.
 * BTW This dirty impl is only fast solution for my current limited needs:
 * no saved timestamps, loglevels, immediate .toString, ...
 */
class UHackySharedFlowLog(
  val minLevel: ULogLevel = ULogLevel.INFO,
  val replayCacheSize: Int = 2048,
  val alsoPrintLn: Boolean = true,
  val toLogLine: (ULogLevel, Any?) -> String = { level, data -> "kg ${level.symbol} $data" },
  // FIXME_later: some shortened string conversion? like ustr from... UWidgets?
) : ULog {

  val flow = MutableSharedFlow<String>(replayCacheSize, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override operator fun invoke(level: ULogLevel, data: Any?) {
    if (level >= minLevel) {
      val str = toLogLine(level, data)
      flow.tryEmit(str)
      if (alsoPrintLn) println(str)
    }
  }
}

var ulog: ULog = UHackySharedFlowLog()

val ulogCache: List<String>? get() = (ulog as? UHackySharedFlowLog)?.flow?.replayCache
