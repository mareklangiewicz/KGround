package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*
import kotlin.test.*
import kotlin.test.Test

class GpgTest {
    @Test fun testGpgHelp() = gpg { -help }
        .checkWithUser("gpg --help")

    @Test fun testGpgListKeys() = gpg(listkeys)
        .checkWithUser("gpg --list-keys")

    @Test fun testGpgListKeysVerbose() = gpg(listkeys) { -verbose }
        .checkWithUser("gpg --verbose --list-keys")

    @Suppress("DEPRECATION")
    @Test fun testGpgEncryptDecrypt() = ifOnNiceJvmPlatform {
        val inFile = mktempExec("testGED")
        val encFile = "$inFile.enc"
        val decFile = "$inFile.dec"
        writeFileWithEchoExec("some plain text 667", outFile = inFile)
        gpgEncryptPass("correct pass", inFile, encFile).exec()
        gpgDecryptPass("correct pass", encFile, decFile).exec()
        val decrypted = readFileWithCatExec(decFile, singleLine = true)
        assertEquals("some plain text 667", decrypted)
        val err = start(gpgDecryptPass("incorrect pass", encFile, "$decFile.err")).await()
        assertEquals(2, err.exitValue)
        rm { +inFile; +encFile; +decFile }.exec()
    }
}

private fun ifOnNiceJvmPlatform(block: CliPlatform.() -> Unit) = CliPlatform.SYS.run {
    if (isJvm && isUbuntu) block() else println("Disabled on this platform.")
}

