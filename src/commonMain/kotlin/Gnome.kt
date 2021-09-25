@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

// FIXME: not really gnome related??
/** [linux man](https://man7.org/linux/man-pages/man1/journalctl.1.html) */
data class JournalCtl(
    val options: MutableList<Option> = mutableListOf(),
    val matches: MutableList<String> = mutableListOf()
) : Kommand {

    sealed class Option {
        /**
         * Show only the most recent journal entries, and continuously
         * print new entries as they are appended to the journal.
         */
        object follow : Option() { override fun toString() = "-f" }

        /**
         * generates a very terse output, only showing the actual
         * message of each journal entry with no metadata, not even
         * a timestamp.
         */
        object cat : Option() { override fun toString() = "-o cat" }
        object help : Option() { override fun toString() = "--help" }
        object version : Option() { override fun toString() = "--version" }

    }

    override fun toString() = "journalctl ${options.joinToString(" ")} ${matches.joinToString(" ")}"

    operator fun String.unaryPlus() = matches.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}

fun journalctl(vararg options: JournalCtl.Option, init: JournalCtl.() -> Unit) = JournalCtl(options.toMutableList()).apply(init)
