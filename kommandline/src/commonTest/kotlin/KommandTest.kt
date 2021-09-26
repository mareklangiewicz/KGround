import pl.mareklangiewicz.kommand.Adb.Command.devices
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
}
