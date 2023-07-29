@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*

fun ghSecretSet(
    vararg useNamedArgs: Unit,
    secretName: String,
    secretValue: String,
    repoPath: String? = null
) =
    ghSecretSet(secretName, repoPath = repoPath).reduced {
        stdin.collect(flowOf(secretValue), lineEnd = "", finallyStdinClose = true)
        stdout.onEachLogWithMillis().toList()
    }

/**
 * Secret values are locally encrypted before being sent to GitHub.
 * secretValue should be written to stdin; if not, the gh will try to ask interactively (in terminal)
 * @param secretName
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format
 */
fun ghSecretSet(
    secretName: String,
    vararg useNamedArgs: Unit,
    repoPath: String? = null
) =
    gh(GhCmd.SecretSet()) { +secretName; repoPath?.let { -Repo(it) } }

fun ghSecretDelete(
    secretName: String,
    vararg useNamedArgs: Unit,
    repoPath: String? = null
) =
    gh(GhCmd.SecretDelete()) { +secretName; repoPath?.let { -Repo(it) } }

fun ghSecretList(repoPath: String? = null, init: GhCmd.SecretList.() -> Unit = {}) =
    gh(GhCmd.SecretList()) { repoPath?.let { -Repo(it) }; init() }

/**
 * Display the description and the README of a GitHub repository.
 * With no argument, the repository for the current directory is displayed.
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format.
 * @param branch Non-null means: View a specific branch of the repository.
 * @param web True means open repo in browser instead of printing info to stdout.
 */
fun ghRepoView(
    repoPath: String? = null,
    branch: String? = null,
    web: Boolean = false,
    init: GhCmd.RepoView.() -> Unit = {}
) =
    gh(GhCmd.RepoView()) { repoPath?.let { +it }; branch?.let { -Branch(it) }; if (web) -Web; init() }

fun ghHelp(init: GhCmd.Help.() -> Unit = {}) =
    gh(GhCmd.Help(), init)

fun ghVersion(init: GhCmd.Version.() -> Unit = {}) =
    gh(GhCmd.Version(), init)

fun ghStatus(init: GhCmd.Status.() -> Unit = {}) =
    gh(GhCmd.Status(), init)

fun <GhOptT: KOptGh, GhCmdT: GhCmd<GhOptT>> gh(cmd: GhCmdT, init: GhCmdT.() -> Unit = {}) =
    Gh(cmd.apply(init))


/** [gh manual](https://cli.github.com/manual/index) */
data class Gh(
    val cmd: GhCmd<*>
) : Kommand {
    override val name get() = "gh"
    override val args get() = cmd.toArgs()
}


abstract class GhCmd<KOptGhT: KOptGh>: Kommand {

    val cmdNameWords get() = this::class.simpleName!!
        .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }

    val nonopts: MutableList<String> = mutableListOf()
    val opts: MutableList<KOptGhT> = mutableListOf()

    override val name get() = cmdNameWords.first()
    override val args get() = cmdNameWords.drop(1) + nonopts + opts.toArgsFlat()

    operator fun String.unaryPlus() = nonopts.add(this)
    operator fun KOptGhT.unaryMinus() = opts.add(this)

    class Help: GhCmd<KOptGhCommon>()
    class Version: GhCmd<KOptGhCommon>()
    class Status: GhCmd<KOptGhStatus>()
    class SecretList: GhCmd<KOptGhSecretList>()
    class SecretSet: GhCmd<KOptGhSecretSet>()
    class SecretDelete: GhCmd<KOptGhSecretDelete>()
    class RepoView: GhCmd<KOptGhRepoView>()
}


abstract class GhOpt(val arg: String? = null): KOptGh {
    override fun toArgs(): List<String> = listOf("--" + this::class.simpleName!!.lowercase()) plusIfNN arg
}


// Reversed hierarhy of options to mark which GhCmd accepts which options.
// KOptGh prefix chosen to be clearly distinct from normal implementation tree prefixes: Gh, GhCmd, GhOpt

interface KOptGh: KOpt
interface KOptGhStatus: KOptGh
interface KOptGhRepoView: KOptGh
interface KOptGhSecretList: KOptGh
interface KOptGhSecretSet: KOptGh
interface KOptGhSecretDelete: KOptGh
interface KOptGhSecret: KOptGh, KOptGhSecretList, KOptGhSecretSet, KOptGhSecretDelete
interface KOptGhCommon: KOptGh, KOptGhStatus, KOptGhSecret


data object Help: GhOpt(), KOptGhCommon

/**
 * It's impossible to use anyway because we always have mandatory GhCmd in Gh class.
 * But let's leave it anyway to signal that actual gh command accepts such (unnecessary) option.
 */
@Deprecated("Use version command instead of option.", ReplaceWith("ghVersion"))
data object Version: GhOpt("version")

/** @param path [HOST/]OWNER/REPO */
data class Repo(val path: String): GhOpt(path), KOptGhSecret

/** @param ghApp {actions|codespaces|dependabot} */
data class App(val ghApp: String): GhOpt(ghApp), KOptGhSecret

data class Env(val ghEnv: String): GhOpt(ghEnv), KOptGhSecret

data class Org(val ghOrg: String): GhOpt(ghOrg), KOptGhSecret, KOptGhStatus

data object User: GhOpt(), KOptGhSecret

/** @param vis {all|private|selected} */
data class Visibility(val vis: String): GhOpt(vis), KOptGhSecretSet

/** @param repos list of repos to exclude in owner/name format */
data class Exclude(val repos: List<String>): GhOpt(repos.joinToString(",")), KOptGhStatus {
    constructor(vararg repo: String): this(repo.toList())
}

data object Web: GhOpt(), KOptGhRepoView

data class Branch(val name: String): GhOpt(name), KOptGhRepoView
