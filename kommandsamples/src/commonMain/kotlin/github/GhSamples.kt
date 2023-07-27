package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.CliPlatform

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
        ghStatus { -Org("orgbla"); -Exclude("langara/bla", "langara/ble") } s
                "gh status --org orgbla --exclude langara/bla,langara/ble"

    val secretList =
        ghSecretList() s
                "gh secret list"

    val secretListHelp =
        ghSecretList { -Help } s
                "gh secret list --help"

    val secretListForAbcdK =
        ghSecretList("langara/AbcdK") s
                "gh secret list --repo langara/AbcdK"

    /** Not providing fake secret as input stream when starting this kommand will ask for it interactively */
    val secretSetFakeSecretInAbcdK =
        ghSecretSet("FAKE_SECRET", "langara/AbcdK") s
                "gh secret set FAKE_SECRET --repo langara/AbcdK"

    @Deprecated("FIXME")
    val execs = listOf(
        CliPlatform::ghSecretListExec,
        CliPlatform::ghSecretSetExec,
        CliPlatform::ghSecretSetFromFileExec
    )
}