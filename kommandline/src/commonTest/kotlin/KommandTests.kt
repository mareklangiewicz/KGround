package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.Adb.*
import pl.mareklangiewicz.kommand.Adb.Command.*
import pl.mareklangiewicz.kommand.Adb.Option.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.iproute2.*
import pl.mareklangiewicz.kommand.Vim.Option.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.debian.*
import kotlin.test.*
import kotlin.test.Ignore
import kotlin.test.Test


class KommandTests {

    // TODO: use runTestUSpek when more tests
    @Test fun testBashQuoteMetaChars() {
        val str = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
        val out = bashQuoteMetaChars(str)
        out chkEq "abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno"
    }
}

@OptIn(DelicateApi::class)
@Ignore // TODO NOW: move&rewrite it all. some to samples, some above, etc. consider kotlin/native too
class KommandTestOld {

    @Test fun testMkDir1() = mkdir("/tmp/testMkDir1/blaa/blee", withParents = true)
        .chkLineRawAndExec("mkdir -p /tmp/testMkDir1/blaa/blee")

    @Test fun testRm1() = rm { -RmOpt.Dir; +"/tmp/testMkDir1/blaa/blee" }
        .tryInteractivelyCheck("rm -d /tmp/testMkDir1/blaa/blee")

    @Test fun testRm2() = rmTreeWithForce("/tmp/testMkDir1") { cli, path ->
        // double check if we are removing what we think we are:
        ls(path).axb(cli) == listOf("blaa")
    }.axb(SYS).logEach()

    @Test fun testSs1() = ssTulpn().tryInteractivelyCheck() // ss -tulpn

    @Test fun testAdbDevices() = adb(Devices) { -Option.All; -Usb }.tryInteractivelyCheck("adb -a -d devices")
    @Test fun testAdbShell() = adb(Shell).tryInteractivelyCheck("adb shell")

    @Test fun testVim() {
        val kommand = vim(".") { -Gui; -ServerName("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.tryInteractivelyCheck("vim -g --servername DDDD .")
    }
    @Test fun testWhich() = which("vim", "ls", all = true).tryInteractivelyCheck()

    @Ignore
    @Test fun testMkTemp() = println(mktemp().axb(SYS))
    @Test fun testBash() {
        val kommand1 = vim(".") { -Gui; -ServerName("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.tryInteractivelyCheck("bash -c vim -g --servername DDDD .")
    }
    @Ignore // Let's not print all env vars on github actions.
    @Test fun testBashGetExports() = bashGetExportsMap().axb(SYS)
        .logEachEntry { println("exported env: ${it.key} == \"${it.value}\"") }

    @Test fun testIdeOpen() = ideOpen("/home/marek/.bashrc", line = 15, column = 15).axb(SYS).unit

    @Test fun testIdeDiff() = ideDiff("/home/marek/.bashrc", "/home/marek/.profile").axb(SYS).unit
}
