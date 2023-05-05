package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.FileTest.*

fun CliPlatform.testIfSameFiles(file1: String, file2:String) = testIf(file1, "-ef", file2)
fun CliPlatform.testIfFirstFileNewer(file1: String, file2:String) = testIf(file1, "-nt", file2)
fun CliPlatform.testIfFirstFileOlder(file1: String, file2:String) = testIf(file1, "-ot", file2)

/** Can be any kind of file (e.g., directory) */
fun CliPlatform.testIfFileIsThere(file: String) = testIfFile(file, exists)
fun CliPlatform.testIfFileIsRegular(file: String) = testIfFile(file, regular)
fun CliPlatform.testIfFileIsDirectory(file: String) = testIfFile(file, directory)
fun CliPlatform.testIfFileIsSymLink(file: String) = testIfFile(file, symbolicLink)
fun CliPlatform.testIfFileIsPipe(file: String) = testIfFile(file, namedPipe)
fun CliPlatform.testIfFileHasGrantedRead(file: String) = testIfFile(file, grantedRead)
fun CliPlatform.testIfFileHasGrantedWrite(file: String) = testIfFile(file, grantedWrite)
fun CliPlatform.testIfFileHasGrantedExec(file: String) = testIfFile(file, grantedExec)

enum class FileTest(val code: Char) {
    blockSpecial('b'), charSpecial('c'), directory('d'), exists('e'), regular('f'),
    setGroupID('g'), ownedByEffectiveGroupID('G'), symbolicLink('h'), sticky('k'), modified('N'),
    ownedByEffectiveUserID('O'), namedPipe('p'), grantedRead('r'), notEmpty('s'), socket('S'),
    fdOpenOnTerminal('t'), setUserID('u'), grantedWrite('w'), grantedExec('x')

}

fun CliPlatform.testIfFile(file: String, ftest: FileTest) = testIf("-${ftest.code}", file)

fun CliPlatform.testIf(vararg tokens: String): Boolean {
    val result = start(test(*tokens)).await()
    return when (result.exitValue) {
        0 -> true
        1 -> false
        2 -> error("Platform test ended with error (2).\n$result ")
        else -> error("Unexpected platform test exit value (${result.exitValue}).\n$result")
    }
}

fun test(vararg tokens: String) = test { tokens.forEach { +it } }
fun test(init: Test.() -> Unit = {}) = Test().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/test.1.html) */
data class Test(val tokens: MutableList<String> = mutableListOf()) : Kommand {
    // no --help and --verbose options by design. (not always supported anyway - can lead to difficult bugs)
    override val name get() = "test"
    override val args get() = tokens
    operator fun String.unaryPlus() = tokens.add(this)
}
