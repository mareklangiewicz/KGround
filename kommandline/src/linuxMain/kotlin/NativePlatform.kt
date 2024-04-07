package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.*


actual fun provideSysCLI(): CLI = NativeCLI()

class NativeCLI : CLI {

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
        TODO("Native CLI not implemented yet.")
        // TODO_someday: implement using posix or sth (watch okio - maybe they will implement some wrapper)
    }
}