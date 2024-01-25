package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

@OptIn(DelicateApi::class) @NotPortableApi
fun String.commentOutMultiplatformFun(): String {
    val output1 = ureExpectFun.notCommentedOut().compile().replace(this) { "/*\n${it.value}\n*/" }
    val output2 = ureIR("actual fun").compile().replace(output1) { "/*actual*/ fun" }
    return ureIR("actual suspend fun").compile().replace(output2) { "/*actual*/ suspend fun" }
    // FIXME_maybe: merge this two replaces to one using better URE
}

@OptIn(DelicateApi::class) @NotPortableApi
fun String.undoCommentOutMultiplatformFun(): String {
    val myFun = ure("myFun") { 1 of ureExpectFun }
    val output1 = myFun.commentedOut().compile().replace(this) { it["myFun"] }
    return ureIR("/\\*actual\\*/").compile().replace(output1) { "actual" }
}

private val ureKeyword = ure { 1..MAX of chaz }.withWordBoundaries()

private val ureTypedef = ure {
    1 of chWord
    0..1 of {
        0..1 of chSpace
        ch("\\<")
        1..MAX of chAny
        ch("\\>")
    }
}


private val ureFunParamsInLine = ure {
    1 of ch("\\(")
    0..MAX of chAny
    1 of ch("\\)")
}

private val ureFunParamsMultiLine = ure {
    1 of ch("\\(")
    1 of ureBlankRestOfLine()
    x(0..MAX, reluctant = true) of ureAnyLine()
    1 of ch("\\)")
    1 of ureBlankRestOfLine()
}

private val ureFunParams = ureFunParamsInLine or ureFunParamsMultiLine

@OptIn(DelicateApi::class)
private val ureFunDeclaration = ure {
    1 of ureIR("fun")
    1..MAX of chSpace
    0..1 of { // receiver
        1 of ureTypedef
        1 of chDot
    }
    1..MAX of chWord // funname
    1 of ureFunParams
    0..1 of { // :Type<..>
        0..1 of chSpace
        1 of ch(":")
        0..MAX of chSpace
        1 of ureTypedef
    }
}

@OptIn(DelicateApi::class)
val ureExpectFun = ure {
    1 of bBOLine
    0..1 of { 1 of ureIR("@Composable"); 1..MAX of chSpace }
    0..MAX of { 1 of ureKeyword; 1..MAX of chSpace }
    1 of ureIR("expect ")
    0..1 of ureIR("suspend ")
    1 of ureFunDeclaration
    0..MAX of chSpace
    1 of bEOLine
}
