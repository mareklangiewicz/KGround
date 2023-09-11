package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import kotlin.test.*
import kotlin.test.Test

@OptIn(DelicateKommandApi::class)
class GpgTest {
    @Test fun testGpgHelp() = gpg { -Help }
        .chkWithUser("gpg --help")

    @Test fun testGpgListKeys() = gpg(ListPublicKeys)
        .chkWithUser("gpg --list-public-keys")

    @Test fun testGpgListKeysVerbose() = gpg(ListPublicKeys) { -Verbose }
        .chkWithUser("gpg --list-public-keys --verbose")

    @Test fun testGpgListSecretKeysVerbose() = gpg(ListSecretKeys) { -Verbose }
        .chkWithUser("gpg --list-secret-keys --verbose")

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

