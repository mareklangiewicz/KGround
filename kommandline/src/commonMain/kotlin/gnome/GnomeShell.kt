@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option

/**[gnome-shell ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-shell.1.html) */
fun gnomeshell(vararg options: Option, init: GnomeShell.() -> Unit = {}) =
    GnomeShell(options.toMutableList()).apply(init)

/**[gnome-shell ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-shell.1.html) */
data class GnomeShell(
    val options: MutableList<Option> = mutableListOf()
) : Kommand {
    override val name get() = "gnome-shell"
    override val args get() = options.map { it.str }

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = if (arg == null) name else "$name=$arg"

        object nested : Option("--nested")
        object wayland : Option("--wayland")
        object replace : Option("--replace")
        object smdisable : Option("--sm-disable")
        data class smclientid(val id: String) : Option("--sm-client-id", id)
        data class smsavefile(val file: String) : Option("--sm-save-file", file)
        data class screen(val s: String) : Option("--screen", s)
        data class display(val d: String) : Option("--display", d)
        object sync : Option("--sync")
        object version : Option("--version")
        object help : Option("--help")
        data class mode(val m: String) : Option("--mode", m)
        object listmodes : Option("--listmodes")
        data class clutterdisplay(val d: String) : Option("--clutter-display", d)
    }

    operator fun Option.unaryMinus() = options.add(this)
}
