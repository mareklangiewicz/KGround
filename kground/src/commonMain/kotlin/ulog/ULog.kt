package pl.mareklangiewicz.ulog

import pl.mareklangiewicz.ulog.ULogLevel.*

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
    fun ulog(level: ULogLevel, data: Any?)
}

fun ULog.ulogd(data: Any?) = ulog(DEBUG, data)
fun ULog.ulogi(data: Any?) = ulog(INFO, data)
fun ULog.ulogw(data: Any?) = ulog(WARN, data)
fun ULog.uloge(data: Any?) = ulog(ERROR, data)
// No convenience methods for other levels on purpose.
// Those levels are only for special cases and shouldn't be overused.
// I don't want any tags and/or exceptions here in API, any tags/keys/etc can be passed inside data.
// I want MINIMALISTIC API here, and to promote single arg functions that compose better with UPue.

class ULogPrintLn(private val prefix: String = "ulog", private val separator: String = " "): ULog {
    override fun ulog(level: ULogLevel, data: Any?) {
        if (level.ordinal > 1) println("$prefix$separator${level.symbol}$separator$data")
    }
}