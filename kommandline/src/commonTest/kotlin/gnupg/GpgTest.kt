package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*
import kotlin.test.*
import kotlin.test.Test

class GpgTest {
    @Test fun testGpgHelp() = gpg { -Help }
        .chkWithUser("gpg --help")

    @Test fun testGpgListKeys() = gpg(ListKeys)
        .chkWithUser("gpg --list-keys")

    @Test fun testGpgListKeysVerbose() = gpg(ListKeys) { -Verbose }
        .chkWithUser("gpg --verbose --list-keys")

    @Test fun testGpgListSecretKeysVerbose() = gpg(ListSecretKeys) { -Verbose }
        .chkWithUser("gpg --verbose --list-secret-keys")

    @Suppress("DEPRECATION")
    @Test fun testGpgEncryptDecrypt() = ifOnNiceJvmPlatform {
        val inFile = mktemp(prefix = "testGED").execb(this)
        val encFile = "$inFile.enc"
        val decFile = "$inFile.dec"
        writeFileWithDD(inLines = listOf("some plain text 667"), outFile = inFile).execb(this)
        gpgEncryptPass("correct pass", inFile, encFile).execb(this)
        gpgDecryptPass("correct pass", encFile, decFile).execb(this)
        val decrypted = readFileWithCat(decFile).execb(this).single()
        assertEquals("some plain text 667", decrypted)
        val errCode = start(gpgDecryptPass("incorrect pass", encFile, "$decFile.err")).waitForExit()
        assertEquals(2, errCode)
        rm { +inFile; +encFile; +decFile }.execb(this)
    }
}

private fun ifOnNiceJvmPlatform(block: CliPlatform.() -> Unit) = CliPlatform.SYS.run {
    if (isJvm && isUbuntu) block() else println("Disabled on this platform.")
}

