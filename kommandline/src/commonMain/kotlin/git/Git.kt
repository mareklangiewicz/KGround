package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.git.Git.*
import pl.mareklangiewicz.kommand.git.Git.GitCmd.*

/** @return single line with hash of given revision/commit */
fun gitHash(revision: String = "HEAD") = git(revparse) { +revision }
fun gitHelp(commandOrConcept: String? = null) = git(help) { commandOrConcept?.let { +it } }
fun gitStatus(short: Boolean = false, verbose: Boolean = false, vararg pathSpecs: String) = git(status) {
    if (short) +"-s"
    if (verbose) +"-v"
    if (pathSpecs.any { it.startsWith("-") }) +"--"
    pathSpecs.forEach { +it }
}

fun git(command: GitCmd? = null, init: Git.() -> Unit = {}) = Git(command).apply(init)

/** https://git-scm.com/docs/user-manual.html */
data class Git(
    var command: GitCmd? = null,
    val stuff: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf() // global options
): Kommand {
    override val name get() = "git"
    override val args get() = options.flatMap { it.str } + stuff.prependIfNN(command?.str)

    sealed class GitCmd(val str: String) {
        data object add : GitCmd("add")
        data object archive : GitCmd("archive")
        data object bisect : GitCmd("bisect")
        data object branch : GitCmd("branch")
        data object bundle : GitCmd("bundle")
        data object checkout : GitCmd("checkout")
        data object cherrypick : GitCmd("cherry-pick")
        data object citool : GitCmd("citool")
        data object clean : GitCmd("clean")
        data object clone : GitCmd("clone")
        data object commit : GitCmd("commit")
        data object describe : GitCmd("describe")
        data object diff : GitCmd("diff")
        data object fetch : GitCmd("fetch")
        data object gc : GitCmd("gc")
        data object grep : GitCmd("grep")
        data object gui : GitCmd("gui")
        data object help : GitCmd("help")
        data object init : GitCmd("init")
        data object log : GitCmd("log")
        data object maintenance : GitCmd("maintenance")
        data object merge : GitCmd("merge")
        data object mv : GitCmd("mv")
        data object notes : GitCmd("notes")
        data object pull : GitCmd("pull")
        data object push : GitCmd("push")
        data object rebase : GitCmd("rebase")
        data object reset : GitCmd("reset")
        data object restore : GitCmd("restore")
        data object revert : GitCmd("revert")
        data object rm : GitCmd("rm")
        data object shortlog : GitCmd("shortlog")
        data object show : GitCmd("show")
        data object stash : GitCmd("stash")
        data object status : GitCmd("status")
        data object submodule : GitCmd("submodule")
        data object switch : GitCmd("switch")
        data object tag : GitCmd("tag")
        data object worktree : GitCmd("worktree")

        data object revparse : GitCmd("rev-parse")
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
