package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Adb.Command.devices
import pl.mareklangiewicz.kommand.Adb.Option
import pl.mareklangiewicz.kommand.Adb.Option.usb
import pl.mareklangiewicz.kommand.Ls.Option.*
import pl.mareklangiewicz.kommand.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.Vim.Option.gui
import pl.mareklangiewicz.kommand.Vim.Option.servername
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO_someday: intellij plugin with @param UI similar to colab notebooks
//private const val INTERACTIVE_TESTS_ENABLED = true
//private const val INTERACTIVE_TESTS_ENABLED = false
private val INTERACTIVE_TESTS_ENABLED = SYS.isGnome

fun ifInteractive(block: () -> Unit) =
    if (INTERACTIVE_TESTS_ENABLED) block() else println("Interactive tests are disabled.")

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: Platform = SYS) {
    this.println()
    if (expectedKommandLine != null) assertEquals(expectedKommandLine, line())
    ifInteractive { platform.execInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}


class KommandTest {
    @Test fun testBashQuoteMetaChars() {
        val str = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
        val out = bashQuoteMetaChars(str)
        println(str)
        println(out)
        assertEquals("abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno", out)
    }
    @Test fun testLs() = ls { -all; -author; -long; -sort(TIME); +".."; +"/usr" }
        .checkWithUser("ls -a --author -l --sort=time .. /usr")
    @Test fun testAdb() = adb(devices) { -Option.all; -usb }
        .checkWithUser("adb -a -d devices")
    @Test fun testVim() {
        val kommand = vim(".") { -gui; -servername("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.checkWithUser("vim -g --servername DDDD .")
    }
    @Test fun testMkTemp() = mktemp().checkWithUser()
    @Test fun testWhich() = which { +"vim" }.checkWithUser()
    @Test fun testCreateTempFile() = println(SYS.createTempFile())
    @Test fun testBash() {
        val kommand1 = vim(".") { -gui; -servername("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.checkWithUser("bash -c vim\\ -g\\ --servername\\ DDDD\\ .")
    }

    @Test fun testBashGetExports() {
        SYS.bashGetExports().forEach { println(it) }
        // TODO NOW kommands: idea; ideap; tests/demos for it; a way to put any kommand output (or anything) to clipboard; and also to open it in idea (scratch? tmp file?)
        //  test for use case: automatic save bash exports to temp file and open in ideap..
    }
}
