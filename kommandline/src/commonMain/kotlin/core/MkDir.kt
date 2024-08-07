package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.MkDirOpt.*
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class)
fun mkdir(dir: Path, withParents: Boolean = false) =
  mkdir { if (withParents) -Parents; +dir.strf }.reducedOutToUnit()

@DelicateApi
fun mkdir(init: MkDir.() -> Unit = {}) = MkDir().apply(init)

/**
 * [linux man](https://man7.org/linux/man-pages/man1/mkdir.1.html)
 * [gnu coreutils mkdir manual](https://www.gnu.org/software/coreutils/manual/html_node/mkdir-invocation.html#mkdir-invocation)
 */
@DelicateApi
data class MkDir(
  override val opts: MutableList<MkDirOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<MkDirOpt> {
  override val name get() = "mkdir"
}

@DelicateApi
interface MkDirOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), MkDirOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), MkDirOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), MkDirOpt
  // endregion [GNU Common Opts]

  /** set file mode (as in chmod), not a=rwx - umask */
  data class Mode(val mode: String) : KOptS("m", mode), MkDirOpt

  /**
   * no error if existing, make parent directories as needed,
   * with their file modes unaffected by any -m option.
   */
  data object Parents : KOptS("p"), MkDirOpt

  /** print a message for each created directory */
  data object Verbose : MkDirOpt, KOptLN() // Don't risk short -v (ambiguity with "version")
}
