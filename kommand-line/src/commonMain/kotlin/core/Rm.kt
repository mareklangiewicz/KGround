package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.RmOpt.*
import pl.mareklangiewicz.udata.MutLO
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class)
fun rmFileIfExists(file: Path): ReducedScript<List<String>> = ReducedScript {
  val exists = testIfFileExists(file).ax()
  if (exists) rm(file).ax()
  else listOf("File not found")
}

@OptIn(DelicateApi::class)
fun rmDirIfEmpty(dir: Path): Rm = rm { -Dir; +dir.strf }

@DelicateApi
fun rmTreeWithForce(rootDir: Path, doubleChk: suspend (path: Path) -> Boolean): ReducedScript<List<String>> =
  ReducedScript {
    doubleChk(rootDir).chkTrue { "ERROR: Can not remove whole '$rootDir' tree. Double chk failed." }
    rm(rootDir, recursive = true, force = true).ax()
  }

@DelicateApi
fun rm(
  path: Path,
  vararg useNamedArgs: Unit,
  recursive: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
): Rm = rm { if (recursive) -Recursive; if (force) -Force; if (verbose) -Verbose; +path.strf }

@DelicateApi
fun rm(init: Rm.() -> Unit): Rm = Rm().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/rm.1.html) */
@DelicateApi
data class Rm(
  override val opts: MutableList<RmOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
) : KommandTypical<RmOpt> {
  override val name get() = "rm"
}

@DelicateApi
interface RmOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), RmOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), RmOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), RmOpt
  // endregion [GNU Common Opts]


  /** ignore nonexistent files and arguments, never prompt */
  data object Force : RmOpt, KOptLN() // Don't risk short -f (better to be explicit with FORCE)

  /** prompt before every removal */
  data object PromptAlways : RmOpt, KOptS("i")

  /** prompt once before removing more than three files, or when removing  recursively */
  data object PromptOnce : RmOpt, KOptS("I")

  data object OneFileSystem : RmOpt, KOptLN()

  data object Recursive : RmOpt, KOptLN() // Don't risk short -r or -R (better to be explicit about RECURSIVE)

  /** remove empty directories */
  data object Dir : RmOpt, KOptS("d")

  /** explain what is being done */
  data object Verbose : RmOpt, KOptLN() // Don't risk short -v (ambiguity with "version")
}
