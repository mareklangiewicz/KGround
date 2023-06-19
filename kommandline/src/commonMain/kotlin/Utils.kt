package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNotNull(element: T?) = if (element == null) this else listOf(element) + this

fun List<String>.printlns() = forEach(::println)


// the ".enabled" suffix is important, so it's clear the user explicitly enabled a boolean "flag"
fun CliPlatform.isUserFlagEnabled(key: String) = konfigInUserHomeConfigDir()["$key.enabled"]?.trim().toBoolean()
fun CliPlatform.setUserFlag(key: String, enabled: Boolean) { konfigInUserHomeConfigDir()["$key.enabled"] = enabled.toString() }

private val interactive by lazy {
    when {
        SYS.isJvm -> SYS.isUserFlagEnabled("code.interactive")
        else -> {
            println("Interactive stuff is only available on jvm platform (for now).")
            false
        }
    }
}

fun ifInteractive(block: () -> Unit) = if (interactive) block() else println("Interactive code is disabled.")

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.checkInIdeap(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, dir = execInDir, outFile = tmpFile).await()
        start(ideap { +tmpFile }).await()
    } }
}

