package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

@OptIn(DelicateApi::class) @NotPortableApi
fun String.commentOutMultiplatformFun(): String {
    val output1 = ureExpectFun.notCommentedOut().compile().replace(this) { "/*\n${it.value}\n*/" }
    val output2 = ureText("actual fun").compile().replace(output1) { "/*actual*/ fun" }
    return ureText("actual suspend fun").compile().replace(output2) { "/*actual*/ suspend fun" }
    // FIXME_maybe: merge this two replaces to one using better URE
}

@OptIn(DelicateApi::class) @NotPortableApi
fun String.undoCommentOutMultiplatformFun(): String {
    val myFun = ure("myFun") { 1 of ureExpectFun }
    val output1 = myFun.commentedOut().compile().replace(this) { it["myFun"] }
    return ureText("/*actual*/").compile().replace(output1) { "actual" }
}

private val ureKeyword = ure { 1..MAX of chLower }.withWordBoundaries()

private val ureTypedef = ure {
    1 of chWord
    0..1 of {
        0..1 of chWhiteSpace
        ch("\\<")
        1..MAX of chAnyInLine
        ch("\\>")
    }
}


private val ureFunParamsInLine = ure {
    1 of ch("\\(")
    0..MAX of chAnyInLine
    1 of ch("\\)")
}

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunParamsMultiLine = ure {
    1 of ch("\\(")
    1 of ureBlankRestOfLine()
    x(0..MAX, reluctant = true) of chAnyAtAll
    1 of ch("\\)")
}

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunParams = ureFunParamsInLine or ureFunParamsMultiLine

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunDeclaration = ure {
    1 of ureText("fun")
    1..MAX of chWhiteSpace
    0..1 of { // receiver
        1 of ureTypedef
        1 of chDot
    }
    1..MAX of chWord // funname
    1 of ureFunParams
    0..1 of { // :Type<..>
        0..1 of chWhiteSpace
        1 of ch(":")
        0..MAX of chWhiteSpace
        1 of ureTypedef
    }
}

@DelicateApi("Matches correctly only in typical cases.")
val ureExpectFun = ure {
    1 of atBOLine
    0..1 of { 1 of ureText("@Composable"); 1..MAX of chWhiteSpace }
    0..MAX of { 1 of ureKeyword; 1..MAX of chWhiteSpace }
    1 of ureText("expect ")
    0..1 of ureText("suspend ")
    1 of ureFunDeclaration
    0..MAX of chWhiteSpace
    1 of atEOLine
}
