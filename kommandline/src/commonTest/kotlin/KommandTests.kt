@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.kommand

import kotlin.math.*
import kotlin.random.*
import kotlin.test.*
import okio.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.konfig.*
import pl.mareklangiewicz.kommand.shell.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.uspek.*


class KommandTests {

  init {
    "INIT ${this::class.simpleName}".teePP
  }

  val platform = getCurrentPlatformKind()

  @Test fun t() = runTestUSpekWithWorkarounds {

    "On string with bash meta chars" so {
      val string = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
      "bash quote meta chars correctly" so {
        val quoted = bashQuoteMetaChars(string)
        quoted chkEq "abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno"
      }
    }

    if (platform == "JVM") "On JVM only" so { // TODO_someday: On Native? On NodeJs?

      "On real file system on tmp dir" so {

        "On mktemp kommand" so {
          var tmpFile = "/tmp/fake".pth
          try {
            tmpFile = mktemp(path = "/tmp".pth, prefix = "tmpFile").ax()
            "name is fine" so { tmpFile.chkThis { strf.startsWith("/tmp/tmpFile") && strf.endsWith(".tmp") } }
            "file is there" so {
              lsRegFiles("/tmp".pth).ax().chkThis { any { "/tmp/$it" == tmpFile.strf } }
            }
          } finally {
            rmFileIfExists(tmpFile).ax()
          }
        }
        onMkDirWithParents()
      }
    }
  }
}

private suspend fun onMkDirWithParents() {
  "On mkdir with parents" so {
    // Note: random dirName can't be in test name bc uspek would loop infinitely finding new "branches"
    val dirName = "testDirTmp" + Random.nextLong().absoluteValue
    val tmpDir = "/tmp".pth / dirName
    val tmpDirBla = tmpDir / "bla"
    val tmpDirBlaBle = tmpDirBla / "ble"

    try {
      mkdir(tmpDirBlaBle, withParents = true).chkLineRaw("mkdir -p $tmpDirBlaBle").ax()

      "check created dirs with ls" so {
        lsSubDirs("/tmp".pth).chkLineRaw("ls --indicator-style=slash /tmp")
          .ax().chkThis { strf.contains(dirName) }
      }
      "ls tmp dir is not file" so { lsRegFiles("/tmp".pth).ax().chkThis { !strf.contains(dirName) } }

      "On rm empty ble" so {
        rmDirIfEmpty(tmpDirBlaBle).ax()

        "bla does not contain ble" so { lsSubDirs(tmpDirBla).ax().chkEmpty() }
      }

      onTouchyBluFile(tmpDirBlaBle)

      "On rmTreeWithForce" so {
        rmTreeWithForce(tmpDir) { path -> path.strf.startsWith("/tmp/testDirTmp") }.ax()

        "tmp does not contain our dir" so { lsSubDirs("/tmp".pth).ax().chkThis { !strf.contains(dirName) } }
      }

      "On konfig in tmpDir" so {
        val konfigNewDir = tmpDir / "tmpKonfigForTests"
        val konfig = konfigInDir(konfigNewDir, localCLI())

        testGivenNewKonfigInDir(konfig, konfigNewDir)
      }

    } finally {
      // Clean up. Notice: The "On rmTreeWithForce" above is only for specific test branch,
      // but here we always make sure we clean up in all uspek cases.
      rmTreeWithForce(tmpDir) { path -> path.strf.startsWith("/tmp/testDirTmp") }.ax()
    }
  }
}

private suspend fun onTouchyBluFile(tmpDirForBlu: Path) {

  "On touchy blu file" so {
    val bluName = "blu.touchy"
    val bluPath = tmpDirForBlu / bluName
    touch(bluPath).ax()

    "ls blu is there" so { lsRegFiles(tmpDirForBlu).ax().chkThis { strf.contains(bluName) } }

    "On blu file content" so {
      "it is empty" so { readFileWithCat(bluPath).ax().chkEmpty() }
      "On write poem" so {
        val poem = listOf("NOTHING IS FAIR IN THIS WORLD OF MADNESS!")
        writeFileWithDD(poem, bluPath).ax()
        "poem is there" so { readFileWithCat(bluPath).ax() chkEq poem }
        "On write empty list of lines" so {
          writeFileWithDD(emptyList<String>(), bluPath).ax()
          "it is empty again" so { readFileWithCat(bluPath).ax().chkEmpty() }
        }
      }
    }

    "On rm blu" so {
      rm(bluPath).ax()

      "ls blu is NOT there" so { lsRegFiles(tmpDirForBlu).ax().chkThis { !strf.contains(bluName) } }
    }

    "On rm wrong file name" so {
      "using nice wrapper outputs File not found" so {
        rmFileIfExists("$bluPath.wrong".pth).ax().chkEq(listOf("File not found"))
      }
      "using plain rm throws BadExitStateErr".soThrows<BadExitStateErr> {
        rm("$bluPath.wrong".pth).ax()
      }
    }
  }
}

