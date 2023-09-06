package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.RmOpt.*

@OptIn(DelicateKommandApi::class)
fun rmIfFileIsThere(file: String) = ReducedScript { platform, dir ->
    val isThere = platform.testIfFileIsThere(file) // FIXME tests should also use reduced stuff
    if (isThere) rm(file).exec(platform, dir = dir)
    else listOf("File not found")
}

@DelicateKommandApi
fun rmTreeWithForce(path: String, doubleChk: CliPlatform.(path: String) -> Boolean) =
    ReducedScript { platform, dir ->
        chk(platform.doubleChk(path)) { "ERROR: Can not remove whole '$path' tree. Double chk failed." }
        rm(path, recursive = true, force = true).exec(platform, dir = dir)
    }

@DelicateKommandApi
fun rm(
    path: String,
    vararg useNamedArgs: Unit,
    recursive: Boolean = false,
    force: Boolean = false,
    verbose: Boolean = false
) = rm { if (recursive) -Recursive; if (force) -Force; if (verbose) -Verbose; +path }

@DelicateKommandApi
fun rm(init: Rm.() -> Unit) = Rm().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/rm.1.html) */
@DelicateKommandApi
data class Rm(
    override val opts: MutableList<RmOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<RmOpt> { override val name get() = "rm" }

@DelicateKommandApi
interface RmOpt: KOptTypical {

    /** ignore nonexistent files and arguments, never prompt */
    data object Force : RmOpt, KOptS("f")

    /** prompt before every removal */
    data object PromptAlways : RmOpt, KOptS("i")

    /** prompt once before removing more than three files, or when removing  recursively */
    data object PromptOnce : RmOpt, KOptS("I")

    data object OneFileSystem : RmOpt, KOptL("one-file-system")

    data object Recursive : RmOpt, KOptS("r")

    /** remove empty directories */
    data object Dir : RmOpt, KOptS("d")

    /** explain what is being done */
    data object Verbose : RmOpt, KOptS("v")

    data object Help : RmOpt, KOptL("help")

    data object Version : RmOpt, KOptL("version")
}
