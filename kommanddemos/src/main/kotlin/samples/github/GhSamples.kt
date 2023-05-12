package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.github.Gh.Cmd.*
import pl.mareklangiewicz.kommand.github.Gh.Option.*
import pl.mareklangiewicz.kommand.samples.*

object GhSamples {
    val help = gh(Gh.Cmd.help) s "gh help"
    val helpOption = gh { -Gh.Option.help } s "gh --help"
    val version = gh { -Gh.Option.version } s "gh --version"
    val status = gh(Gh.Cmd.status) s "gh status"
    val secretListHelp = gh(secret_list) { -Gh.Option.help } s "gh secret list --help"
    val secretList = gh(secret_list) s "gh secret list"
    val secretListForAbcdK =
        gh(secret_list) { -Gh.Option.repo("langara/AbcdK") } s "gh secret list --repo langara/AbcdK"

    /** I don't provide fake secret as input stream when starting this kommand, so it will ask me interactively */
    val secretSetFakeSecretInAbcdK =
        gh(secret_set) { +"FAKE_SECRET"; -repo("langara/AbcdK") } s "gh secret set FAKE_SECRET --repo langara/AbcdK"
}