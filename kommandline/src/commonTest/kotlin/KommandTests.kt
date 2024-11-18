@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.kommand

import kotlin.random.*
import kotlin.test.*
import okio.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.tee.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
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
          var tmpFile = "/tmp/fake".P
          try {
            tmpFile = mktemp(path = "/tmp".P, prefix = "tmpFile").ax()
            "name is fine" so { tmpFile.chkThis { strf.startsWith("/tmp/tmpFile") && strf.endsWith(".tmp") } }
            "file is there" so {
              lsRegFiles("/tmp".P).ax().chkThis { any { "/tmp/$it" == tmpFile.strf } }
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
    val dirName = "testDirTmp$rndBigLong"
    val tmpDir = "/tmp".P / dirName
    val tmpDirBla = tmpDir / "bla"
    val tmpDirBlaBle = tmpDirBla / "ble"

    try {
      mkdir(tmpDirBlaBle, withParents = true).chkEqLineRaw("mkdir -p $tmpDirBlaBle").ax()

      "check created dirs with ls" so {
        lsSubDirs("/tmp".P).chkEqLineRaw("ls --indicator-style=slash /tmp")
          .ax().chkThis { strf.contains(dirName) }
      }
      "ls tmp dir is not file" so { lsRegFiles("/tmp".P).ax().chkThis { !strf.contains(dirName) } }

      "On rm empty ble" so {
        rmDirIfEmpty(tmpDirBlaBle).ax()

        "bla does not contain ble" so { lsSubDirs(tmpDirBla).ax().chkEmpty() }
      }

      onTouchyBluFile(tmpDirBlaBle)

      "On rmTreeWithForce" so {
        rmTreeWithForce(tmpDir) { path -> path.strf.startsWith("/tmp/testDirTmp") }.ax()

        "tmp does not contain our dir" so { lsSubDirs("/tmp".P).ax().chkThis { !strf.contains(dirName) } }
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

@OptIn(DelicateApi::class)
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
          writeFileWithDD(emptyList(), bluPath).ax()
          "it is empty again" so { readFileWithCat(bluPath).ax().chkEmpty() }
        }
        onMvLonelyTmpFileAround(bluPath)
        onCpLonelyLittleTmpFileAround(bluPath)
        onLnLonelyTmpFileAround(bluPath)
      }
    }

    "On rm blu" so {
      rm(bluPath).ax()

      "ls blu is NOT there" so { lsRegFiles(tmpDirForBlu).ax().chkThis { !strf.contains(bluName) } }
    }

    "On rm wrong file name" so {
      "using nice wrapper outputs File not found" so {
        rmFileIfExists("$bluPath.wrong".P).ax().chkEq(listOf("File not found"))
      }
      "using plain rm throws BadExitStateErr".soThrows<BadExitStateErr> {
        rm("$bluPath.wrong".P).ax()
      }
    }
  }
}

private suspend fun onMvLonelyTmpFileAround(fileP: Path) {
  val dirP = fileP.parent!!
  lsRegFiles(dirP).ax().chkThis({ "file $fileP is not lonely in it's dir"}) { single() == fileP.name.P }
  "On mv file around" so {
    val file2P = dirP / "2rnd$rndBigLong"
    val file3P = dirP / "3rnd$rndBigLong"
    "On mv to file2" so {
      mvSingle(fileP, file2P)
        .chkThisLineRaw { split(' ').chkSize(4, 4).drop(2).all { it.startsWith("/tmp/") } }
        .ax()
      "moved to file2" so {
        lsRegFiles(dirP).ax().chkThis { single() == file2P.name.P }
      }
      "On mv to file3" so {
        mvSingle(file2P, file3P)
          .chkThisLineRaw { split(' ').chkSize(4, 4).drop(2).all { it.startsWith("/tmp/") } }
          .ax()
        "moved to file3" so {
          lsRegFiles(dirP).ax().chkThis { single() == file3P.name.P }
        }
        "On mv to original file" so {
          mvSingle(file3P, fileP)
            .chkThisLineRaw { split(' ').chkSize(4, 4).drop(2).all { it.startsWith("/tmp/") } }
            .ax()
          "moved to original file" so {
            lsRegFiles(dirP).ax().chkThis { single() == fileP.name.P }
          }
        }
      }
    }
  }
}

private suspend fun onCpLonelyLittleTmpFileAround(fileP: Path) {
  val dirP = fileP.parent!!
  lsRegFiles(dirP).ax().chkThis({ "file $fileP is not lonely in it's dir"}) { single() == fileP.name.P }
  val fileSize = statFileSizeBytes(fileP).ax()
  fileSize.chkIn(max = 1_000_000L) { "file $fileP is too fat!" }
  "On cp file around" so {
    val file2P = dirP / "2rnd$rndBigLong"
    val file3P = dirP / "3rnd$rndBigLong"
    "On cp to file2" so {
      cpSingle(fileP, file2P)
        .chkThisLineRaw { split(' ').chkSize(4, 4).drop(2).all { it.startsWith("/tmp/") } }
        .ax()
      "copied to file2" so {
        lsRegFiles(dirP).ax().chkThis { toSet() == setOf(fileP.name.P, file2P.name.P) }
      }
      "On cp to file3" so {
        cpSingle(file2P, file3P)
          .chkThisLineRaw { split(' ').chkSize(4, 4).drop(2).all { it.startsWith("/tmp/") } }
          .ax()
        "copied to file3" so {
          lsRegFiles(dirP).ax().chkThis { toSet() == setOf(fileP.name.P, file2P.name.P, file3P.name.P) }
        }
        "all three same size" so {
          statFileSizeBytes(file2P).ax().chkEq(fileSize)
          statFileSizeBytes(file3P).ax().chkEq(fileSize)
        }
      }
    }
  }
}


private suspend fun onLnLonelyTmpFileAround(fileP: Path) {
  val dirP = fileP.parent!!
  lsRegFiles(dirP).ax().chkThis({ "file $fileP is not lonely in it's dir"}) { single() == fileP.name.P }
  "On ln file around" so {
    val file2P = dirP / "2rnd$rndBigLong"
    val file3P = dirP / "3rnd$rndBigLong"
    "On lnSymSingle as file2" so {
      lnSymSingle(fileP, file2P)
        .chkThisLineRaw { split(' ').chkSize(5, 5).drop(3).all { it.startsWith("/tmp/") } }
        .ax()
      "linked softly as file2" so {
        ls(dirP, wIndicator = IndicatorStyle.FileType).ax().chkThis { contains(file2P.name + "@") }
      }
      "On lnSymSingle as file3" so {
        lnSymSingle(file2P, file3P)
          .chkThisLineRaw { split(' ').chkSize(5, 5).drop(3).all { it.startsWith("/tmp/") } }
          .ax()
        "linked softly as file3" so {
          ls(dirP, wIndicator = IndicatorStyle.FileType).ax().chkThis { contains(file3P.name + "@") }
        }
      }
    }
  }
}


// TODO_later: use public ones from kground after publishing new kground
private val rndBigLong get() = Random.nextLong(100_000L, Long.MAX_VALUE - 10_000L)
private val rndBigLongStr get() = rndBigLong.strf
private val rndBigInt get() = Random.nextInt(100_000, Int.MAX_VALUE - 1000)
private val rndBigIntStr get() = rndBigInt.strf
