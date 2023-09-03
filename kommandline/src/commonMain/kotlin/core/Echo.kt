package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

fun echo(text: String) = echo { +text }

fun echo(init: Echo.() -> Unit = {}) = Echo().apply(init)
/**
 * [gnu coreutils echo manual](https://www.gnu.org/software/coreutils/manual/html_node/echo-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/echo.1.html)
 */
data class Echo(
    val options: MutableList<Option> = mutableListOf(),
    val text: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "echo"
    override val args get() = options.map { it.str } + text

    sealed class Option(val str: String) {
        data object noTrailingNewLine : Option("-n")
        data object enableBackslashEscapes : Option("-e")
        data object help : Option("--help")
        data object version : Option("--version")
    }

    operator fun String.unaryPlus() = text.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
