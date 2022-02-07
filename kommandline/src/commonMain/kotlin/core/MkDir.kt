package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

fun mkdir(init: MkDir.() -> Unit = {}) = MkDir().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/mkdir.1.html) */
data class MkDir(
    val options: MutableList<Option> = mutableListOf(),
    val dirs: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "mkdir"
    override val args get() = options.map { it.str } + dirs

    sealed class Option(val str: String) {
        object parents : Option("--parents")
        data class mode(val mode: String): Option("--mode=$mode")
        object verbose : Option("--verbose")
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = dirs.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
