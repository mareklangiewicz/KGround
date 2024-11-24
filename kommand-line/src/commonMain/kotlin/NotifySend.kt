package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.udata.MutLO

/** [notify-send ubuntu manpage](https://manpages.ubuntu.com/manpages/impish/man1/notify-send.1.html) */
fun notify(summary: String = "", body: String? = null, init: NotifySend.() -> Unit = {}) =
  NotifySend(summary, body).apply(init)

/** [notify-send ubuntu manpage](https://manpages.ubuntu.com/manpages/impish/man1/notify-send.1.html) */
data class NotifySend(
  var summary: String = "",
  var body: String? = null,
  val options: MutableList<Option> = MutLO(),
) : Kommand {
  override val name get() = "notify-send"
  override val args get() = options.map { it.str } + summary plusIfNN body

  sealed class Option(val str: String) {
    /** Specifies the urgency level (low, normal, critical). */ // TODO_later: enum for level
    data class Urgency(val level: String) : Option("--urgency=$level")
    data object Help : Option("--help") // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
    data object Version : Option("--version") // Don't risk short -v (ambiguity with "verbose" for many commands)
    // TODO_someday: other options like icon, category, hint..
  }

  operator fun Option.unaryMinus() = options.add(this)
}
