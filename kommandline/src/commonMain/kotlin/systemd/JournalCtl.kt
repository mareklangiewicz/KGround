package pl.mareklangiewicz.kommand.systemd

import pl.mareklangiewicz.kommand.*

/** [journalctl linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
fun journalctl(init: JournalCtl.() -> Unit = {}) = JournalCtl().apply(init)

/** [journalctl linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
data class JournalCtl(
  val options: MutableList<Option> = mutableListOf(),
  val matches: MutableList<String> = mutableListOf(),
) : Kommand {
  override val name get() = "journalctl"
  override val args get() = options.map { it.str } + matches

  sealed class Option(val str: String) {
    /**
     * Show only the most recent journal entries, and continuously
     * print new entries as they are appended to the journal.
     */
    data object Follow : Option("-f")
    /**
     * generates a very terse output, only showing the actual
     * message of each journal entry with no metadata, not even
     * a timestamp.
     */
    data object Cat : Option("-ocat") // FIXME_later: separate -o and type
    data object Help : Option("--help") // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
    data object Version : Option("--version") // Don't risk short -v (ambiguity with "verbose" for many commands)
  }

  operator fun String.unaryPlus() = matches.add(this)
  operator fun Option.unaryMinus() = options.add(this)
}
