package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.github.Gh.Cmd.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.CliPlatform

data object GhSamples {
    val help = gh(Gh.Cmd.help) s "gh help"
    val helpOption = gh { -Gh.Option.help } s "gh --help"
    val version = gh { -Gh.Option.version } s "gh --version"
    val status = gh(Gh.Cmd.status) s "gh status"
    val secretListHelp = gh(secret_list) { -Gh.Option.help } s "gh secret list --help"
    val secretList = ghSecretList() s "gh secret list"
    val secretListForAbcdK = ghSecretList("langara/AbcdK") s "gh secret list --repo langara/AbcdK"

    // Not providing fake secret as input stream when starting this kommand will ask for it interactively
    val secretSetFakeSecretInAbcdK =
        ghSecretSet("FAKE_SECRET", "langara/AbcdK") s "gh secret set FAKE_SECRET --repo langara/AbcdK"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs = listOf(
        CliPlatform::ghSecretListExec,
        CliPlatform::ghSecretSetExec,
        CliPlatform::ghSecretSetFromFileExec
    )
}