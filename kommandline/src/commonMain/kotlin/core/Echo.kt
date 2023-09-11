package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.EchoOpt.*

/**
 * @param withNewLine adds trailing newline at the end of [line].
 *   Note: normally it doesn't matter because in KommandLine stdout is read line-wise anyway.
 *   But it matters when we redirect output to file, for example with: [CliPlatform.start] start(..., outFile=...),
 *   or when we inject echo kommand line into some bash script with pipes, etc.
 */

@OptIn(DelicateKommandApi::class)
fun echo(
    line: String,
    vararg useNamedArgs: Unit,
    withEscapes: Boolean = false,
    withNewLine: Boolean = true,
) = echo {
    if (withEscapes) -Escapes(enable = true)
    if (!withNewLine) -NoNewLine
    +line
}

@DelicateKommandApi
fun echo(init: Echo.() -> Unit = {}) = Echo().apply(init)

/**
 * [gnu coreutils echo manual](https://www.gnu.org/software/coreutils/manual/html_node/echo-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/echo.1.html)
 */
@DelicateKommandApi
data class Echo(
    override val opts: MutableList<EchoOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<EchoOpt> { override val name get() = "echo" }

@DelicateKommandApi
interface EchoOpt: KOptTypical {
    data object NoNewLine : KOptS("n"), EchoOpt
    data class Escapes(val enable: Boolean = false) : KOptS(if (enable) "e" else "E"), EchoOpt
    data object Help : KOptLN(), EchoOpt
    data object Version : KOptLN(), EchoOpt
}
