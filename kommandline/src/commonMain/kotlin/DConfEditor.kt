package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.DConfEditor.Option

/** [dconf-editor ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/dconf-editor.1.html) */
fun dconfedit(vararg options: Option, init: DConfEditor.() -> Unit = {}) =
    DConfEditor(options.toMutableList()).apply(init)

/** [dconf-editor ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/dconf-editor.1.html) */
data class DConfEditor(
    val options: MutableList<Option> = mutableListOf(),
    val nonopts: MutableList<String> = mutableListOf(),
) : Kommand {
    override val name get() = "dconf-editor"
    override val args get() = options.map { it.str } + nonopts

    sealed class Option(val str: String) {
        data object Help : Option("--help")
        data object Version : Option("--version")
        data object RelocatableSchemas : Option("--list-relocatable-schemas")
        data object SkipWarning : Option("--I-understand-that-changing-options-can-break-applications")
    }

    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}