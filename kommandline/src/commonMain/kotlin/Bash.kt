@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Bash.Option.command

fun bash(script: String, pause: Boolean = false, init: Bash.() -> Unit = {}) =
    Bash(mutableListOf(if (pause) "$script ; echo END.ENTER; read" else script)).apply { -command; init() }
fun bash(kommand: Kommand, pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(kommand.line(), pause, init)
    // FIXME_someday: I assumed kommand.line() is correct script and will not interfere with surrounding stuff

fun bashQuoteMetaChars(script: String) = script.replace(Regex("([|&;<>() \\\\\"\\t\\n])"), "\\\\$1")

fun CliPlatform.bashGetExports(): Map<String, String> = bash("export")()
    .mapNotNull { line -> Regex("declare -x (\\w+)=\"(.*)\"").matchEntire(line) }
    .associate { match -> match.groups[1]!!.value to match.groups[2]!!.value }

fun CliPlatform.bashGetExportsToFile(outFile: String) =
    start(bash("export"), outFile = outFile).await().check(expectedOutput = emptyList())


// TODO_someday: better bash composition support; make sure I correctly 'quote' stuff when composing Kommands with Bash
// https://www.gnu.org/savannah-checkouts/gnu/bash/manual/bash.html#Quoting
// TODO_maybe: typesafe DSL for composing bash scripts? (similar to URE)
data class Bash(
    /** a command string (usually just one string with or without spaces) or a file (when no -c option provided) */
    val nonopts: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
): Kommand {
    override val name get() = "bash"
    override val args get() = options.map { it.str } + nonopts

    sealed class Option(val str: String) {
        /** interpret nonopts as a command to run */
        object command : Option("-c")
        object interactive : Option("-i")
        object login : Option("-l")
        object restricted : Option("-r")
        object posix : Option("--posix")
        object help : Option("--help")
        object version : Option("--version")
        object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
