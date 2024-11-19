package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kground.io.PRel
import pl.mareklangiewicz.kommand.*

@OptIn(DelicateApi::class)
fun mktemp(
  vararg useNamedArgs: Unit,
  path: Path = PRel,
  prefix: String = "tmp.",
  suffix: String = ".tmp",
): ReducedKommand<Path> = mktemp("$path/${prefix}XXXXXX${suffix}").reducedOut { single().P }

@DelicateApi
fun mktemp(template: String, init: MkTemp.() -> Unit = {}): MkTemp = mktemp { +template; init() }

@DelicateApi
fun mktemp(init: MkTemp.() -> Unit) = MkTemp().apply(init)

@DelicateApi
data class MkTemp(
  override val opts: MutableList<MkTempOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<MkTempOpt> {
  override val name get() = "mktemp"
}

@DelicateApi
interface MkTempOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), MkTempOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), MkTempOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), MkTempOpt
  // endregion [GNU Common Opts]

  data object Directory : KOptS("d"), MkTempOpt
  data object DryRun : KOptS("u"), MkTempOpt
  data object Quiet : KOptS("q"), MkTempOpt
  data class Suffix(val suffix: String) : KOptLN(suffix), MkTempOpt
  data class TmpDir(val dir: String? = null) : KOptS("p", dir), MkTempOpt
}
