package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.git.Git.*
import pl.mareklangiewicz.kommand.git.Git.Command.*

fun gitHash(revision: String = "HEAD") = git(revparse) { +revision }
fun gitHelp(commandOrConcept: String? = null) = git(help) { commandOrConcept?.let { +it } }
fun gitStatus(short: Boolean = false, verbose: Boolean = false, vararg pathSpecs: String) = git(status) {
    if (short) +"-s"
    if (verbose) +"-v"
    if (pathSpecs.any { it.startsWith("-") }) +"--"
    pathSpecs.forEach { +it }
}

fun git(command: Command? = null, init: Git.() -> Unit = {}) = Git(command).apply(init)

/** https://git-scm.com/docs/user-manual.html */
data class Git(
    var command: Command? = null,
    val stuff: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf() // global options
): Kommand {
    override val name get() = "git"
    override val args get() = options.flatMap { it.str } + (stuff prependIfNotNull command?.str)

    sealed class Command(val str: String) {
        object add : Command("add")
        object archive : Command("archive")
        object bisect : Command("bisect")
        object branch : Command("branch")
        object bundle : Command("bundle")
        object checkout : Command("checkout")
        object cherrypick : Command("cherry-pick")
        object citool : Command("citool")
        object clean : Command("clean")
        object clone : Command("clone")
        object commit : Command("commit")
        object describe : Command("describe")
        object diff : Command("diff")
        object fetch : Command("fetch")
        object gc : Command("gc")
        object grep : Command("grep")
        object gui : Command("gui")
        object help : Command("help")
        object init : Command("init")
        object log : Command("log")
        object maintenance : Command("maintenance")
        object merge : Command("merge")
        object mv : Command("mv")
        object notes : Command("notes")
        object pull : Command("pull")
        object push : Command("push")
        object rebase : Command("rebase")
        object reset : Command("reset")
        object restore : Command("restore")
        object revert : Command("revert")
        object rm : Command("rm")
        object shortlog : Command("shortlog")
        object show : Command("show")
        object stash : Command("stash")
        object status : Command("status")
        object submodule : Command("submodule")
        object switch : Command("switch")
        object tag : Command("tag")
        object worktree : Command("worktree")

        object revparse : Command("rev-parse")
    }

    sealed class Option(val name: String, open val arg: String? = null) {
        open val str get() = listOf(name) plusIfNotNull arg
        object help : Option("--help")
        object version : Option("--version")
        object paginate : Option("--paginate")
        object bare : Option("--bare")

        class inpath(val path: String): Option("-C", path)

        sealed class OptionEq(name: String, arg: String) : Option(name, arg) {
            override val str get() = listOf("$name=$arg")
        }

        class gitdir(dir: String) : OptionEq("--git-dir", dir)
        class worktree(path: String) : OptionEq("--work-tree", path)
        class namespace(path: String) : OptionEq("--namespace", path)
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}
