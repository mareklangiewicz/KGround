@file:Suppress("unused")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.ZenityOpt.*
import pl.mareklangiewicz.kommand.ZenityType.*

fun CliPlatform.zenityAskIfExec(question: String, atitle: String? = null): Boolean =
    start(zenityAskIf(question, atitle)).waitForExit() == 0

fun CliPlatform.zenityAskForPasswordExec(question: String = "Enter password", atitle: String? = null): String =
    zenityAskForPassword(question, atitle).execb(this).single()

fun CliPlatform.zenityAskForEntryExec(question: String, atitle: String? = null, suggested: String? = null): String =
    zenityAskForEntry(question, atitle, suggested).execb(this).single()

fun zenityAskIf(question: String, atitle: String? = null) = zenity(ZenityType.question) {
    -text(question)
    -nowrap
    atitle?.let { -title(it) }
}

fun zenityAskForPassword(question: String = "Enter password", atitle: String? = null) =
    zenity(entry) {
        -hidetext
        -text(question)
        atitle?.let { -title(it) }
    }

fun zenityAskForEntry(question: String, atitle: String? = null, suggested: String? = null) =
    zenity(entry) {
        -text(question)
        atitle?.let { -title(it) }
        suggested?.let { -entrytext(it) }
    }

fun zenity(type: ZenityType, init: Zenity.() -> Unit = {}) = Zenity(type).apply(init)

/*
* https://help.gnome.org/users/zenity/stable/index.html.en
* https://linux.die.net/man/1/zenity
*/
data class Zenity(
    var type: ZenityType,
    val opts: MutableList<ZenityOpt> = mutableListOf(),
    val nonopts: MutableList<String> = mutableListOf()
): Kommand {
    override val name get() = "zenity"
    override val args get() = listOf(type.str) + opts.map { it.str } + nonopts
    operator fun ZenityOpt.unaryMinus() = opts.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}

enum class ZenityType(val str: String) {
    calendar("--calendar"), entry("--entry"), error("--error"), fileselection("--file-selection"), info("--info"),
    list("--list"), notification("--notification"), progress("--progress"), question("--question"),
    textinfo("--text-info"), warning("--warning"), scale("--scale")
}

sealed class ZenityOpt(val name: String, val arg: String? = null) {
    val str get() = arg?.let { "$name=$it" } ?: name // TODO_someday: some fun similar to plusIfNotNull for such cases
    data object help : ZenityOpt("--help")
    data object version : ZenityOpt("--version")
    data object about : ZenityOpt("--about")
    data class title(val t: String): ZenityOpt("--title", t)
    /** icon path or one of keywords: info, warning, question, error */
    data class icon(val icon: String): ZenityOpt("--window-icon", icon)
    data class timeout(val seconds: Int): ZenityOpt("--timeout", seconds.toString())
    data class text(val t: String): ZenityOpt("--text", t)
    data class day(val d: Int): ZenityOpt("--day", d.toString())
    data class month(val m: Int): ZenityOpt("--month", m.toString())
    data class year(val y: Int): ZenityOpt("--year", y.toString())
    data class dateformat(val format: String): ZenityOpt("--date-format", format)
    data class entrytext(val t: String): ZenityOpt("--entry-text", t)
    data object hidetext : ZenityOpt("--hide-text")
    data object nowrap : ZenityOpt("--no-wrap")
    data class filename(val fn: String): ZenityOpt("--filename", fn)
    data object multiple : ZenityOpt("--multiple")
    data object directory : ZenityOpt("--directory")
    data object save : ZenityOpt("--save")
    data class separator(val s: String): ZenityOpt("--separator", s)
    data object confirmoverwrite : ZenityOpt("--confirm-overwrite")
    data class column(val header: String): ZenityOpt("--column", header)
    data object checklist : ZenityOpt("--checklist")
    data object radiolist : ZenityOpt("--radiolist")
    data object editable : ZenityOpt("--editable")
    data class printcolumn(val c: String): ZenityOpt("--print-column", c)
    data class hidecolumn(val c: Int): ZenityOpt("--hide-column", c.toString())
    data object listen : ZenityOpt("--listen")
    data class percentage(val p: Int): ZenityOpt("--percentage", p.toString())
    data object autoclose : ZenityOpt("--auto-close")
    data object autokill : ZenityOpt("--auto-kill")
    data object pulsate : ZenityOpt("--pulsate")
    data class initvalue(val v: Int): ZenityOpt("--value", v.toString())
    data class minvalue(val v: Int): ZenityOpt("--min-value", v.toString())
    data class maxvalue(val v: Int): ZenityOpt("--max-value", v.toString())
    data class step(val v: Int): ZenityOpt("--step", v.toString())
    data object printpartial : ZenityOpt("--print-partial")
}


