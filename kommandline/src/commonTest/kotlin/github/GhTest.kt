package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.*
import kotlin.test.Test

class GhTest {
    @Test fun testGhHelp() = ghHelp().checkWithUser("gh help")
    @Test fun testGhVersion() = ghVersion().checkWithUser("gh version")
    @Test fun testGhStatus() = ghStatus().checkWithUser("gh status")
    @Test fun testGhSecretListHelp() = ghSecretList { -Help }.checkWithUser("gh secret list --help")
    @Test fun testGhSecretList() = ghSecretList().checkWithUser("gh secret list")
    @Test fun testGhSecretListForAbcdK() = ghSecretList("langara/AbcdK")
        .checkWithUser("gh secret list --repo langara/AbcdK")

    // I don't provide fake secret as input stream when starting this kommand, so it will ask me interactively
    @Test fun testGhSecretSetFakeForAbcdK() = ghSecretSet("FAKE_SECRET", repoPath = "langara/AbcdK")
        .checkWithUser("gh secret set FAKE_SECRET --repo langara/AbcdK")
}