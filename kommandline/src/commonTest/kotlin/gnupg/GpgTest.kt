package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheck
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.ulog.d
import kotlin.test.*
import kotlin.test.Test

@OptIn(DelicateApi::class)
class GpgTest {
  @Test fun testGpgHelp() = gpg { -Help }
    .tryInteractivelyCheck("gpg --help")

  @Test fun testGpgListKeys() = gpg(ListPublicKeys)
    .tryInteractivelyCheck("gpg --list-public-keys")

  @Test fun testGpgListKeysVerbose() = gpg(ListPublicKeys) { -Verbose }
    .tryInteractivelyCheck("gpg --list-public-keys --verbose")

  @Test fun testGpgListSecretKeysVerbose() = gpg(ListSecretKeys) { -Verbose }
    .tryInteractivelyCheck("gpg --list-secret-keys --verbose")

  @Suppress("DEPRECATION")
  @Test fun testGpgEncryptDecrypt() = ifOnNiceJvmCLI {
    val inFile = mktemp(prefix = "testGED").axb(this)
    val encFile = "$inFile.enc"
    val decFile = "$inFile.dec"
    writeFileWithDD(inLines = listOf("some plain text 667"), outFile = inFile).axb(this)
    gpgEncryptPass("correct pass", inFile, encFile).axb(this)
    gpgDecryptPass("correct pass", encFile, decFile).axb(this)
    val decrypted = readFileWithCat(decFile).axb(this).single()
    assertEquals("some plain text 667", decrypted)
    val errCode = start(gpgDecryptPass("incorrect pass", encFile, "$decFile.err")).waitForExit()
    assertEquals(2, errCode)
    rm { +inFile; +encFile; +decFile }.axb(this)
  }
}

private fun ifOnNiceJvmCLI(block: CLI.() -> Unit) = CLI.SYS.run {
  if (isJvm && isUbuntu) block() else ulog.d("Disabled on this CLI.")
}

