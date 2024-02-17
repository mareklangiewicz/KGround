@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.term.TermKittyOpt.*

/**
 * [kitty homepage](https://sw.kovidgoyal.net/kitty/)
 * [kitty docs](file:///usr/share/doc/kitty/html/overview.html)
 * [kitty man page ubuntu outdated?](https://manpages.ubuntu.com/manpages/focal/man1/kitty.1.html)
 */
@OptIn(DelicateApi::class)
fun termKitty(
    kommand: Kommand? = null,
    one: Boolean = true,
    detach: Boolean = true,
    hold: Boolean = false
) = termKitty(kommand) { if (one) -One; if (detach) -Detach; if (hold) -Hold  }

@DelicateApi
fun termKitty(kommand: Kommand?, init: TermKitty.() -> Unit) =
    TermKitty().apply {
        init()
        kommand?.let { -EOOpt; nonopts.addAll(kommand.toArgs()) }
            // I added the "--" (EOOpt) separator even though docs don't say anything about it, but it works,
            // and it clearly separates options from command (and it's options) to run,
            // and it's consistent with other kommands like termGnome.
    }

/**
 * [kitty homepage](https://sw.kovidgoyal.net/kitty/)
 * [kitty docs](file:///usr/share/doc/kitty/html/overview.html)
 * [kitty man page ubuntu outdated?](https://manpages.ubuntu.com/manpages/focal/man1/kitty.1.html)
 */
@DelicateApi
data class TermKitty(
    override val opts: MutableList<TermKittyOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<TermKittyOpt> {
    override val name get() = "kitty"
}

@OptIn(DelicateApi::class)
interface TermKittyOpt: KOptTypical {
    data class Title(val title: String) : KOptS("T", title), TermKittyOpt
    data class Config(val file: String) : KOptS("c", file), TermKittyOpt
    data class Directory(val directory: String) : KOptS("d", directory), TermKittyOpt

    /** Detach from the controlling terminal, if any. */
    data object Detach : KOptL("detach"), TermKittyOpt

    /**
     * Remain open after child process exits. Note that this only affects the first window.
     * You can quit by either using the close window shortcut or pressing any key.
     */
    data object Hold : KOptL("hold"), TermKittyOpt

    /**
     * Single instance. If specified, only a single instance of kitty will run.
     * New invocations will instead create a new top-level window in the existing kitty instance.
     * This allows kitty to share a single sprite cache on the GPU and also reduces startup time.
     * You can also have separate groups of kitty instances by using the kitty --instance-group option.
     */
    data object One : KOptS("1"), TermKittyOpt

    /**
     * Instance group. Used in combination with "One" option.
     * All kitty invocations with the same group will result in new windows
     * being created in the first kitty instance within that group.
     */
    data class OneInGroup(val group: String) : KOptL("instance-group", group, nameSeparator = " "), TermKittyOpt

    data object Help : KOptS("h"), TermKittyOpt
    data object Version : KOptS("v"), TermKittyOpt
    data object EOOpt : KOptL(""), TermKittyOpt
}