package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Adb.Command.devices
import pl.mareklangiewicz.kommand.Adb.Option
import pl.mareklangiewicz.kommand.Adb.Option.usb
import pl.mareklangiewicz.kommand.Ide.Cmd.diff
import pl.mareklangiewicz.kommand.Ide.Option.col
import pl.mareklangiewicz.kommand.Ide.Option.ln
import pl.mareklangiewicz.kommand.Ls.Option.*
import pl.mareklangiewicz.kommand.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.Man.Section.systemcall
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.Vim.Option.gui
import pl.mareklangiewicz.kommand.Vim.Option.servername
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals


class KommandTest {
    @Test fun testBashQuoteMetaChars() {
        val str = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
        val out = bashQuoteMetaChars(str)
        println(str)
        println(out)
        assertEquals("abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno", out)
    }
    @Test fun testLs1() = ls { -all; -author; -long; -sort(TIME); +".."; +"/usr" }
        .checkWithUser("ls -a --author -l --sort=time .. /usr")
    @Test fun testLs2() = ls { -all; -author; -long; -humanReadable; +"/home/marek" }.checkInIdeap()

    @Test fun testManMan() = man { +"man" }.checkWithUser()
    @Test fun testManVim() = man { +"vim" }.checkWithUser()
    @Test fun testManOpenAll() = man { -Man.Option.all; +"open" }.checkWithUser()
    @Test fun testManOpen2() = man(2) { +"open" }.checkWithUser()
    @Test fun testManOpenSys() = man(systemcall) { +"open" }.checkWithUser()
    @Test fun testManApropos() = man { -Man.Option.apropos; +"package" }.checkWithUser()
    @Test fun testManWhatis() = man { -Man.Option.whatis; +"which" }.checkWithUser()

    @Test fun testAdb() = adb(devices) { -Option.all; -usb }
        .checkWithUser("adb -a -d devices")
    @Test fun testVim() {
        val kommand = vim(".") { -gui; -servername("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.checkWithUser("vim -g --servername DDDD .")
    }
    @Test fun testMkTemp() = mktemp().checkWithUser()
    @Test fun testWhich() = which { +"vim" }.checkWithUser()

    @Ignore // jitpack
    @Test fun testCreateTempFile() = println(SYS.createTempFile())
    @Test fun testBash() {
        val kommand1 = vim(".") { -gui; -servername("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.checkWithUser("bash -c vim\\ -g\\ --servername\\ DDDD\\ .")
    }
    @Test fun testBashGetExports() = SYS.bashGetExports().forEach { println(it) }
    @Test fun testIdeap() = ideap { +"/home/marek/.bashrc"; -ln(2); -col(13) }.checkWithUser()
    @Test fun testIdeapDiff() = ideap(diff) { +"/home/marek/.bashrc"; +"/home/marek/.profile" }.checkWithUser()
}
