package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.MkdirOpt.*

@OptIn(DelicateKommandApi::class)
fun mkdir(dir: String, withParents: Boolean = false) = mkdir { if (withParents) -Parents; +dir }

@DelicateKommandApi
fun mkdir(init: Mkdir.() -> Unit = {}) = Mkdir().apply(init)

/**
 * [linux man](https://man7.org/linux/man-pages/man1/mkdir.1.html)
 * [gnu coreutils mkdir manual](https://www.gnu.org/software/coreutils/manual/html_node/mkdir-invocation.html#mkdir-invocation)
 */
@DelicateKommandApi
class Mkdir(
    override val opts: MutableList<MkdirOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<MkdirOpt> {
    override val name get() = "mkdir"
}

@DelicateKommandApi
interface MkdirOpt: KOptTypical {

    /** set file mode (as in chmod), not a=rwx - umask */
    data class Mode(val mode: String): KOptS("m", mode), MkdirOpt

    /**
     * no error if existing, make parent directories as needed,
     * with their file modes unaffected by any -m option.
     */
    data object Parents : KOptS("p"), MkdirOpt

    /** print a message for each created directory */
    data object Verbose : KOptS("v"), MkdirOpt

    data object Help : KOptL("help"), MkdirOpt

    data object Version : KOptL("version"), MkdirOpt
}
