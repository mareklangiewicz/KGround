package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*


@DelicateApi
fun cat(init: Cat.() -> Unit = {}) = Cat().apply(init)
/**
 * [gnu coreutils cat](https://www.gnu.org/software/coreutils/manual/html_node/cat-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/cat.1.html)
 */
@DelicateApi
data class Cat(
  override val opts: MutableList<CatOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<CatOpt> {
  override val name get() = "cat"
}

@DelicateApi
interface CatOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), CatOpt
  data object Version : KOptLN(), CatOpt
  data object EOOpt : KOptL(""), CatOpt
  // endregion [GNU Common Opts]

  /** Number all output lines, starting with 1. This option is ignored if -b is in effect. */
  data object NumberAll : KOptS("n"), CatOpt

  /** Number all nonempty output lines, starting with 1. */
  data object NumberNonBlank : KOptS("b"), CatOpt

  /** Suppress repeated adjacent blank lines; output just one empty line instead of several. */
  data object SqueezeBlank : KOptS("s"), CatOpt

  /** Display a ‘$’ after the end of each line. The \r\n combination is shown as ‘^M$’. */
  data object ShowLineEnds : KOptS("E"), CatOpt

  /** Display TAB characters as ‘^I’. */
  data object ShowTabs : KOptS("T"), CatOpt

  /**
   * Display control characters except for LFD and TAB using ‘^’ notation
   * and precede characters that have the high bit set with ‘M-’.
   */
  data object ShowNonPrinting : KOptS("v"), CatOpt

  /** Equivalent to -vET. */
  data object ShowAll : KOptS("A"), CatOpt

  /** Equivalent to -vE. */
  data object ShowNonPrintingAndLineEnds : KOptS("e"), CatOpt

  /** Equivalent to -vT. */
  data object ShowNonPrintingAndTabs : KOptS("t"), CatOpt
}
