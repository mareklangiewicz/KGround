@file:Suppress("unused")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.ZenityOpt.*

fun CliPlatform.zenityAskIfExec(question: String, title: String? = null): Boolean =
    start(zenityAskIf(question, title)).waitForExit() == 0

fun CliPlatform.zenityAskForPasswordExec(question: String = "Enter password", title: String? = null): String =
    zenityAskForPassword(question, title).execb(this).single()

fun CliPlatform.zenityAskForEntryExec(question: String, title: String? = null, suggested: String? = null): String =
    zenityAskForEntry(question, title, suggested).execb(this).single()

@OptIn(DelicateKommandApi::class)
fun zenityAskIf(question: String, title: String? = null) = zenity(Type.Question) {
    -Text(question)
    -NoWrap
    title?.let { -Title(it) }
}

@OptIn(DelicateKommandApi::class)
fun zenityAskForPassword(question: String = "Enter password", title: String? = null) =
    zenity(Type.Entry) {
        -HideText
        -Text(question)
        title?.let { -Title(it) }
    }

@OptIn(DelicateKommandApi::class)
fun zenityAskForEntry(question: String, title: String? = null, suggested: String? = null) =
    zenity(Type.Entry) {
        -Text(question)
        title?.let { -Title(it) }
        suggested?.let { -EntryText(it) }
    }

@DelicateKommandApi
fun zenity(type: Type, init: Zenity.() -> Unit = {}) = Zenity().apply { -type; init() }

/*
* https://help.gnome.org/users/zenity/stable/index.html.en
* https://linux.die.net/man/1/zenity
*/
@OptIn(DelicateKommandApi::class)
data class Zenity(
    override val opts: MutableList<ZenityOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<ZenityOpt> { override val name get() = "zenity" }

@DelicateKommandApi
interface ZenityOpt : KOptTypical {

    sealed class Type(name: String): ZenityOpt, KOptL(name) {
        data object Calendar : Type("calendar")
        data object Entry : Type("entry")
        data object Error : Type("error")
        data object FileSelection : Type("file-selection")
        data object Info : Type("info")
        data object List : Type("list")
        data object Notification : Type("notification")
        data object Progress : Type("progress")
        data object Question : Type("question")
        data object TextInfo : Type("text-info")
        data object Warning : Type("warning")
        data object Scale : Type("scale")
    }

    data object Help : ZenityOpt, KOptL("help")
    data object Version : ZenityOpt, KOptL("version")
    data object About : ZenityOpt, KOptL("about")
    data class Title(val t: String): ZenityOpt, KOptL("title", t)
    /** icon path or one of keywords: info, warning, question, error */
    data class Icon(val icon: String): ZenityOpt, KOptL("window-icon", icon)
    data class Timeout(val seconds: Int): ZenityOpt, KOptL("timeout", seconds.toString())
    data class Text(val t: String): ZenityOpt, KOptL("text", t)
    data class Day(val d: Int): ZenityOpt, KOptL("day", d.toString())
    data class Month(val m: Int): ZenityOpt, KOptL("month", m.toString())
    data class Year(val y: Int): ZenityOpt, KOptL("year", y.toString())
    data class DateFormat(val format: String): ZenityOpt, KOptL("date-format", format)
    data class EntryText(val t: String): ZenityOpt, KOptL("entry-text", t)
    data object HideText : ZenityOpt, KOptL("hide-text")
    data object NoWrap : ZenityOpt, KOptL("no-wrap")
    data class FileName(val fn: String): ZenityOpt, KOptL("filename", fn)
    data object Multiple : ZenityOpt, KOptL("multiple")
    data object Directory : ZenityOpt, KOptL("directory")
    data object Save : ZenityOpt, KOptL("save")
    data class Separator(val s: String): ZenityOpt, KOptL("separator", s)
    data object ConfirmOverwrite : ZenityOpt, KOptL("confirm-overwrite")
    data class Column(val header: String): ZenityOpt, KOptL("column", header)
    data object CheckList : ZenityOpt, KOptL("checklist")
    data object RadioList : ZenityOpt, KOptL("radiolist")
    data object Editable : ZenityOpt, KOptL("editable")
    data class PrintColumn(val c: String): ZenityOpt, KOptL("print-column", c)
    data class HideColumn(val c: Int): ZenityOpt, KOptL("hide-column", c.toString())
    data object Listen : ZenityOpt, KOptL("listen")
    data class Percentage(val p: Int): ZenityOpt, KOptL("percentage", p.toString())
    data object AutoClose : ZenityOpt, KOptL("auto-close")
    data object AutoKill : ZenityOpt, KOptL("auto-kill")
    data object Pulsate : ZenityOpt, KOptL("pulsate")
    data class InitValue(val v: Int): ZenityOpt, KOptL("value", v.toString())
    data class MinValue(val v: Int): ZenityOpt, KOptL("min-value", v.toString())
    data class MaxValue(val v: Int): ZenityOpt, KOptL("max-value", v.toString())
    data class Step(val v: Int): ZenityOpt, KOptL("step", v.toString())
    data object PrintPartial : ZenityOpt, KOptL("print-partial")
}


