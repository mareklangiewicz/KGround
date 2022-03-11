package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.*

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNotNull(element: T?) = if (element == null) this else listOf(element) + this

fun List<String>.printlns() = forEach(::println)

private val interactive by lazy {
    if (SYS.isJvm) SYS.konfig()["interactive_code"].toBoolean()
    else {
        println("Interactive stuff is only available on jvm platform (for now).")
        false
    }
}

fun ifInteractive(block: () -> Unit) =
    if (interactive) block() else println("Interactive code is disabled.")

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: Platform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.checkInIdeap(expectedKommandLine: String? = null, execInDir: String? = null, platform: Platform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, execInDir, outFile = tmpFile).await()
        start(ideap { +tmpFile }).await()
    } }
}

