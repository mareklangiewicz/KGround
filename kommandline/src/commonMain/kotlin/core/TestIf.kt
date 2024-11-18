package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.TestIfFile.*
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.udata.strf

// names here are all like testIf/TestIf instead of just test/Test,
// mostly to be different from normal test annotations/classes/functions

@OptIn(DelicateApi::class)
fun testIfSameFiles(file1: String, file2: String) = testIf(file1, "-ef", file2)

@OptIn(DelicateApi::class)
fun testIfFirstFileNewer(file1: String, file2: String) = testIf(file1, "-nt", file2)

@OptIn(DelicateApi::class)
fun testIfFirstFileOlder(file1: String, file2: String) = testIf(file1, "-ot", file2)

/** Can be any kind of file (e.g., directory) */
fun testIfFileExists(file: Path) = testIf(file, FileExists)

fun testIfFileIsRegular(file: Path) = testIf(file, FileIsRegular)

fun testIfFileIsDirectory(file: Path) = testIf(file, FileIsDirectory)

fun testIfFileIsSymLink(file: Path) = testIf(file, FileIsSymLink)

fun testIfFileIsNamedPipe(file: Path) = testIf(file, FileIsNamedPipe)

fun testIfFileHasGrantedRead(file: Path) = testIf(file, FileHasGrantedRead)

fun testIfFileHasGrantedWrite(file: Path) = testIf(file, FileHasGrantedWrite)

fun testIfFileHasGrantedExec(file: Path) = testIf(file, FileHasGrantedExec)


enum class TestIfFile(val code: Char) {
  FileIsBlockSpecial('b'),
  FileIsCharSpecial('c'),
  FileIsDirectory('d'),
  /** Can be any kind of file (e.g., directory) */
  FileExists('e'),
  FileIsRegular('f'),
  FileIsSetGroupID('g'),
  FileIsOwnedByEffectiveGroupID('G'),
  FileIsSymLink('h'),
  FileIsSticky('k'),
  /** has been modified since it was last read */
  FileIsModified('N'),
  FileIsOwnedByEffectiveUserID('O'),
  FileIsNamedPipe('p'),
  FileHasGrantedRead('r'),
  FileIsNotEmpty('s'),
  FileIsSocket('S'),
  FileDescriptorIsOpenedOnTerminal('t'),
  FileHasSetUserID('u'),
  FileHasGrantedWrite('w'),
  FileHasGrantedExec('x')
}

@OptIn(DelicateApi::class)
fun testIf(file: Path, testIfFile: TestIfFile) = testIf("-${testIfFile.code}", file.strf)


// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
@DelicateApi
fun testIf(vararg tokens: String) = testIf { this.tokens.addAll(tokens) }
  // not collecting streams, because they should be empty anyway, and test needs to be fast.
  .reducedExit {
    when (it) {
      0 -> true
      1 -> false
      2 -> bad { "CLI test ended with error (2)." }
      else -> bad { "Unexpected CLI test exit value ($it)." }
    }
  }

@DelicateApi
fun testIf(init: TestIf.() -> Unit) = TestIf().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/test.1.html) */
@DelicateApi
data class TestIf(val tokens: MutableList<String> = mutableListOf()) : Kommand {
  // no --help and --version options by design. (not always supported anyway - can lead to difficult bugs)
  override val name get() = "test"
  override val args get() = tokens
  operator fun String.unaryPlus() = tokens.add(this)
}
