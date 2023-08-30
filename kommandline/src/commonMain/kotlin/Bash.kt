@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*

@DelicateKommandApi
fun bash(script: String, pause: Boolean = false, init: Bash.() -> Unit = {}) =
    Bash().apply {
        - BashOpt.Command
        + if (pause) "$script ; echo END.ENTER; read" else script
        init()
    }

@DelicateKommandApi
fun Kommand.withBash(pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(this, pause, init)

@DelicateKommandApi
fun bash(kommand: Kommand, pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(kommand.line(), pause, init)
    // FIXME_someday: I assumed kommand.line() is correct script and will not interfere with surrounding stuff

fun bashQuoteMetaChars(script: String) = script.replace(Regex("([|&;<>() \\\\\"\\t\\n])"), "\\\\$1")

@OptIn(DelicateKommandApi::class)
fun bashGetExportsMap() =
    bash("export").reduced {
        stdout.toList()
            .mapNotNull { line -> Regex("declare -x (\\w+)=\"(.*)\"").matchEntire(line) }
            .associate { match -> match.groups[1]!!.value to match.groups[2]!!.value }
    }

@OptIn(DelicateKommandApi::class)
fun bashGetExportsToFile(outFile: String) =
    bash("export > $outFile").reduced()


// TODO_someday: better bash composition support; make sure I correctly 'quote' stuff when composing Kommands with Bash
// https://www.gnu.org/savannah-checkouts/gnu/bash/manual/bash.html#Quoting
// TODO_maybe: typesafe DSL for composing bash scripts? (similar to URE)
// TODO NOW: mark all lowlevel stuff as delicate api; default api should always fail fast, for example:
//  - always check weird filenames (like with \n)
//  - bash -c more than one nonopt (is confusing and should be opt in)
//  - ssh host command separatearg (instead of ssh host "command arg") is delicate
//    because it always concatenate separatearg with just space and send to remote shell as one script
//  - generally all direct manipulation of Kommand classes should be marked as @DelicateKommandApi!
@DelicateKommandApi
data class Bash(
    override val opts: MutableList<BashOpt> = mutableListOf(),
    /** Normally just one command string (with or without spaces) or a file (when no -c option provided) */
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<BashOpt> {
    override val name get() = "bash"
}

@DelicateKommandApi
interface BashOpt: KOptTypical {
    /**
     * interpret first from nonopts as a command_string to run
     * If more nonopts present, they are used to override env variables $0 $1 $2...
     */
    data object Command : BashOpt, KOptS("c")
    data object Interactive : BashOpt, KOptS("i")
    data object Login : BashOpt, KOptS("l")
    data object Restricted : BashOpt, KOptS("r")

    /**
     * if the -s option is present, or if no arguments remain after option processing,
     * then commands are read from the standard input.
     * This option allows the positional parameters to be set
     * when invoking an interactiveshell or when reading input through a pipe.
     */
    data object Stdin : BashOpt, KOptS("s")
    data object Posix : BashOpt, KOptL("posix")
    data object Help : BashOpt, KOptL("help")
    data object Version : BashOpt, KOptL("version")

    /** Print shell input lines as they are read. */
    data object PrintInput : BashOpt, KOptS("v")

    /** Print commands and their arguments as they are executed. */
    data object PrintExecs : BashOpt, KOptS("x")
}

