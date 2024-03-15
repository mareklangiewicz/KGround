package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kommand.*


actual fun SysPlatform(): SysPlatform = NativePlatform()

class NativePlatform: SysPlatform {

    override val isRedirectFileSupported get() = false

    @DelicateApi
    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?,
        outFileAppend: Boolean,
        errToOut: Boolean,
        errFile: String?,
        errFileAppend: Boolean,
        envModify: (MutableMap<String, String>.() -> Unit)?,
    ): ExecProcess {
        TODO("Native CliPlatform not implemented yet.")
        // TODO_someday: implement using posix or sth (watch okio - maybe they will implement some wrapper)
    }
}