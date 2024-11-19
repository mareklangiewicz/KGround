package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.SecondaryApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.EchoOpt.*

/**
 * @param withNewLine adds trailing newline at the end of [line].
 *   Note: normally it doesn't matter because in KommandLine stdout is read line-wise anyway.
 *   But it matters when we redirect output to file, for example with: [CLI.lx] lx(..., outFile=...),
 *   or when we inject echo kommand line into some bash script with pipes, etc.
 */

@OptIn(DelicateApi::class)
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

@DelicateApi
fun echo(init: Echo.() -> Unit = {}) = Echo().apply(init)

/**
 * [gnu coreutils echo manual](https://www.gnu.org/software/coreutils/manual/html_node/echo-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/echo.1.html)
 */
@DelicateApi
data class Echo(
  override val opts: MutableList<EchoOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<EchoOpt> {
  override val name get() = "echo"
}

@DelicateApi
interface EchoOpt : KOptTypical {
  data object NoNewLine : KOptS("n"), EchoOpt
  data class Escapes(val enable: Boolean = false) : KOptS(if (enable) "e" else "E"), EchoOpt
  @SecondaryApi("May not work if shell builtin echo is used instead of actual /usr/bin/echo")
  data object Help : KOptLN(), EchoOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  @SecondaryApi("May not work if shell builtin echo is used instead of actual /usr/bin/echo")
  data object Version : KOptLN(), EchoOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
}
