@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeExt.Cmd
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.help
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.install

// FIXME_someday: journalctl is not really gnome related??
fun journalctl(init: JournalCtl.() -> Unit = {}) = JournalCtl().apply(init)
fun gnometerm(kommand: Kommand? = null, init: GnomeTerm.() -> Unit = {}) = GnomeTerm(kommand).apply(init)

fun gnomeext(cmd: Cmd, init: GnomeExt.() -> Unit = {}) = GnomeExt(cmd).apply(init)

fun notify(summary: String = "", body: String? = null, init: NotifySend.() -> Unit = {}) =
    NotifySend(summary, body).apply(init)

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

data class GnomeExt(
    var cmd: Cmd = help(),
    val options: MutableList<Option> = mutableListOf()
) : Kommand {
    override val name get() = "gnome-extensions"
    override val args get() = cmd.str + options.map { it.str } plusIfNotNull (cmd as? install)?.pack

    sealed class Cmd(val name: String, open val uuid: String? = null) {
        open val str get() = uuid?.let { listOf(name, it) } ?: listOf(name)
        /** Displays a short synopsis of the available commands or provides detailed help on a specific command. */
        data class help(val cmdname: String? = null): Cmd("help") {
            override val str get() = cmdname?.let { listOf(name, it) } ?: listOf(name)
        }
        /** Prints the program version. */
        object version: Cmd("version")

        /**
         * Enables the extension identified by UUID.
         * The command will not detect any errors from the extension itself,
         * use the info command to confirm that the extension state is ENABLED.
         * If the extension is already enabled, the command will do nothing.
         */
        data class enable(override val uuid: String): Cmd("enable", uuid)
        /** Disables the extension identified by UUID. If the extension is not enabled, the command will do nothing. */
        data class disable(override val uuid: String): Cmd("disable", uuid)

        /**
         * Reset the extension identified by UUID.
         * The extension will be disabled in GNOME, but may be enabled by other sessions like GNOME Classic.
         */
        data class reset(override val uuid: String): Cmd("reset", uuid)

        /** Show details of the extension identified by UUID, including name, description and state. */
        data class info(override val uuid: String): Cmd("info", uuid)
        /** Synonym of info. */
        data class show(override val uuid: String): Cmd("show", uuid)
        /** Displays a list of installed extensions. */
        object list: Cmd("list")
        /** Open the preference dialog of the extension identified by UUID. */
        data class prefs(override val uuid: String): Cmd("prefs", uuid)
        /** Creates a new extension from a template. */
        object create: Cmd("create")

        /**
         * Creates an extension bundle that is suitable for publishing.
         * The bundle will always include the required files extension.js and metadata.json, as well as the optional
         * stylesheet.css and prefs.js if found. Each additional source that should be included must be specified
         * with --extra-source.
         * If the extension includes one or more GSettings schemas, they can either be placed in a schemas/ folder to
         * be picked up automatically, or be specified with --schema.
         * Similarily, translations are included automatically when they are located in a po/ folder, otherwise the
         * --podir option can be used to point to the correct directory. If no gettext domain is provided on the
         * command line, the value of the gettext-domain metadata field is used if it exists, and the extension UUID
         * if not.
         * All files are searched in SOURCE-DIRECTORY if specified, or the current directory otherwise.
         */
        object pack: Cmd("pack")

        /**
         * Installs an extension from the bundle PACK.
         * The command unpacks the extension files and moves them to the expected location in the user’s $HOME, so
         * that it will be loaded in the next session.
         * It is mainly intended for testing, not as a replacement for GNOME Software or the extension website. As
         * extensions have privileged access to the user’s session, it is advised to never load extensions from
         * untrusted sources without carefully reviewing their content.
         */
        data class install(val pack: String): Cmd("install")
        /** Uninstalls the extension identified by UUID. */
        data class uninstall(override val uuid: String): Cmd("uninstall", uuid)
    }

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = arg?.let { "$name=$arg" } ?: name

        // common options:
        /** Do not print error messages */
        object quiet : Option("--quiet")

        // options for list cmd:
        object user : Option("--user")
        object system : Option("--system")
        object enabled : Option("--enabled")
        object disabled : Option("--disabled")
        object prefs : Option("--prefs")
        object updates : Option("--updates")
        object details : Option("--details")

        // options for create cmd:
        data class extname(val n: String) : Option("--name", n)
        data class extdesc(val d: String) : Option("--description", d)
        data class extuuid(val u: String) : Option("--uuid", u)
        data class template(val t: String) : Option("--template", t)
        object interactive : Option("--interactive")

        // options for pack cmd:
        data class extrasource(val path: String) : Option("--extra-source", path)
        data class schema(val s: String) : Option("--schema", s)
        data class podir(val p: String) : Option("--podir", p)
        data class gettextdomain(val d: String) : Option("--gettext-domain", d)
        data class outdir(val d: String) : Option("--out-dir", d)
        object force : Option("--force") // also for install cmd
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