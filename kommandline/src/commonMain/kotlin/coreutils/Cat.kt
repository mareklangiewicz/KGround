package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*


// TODO_someday: move head/tail so separate files and create more specific kommand data classes

fun CliPlatform.readFileHeadExec(path: String, nrLines: Int = 10) =
    kommand("head", "-n", "$nrLines", path).exec()
fun CliPlatform.readFileFirstLineExec(path: String) =
    readFileHeadExec(path, 1).single()
fun CliPlatform.readFileTailExec(path: String, nrLines: Int = 10) =
    kommand("tail", "-n", "$nrLines", path).exec()
fun CliPlatform.readFileLastLineExec(path: String) =
    readFileTailExec(path, 1).single()



/**
 * If singleLine is true and the file contains more or less than one line, it throws runtime exception.
 * It should never return just part of the file.
 */
fun CliPlatform.readFileWithCatExec(file: String, singleLine: Boolean = false): String = cat { +file }.exec().run {
    if (singleLine) single() else joinToString("\n")
}

/**
 * If singleLine is true and the file contains more, or less, it returns null.
 * If other RuntimeException happens, it also returns null.
 * It should never return just part of the file.
 */
fun CliPlatform.tryToReadFileWithCatExec(file: String, singleLine: Boolean = false): String? =
    try { readFileWithCatExec(file, singleLine) } catch (e: RuntimeException) { null }

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
