@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Zenity.DialogType
import pl.mareklangiewicz.kommand.Zenity.Option.nowrap
import pl.mareklangiewicz.kommand.Zenity.Option.text
import pl.mareklangiewicz.kommand.Zenity.Option.title

// FIXME_someday: journalctl is not really gnome related??
fun journalctl(init: JournalCtl.() -> Unit = {}) = JournalCtl().apply(init)
fun gnometerm(kommand: Kommand? = null, init: GnomeTerm.() -> Unit = {}) = GnomeTerm(kommand).apply(init)

// TODO: better support for all gnome-extensions subcommands
fun gnomeext_list() = kommand("gnome-extensions", "list")
fun gnomeext_prefs(extuuid: String) = kommand("gnome-extensions", "prefs", extuuid)

fun notify(summary: String = "", body: String? = null, init: NotifySend.() -> Unit = {}) =
    NotifySend(summary, body).apply(init)

fun zenity(type: DialogType, init: Zenity.() -> Unit = {}) = Zenity(type).apply(init)

fun zenityAskIf(question: String, atitle: String? = null): Boolean = zenity(DialogType.question) {
    -text(question)
    -nowrap
    atitle?.let { -title(it) }
}.shell().exitValue == 0

fun Kommand.execInGnomeTermIfUserConfirms(
    confirmation: String = "Run ::${line()}:: in gnome terminal?",
    insideBash: Boolean = true,
    pauseBeforeExit: Boolean = insideBash,
    execInDir: String? = null
) {
    if (zenityAskIf(confirmation)) {
        val k = when {
            insideBash -> bash(this, pauseBeforeExit)
            pauseBeforeExit -> error("Can not pause before exit if not using bash shell")
            else -> this
        }
        gnometerm(k).exec(execInDir)
    }
}


/** [linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
data class JournalCtl(
    val options: MutableList<Option> = mutableListOf(),
    val matches: MutableList<String> = mutableListOf()
) : Kommand {
    override val name get() = "journalctl"
    override val args get() = options.map { it.str } + matches

    sealed class Option(val str: String) {
        /**
         * Show only the most recent journal entries, and continuously
         * print new entries as they are appended to the journal.
         */
        object follow : Option("-f")
        /**
         * generates a very terse output, only showing the actual
         * message of each journal entry with no metadata, not even
         * a timestamp.
         */
        object cat : Option("-ocat") // FIXME_later: separate -o and type
        object help : Option("--help")
        object version : Option("--version")
    }
    operator fun String.unaryPlus() = matches.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}

data class NotifySend(
    var summary: String = "",
    var body: String? = null,
    val options: MutableList<Option> = mutableListOf(),
): Kommand {
    override val name get() = "notify-send"
    override val args get() = options.map { it.str } + summary plusIfNotNull body
    sealed class Option(val str: String) {
        /** Specifies the urgency level (low, normal, critical). */ // TODO_later: enum for level
        data class urgency(val level: String) : Option("--urgency=$level")
        object help : Option("--help")
        object version : Option("--version")
        // TODO_someday: other options like icon, category, hint..
    }
    operator fun Option.unaryMinus() = options.add(this)
}

/**
 * https://help.gnome.org/users/zenity/stable/index.html.en
 * https://linux.die.net/man/1/zenity
 */
data class Zenity(
    var type: DialogType,
    val options: MutableList<Option> = mutableListOf(),
): Kommand {
    override val name get() = "zenity"
    override val args get() = listOf(type.str) + options.map { it.str }
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
}



data class GnomeTerm(
    val kommand: Kommand? = null,
    val options: MutableList<Option> = mutableListOf()
) : Kommand {
    override val name get() = "gnome-terminal"
    override val args get() = options.map { it.str } + kommand?.let { listOf("--", it.name) + it.args }.orEmpty()

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = arg?.let { "$name=$arg" } ?: name
        data class title(val title: String) : Option("--title", title)
        object help : Option("--help")
        object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}

// TODO: commands: gapplication; gnome-extensions; dbus-send
// for example to be able to clear notifications in optimal ways in comparison to:
// dbus-send --session --type=method_call --dest=org.gnome.Shell /org/gnome/Shell org.gnome.Shell.Eval string:'Main.panel.statusArea.dateMenu._messageList._sectionList.get_children().forEach(s => s.clear());'
// TODO: also: analyze and implement Kommands for stuff like:
// dbus-run-session -- gnome-shell --nested --wayland
// xgettext --output=locale/example.pot *.js (https://www.codeproject.com/Articles/5271677/How-to-Create-A-GNOME-Extension)
// msginit --locale fr --input locale/example.pot --output
// msgfmt example.po --output-file=example.mo
// dconf-editor
// glib-compile-schemas schemas/