@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.kommand

import okio.Path
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.udata.strf
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.chkEmpty
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.bad.chkThis
import pl.mareklangiewicz.bad.chkThrows
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kgroundx.maintenance.ZenitySupervisor
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.shell.bashQuoteMetaChars
import pl.mareklangiewicz.kommand.konfig.IKonfig
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.uspek.USpekContext
import pl.mareklangiewicz.uspek.USpekTree
import pl.mareklangiewicz.uspek.failed
import pl.mareklangiewicz.uspek.so
import pl.mareklangiewicz.uspek.suspek
import pl.mareklangiewicz.uspek.ucontext



suspend fun testGivenNewKonfigInDir(konfig: IKonfig, dir: Path) {
  "is empty" so { konfig.keys.len chkEq 0 }
  "dir is created" so { testIfFileIsDirectory(dir).ax() chkEq true }
  "dir is empty" so { ls(dir, wHidden = true).ax().size chkEq 0 }

  "On setting new key and value" so {
    konfig["somekey1"] = "somevalue1"

    "get returns stored value" so { konfig["somekey1"] chkEq "somevalue1" }
    "file is created" so { testIfFileIsRegular(dir / "somekey1").ax() chkEq true }
    "no other files there" so { ls(dir, wHidden = true).ax() chkEq listOf("somekey1") }
    "file for somekey1 contains correct content" so {
      val content = readFileWithCat(dir / "somekey1").ax().joinToString("\n")
      content chkEq "somevalue1"
    }

    "On changing value to null" so {
      konfig["somekey1"] = null

      "get returns null" so { konfig["somekey1"] chkEq null }
      "file is removed" so { testIfFileExists(dir / "somekey1").ax() chkEq false }
      "no files in konfig dir" so { ls(dir, wHidden = true).ax().size chkEq 0 }

      "On touch removed file again" so {
        touch(dir / "somekey1").ax()
        "file is there again" so { testIfFileExists(dir / "somekey1").ax() chkEq true }
        "get returns not null but empty value" so { konfig["somekey1"] chkEq "" }
      }
    }
  }

  // "On konfig.asEncodedIfAbc16" so {
  //   val konfigAbc16 = konfig.asEncodedIfAbc16(null)
  //   // TODO: more tests
  // }
}

