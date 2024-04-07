@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.samples.*

data object GhSamples {
    val help =
        ghHelp() s
                "gh help"

    val helpHelp =
        ghHelp { -Help } s
                "gh help --help"

    val helpSecret =
        ghHelp { +"secret" } s
                "gh help secret"

    val helpSecretList =
        ghHelp { +"secret"; +"list" } s
                "gh help secret list"

    val version =
        ghVersion() s
                "gh version"

    val versionHelp =
        ghVersion { -Help } s
                "gh version --help"

    val status =
        ghStatus() s
                "gh status"

    val statusOrgExclude =
        ghStatus { -Org("orgbla"); -Exclude("mareklangiewicz/bla", "mareklangiewicz/ble") } s
                "gh status --org orgbla --exclude mareklangiewicz/bla,mareklangiewicz/ble"

    val secretList =
        ghSecretList() s
                "gh secret list"

    val secretListHelp =
        ghSecretList { -Help } s
                "gh secret list --help"

    val secretListForAbcdK =
        ghSecretList("mareklangiewicz/AbcdK") s
                "gh secret list --repo mareklangiewicz/AbcdK"

    /** Not providing fake secret as input stream when starting this kommand will ask for it interactively */
    val secretSetFakeSecretInAbcdK =
        ghSecretSet("FAKE_SECRET", repoPath = "mareklangiewicz/AbcdK") s
                "gh secret set FAKE_SECRET --repo mareklangiewicz/AbcdK"

    val secretSetConcreteFakeSecret66InAbcdK =
        ghSecretSet(
          secretName = "FAKE_SECRET_66",
          secretValue = "concretevalue66",
          repoPath = "mareklangiewicz/AbcdK",
        ) rs
                "gh secret set FAKE_SECRET_66 --repo mareklangiewicz/AbcdK"

    val secretSetConcreteFakeSecret67InAbcdK =
        ghSecretSet(
          secretName = "FAKE_SECRET_67",
          secretValue = "concretevalue67",
          repoPath = "mareklangiewicz/AbcdK",
        ) rs
                "gh secret set FAKE_SECRET_67 --repo mareklangiewicz/AbcdK"

    val secretDeleteFakeSecretInAbcdK =
        ghSecretDelete(secretName = "FAKE_SECRET", repoPath = "mareklangiewicz/AbcdK") s
                "gh secret delete FAKE_SECRET --repo mareklangiewicz/AbcdK"

    val repoViewAbcdKWeb =
        ghRepoView("mareklangiewicz/AbcdK", web = true) s
                "gh repo view mareklangiewicz/AbcdK --web"

    val repoViewKotlinXCoroutinesDevelopWeb =
        ghRepoView("Kotlin/kotlinx.coroutines", branch = "develop", web = true) s
                "gh repo view Kotlin/kotlinx.coroutines --branch develop --web"

    val repoList =
        ghRepoList() s
                "gh repo list"

    val repoListRomanElizarov =
        ghRepoList("elizarov") s
                "gh repo list elizarov"

    val repoListAvailableJsonFields =
        ghRepoList { -Json() } s
                "gh repo list --json"

    val myPublicRepoListNamesAndUrls =
        ghMyRepoList().outputFields("name", "url") s
                "gh repo list mareklangiewicz --limit 1000 --language kotlin --no-archived --source --visibility public --json name,url --jq .[]|.name,.url"


    val myPublicRepoMarkdownList =
        ghMyRepoList().reducedToMarkdownList() rs
                myPublicRepoListNamesAndUrls.expectedLineRaw

}

fun ghMyRepoList(limit: Int = 1000, language: String? = "kotlin", onlyPublic: Boolean = true) = ghRepoList(
    "mareklangiewicz",
    limit = limit,
    onlyLanguage = language,
    onlyNotArchived = true,
    onlyNotForks = true,
    onlyPublic = onlyPublic,
)
