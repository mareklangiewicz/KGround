@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.*

fun ghHelp(init: GhHelp.() -> Unit = {}) = GhHelp().apply(init)
fun ghVersion(init: GhVersion.() -> Unit = {}) = GhVersion().apply(init)
fun ghStatus(init: GhStatus.() -> Unit = {}) = GhStatus().apply(init)

/** [gh manual](https://cli.github.com/manual/index) */
abstract class GhKommand<KOptGhT: KOptGh>: Kommand {

    val ghKommandNameWords get() = classSimpleWords()
        .also { check(it.first() == "gh") }

    val nonopts: MutableList<String> = mutableListOf()
    val opts: MutableList<KOptGhT> = mutableListOf()

    override val name get() = ghKommandNameWords.first()
    override val args get() = ghKommandNameWords.drop(1) + nonopts + opts.toArgsFlat()

    operator fun String.unaryPlus() = nonopts.add(this)
    operator fun KOptGhT.unaryMinus() = opts.add(this)
}

class GhHelp: GhKommand<KOptGhCommon>()
class GhVersion: GhKommand<KOptGhCommon>()
class GhStatus: GhKommand<KOptGhStatus>()
class GhSecretList: GhKommand<KOptGhSecretList>()
class GhSecretSet: GhKommand<KOptGhSecretSet>()
class GhSecretDelete: GhKommand<KOptGhSecretDelete>()
class GhRepoView: GhKommand<KOptGhRepoView>()
class GhRepoList: GhKommand<KOptGhRepoList>()

abstract class GhOpt(val arg: String? = null): KOptGh {
    override fun toArgs(): List<String> = listOf("--" + classSimpleWords().joinToString("-")) plusIfNN arg
}


// Reversed hierarhy of options to mark which GhKommand accepts which options.
// KOptGh prefix chosen to be clearly distinct from normal implementation tree prefixes: GhKommand, GhXXX: GhKommand, GhOpt

interface KOptGh: KOpt
interface KOptGhStatus: KOptGh
interface KOptGhRepoView: KOptGh
interface KOptGhRepoList: KOptGh
interface KOptGhRepo: KOptGh, KOptGhRepoList, KOptGhRepoView
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

data class Org(val ghOrganization: String): GhOpt(ghOrganization), KOptGhSecret, KOptGhStatus

data object User: GhOpt(), KOptGhSecret

data class Visibility(val vis: String): GhOpt(vis), KOptGhSecretSet, KOptGhRepoList

/** @param repos list of repos to exclude in owner/name format */
data class Exclude(val repos: List<String>): GhOpt(repos.joinToString(",")), KOptGhStatus {
    constructor(vararg repo: String): this(repo.toList())
}

data object Web: GhOpt(), KOptGhRepoView

data class Branch(val name: String): GhOpt(name), KOptGhRepoView

data object Archived: GhOpt(), KOptGhRepoList
data object NoArchived: GhOpt(), KOptGhRepoList
data object Fork: GhOpt(), KOptGhRepoList
data object Source: GhOpt(), KOptGhRepoList // means NotFork

data class Language(val name: String): GhOpt(name), KOptGhRepoList

data class Limit(val max: Int): GhOpt(max.toString()), KOptGhRepoList

data class Topic(val name: String): GhOpt(name), KOptGhRepoList

// null means just list available json fields, no actual data
data class Json(val fields: String? = null): GhOpt(fields), KOptGhRepo
data class Jq(val expression: String): GhOpt(expression), KOptGhRepo
data class Template(val template: String): GhOpt(template), KOptGhRepo
