package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

fun CliPlatform.rmIfFileIsThereExec(file: String) =
    if (testIfFileIsThere(file)) rm { +file }.exec() else listOf("File not found")

fun CliPlatform.rmTreeWithForceExec(path: String, doubleCheck: CliPlatform.(path: String) -> Boolean): List<String> {
    check(doubleCheck(path)) { "ERROR: Can not remove whole '$path' tree. Double check failed." }
    return rm { -Rm.Option.recursive; -Rm.Option.force; +path }.exec()
}

fun rm(init: Rm.() -> Unit = {}) = Rm().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/rm.1.html) */
data class Rm(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "rm"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object force : Option("--force")
        object interactive : Option("--interactive")
        object onefs : Option("--one-file-system")
        object recursive : Option("--recursive")
        object dir : Option("--dir")
        object verbose : Option("--verbose")
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
