package pl.mareklangiewicz.kommand

import okio.Path
import pl.mareklangiewicz.annotations.*


actual fun getSysCLI(): CLI = NativeCLI()

class NativeCLI : CLI {

  override val isRedirectFileSupported get() = false

  @DelicateApi
  override fun lx(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    workDir: Path?,
    inFile: Path?,
    outFile: Path?,
    outFileAppend: Boolean,
    errToOut: Boolean,
    errFile: Path?,
    errFileAppend: Boolean,
    envModify: (MutableMap<String, String>.() -> Unit)?,
  ): ExecProcess {
    TODO("Native CLI not implemented yet.")
    // TODO_someday: implement using posix or sth (watch okio - maybe they will implement some wrapper)
  }
}
