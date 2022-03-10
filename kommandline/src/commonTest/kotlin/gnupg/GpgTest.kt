package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*
import kotlin.test.*
class GpgTest {
    @Test fun testGpgHelp() = gpg { -help }
        .checkWithUser("gpg --help")

    @Test fun testGpgListKeys() = gpg(listkeys)
        .checkWithUser("gpg --list-keys")

    @Test fun testGpgListKeysVerbose() = gpg(listkeys) { -verbose }
        .checkWithUser("gpg --verbose --list-keys")
}
