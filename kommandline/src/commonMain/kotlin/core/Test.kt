package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.TestFile.*

@OptIn(DelicateKommandApi::class)
fun testIfSameFiles(file1: String, file2:String) = testIf(file1, "-ef", file2)

@OptIn(DelicateKommandApi::class)
fun testIfFirstFileNewer(file1: String, file2:String) = testIf(file1, "-nt", file2)

@OptIn(DelicateKommandApi::class)
fun testIfFirstFileOlder(file1: String, file2:String) = testIf(file1, "-ot", file2)

/** Can be any kind of file (e.g., directory) */
fun testIfFileExists(file: String) = testIf(file, FileExists)

fun testIfFileIsRegular(file: String) = testIf(file, FileIsRegular)

fun testIfFileIsDirectory(file: String) = testIf(file, FileIsDirectory)

fun testIfFileIsSymLink(file: String) = testIf(file, FileIsSymLink)

fun testIfFileIsPipe(file: String) = testIf(file, FileIsNamedPipe)

fun testIfFileHasGrantedRead(file: String) = testIf(file, FileHasGrantedRead)

fun testIfFileHasGrantedWrite(file: String) = testIf(file, FileHasGrantedWrite)

fun testIfFileHasGrantedExec(file: String) = testIf(file, FileHasGrantedExec)


enum class TestFile(val code: Char) {
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

@OptIn(DelicateKommandApi::class)
fun testIf(file: String, testFile: TestFile) = testIf("-${testFile.code}", file)


// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
@DelicateKommandApi
fun testIf(vararg tokens: String) = test { this.tokens.addAll(tokens) }
    .reducedManually {
        // not collecting streams, because they should be empty anyway, and test needs to be fast.
        when (val exit = awaitExit()) {
            0 -> true
            1 -> false
            2 -> bad { "Platform test ended with error (2)." }
            else -> bad { "Unexpected platform test exit value ($exit)." }
        }
    }

@DelicateKommandApi
fun test(init: Test.() -> Unit = {}) = Test().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/test.1.html) */
@DelicateKommandApi
data class Test(val tokens: MutableList<String> = mutableListOf()) : Kommand {
    // no --help and --version options by design. (not always supported anyway - can lead to difficult bugs)
    override val name get() = "test"
    override val args get() = tokens
    operator fun String.unaryPlus() = tokens.add(this)
}
