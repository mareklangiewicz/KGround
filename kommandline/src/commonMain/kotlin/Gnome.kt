@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

// FIXME_someday: journalctl is not really gnome related??
fun journalctl(init: JournalCtl.() -> Unit = {}) = JournalCtl().apply(init)
fun gnometerm(kommand: Kommand? = null, init: GnomeTerm.() -> Unit = {}) = GnomeTerm(kommand).apply(init)

// TODO: better support for all gnome-extensions subcommands
fun gnomeext_list() = kommand("gnome-extensions", "list")
fun gnomeext_prefs(extuuid: String) = kommand("gnome-extensions", "prefs", extuuid)

fun notify(summary: String = "", body: String? = null, init: NotifySend.() -> Unit = {}) =
    NotifySend(summary, body).apply(init)

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
        object cat : Option("-o cat") // FIXME_later: separate -o and type
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