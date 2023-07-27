@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.*

/**
 * Secret values are locally encrypted before being sent to GitHub.
 * @param secretName
 * @param secretValue if not provided, the gh will try to ask interactively (in terminal)
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format
 */
fun CliPlatform.ghSecretSetExec(secretName: String, secretValue: String? = null, repoPath: String? = null) =
    ghSecretSet(secretName, repoPath).exec(inContent = secretValue)

fun CliPlatform.ghSecretSetFromFileExec(secretName: String, filePath: String, repoPath: String? = null) =
    ghSecretSet(secretName, repoPath).exec(inFile = filePath)

fun CliPlatform.ghSecretListExec(repoPath: String? = null) = ghSecretList(repoPath).exec()

/**
 * Secret values are locally encrypted before being sent to GitHub.
 * secretValue should be given as input to .exec(..); if not provided, the gh will try to ask interactively (in terminal)
 * @param secretName
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format
 */
fun ghSecretSet(secretName: String, repoPath: String? = null) =
    gh(GhCmd.SecretSet()) { +secretName; repoPath?.let { -Repo(it) } }

fun ghSecretList(repoPath: String? = null, init: GhCmd.SecretList.() -> Unit = {}) =
    gh(GhCmd.SecretList()) { repoPath?.let { -Repo(it) }; init() }

fun ghHelp(init: GhCmd.Help.() -> Unit = {}) =
    gh(GhCmd.Help(), init)

fun ghVersion(init: GhCmd.Version.() -> Unit = {}) =
    gh(GhCmd.Version(), init)

fun ghStatus(init: GhCmd.Status.() -> Unit = {}) =
    gh(GhCmd.Status(), init)

fun <GhOptT: KOpt, GhCmdT: GhCmd<GhOptT>> gh(cmd: GhCmdT, init: GhCmdT.() -> Unit = {}) =
    Gh(cmd.apply(init))

/** [gh manual](https://cli.github.com/manual/index) */
data class Gh(
    val cmd: GhCmd<*>
) : Kommand {
    override val name get() = "gh"
    override val args get() = cmd.toArgs()
}

abstract class GhCmd<KOptT: KOpt>: Kommand {

    val cmdNameWords get() = this::class.simpleName!!
        .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }

    val nonopts: MutableList<String> = mutableListOf()
    val opts: MutableList<KOptT> = mutableListOf()

    override val name get() = cmdNameWords.first()
    override val args get() = cmdNameWords.drop(1) + nonopts + opts.toArgsFlat()

    operator fun String.unaryPlus() = nonopts.add(this)
    operator fun KOptT.unaryMinus() = opts.add(this)

    class Help: GhCmd<GhCommonOpt>()
    class Version: GhCmd<GhCommonOpt>()
    class Status: GhCmd<GhStatusOpt>()
    class SecretList: GhCmd<GhSecretListOpt>()
    class SecretSet(val secretName: String? = null): GhCmd<GhSecretSetOpt>()
}

interface GhStatusOpt: KOpt
interface GhSecretOpt: KOpt, GhSecretListOpt, GhSecretSetOpt
interface GhSecretListOpt: KOpt
interface GhSecretSetOpt: KOpt
interface GhCommonOpt: KOpt, GhStatusOpt, GhSecretOpt

/** @param path [HOST/]OWNER/REPO */
data class Repo(val path: String): GhOpt("repo", path), GhSecretOpt

data object Help: GhOpt("help"), GhCommonOpt

/**
 * It's impossible to use anyway because we always have mandatory GhCmd in Gh class.
 * But let's leave it anyway to signal that actual gh command accepts such (unnecessary) option.
 */
@Deprecated("Use version command instead of option.", ReplaceWith("ghVersion"))
data object Version: GhOpt("version")

/** @param repos list of repos to exclude in owner/name format */
data class Exclude(val repos: List<String>): GhOpt("exclude", repos.joinToString(",")), GhStatusOpt {
    constructor(vararg repo: String): this(repo.toList())
}


data class Org(val organization: String): GhOpt("org", organization), GhStatusOpt

abstract class GhOpt(name: String, arg: String? = null): KOptL(name, arg, nameSeparator = " ") {
    // TODO_later: can I also set name automatially using ::class.simpleName?
}