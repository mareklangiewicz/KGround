@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.Kommand

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
fun gnometerm(kommand: Kommand? = null, init: GnomeTerm.() -> Unit = {}) = GnomeTerm(kommand).apply(init)

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
data class GnomeTerm(
    val kommand: Kommand? = null,
    val options: MutableList<Option> = mutableListOf()
) : Kommand {
    override val name get() = "gnome-terminal"
    override val args get() = options.map { it.str } + kommand?.let { listOf("--", it.name) + it.args }.orEmpty()

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = arg?.let { "$name=$arg" } ?: name
        data class title(val title: String) : Option("--title", title)
        data object help : Option("--help")
        data object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}