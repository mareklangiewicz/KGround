package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kground.namelowords
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.IndicatorStyle.*
import pl.mareklangiewicz.udata.strf

fun lsRegFiles(dir: Path, wHidden: Boolean = false): ReducedKommand<List<Path>> =
  ls(dir, wHidden = wHidden, wIndicator = Slash).reducedOut { toList().filter { !it.endsWith('/') }.map { it.pth } }

fun lsSubDirs(dir: Path, wHidden: Boolean = false): ReducedKommand<List<Path>> =
  ls(dir, wHidden = wHidden, wIndicator = Slash).reducedOut {
    toList().filter { it.endsWith('/') }.map { it.dropLast(1).pth }
  }

/**
 * Generally null values mean: default, so it depends on the underlying system (not necessarily "none").
 * For example, on my machine "man ls" says that default indicator-style is "none",
 * but when I try it (no alias, output piped to file), it works as if it was "slash".
 * BTW default color should always be fine (null == default == "auto"),
 * because it only adds colors when stdout is terminal (not piped to any file or sth)
 */
fun ls(
  vararg paths: Path,
  wHidden: Boolean = false,
  wIndicator: IndicatorStyle? = null,
  wColor: ColorType? = null,
  wDirsFirst: Boolean = false,
  init: Ls.() -> Unit = {},
) = Ls().apply {
  for (p in paths) +p.strf
  if (wHidden) -AlmostAll
  wIndicator?.let { -Indicator(it) }
  wColor?.let { -Color(it) }
  if (wDirsFirst) -DirsFirst
  init()
}


/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
@OptIn(DelicateApi::class)
data class Ls(
  override val opts: MutableList<LsOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<LsOpt> {
  override val name get() = "ls"
}

@OptIn(DelicateApi::class)
interface LsOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), LsOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), LsOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), LsOpt
  // endregion [GNU Common Opts]

  /** list one file per line even when stdout is terminal */
  data object One : KOptS("1"), LsOpt

  /** end each output line with NUL, not newline */
  data object Zero : KOptLN(), LsOpt

  /** do not ignore entries starting with "." */
  data object All : KOptS("a"), LsOpt
  /** do not list implied "." and ".." */
  data object AlmostAll : KOptS("A"), LsOpt

  data object Author : KOptLN(), LsOpt

  data object Escape : KOptLN(), LsOpt

  data class BlockSize(val size: String) : KOptLN(size), LsOpt

  data object IgnoreBackups : KOptLN(), LsOpt

  data object ListByColumns : KOptS("C"), LsOpt
  data object ListByLines : KOptS("x"), LsOpt

  data class Color(val type: ColorType) : KOptLN(type.namelowords("")), LsOpt
  enum class ColorType { Always, Auto, Never }

  data object Directory : KOptS("d"), LsOpt

  data object Dired : KOptLN(), LsOpt

  /** do not sort, enable -aU, disable -ls --color */
  data object Raw : KOptS("f"), LsOpt

  /** append indicator (one of *=/>@|) to entries */
  data object Classify : KOptS("F"), LsOpt

  /** like classify, except do not add * symbol */
  data object ClassifyFileType : KOptL("file-type"), LsOpt

  data class Format(val type: FormatType) : KOptLN(type.namelowords("")), LsOpt
  enum class FormatType { Across, Commas, Horizontal, Long, SingleColumn, Verbose, Vertical }

  /** like -l --time-style=full-iso */
  data object FullTime : KOptLN(), LsOpt

  data object DirsFirst : KOptL("group-directories-first"), LsOpt

  data object NoGroup : KOptS("G"), LsOpt

  data object LongFormat : KOptS("l"), LsOpt // option name is not just Long to avoid clash with Long data type
  data object LongWithoutOwner : KOptS("g"), LsOpt
  data object LongWithoutGroup : KOptS("o"), LsOpt

  data object HumanReadable : KOptS("h"), LsOpt
  data object HumanReadableSI : KOptL("si"), LsOpt

  data object Dereference : KOptLN(), LsOpt
  data object DereferenceCommandLine : KOptLN(), LsOpt
  data object DereferenceCommandLineSymlinkToDir : KOptLN(), LsOpt

  data class Hide(val pattern: String) : KOptLN(pattern), LsOpt
  /** print ? instead of nongraphic characters */
  data object HideControlChars : KOptLN(), LsOpt
  /** show nongraphic characters as-is (the default, unless program is 'ls' and output is a terminal) */
  data object ShowControlChars : KOptLN(), LsOpt

  data class Hyperlink(val type: HyperlinkType) : KOptLN(type.namelowords("")), LsOpt
  enum class HyperlinkType { Always, Auto, Never }

  data class Indicator(val style: IndicatorStyle) : KOptL("indicator-style", style.namelowords("-")), LsOpt
  enum class IndicatorStyle { None, Slash, FileType, Classify }

  data object IndicatorSlash : KOptS("p"), LsOpt

  data object INode : KOptL("inode"), LsOpt

  data class Ignore(val pattern: String) : KOptLN(pattern), LsOpt

  data object Kibibytes : KOptLN(), LsOpt

  data object Commas : KOptS("m"), LsOpt

  data object NumericUidGid : KOptLN(), LsOpt

  data object Literal : KOptLN(), LsOpt

  data object QuoteName : KOptLN(), LsOpt

  data class Quoting(val style: QuotingStyle) : KOptL("quoting-style", style.namelowords("-")), LsOpt
  enum class QuotingStyle { Literal, Locale, Shell, ShellAlways, ShellEscape, ShellEscapeAlways, C, Escape }

  data object Reverse : KOptS("r"), LsOpt

  data object Recursive : KOptS("R"), LsOpt

  data object Size : KOptS("s"), LsOpt

  data class Sort(val type: SortType) : KOptLN(type.namelowords("")), LsOpt
  enum class SortType { None, Size, Time, Version, Extension }
  /** largest first */
  data object SortBySize : KOptS("S"), LsOpt
  data object SortByTime : KOptS("t"), LsOpt
  /** do not sort */
  data object SortByNothing : KOptS("U"), LsOpt
  /** Natural sort of (version) numbers within text */
  data object SortByVersion : KOptS("v"), LsOpt
  data object SortByExtension : KOptS("X"), LsOpt

  data class Time(val type: TimeType) : KOptLN(type.namelowords("")), LsOpt

  /**
   * There are duplicates:
   * last access time: ATime, Access, Use
   * last change time: CTime, Status
   * creation time: Birth, Creation
   */
  enum class TimeType { ATime, Access, Use, CTime, Status, Birth, Creation }

  data object TimeOfAccess : KOptS("u"), LsOpt
  data object TimeOfChange : KOptS("c"), LsOpt
  data object TimeOfBirth : KOptL("time", "birth"), LsOpt

  data class TimeStyle(val style: String) : KOptLN(style), LsOpt

  data class TabSize(val size: Int) : KOptL("tabsize", "$size"), LsOpt

  data class Width(val columns: Int) : KOptLN("$columns"), LsOpt

  data object PrintContext : KOptL("context"), LsOpt
}
