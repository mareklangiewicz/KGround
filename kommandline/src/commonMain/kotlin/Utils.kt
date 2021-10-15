package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNotNull(element: T?) = if (element == null) this else listOf(element) + this

fun List<String>.printlns() = forEach(::println)

// TODO_someday: intellij plugin with @param UI similar to colab notebooks
//private const val INTERACTIVE_CHECKS_ENABLED = true
//private const val INTERACTIVE_CHECKS_ENABLED = false
private val INTERACTIVE_CHECKS_ENABLED = Platform.SYS.isGnome

fun ifInteractive(block: () -> Unit) =
    if (INTERACTIVE_CHECKS_ENABLED) block() else println("Interactive tests are disabled.")

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: Platform = Platform.SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.checkInIdeap(expectedKommandLine: String? = null, execInDir: String? = null, platform: Platform = Platform.SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, execInDir, outFile = tmpFile).await()
        start(ideap { +tmpFile }).await()
    } }
}

