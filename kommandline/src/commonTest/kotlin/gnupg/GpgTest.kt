package pl.mareklangiewicz.kommand.gnupg

import kotlin.test.*
import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.runBlockingWithCLIOnJvmOnly
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.uctx.uctx


// FIXME NOW: refactor as samples

@OptIn(DelicateApi::class, NotPortableApi::class)
class GpgTest {
  @Test fun testGpgHelp() = gpg { -Help }
    .tryInteractivelyCheckBlockingOrErr("gpg --help")

  @Test fun testGpgListKeys() = gpg(ListPublicKeys)
    .tryInteractivelyCheckBlockingOrErr("gpg --list-public-keys")

  @Test fun testGpgListKeysVerbose() = gpg(ListPublicKeys) { -Verbose }
    .tryInteractivelyCheckBlockingOrErr("gpg --list-public-keys --verbose")

  @Test fun testGpgListSecretKeysVerbose() = gpg(ListSecretKeys) { -Verbose }
    .tryInteractivelyCheckBlockingOrErr("gpg --list-secret-keys --verbose")

  @Suppress("DEPRECATION")
  @Test fun testGpgEncryptDecrypt() {
    runBlockingWithCLIOnJvmOnly {
      val inFile = mktemp(prefix = "testGED").ax()
      val encFile = "$inFile.enc"
      val decFile = "$inFile.dec"
      writeFileWithDD(inLines = listOf("some plain text 667"), outFile = inFile).ax()
      gpgEncryptPass("correct pass", inFile, encFile).ax()
      gpgDecryptPass("correct pass", encFile, decFile).ax()
      val decrypted = readFileWithCat(decFile).ax().single()
      assertEquals("some plain text 667", decrypted)
      val errCode = implictx<CLI>().start(gpgDecryptPass("incorrect pass", encFile, "$decFile.err")).waitForExit()
      assertEquals(2, errCode)
      rm { +inFile; +encFile; +decFile }.ax()
    }
  }
}

