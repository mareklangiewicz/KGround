package pl.mareklangiewicz.kommand.konfig

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.coreutils.MkDir.Option.*

/**
 * Designed for rather small one-line values. Multiline is not strictly forbidden but can lead to strange edge cases.
 * Designed mostly for implementation where each key represent file in special directory. Value is content of the file.
 */
interface Konfig {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String?)
    val keys: List<String>
}

/**
 * Works best when values don't have special characters like new-line etc.
 * Also keys are implemented as files, so should be simple names without special chars like for example '/'.
 * I want to be able to use it over ssh and/or adb, so that another reason to avoid special chars.
 */
class KonfigImpl(val dir: String, val platform: Platform = Platform.SYS): Konfig {

    init { platform.run { mkdir { -parents; +dir }() } }

    override fun get(key: String): String? {
        val result = platform.start(cat { +"$dir/$key" }).await()
        return when (result.exitValue) {
            0 -> result.stdOutAndErr.joinToString("\n")
            else -> null
        }
    }

    override fun set(key: String, value: String?) {
        val file = "$dir/$key"
        platform.run {
            if (value == null) rmIfExists(file)
            else echo(value)(outFile = file)
        }
    }

    override val keys: List<String>
        get() = platform.lsRegFiles(dir)
}

fun Platform.konfig(dir: String = pathToUserHome!! + "/.config/konfig"): Konfig = KonfigImpl(dir, this)

fun Konfig.printAll() = keys.forEach(::print)
fun Konfig.print(key: String) = println("    konfig[\"$key\"] == \"${this[key]}\"")
