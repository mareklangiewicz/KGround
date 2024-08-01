package pl.mareklangiewicz.kommand.core

import kotlin.math.absoluteValue
import kotlin.random.Random
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.*

// TODO: Lazy/fast implementation; implement sth solid instead.
@DelicateApi("TODO: implement some versatile version of downloading with curl instead.")
suspend fun curlDownloadTmpFile(
  url: String,
  name: String = "tmp${Random.nextLong().absoluteValue}.txt",
): Path {
  val fs = localUFileSys()
  val dir = fs.pathToSomeTmpOrHome
  val path = dir / name
  fs.createDirectories(dir)
  curlDownload(url, path)
  return path
}


// TODO: Lazy/fast implementation; implement sth solid instead.
@DelicateApi("TODO: implement some versatile version of downloading with curl instead.")
suspend fun curlDownload(url: String, to: Path) {
  val cli = localCLI()
  val log = localULog()
  // TODO: Add curl as Kommand, then use it here
  // -s so no progress bars on error stream; -S to report actual errors on error stream
  val k = kommand("curl", "-s", "-S", "-o", to.strf, url)
  val result = cli.lx(k).waitForResult()
  result.unwrap { err ->
    if (err.isNotEmpty()) {
      log.e("FAIL: Error stream was not empty:")
      err.logEach(log, ULogLevel.ERROR)
      false
    } else true
  }
}
