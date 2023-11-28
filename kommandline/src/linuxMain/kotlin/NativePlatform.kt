package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*


actual typealias SysPlatform = NativePlatform

class NativePlatform: CliPlatform {

    override val isRedirectFileSupported get() = false

    @DelicateKommandApi
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