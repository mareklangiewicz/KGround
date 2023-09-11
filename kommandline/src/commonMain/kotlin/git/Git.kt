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
fun git(cmd: GitCmd? = null, init: Git.() -> Unit = {}) = Git().apply {
    init()
    cmd?.let {
        chk(opts.all { it !is GitCmd }) { "There can be only one GitCmd" }
        -cmd
    }
}

/** https://git-scm.com/docs/user-manual.html */
@DelicateKommandApi
data class Git(
    override val opts: MutableList<GitOpt> = mutableListOf(), // last GitOpt should always be GitCmd
    override val nonopts: MutableList<String> = mutableListOf(), // here is all stuff local for given GitCmd
): KommandTypical<GitOpt> { override val name get() = "git" }

@OptIn(DelicateKommandApi::class)
interface GitOpt: KOptTypical {
    data object Help : GitOpt, KOptLN()
    data object Version : GitOpt, KOptLN()
    data object Paginate : GitOpt, KOptLN()
    data object Bare : GitOpt, KOptLN()

    data class InPath(val path: String): GitOpt, KOptS("C", path)

    data class GitDir(val dir: String) : GitOpt, KOptLN(dir)
    data class WorkTree(val path: String) : GitOpt, KOptLN(path)
    data class Namespace(val path: String) : GitOpt, KOptLN(path)
}

@OptIn(DelicateKommandApi::class)
sealed class GitCmd: GitOpt, KOptLN(namePrefix = "") {
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

