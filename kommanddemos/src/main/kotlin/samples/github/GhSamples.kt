package pl.mareklangiewicz.kommand.github

import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import pl.mareklangiewicz.kommand.github.Gh.Cmd.*
import pl.mareklangiewicz.kommand.github.Gh.Option.*
import pl.mareklangiewicz.kommand.github.Gh.Option.help
import pl.mareklangiewicz.kommand.samples.*

val GhSamples = "github.gh".st(
    gh(Gh.Cmd.help) s "gh help",
    gh { -help } s "gh --help",
    gh { -version } s "gh --version",
    gh(status) s "gh status",
    gh(secret_list) { -help } s "gh secret list --help",
    gh(secret_list) s "gh secret list",
    gh(secret_list) { -repo("langara/AbcdK") } s "gh secret list --repo langara/AbcdK",
    // I don't provide fake secret as input stream when starting this kommand, so it will ask me interactively
    gh(secret_set) { +"FAKE_SECRET"; -repo("langara/AbcdK") } s "gh secret set FAKE_SECRET --repo langara/AbcdK",
)