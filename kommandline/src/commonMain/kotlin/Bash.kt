@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

fun bash(script: String, pause: Boolean = false, init: Bash.() -> Unit = {}) =
    Bash(mutableListOf(if (pause) "$script ; echo END.ENTER; read" else script)).apply { -BashOpt.Command; init() }

fun Kommand.withBash(pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(this, pause, init)

fun bash(kommand: Kommand, pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(kommand.line(), pause, init)
    // FIXME_someday: I assumed kommand.line() is correct script and will not interfere with surrounding stuff

fun bashQuoteMetaChars(script: String) = script.replace(Regex("([|&;<>() \\\\\"\\t\\n])"), "\\\\$1")

fun CliPlatform.bashGetExportsExec(): Map<String, String> = bash("export")
    .exec()
    .mapNotNull { line -> Regex("declare -x (\\w+)=\"(.*)\"").matchEntire(line) }
    .associate { match -> match.groups[1]!!.value to match.groups[2]!!.value }

fun CliPlatform.bashGetExportsToFileExec(outFile: String) =
    start(bash("export"), outFile = outFile).waitForResult().check(expectedOutput = emptyList())


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
    /** Normally just one command string (with or without spaces) or a file (when no -c option provided) */
    override val nonopts: MutableList<String> = mutableListOf(),
    override val opts: MutableList<BashOpt> = mutableListOf(),
): KommandTypical<BashOpt> {
    override val name get() = "bash"
}
interface BashOpt: KOptTypical {
    /**
     * interpret first from nonopts as a command_string to run
     * If more nonopts present, they are used to override env variables $0 $1 $2...
     */
    object Command : BashOptS("c")
    object Interactive : BashOptS("i")
    object Login : BashOptS("l")
    object Restricted : BashOptS("r")
    object Posix : BashOptL("posix")
    object Help : BashOptL("help")
    object Version : BashOptL("version")
    object Verbose : BashOptL("verbose")

    open class BashOptS(override val name: String): KOptS(name), BashOpt
    open class BashOptL(override val name: String): KOptL(name), BashOpt
}

