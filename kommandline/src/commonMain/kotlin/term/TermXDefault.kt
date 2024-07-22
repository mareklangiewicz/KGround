@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*

/**
 * Marker for [Kommand] that starts new terminal emulator.
 * Sometimes useful to dynamically decide how to launch some kommand in terminal.
 * To avoid wrapping terminal kommand in another terminal kommand.
 */
interface TermKommand : Kommand

/** [debian packages providing x-terminal-emulator](https://packages.debian.org/stable/virtual/x-terminal-emulator) */
@OptIn(DelicateApi::class)
fun termXDefault(kommand: Kommand? = null, init: TermXDefault.() -> Unit = {}): TermXDefault = TermXDefault().apply {
  init()
  kommand?.let { -KOptL(""); nonopts.addAll(kommand.toArgs()) }
  // I assume the "--" separator support. It works at least for gnome-term and kitty,
  // and it clearly separates options from command (and its options) to run.
}

/** [debian packages providing x-terminal-emulator](https://packages.debian.org/stable/virtual/x-terminal-emulator) */
@DelicateApi("Requires x-terminal-emulator; different terminals accept different options.")
data class TermXDefault(
  override val opts: MutableList<KOptTypical> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<KOptTypical>, TermKommand {
  override val name get() = "x-terminal-emulator"
}


