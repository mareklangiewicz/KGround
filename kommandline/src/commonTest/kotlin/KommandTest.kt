package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Adb.Command.devices
import pl.mareklangiewicz.kommand.Adb.Option
import pl.mareklangiewicz.kommand.Adb.Option.usb
import pl.mareklangiewicz.kommand.Ls.Option.*
import pl.mareklangiewicz.kommand.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.Vim.Option.gui
import pl.mareklangiewicz.kommand.Vim.Option.servername
import kotlin.test.Test
import kotlin.test.assertEquals

class KommandTest {

    @Test
    fun testLs() {

        val kommand = ls {
            - all
            - author
            - long
            - sort(TIME)
            + ".."
            + "/usr"
        }

        kommand.println()

        assertEquals("ls -a --author -l --sort=time .. /usr", kommand.line())
    }

    @Test
    fun testAdb() {

        val kommand = adb(devices) {
            - Option.all
            - usb
        }

        kommand.println()

        assertEquals("adb -a -d devices", kommand.line())
    }

    @Test
    fun testVim() {
        val kommand = vim(".") { -gui; -servername("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        assertEquals("vim -g --servername DDDD .", kommand.line())
        //kommand.exec()
    }
}
