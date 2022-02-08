package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*

fun Platform.testIfSameFiles(file1: String, file2:String) = testIf(file1, "-ef", file2)
fun Platform.testIfFirstFileNewer(file1: String, file2:String) = testIf(file1, "-nt", file2)
fun Platform.testIfFirstFileOlder(file1: String, file2:String) = testIf(file1, "-ot", file2)

enum class FileTest(val code: Char) {
    blockSpecial('b'), charSpecial('c'), directory('d'), exists('e'), regular('f'),
    setGroupID('g'), ownedByEffectiveGroupID('G'), symbolicLink('h'), sticky('k'), modified('N'),
    ownedByEffectiveUserID('O'), namedPipe('p'), grantedRead('r'), notEmpty('s'), socket('S'),
    fdOpenOnTerminal('t'), setUserID('u'), grantedWrite('w'), grantedExec('x')

}

fun Platform.testIfFile(file: String, ftest: FileTest) = testIf("-${ftest.code}", file)

fun Platform.testIf(vararg tokens: String): Boolean {
    val result = start(test(*tokens)).await()
    return when (result.exitValue) {
        0 -> true
        1 -> false
        2 -> throw IllegalStateException("Platform test ended with error (2).\n$result ")
        else -> throw IllegalStateException("Unexpected platform test exit value (${result.exitValue}).\n$result")
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
