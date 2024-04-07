@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*

fun ghSecretList(repoPath: String? = null, init: GhSecretList.() -> Unit = {}) =
    GhSecretList().apply { repoPath?.let { -Repo(it) }; init() }

fun ghSecretSet(
    vararg useNamedArgs: Unit,
    secretName: String,
    secretValue: String,
    repoPath: String? = null,
) =
    ghSecretSet(secretName, repoPath = repoPath).reducedManually {
        stdin.collect(flowOf(secretValue), lineEnd = "", finallyStdinClose = true)
        awaitAndChkExit(firstCollectErr = true)
    }

/**
 * Secret values are locally encrypted before being sent to GitHub.
 * secretValue should be written to stdin; if not, the gh will try to ask interactively (in terminal)
 * @param secretName
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format
 * @param org Set secret for given organization.
 */
fun ghSecretSet(
    secretName: String,
    vararg useNamedArgs: Unit,
    repoPath: String? = null,
    org: String? = null,
    orgVAll: Boolean = false,
    orgVPrivate: Boolean = false,
    orgVSelected: Boolean = false,
) =
    GhSecretSet().apply {
        +secretName
        repoPath?.let { -Repo(it) }
        org?.let { -Org(it) }
        orgVAll && -Visibility("all")
        orgVPrivate && -Visibility("private")
        orgVSelected && -Visibility("selected")
    }

fun ghSecretDelete(
    secretName: String,
    vararg useNamedArgs: Unit,
    repoPath: String? = null,
) =
    GhSecretDelete().apply { +secretName; repoPath?.let { -Repo(it) } }
