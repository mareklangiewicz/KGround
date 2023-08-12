package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.git.Git.*
import pl.mareklangiewicz.kommand.git.Git.Command.*

/** @return single line with hash of given revision/commit */
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
    override val args get() = options.flatMap { it.str } + stuff.prependIfNN(command?.str)

    sealed class Command(val str: String) {
        data object add : Command("add")
        data object archive : Command("archive")
        data object bisect : Command("bisect")
        data object branch : Command("branch")
        data object bundle : Command("bundle")
        data object checkout : Command("checkout")
        data object cherrypick : Command("cherry-pick")
        data object citool : Command("citool")
        data object clean : Command("clean")
        data object clone : Command("clone")
        data object commit : Command("commit")
        data object describe : Command("describe")
        data object diff : Command("diff")
        data object fetch : Command("fetch")
        data object gc : Command("gc")
        data object grep : Command("grep")
        data object gui : Command("gui")
        data object help : Command("help")
        data object init : Command("init")
        data object log : Command("log")
        data object maintenance : Command("maintenance")
        data object merge : Command("merge")
        data object mv : Command("mv")
        data object notes : Command("notes")
        data object pull : Command("pull")
        data object push : Command("push")
        data object rebase : Command("rebase")
        data object reset : Command("reset")
        data object restore : Command("restore")
        data object revert : Command("revert")
        data object rm : Command("rm")
        data object shortlog : Command("shortlog")
        data object show : Command("show")
        data object stash : Command("stash")
        data object status : Command("status")
        data object submodule : Command("submodule")
        data object switch : Command("switch")
        data object tag : Command("tag")
        data object worktree : Command("worktree")

        data object revparse : Command("rev-parse")
    }

    sealed class Option(val name: String, open val arg: String? = null) {
        open val str get() = listOf(name) plusIfNN arg
        data object help : Option("--help")
        data object version : Option("--version")
        data object paginate : Option("--paginate")
        data object bare : Option("--bare")

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
