@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.github.Gh.Cmd
import pl.mareklangiewicz.kommand.github.Gh.Cmd.*
import pl.mareklangiewicz.kommand.github.Gh.Option.*

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
    gh(secret_set) { +secretName; repoPath?.let { -repo(it) } }

fun ghSecretList(repoPath: String? = null) = gh(secret_list) { repoPath?.let { -repo(it) } }

fun gh(cmd: Cmd? = null, init: Gh.() -> Unit = {}) = Gh(cmd).apply(init)

/** [gh manual](https://cli.github.com/manual/index) */
data class Gh(
    val cmd: Cmd? = null,
    val cmdargs: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
) : Kommand {

    override val name get() = "gh"
    override val args get() = cmd?.str?.split(' ').orEmpty() + cmdargs + options.flatMap { it.str }

    enum class Cmd(val str: String) {
        help("help"),
        status("status"),
        secret_list("secret list"),
        secret_set("secret set"),
    }

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNN arg
        object help : Option("--help")
        object version : Option("--version")
        /** @param repoPath [HOST/]OWNER/REPO */
        data class repo(val repoPath: String): Option("--repo", repoPath)
    }

    operator fun String.unaryPlus() = cmdargs.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}
