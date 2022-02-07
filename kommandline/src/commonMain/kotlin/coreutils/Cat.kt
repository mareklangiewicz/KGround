package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*

fun cat(init: Cat.() -> Unit = {}) = Cat().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/cat.1.html) */
data class Cat(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "cat"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object showEnds : Option("--show-ends")
        object showNonPrinting : Option("--show-nonprinting")
        object showTabs : Option("--show-tabs")
        object squeezeBlank : Option("--squeeze-blank")
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
