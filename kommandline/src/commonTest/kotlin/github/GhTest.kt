package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.checkWithUser
import pl.mareklangiewicz.kommand.github.Gh.Cmd.*
import pl.mareklangiewicz.kommand.github.Gh.Option.*
import pl.mareklangiewicz.kommand.github.Gh.Option.help
import kotlin.test.Test

class GhTest {
    @Test fun testGhHelp1() = gh(Gh.Cmd.help).checkWithUser("gh help")
    @Test fun testGhHelp2() = gh { -help }.checkWithUser("gh --help")
    @Test fun testGhVersion() = gh { -version }.checkWithUser("gh --version")
    @Test fun testGhStatus() = gh(status).checkWithUser("gh status")
    @Test fun testGhSecretListHelp() = gh(secret_list) { -help }.checkWithUser("gh secret list --help")
    @Test fun testGhSecretList() = gh(secret_list).checkWithUser("gh secret list")
    @Test fun testGhSecretListForAbcdK() = gh(secret_list) { -repo("langara/AbcdK") }
        .checkWithUser("gh secret list --repo langara/AbcdK")

    // I don't provide fake secret as input stream when starting this kommand, so it will ask me interactively
    @Test fun testGhSecretSetFakeForAbcdK() = gh(secret_set) { +"FAKE_SECRET"; -repo("langara/AbcdK") }
        .checkWithUser("gh secret set FAKE_SECRET --repo langara/AbcdK")
}