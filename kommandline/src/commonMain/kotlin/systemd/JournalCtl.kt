package pl.mareklangiewicz.kommand.systemd

import pl.mareklangiewicz.kommand.*

/** [journalctl linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
fun journalctl(init: JournalCtl.() -> Unit = {}) = JournalCtl().apply(init)

/** [journalctl linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
data class JournalCtl(
    val options: MutableList<Option> = mutableListOf(),
    val matches: MutableList<String> = mutableListOf()
) : Kommand {
    override val name get() = "journalctl"
    override val args get() = options.map { it.str } + matches

    sealed class Option(val str: String) {
        /**
         * Show only the most recent journal entries, and continuously
         * print new entries as they are appended to the journal.
         */
        object follow : Option("-f")
        /**
         * generates a very terse output, only showing the actual
         * message of each journal entry with no metadata, not even
         * a timestamp.
         */
        object cat : Option("-ocat") // FIXME_later: separate -o and type
        object help : Option("--help")
        object version : Option("--version")
    }
    operator fun String.unaryPlus() = matches.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}
