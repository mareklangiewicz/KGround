package pl.mareklangiewicz.ulog.hack

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.bad.bad
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

@ExperimentalApi("Not sure if I want this or if user should do it manually")
val ULog.cacheOrNull: List<String>? get() = (this as? UHackySharedFlowLog)?.flow?.replayCache

@ExperimentalApi("Not sure if I want this or if user should do it manually")
var ULog.minLevelOrThrow: ULogLevel
  get() = (this as? UHackySharedFlowLog)?.minLevel ?: bad { "This logger doesn't have minLevel property" }
  set(value) { (this as? UHackySharedFlowLog).reqNN { "This logger doesn't have minLevel property" }.minLevel = value }
