@file:OptIn(NotPortableApi::class, DelicateApi::class)

package pl.mareklangiewicz.kommand

import kotlin.random.Random
import kotlin.random.nextUInt
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.konfig.IKonfig
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.upue.asEncodedIfAbc16
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory

class KonfigTests {

  @TestFactory
  fun uspekTests() = uspekTestFactory {
    testTmpKonfig()
  }
}

fun testTmpKonfig(cli: CLI = provideSysCLI()) {
  val konfigNewDir = cli.pathToUserTmp!! + "/tmpKonfigForTests" + Random.nextUInt()

  "On konfig in tmp dir" o { // Warning: adding $konfigNewDir to test name would make uspek generate infinite loop!!
    try {
      val konfig = konfigInDir(konfigNewDir, cli)
      testGivenNewKonfigInDir(cli, konfig, konfigNewDir)
    } finally {
      rmTreeWithForce(konfigNewDir) { path -> cli.pathToUserTmp!! in path && "tmpKonfigForTests" in path }
        .axBlockingOrErr(cli)
    }
  }
}

fun testGivenNewKonfigInDir(cli: CLI, konfig: IKonfig, dir: String) {
  "is empty" o { konfig.keys.len eq 0 }
  "dir is created" o { testIfFileIsDirectory(dir).axBlockingOrErr(cli) eq true }
  "dir is empty" o { ls(dir, wHidden = true).axBlockingOrErr(cli).size eq 0 }

  "On setting new key and value" o {
    konfig["somekey1"] = "somevalue1"

    "get returns stored value" o { konfig["somekey1"] eq "somevalue1" }
    "file is created" o { testIfFileIsRegular("$dir/somekey1").axBlockingOrErr(cli) eq true }
    "no other files there" o { ls(dir, wHidden = true).axBlockingOrErr(cli) eq listOf("somekey1") }
    "file for somekey1 contains correct content" o {
      val content = readFileWithCat("$dir/somekey1").axBlockingOrErr(cli).joinToString("\n")
      content eq "somevalue1"
    }

    "On changing value to null" o {
      konfig["somekey1"] = null

      "get returns null" o { konfig["somekey1"] eq null }
      "file is removed" o { testIfFileExists("$dir/somekey1").axBlockingOrErr(cli) eq false }
      "no files in konfig dir" o { ls(dir, wHidden = true).axBlockingOrErr(cli).size eq 0 }

      "On touch removed file again" o {
        touch("$dir/somekey1").axBlockingOrErr(cli)
        "file is there again" o { testIfFileExists("$dir/somekey1").axBlockingOrErr(cli) eq true }
        "get returns not null but empty value" o { konfig["somekey1"] eq "" }
      }
    }
  }

  "On konfig.asEncodedIfAbc16" o {
    val konfigAbc16 = konfig.asEncodedIfAbc16(null)
    // TODO: more tests
  }
}
