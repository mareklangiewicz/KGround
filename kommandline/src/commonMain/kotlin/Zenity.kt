package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Zenity.*
import pl.mareklangiewicz.kommand.Zenity.Option.*

fun zenity(type: DialogType, init: Zenity.() -> Unit = {}) = Zenity(type).apply(init)

fun Platform.zenityAskIf(question: String, atitle: String? = null): Boolean = start(zenity(DialogType.question) {
    -text(question)
    -nowrap
    atitle?.let { -title(it) }
}).await().exitValue == 0

fun Platform.zenityAskForEntry(question: String, atitle: String? = null, suggested: String? = null): String =
    start(zenity(DialogType.entry) {
        -text(question)
        atitle?.let { -title(it) }
        suggested?.let { -entrytext(it) }
    }).await().output().single()


/*
* https://help.gnome.org/users/zenity/stable/index.html.en
* https://linux.die.net/man/1/zenity
*/
data class Zenity(
    var type: DialogType,
    val options: MutableList<Option> = mutableListOf(),
    val data: MutableList<String> = mutableListOf()
): Kommand {
    override val name get() = "zenity"
    override val args get() = listOf(type.str) + options.map { it.str } + data
    enum class DialogType(val str: String) {
        calendar("--calendar"), entry("--entry"), error("--error"), fileselection("--file-selection"), info("--info"),
        list("--list"), notification("--notification"), progress("--progress"), question("--question"),
        textinfo("--text-info"), warning("--warning"), scale("--scale")
    }
    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = arg?.let { "$name=$it" } ?: name // TODO_someday: some fun similar to plusIfNotNull for such cases
        object help : Option("--help")
        object version : Option("--version")
        object about : Option("--about")
        data class title(val t: String): Option("--title", t)
        /** icon path or one of keywords: info, warning, question, error */
        data class icon(val icon: String): Option("--window-icon", icon)
        data class timeout(val seconds: Int): Option("--timeout", seconds.toString())
        data class text(val t: String): Option("--text", t)
        data class day(val d: Int): Option("--day", d.toString())
        data class month(val m: Int): Option("--month", m.toString())
        data class year(val y: Int): Option("--year", y.toString())
        data class dateformat(val format: String): Option("--date-format", format)
        data class entrytext(val t: String): Option("--entry-text", t)
        object hidetext : Option("--hide-text")
        object nowrap : Option("--no-wrap")
        data class filename(val fn: String): Option("--filename", fn)
        object multiple : Option("--multiple")
        object directory : Option("--directory")
        object save : Option("--save")
        data class separator(val s: String): Option("--separator", s)
        object confirmoverwrite : Option("--confirm-overwrite")
        data class column(val header: String): Option("--column", header)
        object checklist : Option("--checklist")
        object radiolist : Option("--radiolist")
        object editable : Option("--editable")
        data class printcolumn(val c: String): Option("--print-column", c)
        data class hidecolumn(val c: Int): Option("--hide-column", c.toString())
        object listen : Option("--listen")
        data class percentage(val p: Int): Option("--percentage", p.toString())
        object autoclose : Option("--auto-close")
        object autokill : Option("--auto-kill")
        object pulsate : Option("--pulsate")
        data class initvalue(val v: Int): Option("--value", v.toString())
        data class minvalue(val v: Int): Option("--min-value", v.toString())
        data class maxvalue(val v: Int): Option("--max-value", v.toString())
        data class step(val v: Int): Option("--step", v.toString())
        object printpartial : Option("--print-partial")
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = data.add(this)
}


