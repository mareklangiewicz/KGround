import pl.mareklangiewicz.kommand.Adb.Command.devices
import pl.mareklangiewicz.kommand.Adb.Command.help
import pl.mareklangiewicz.kommand.Adb.Option
import pl.mareklangiewicz.kommand.Adb.Option.usb
import pl.mareklangiewicz.kommand.Ls.Option.*
import pl.mareklangiewicz.kommand.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.adb
import pl.mareklangiewicz.kommand.ls
import kotlin.test.Test
import kotlin.test.assertEquals

class KommandTest {

    @Test
    fun testLs() {

        val line = ls(all, author) {
            - long
            - sort(TIME)
            + ".."
            + "/usr"
        }

        line.printLine()

        assertEquals("ls -a --author -l --sort=time .. /usr", line.kommandLine())
    }

    @Test
    fun testAdb() {

        val line = adb(devices, Option.all) {
            - usb
        }

        line.printLine()

        assertEquals("adb -a -d devices", line.kommandLine())
    }
}
