package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.MutLO
import pl.mareklangiewicz.udata.strf

/** Update the access and modification times of each file to the current time. Create empty files if necessary. */
@OptIn(DelicateApi::class)
fun touch(vararg files: Path) = touch { nonopts.addAll(files.map { it.strf }) }.reducedOutToUnit()

@DelicateApi
fun touch(init: Touch.() -> Unit) = Touch().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/touch.1.html) */
@DelicateApi
data class Touch(
  override val opts: MutableList<TouchOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
) : KommandTypical<TouchOpt> {
  override val name get() = "touch"
}



@DelicateApi
interface TouchOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), TouchOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), TouchOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), TouchOpt
  // endregion [GNU Common Opts]

  data object TimeOfAccessOnly : TouchOpt, KOptS("a")
  data object TimeOfChangeOnly : TouchOpt, KOptS("m")
  data object DisableCreation : TouchOpt, KOptS("c")
  data class Date(val date: String) : TouchOpt, KOptLN(date)
}
