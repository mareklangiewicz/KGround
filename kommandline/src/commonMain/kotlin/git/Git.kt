package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.git.GitCmd.*

/** @return single line with hash of given revision/commit */
@OptIn(DelicateKommandApi::class)
fun gitHash(revision: String = "HEAD") = git(RevParse) { +revision }

@OptIn(DelicateKommandApi::class)
fun gitHelp(commandOrConcept: String? = null) = git(Help) { commandOrConcept?.let { +it } }

@OptIn(DelicateKommandApi::class)
fun gitStatus(short: Boolean = false, verbose: Boolean = false, vararg pathSpecs: String) = git(Status) {
    if (short) +"-s"
    if (verbose) +"-v"
    if (pathSpecs.any { it.startsWith("-") }) +"--"
    pathSpecs.forEach { +it }
}

@OptIn(DelicateKommandApi::class)
fun git(command: GitCmd? = null, init: Git.() -> Unit = {}) = Git(command).apply(init)

/** https://git-scm.com/docs/user-manual.html */
@DelicateKommandApi
data class Git(
    var command: GitCmd? = null,
    val stuff: MutableList<String> = mutableListOf(),
    val options: MutableList<GitOpt> = mutableListOf() // global options
): Kommand {
    override val name get() = "git"
    override val args get() = options.flatMap { it.str } + stuff.prependIfNN(command?.name)

    operator fun GitOpt.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}

sealed class GitOpt(val name: String, open val arg: String? = null) {
    open val str get() = listOf(name) plusIfNN arg
    data object Help : GitOpt("--help")
    data object Version : GitOpt("--version")
    data object Paginate : GitOpt("--paginate")
    data object Bare : GitOpt("--bare")

    class InPath(val path: String): GitOpt("-C", path)

    sealed class GitOptEq(name: String, arg: String) : GitOpt(name, arg) {
        override val str get() = listOf("$name=$arg")
    }

    class GitDir(dir: String) : GitOptEq("--git-dir", dir)
    class WorkTree(path: String) : GitOptEq("--work-tree", path)
    class Namespace(path: String) : GitOptEq("--namespace", path)
}

@OptIn(DelicateKommandApi::class)
sealed class GitCmd: KOptLN(namePrefix = "") {
    data object Add : GitCmd()
    data object Archive : GitCmd()
    data object Bisect : GitCmd()
    data object Branch : GitCmd()
    data object Bundle : GitCmd()
    data object Checkout : GitCmd()
    data object CherryPick : GitCmd()
    data object Citool : GitCmd()
    data object Clean : GitCmd()
    data object Clone : GitCmd()
    data object Commit : GitCmd()
    data object Describe : GitCmd()
    data object Diff : GitCmd()
    data object Fetch : GitCmd()
    data object Gc : GitCmd()
    data object Grep : GitCmd()
    data object Gui : GitCmd()
    data object Help : GitCmd()
    data object Init : GitCmd()
    data object Log : GitCmd()
    data object Maintenance : GitCmd()
    data object Merge : GitCmd()
    data object Mv : GitCmd()
    data object Notes : GitCmd()
    data object Pull : GitCmd()
    data object Push : GitCmd()
    data object Rebase : GitCmd()
    data object Reset : GitCmd()
    data object Restore : GitCmd()
    data object Revert : GitCmd()
    data object Rm : GitCmd()
    data object Shortlog : GitCmd()
    data object Show : GitCmd()
    data object Stash : GitCmd()
    data object Status : GitCmd()
    data object Submodule : GitCmd()
    data object Switch : GitCmd()
    data object Tag : GitCmd()
    data object Worktree : GitCmd()
    data object RevParse : GitCmd()
}

