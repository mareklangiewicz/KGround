package pl.mareklangiewicz.kommand.demos

import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.extension.ExtensionContext
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.konfig.*
import pl.mareklangiewicz.kommand.term.*


// TODO NOW: move all this kind of stuff to samples


// unfortunately, this can't be moved to main kommandline jvm code, because it depends on jupiter:ExtensionContext
// maybe it could be moved to uspekx-jvm, but that would require uspekx depend on kommandline
fun isUserTestClassEnabled(context: ExtensionContext) =
    isUserFlagEnabled(SYS, "tests." + context.requiredTestClass.simpleName)

@OptIn(DelicateApi::class)
@EnabledIf(
    value = "pl.mareklangiewicz.kommand.demos.MarekLangiewiczKt#isUserTestClassEnabled",
    disabledReason = "tests.MarekLangiewicz not enabled in user konfig"
)
class MarekLangiewicz {

    companion object {
        const val tmpNotesFile = "/home/marek/tmp/tmp.notes"
    }

    @Test fun demo_htop() = idemo { termKitty(kommand("htop")).execb(this) }

    @Test fun demo_btop() = idemo { termKitty(kommand("btop")).execb(this) }

    @Test fun demo_ps() = idemo { termKitty(bash("ps -e | grep " + askEntry("find process"), pause = true)).execb(this) }

    @Test fun demo_man() = idemo { termKitty(man { +askEntry("manual page for") }).execb(this) }

    @Test fun demo_ide() = idemo {
        ide(Ide.Cmd.Open(askEntry("open file in IDE", suggested = "/home/marek/.bashrc"))).execb(this)
    }

    @Test fun demo_bash_export() = idemo {
        bashGetExportsToFile(tmpNotesFile).execb(this)
        ideOpen(tmpNotesFile).execb(this)
    }

    @Test fun demo_xclip() = idemo {
        bash("xclip -o > $tmpNotesFile").execb(this) // FIXME_later: do it with kotlin instead of bash script
        ideOpen(tmpNotesFile).execb(this)
    }

    @Test fun demo_set_konfig_examples() = idemo {
        val k = konfigInDir("/home/marek/tmp/konfig_examples", checkForDangerousValues = false)
        println("before adding anything:")
        k.logEachKeyVal()
        k["tmpExampleInteger1"] = 111.toString()
        k["tmpExampleInteger2"] = 222.toString()
        k["tmpExampleString1"] = "some text 1"
        k["tmpExampleString2"] = "some text 2"
        println("after adding 4 keys:")
        k.logEachKeyVal()
        k["tmpExampleInteger2"] = null
        k["tmpExampleString2"] = null
        println("after nulling 2 keys:")
        k.logEachKeyVal()
        k["tmpExampleInteger1"] = null
        k["tmpExampleString1"] = null
        println("after nulling other 2 keys:")
        k.logEachKeyVal()
    }

    @Test fun code_interactive_switch() = SYS.run {
        val enabled = askIf("Should interactive code be enabled?")
        setUserFlag(this, "code.interactive", enabled)
        println("user flag: code.interactive.enabled = $enabled")
    }

    @Test fun print_all_konfig() = SYS.konfigInUserHomeConfigDir().logEachKeyVal()

    @Test fun experiment() = idemo {
        ideOrGVimOpen(tmpNotesFile).execb(this)
    }
}

private fun idemo(cli: CLI = SYS, block: CLI.() -> Unit) = ifInteractive { cli.block() }
private fun askIf(question: String, cli: CLI = SYS) = zenityAskIf(question).execb(cli)
private fun askEntry(question: String, suggested: String? = null, cli: CLI = SYS) =
    zenityAskForEntry(question, suggested = suggested).execb(cli)
