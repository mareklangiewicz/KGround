package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.udata.MutLO
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class)
fun man(section: ManSection? = null, init: Man.() -> Unit = {}) = man(section?.number, init)

@OptIn(DelicateApi::class)
fun man(sectionNumber: Int?, init: Man.() -> Unit = {}) =
  Man().apply { sectionNumber?.let { +it.strf }; init() }

@DelicateApi
data class Man(
  override val opts: MutableList<ManOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
) : KommandTypical<ManOpt> {
  override val name get() = "man"
}

enum class ManSection(val number: Int) {
  /** Executable programs or shell commands */
  Command(1),
  /** System calls (functions provided by the kernel) */
  SysCall(2),
  /** Library calls (functions within program libraries) */
  LibCall(3),
  /** Special files (usually found in /dev) */
  SpecFile(4),
  /** File formats and conventions, e.g. /etc/passwd */
  FileFormat(5),
  /** Games */
  Game(6),
  /** Miscellaneous (including macro packages and conventions), e.g. man(7), groff(7), man-pages(7) */
  Misc(7),
  /** System administration commands (usually only for root) */
  SysAdmin(8),
  /** Kernel routines [Non standard] */
  KernelRoutine(9),
}

@DelicateApi
interface ManOpt : KOptTypical {

  data object All : KOptS("a"), ManOpt

  data object Update : KOptS("u"), ManOpt

  data object Debug : KOptS("d"), ManOpt

  data object Default : KOptS("D"), ManOpt

  data class Warnings(val warnings: String) : KOptL("warnings", warnings), ManOpt

  data object WhatIs : KOptS("f"), ManOpt

  /**
   * @property global when true makes it search text in all man pages (and likely take some time),
   * instead of searching in short pages descriptions (like the "apropos" cli command).
   */
  data class Apropos(val global: Boolean = false) : KOptS(if (global) "K" else "k"), ManOpt

  data class Where(val catFile: Boolean = false) : KOptS(if (catFile) "W" else "w"), ManOpt

  data class Locale(val locale: String) : KOptS("L", locale), ManOpt

  data object Regex : KOptLN(), ManOpt

  data object Wildcard : KOptLN(), ManOpt

  data object NamesOnly : KOptLN(), ManOpt

  data object Help : KOptLN(), ManOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)

  data object Usage : KOptLN(), ManOpt

  data object Version : KOptLN(), ManOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
}
