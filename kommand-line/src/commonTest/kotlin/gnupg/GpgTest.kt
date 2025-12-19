package pl.mareklangiewicz.kommand.gnupg

import kotlin.test.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.udata.*

@OptIn(DelicateApi::class)
class GpgTest {
  @Suppress("DEPRECATION")
  @Test fun testGpgEncryptDecrypt() {
    runBlockingWithCLIAndULogOnJvmOnly {
      val inFile = mktemp(prefix = "testGED").ax()
      val encFile = "$inFile.enc".P
      val decFile = "$inFile.dec".P
      writeFileWithDD(inLines = listOf("some plain text 667"), outFile = inFile).ax()
      gpgEncryptPass("correct pass", inFile, encFile).ax()
      gpgDecryptPass("correct pass", encFile, decFile).ax()
      val decrypted = readFileWithCat(decFile).ax().single()
      assertEquals("some plain text 667", decrypted)
      val errCode = localCLI().lx(gpgDecryptPass("incorrect pass", encFile, "$decFile.err".P)).waitForExit()
      assertEquals(2, errCode)
      rm { +inFile.strf; +encFile.strf; +decFile.strf }.ax()
    }
  }
}
