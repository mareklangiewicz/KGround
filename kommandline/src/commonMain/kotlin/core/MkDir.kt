package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.MkDirOpt.*

@OptIn(DelicateKommandApi::class)
fun mkdir(dir: String, withParents: Boolean = false) =
    mkdir { if (withParents) -Parents; +dir }.reducedOutToUnit()

@DelicateKommandApi
fun mkdir(init: MkDir.() -> Unit = {}) = MkDir().apply(init)

/**
 * [linux man](https://man7.org/linux/man-pages/man1/mkdir.1.html)
 * [gnu coreutils mkdir manual](https://www.gnu.org/software/coreutils/manual/html_node/mkdir-invocation.html#mkdir-invocation)
 */
@DelicateKommandApi
data class MkDir(
    override val opts: MutableList<MkDirOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<MkDirOpt> { override val name get() = "mkdir" }

@DelicateKommandApi
interface MkDirOpt: KOptTypical {

    /** set file mode (as in chmod), not a=rwx - umask */
    data class Mode(val mode: String): KOptS("m", mode), MkDirOpt

    /**
     * no error if existing, make parent directories as needed,
     * with their file modes unaffected by any -m option.
     */
    data object Parents : KOptS("p"), MkDirOpt

    /** print a message for each created directory */
    data object Verbose : KOptS("v"), MkDirOpt

    data object Help : KOptL("help"), MkDirOpt

    data object Version : KOptL("version"), MkDirOpt
}
