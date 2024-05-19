package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

@OptIn(DelicateApi::class) @NotPortableApi
fun String.commentOutMultiplatformFun(): String {
  val output1 = ureExpectFun.notCommentedOut().replaceAll(this) { "/*\n${it.value}\n*/" }
  val output2 = ureText("actual fun").replaceAll(output1) { "/*actual*/ fun" }
  return ureText("actual suspend fun").replaceAll(output2) { "/*actual*/ suspend fun" }
  // FIXME_maybe: merge this two replaces to one using better URE
}

@OptIn(DelicateApi::class) @NotPortableApi
fun String.undoCommentOutMultiplatformFun(): String {
  val myFun = ure("myFun") { +ureExpectFun }
  val output1 = myFun.commentedOut().replaceAll(this) { it["myFun"] }
  return ureText("/*actual*/").replaceAll(output1) { "actual" }
}

private val ureKeyword = ure { 1..MAX of chLower }.withWordBoundaries()

// TODO_later: Test utils in this file too: there were unnoticed bugs in ureTypedef (see log/history)
private val ureTypedef = ure {
  +chWord
  0..1 of {
    0..1 of chWhiteSpace
    +ch('<')
    1..MAX of chAnyInLine
    +ch('>')
  }
}


private val ureFunParamsInLine = ure {
  +ch('(')
  0..MAX of chAnyInLine
  +ch(')')
}

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunParamsMultiLine = ure {
  +ch('(')
  +ureBlankRestOfLine()
  x(0..MAX, reluctant = true) of chAnyAtAll
  +ch(')')
}

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunParams = ureFunParamsInLine or ureFunParamsMultiLine

@DelicateApi("Matches correctly only in typical cases.")
private val ureFunDeclaration = ure {
  +ureText("fun")
  1..MAX of chWhiteSpace
  0..1 of { // receiver
    +ureTypedef
    +chDot
  }
  1..MAX of chWord // funname
  +ureFunParams
  0..1 of { // :Type<..>
    0..1 of chWhiteSpace
    +ch(':')
    0..MAX of chWhiteSpace
    +ureTypedef
  }
}

@DelicateApi("Matches correctly only in typical cases.")
val ureExpectFun = ure {
  +atBOLine
  0..1 of { +ureText("@Composable"); 1..MAX of chWhiteSpace }
  0..MAX of { +ureKeyword; 1..MAX of chWhiteSpace }
  +ureText("expect ")
  0..1 of ureText("suspend ")
  +ureFunDeclaration
  0..MAX of chWhiteSpace
  +atEOLine
}
