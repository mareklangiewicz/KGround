package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.udata.strf

// TODO_someday: move head/tail so separate files and create more specific kommand data classes

@OptIn(DelicateApi::class)
fun readFileHead(inFile: Path, lines: Int = 10) =
  kommandTypical("head", KOptS("n", "$lines")) { +inFile.strf }.reducedOutToList()

@OptIn(DelicateApi::class)
fun readFileTail(inFile: Path, lines: Int = 10) =
  kommandTypical("tail", KOptS("n", "$lines")) { +inFile.strf }.reducedOutToList()

@OptIn(DelicateApi::class)
fun readFileFirstLine(inFile: Path) =
  readFileHead(inFile, 1).reducedMap { single() }

@OptIn(DelicateApi::class)
fun readFileLastLine(inFile: Path) =
  readFileTail(inFile, 1).reducedMap { single() }


@OptIn(DelicateApi::class)
fun readFileWithCat(inFile: Path) = cat { +inFile.strf }.reducedOutToList()
// In case of essentially just reading/writing files we should use naming scheme starting with readFile/writeFile
// TODO: make sure I follow this rule in other cases too (reading/writing with: dd, scp?, ..)

@OptIn(DelicateApi::class)
fun writeFileWithDD(inLineS: Flow<String>, outFile: Path) =
  kommandTypical("dd") { +"of=$outFile" }.reducedManually {
    stdin.collect(inLineS)
    awaitAndChkExit(firstCollectErr = true)
  }

fun writeFileWithDD(inLines: List<String>, outFile: Path) =
  writeFileWithDD(inLines.asFlow(), outFile)
