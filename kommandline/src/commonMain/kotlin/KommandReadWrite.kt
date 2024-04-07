package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.core.*

// TODO_someday: move head/tail so separate files and create more specific kommand data classes

@OptIn(DelicateApi::class)
fun readFileHead(inFile: String, lines: Int = 10) =
  kommandTypical("head", KOptS("n", "$lines")) { +inFile }.reducedOutToList()

@OptIn(DelicateApi::class)
fun readFileTail(inFile: String, lines: Int = 10) =
  kommandTypical("tail", KOptS("n", "$lines")) { +inFile }.reducedOutToList()

@OptIn(DelicateApi::class)
fun readFileFirstLine(inFile: String) =
  readFileHead(inFile, 1).reducedMap { single() }

@OptIn(DelicateApi::class)
fun readFileLastLine(inFile: String) =
  readFileTail(inFile, 1).reducedMap { single() }


@OptIn(DelicateApi::class)
fun readFileWithCat(inFile: String) = cat { +inFile }.reducedOutToList()
// In case of essentially just reading/writing files we should use naming scheme starting with readFile/writeFile
// TODO: make sure I follow this rule in other cases too (reading/writing with: dd, scp?, ..)

@OptIn(DelicateApi::class)
fun writeFileWithDD(inLineS: Flow<String>, outFile: String) =
  kommandTypical("dd") { +"of=$outFile" }.reducedManually {
    stdin.collect(inLineS)
    awaitAndChkExit(firstCollectErr = true)
  }

fun writeFileWithDD(inLines: List<String>, outFile: String) =
  writeFileWithDD(inLines.asFlow(), outFile)
