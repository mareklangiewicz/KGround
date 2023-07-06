@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

fun bash(script: String, pause: Boolean = false, init: Bash.() -> Unit = {}) =
    Bash(mutableListOf(if (pause) "$script ; echo END.ENTER; read" else script)).apply { -BashOpt.command; init() }

fun Kommand.withBash(pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(this, pause, init)

fun bash(kommand: Kommand, pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(kommand.line(), pause, init)
    // FIXME_someday: I assumed kommand.line() is correct script and will not interfere with surrounding stuff

fun bashQuoteMetaChars(script: String) = script.replace(Regex("([|&;<>() \\\\\"\\t\\n])"), "\\\\$1")

fun CliPlatform.bashGetExportsExec(): Map<String, String> = bash("export")
    .exec()
    .mapNotNull { line -> Regex("declare -x (\\w+)=\"(.*)\"").matchEntire(line) }
    .associate { match -> match.groups[1]!!.value to match.groups[2]!!.value }

fun CliPlatform.bashGetExportsToFileExec(outFile: String) =
    start(bash("export"), outFile = outFile).await().check(expectedOutput = emptyList())


// TODO_someday: better bash composition support; make sure I correctly 'quote' stuff when composing Kommands with Bash
// https://www.gnu.org/savannah-checkouts/gnu/bash/manual/bash.html#Quoting
// TODO_maybe: typesafe DSL for composing bash scripts? (similar to URE)
data class Bash(
    /** Normally just one command string (with or without spaces) or a file (when no -c option provided) */
    val nonopts: MutableList<String> = mutableListOf(),
    val opts: MutableList<BashOpt> = mutableListOf(),
): Kommand {
    override val name get() = "bash"
    override val args get() = opts.flatMap { it.toArgs() } + nonopts

operator fun BashOpt.unaryMinus() = opts.add(this)

}
interface BashOpt: KOpt {
    /**
     * interpret first from nonopts as a command_string to run
     * If more nonopts present, they are used to override env variables $0 $1 $2...
     */
    object command : KOptS("c"), BashOpt
    object interactive : KOptS("i"), BashOpt
    object login : KOptS("l"), BashOpt
    object restricted : KOptS("r"), BashOpt
    object posix : KOptL("posix"), BashOpt
    object help : KOptL("help"), BashOpt
    object version : KOptL("version"), BashOpt
    object verbose : KOptL("verbose"), BashOpt
}

