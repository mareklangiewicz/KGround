package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Adb.Command.*
import pl.mareklangiewicz.kommand.Adb.Option
import pl.mareklangiewicz.kommand.Adb.Option.usb
import pl.mareklangiewicz.kommand.Ide.Cmd.diff
import pl.mareklangiewicz.kommand.Ide.Option.col
import pl.mareklangiewicz.kommand.Ide.Option.ln
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.*
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.Man.Section.systemcall
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.Vim.Option.gui
import pl.mareklangiewicz.kommand.Vim.Option.servername
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.colorType.*
import pl.mareklangiewicz.kommand.coreutils.MkDir.Option.*
import pl.mareklangiewicz.kommand.coreutils.Rm.Option.*
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
    @Test fun testLs1() = ls { -color(ALWAYS); -all; -author; -long; -sort(TIME); +".."; +"/usr" }
        .checkWithUser("ls --color=always -a --author -l --sort=time .. /usr")
    @Test fun testLs2() = ls { -all; -author; -long; -humanReadable; +"/home/marek" }.checkInIdeap()
    @Test fun testLs3() = ls { +"/home/marek" }.checkInIdeap()
    @Test fun testLsHome() = SYS.ls("/home/marek").printlns()
    @Test fun testLsHomeSubDirs() = SYS.lsSubDirs("/home/marek").printlns()
    @Test fun testLsHomeSubDirsWithHidden() = SYS.lsSubDirs("/home/marek", withHidden = true).printlns()
    @Test fun testLsHomeRegFiles() = SYS.lsRegFiles("/home/marek").printlns()

    @Test fun testMkDir1() = mkdir { -parents; +"/tmp/testMkDir1/blaa/blee" }
        .checkWithUser("mkdir --parents /tmp/testMkDir1/blaa/blee")

    @Test fun testRm1() = rm { -dir; +"/tmp/testMkDir1/blaa/blee" }
        .checkWithUser("rm --dir /tmp/testMkDir1/blaa/blee")

    @Test fun testCat1() = cat { +"/etc/fstab" }.checkInIdeap()
    @Test fun testCat2() = cat { +"/etc/fstab"; +"/etc/hosts" }.checkInIdeap()

    @Test fun testManMan() = man { +"man" }.checkWithUser()
    @Test fun testManVim() = man { +"vim" }.checkWithUser()
    @Test fun testManOpenAll() = man { -Man.Option.all; +"open" }.checkWithUser()
    @Test fun testManOpen2() = man(2) { +"open" }.checkWithUser()
    @Test fun testManOpenSys() = man(systemcall) { +"open" }.checkWithUser()
    @Test fun testManApropos() = man { -Man.Option.apropos; +"package" }.checkWithUser()
    @Test fun testManWhatis() = man { -Man.Option.whatis; +"which" }.checkWithUser()

    @Test fun testAdbDevices() = adb(devices) { -Option.all; -usb }.checkWithUser("adb -a -d devices")
    @Test fun testAdbShell() = adb(shell).checkWithUser("adb shell")

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
