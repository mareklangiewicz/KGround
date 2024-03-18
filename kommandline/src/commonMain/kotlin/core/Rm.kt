package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.RmOpt.*

@OptIn(DelicateApi::class)
fun rmIfFileExists(file: String) = ReducedScript { cli, dir ->
    val exists = testIfFileExists(file).exec(cli, dir)
    if (exists) rm(file).exec(cli, dir = dir)
    else listOf("File not found")
}

@DelicateApi
fun rmTreeWithForce(path: String, doubleChk: (cli: CLI, path: String) -> Boolean) =
    ReducedScript { cli, dir ->
        chk(doubleChk(cli, path)) { "ERROR: Can not remove whole '$path' tree. Double chk failed." }
        rm(path, recursive = true, force = true).exec(cli, dir = dir)
    }

@DelicateApi
fun rm(
    path: String,
    vararg useNamedArgs: Unit,
    recursive: Boolean = false,
    force: Boolean = false,
    verbose: Boolean = false
) = rm { if (recursive) -Recursive; if (force) -Force; if (verbose) -Verbose; +path }

@DelicateApi
fun rm(init: Rm.() -> Unit) = Rm().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/rm.1.html) */
@DelicateApi
data class Rm(
    override val opts: MutableList<RmOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<RmOpt> { override val name get() = "rm" }

@DelicateApi
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
