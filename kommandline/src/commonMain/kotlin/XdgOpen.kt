package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*

fun xdgopen(file: String, init: XdgOpen.() -> Unit = {}) = XdgOpen(file).apply(init)

data class XdgOpen(
    var file: String? = null,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "xdg-open"
    override val args get() = options.map { it.str } plusIfNN file

    sealed class Option(val str: String) {
        data object Help : Option("--help")
        data object Manual : Option("--manual")
        data object Version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}