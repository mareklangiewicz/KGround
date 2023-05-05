package pl.mareklangiewicz.kommand

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.konfig.IKonfig
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.upue.asEncodedIfAbc16
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory
import kotlin.random.Random
import kotlin.random.nextUInt

class KonfigTests {

    @TestFactory
    fun uspekTests() = uspekTestFactory {
        SYS.testTmpKonfig()
    }
}

fun Platform.testTmpKonfig() {
    val konfigNewDir = pathToUserTmp!! + "/tmpKonfigForTests" + Random.nextUInt()

    "On konfig in tmp dir" o { // Warning: adding $konfigNewDir to test name would make uspek generate infinite loop!!
        try {
            val konfig = konfigInDir(konfigNewDir)
            testGivenNewKonfigInDir(konfig, konfigNewDir)
        }
        finally {
            rmTreeWithForce(konfigNewDir) { pathToUserTmp!! in it && "tmpKonfigForTests" in it }
        }
    }
}

fun Platform.testGivenNewKonfigInDir(konfig: IKonfig, dir: String) {
    "is empty" o { konfig.keys.len eq 0 }
    "dir is created" o { testIfFileIsDirectory(dir) eq true }
    "dir is empty" o { ls(dir, withHidden = true).size eq 0 }

    "On setting new key and value" o {
        konfig["somekey1"] = "somevalue1"

        "get returns stored value" o { konfig["somekey1"] eq "somevalue1" }
        "file is created" o { testIfFileIsRegular("$dir/somekey1") eq true }
        "no other files there" o { ls(dir, withHidden = true) eq listOf("somekey1") }
        "file for somekey1 contains correct content" o {
            val content = readFileWithCat("$dir/somekey1")
            content eq "somevalue1"
        }

        "On changing value to null" o {
            konfig["somekey1"] = null

            "get returns null" o { konfig["somekey1"] eq null }
            "file is removed" o { testIfFileIsThere("$dir/somekey1") eq false }
            "no files in konfig dir" o { ls(dir, withHidden = true).size eq 0 }
        }
    }

    "On konfig.asEncodedIfAbc16" o {
        val konfigAbc16 = konfig.asEncodedIfAbc16(null)
        // TODO: more tests
    }
}
