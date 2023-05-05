package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNotNull(element: T?) = if (element == null) this else listOf(element) + this

fun List<String>.printlns() = forEach(::println)

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir)
}

fun Kommand.checkInIdeap(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, execInDir, outFile = tmpFile).await()
        start(ideap { +tmpFile }).await()
    }
}

