package pl.mareklangiewicz.kommand.demos

import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.extension.ExtensionContext
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.*
import pl.mareklangiewicz.kommand.konfig.*


// TODO NOW: move all this kind of stuff to samples


// unfortunately, this can't be moved to main kommandline jvm code, because it depends on jupiter:ExtensionContext
// maybe it could be moved to uspekx-jvm, but that would require uspekx depend on kommandline
fun isUserTestClassEnabled(context: ExtensionContext) =
    SYS.isUserFlagEnabled("tests." + context.requiredTestClass.simpleName)

@OptIn(DelicateKommandApi::class)
@EnabledIf(
    value = "pl.mareklangiewicz.kommand.demos.LangaraKt#isUserTestClassEnabled",
    disabledReason = "tests.Langara not enabled in user konfig"
)
class Langara {

    companion object {
        const val tmpNotesFile = "/home/marek/tmp/tmp.notes"
    }

    @Test fun demo_repl() = idemo { LangaraREPL() }

    @Test fun demo_htop() = idemo { termGnome(kommand("htop")).execb(this) }

    @Test fun demo_btop() = idemo { termGnome(kommand("btop")).execb(this) }

    @Test fun demo_ps() = idemo { termGnome(bash("ps -e | grep " + askEntry("find process"), pause = true)).execb(this) }

    @Test fun demo_man() = idemo { termGnome(man { +askEntry("manual page for") }).execb(this) }

    @Test fun demo_ideap() = idemo {
        ideap { +askEntry("open file in ideap", suggested = "/home/marek/.bashrc") }.execb(this)
    }

    @Test fun demo_bash_export() = idemo {
        bashGetExportsToFileExec(tmpNotesFile)
        ideap { +tmpNotesFile }.execb(this)
    }

    @Test fun demo_xclip() = idemo {
        bash("xclip -o > $tmpNotesFile").execb(this) // FIXME_later: do it with kotlin instead of bash script
        ideap { +tmpNotesFile }.execb(this)
    }

    @Test fun demo_set_konfig_examples() = idemo {
        val k = konfigInDir("/home/marek/tmp/konfig_examples", checkForDangerousValues = false)
        println("before adding anything:")
        k.loglns()
        k["tmpExampleInteger1"] = 111.toString()
        k["tmpExampleInteger2"] = 222.toString()
        k["tmpExampleString1"] = "some text 1"
        k["tmpExampleString2"] = "some text 2"
        println("after adding 4 keys:")
        k.loglns()
        k["tmpExampleInteger2"] = null
        k["tmpExampleString2"] = null
        println("after nulling 2 keys:")
        k.loglns()
        k["tmpExampleInteger1"] = null
        k["tmpExampleString1"] = null
        println("after nulling other 2 keys:")
        k.loglns()
    }

    @Test fun code_interactive_switch() = SYS.run {
        val enabled = askIf("Should interactive code be enabled?")
        setUserFlag("code.interactive", enabled)
        println("user flag: code.interactive.enabled = $enabled")
    }

    @Test fun print_all_konfig() = SYS.konfigInUserHomeConfigDir().loglns()

    @Test fun experiment() = idemo {
        val ideaEnabled = bash("ps aux").execb(this).any { it.lowercase().contains("idea") }
        if (ideaEnabled) ideap { +tmpNotesFile }.execb(this) else gvim(tmpNotesFile).execb(this)
    }
}

private fun idemo(platform: CliPlatform = SYS, block: CliPlatform.() -> Unit) = ifInteractive { platform.block() }
private fun CliPlatform.askIf(question: String) = zenityAskIfExec(question)
private fun CliPlatform.askEntry(question: String, suggested: String? = null) = zenityAskForEntryExec(question, suggested = suggested)
