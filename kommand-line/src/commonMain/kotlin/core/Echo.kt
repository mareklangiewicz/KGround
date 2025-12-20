package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.SecondaryApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.EchoOpt.*
import pl.mareklangiewicz.udata.MutLO

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
  override val opts: MutableList<EchoOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
) : KommandTypical<EchoOpt> {
  override val name get() = "echo"
}

@DelicateApi
interface EchoOpt : KOptTypical {
  data object NoNewLine : KOptS("n"), EchoOpt
  data class Escapes(val enable: Boolean = false) : KOptS(if (enable) "e" else "E"), EchoOpt

  // Keeping these two below mostly as documentation showing that echo is a bit unusual command/builtin.
  @Deprecated("Usually will NOT work f.e. if shell builtin echo is used instead of actual /usr/bin/echo")
  data object Help : KOptLN(), EchoOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  @Deprecated("Usually will NOT work f.e. if shell builtin echo is used instead of actual /usr/bin/echo")
  data object Version : KOptLN(), EchoOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
}
