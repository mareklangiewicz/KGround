package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*

fun CliPlatform.writeFileWithEchoExec(text: String, outFile: String) {
    check(isRedirectFileSupported) { "Can't write to file using echo without redirection." }
    echo(text).exec(outFile = outFile)
}

fun echo(text: String) = echo { +text }

fun echo(init: Echo.() -> Unit = {}) = Echo().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/echo.1.html) */
data class Echo(
    val options: MutableList<Option> = mutableListOf(),
    val text: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "echo"
    override val args get() = options.map { it.str } + text

    sealed class Option(val str: String) {
        object noTrailingNewLine : Option("-n")
        object enableBackslashEscapes : Option("-e")
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = text.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
