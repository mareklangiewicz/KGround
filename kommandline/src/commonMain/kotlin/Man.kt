package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Man.Section

fun man(section: Section? = null, init: Man.() -> Unit = {}) = Man(section).apply(init)
fun man(number: Int, init: Man.() -> Unit = {}) = man(enumValues<Section>().single { it.number == number }, init)

data class Man(
    var section: Section? = null,
    val options: MutableList<Option> = mutableListOf(),
    val nonopts: MutableList<String> = mutableListOf()
): Kommand {
    override val name get() = "man"
    override val args get() = options.map { it.str }.plusIfNN(section?.number?.toString()) + nonopts

    enum class Section(val number: Int) {
        execorshell(1), systemcall(2), librarycall(3), specialfile(4), fileformat(5),
        game(6), miscellaneous(7), systemadmin(8), kernelroutine(9)
    }

    sealed class Option(val str: String) {
        object all : Option("--all")
        object update : Option("--update")
        object debug : Option("--debug")
        object default : Option("--default")
        object warnings : Option("--warnings")
        object whatis : Option("--whatis")
        object apropos : Option("--apropos")
        object globalapropos : Option("--global-apropos")
        object where : Option("--where")
        object regex : Option("--regex")
        object wildcard : Option("--wildcard")
        object namesonly : Option("--names-only")
        object help : Option("--help")
        object usage : Option("--usage")
        object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}