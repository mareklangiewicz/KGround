@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeExt.Cmd
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.help
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.install

fun gnomeext(cmd: Cmd, init: GnomeExt.() -> Unit = {}) = GnomeExt(cmd).apply(init)

data class GnomeExt(
    var cmd: Cmd = help(),
    val options: MutableList<Option> = mutableListOf()
) : Kommand {
    override val name get() = "gnome-extensions"
    override val args get() = cmd.str + options.map { it.str } plusIfNotNull (cmd as? install)?.pack

    sealed class Cmd(val name: String, open val uuid: String? = null) {
        open val str get() = listOf(name) plusIfNotNull uuid

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
