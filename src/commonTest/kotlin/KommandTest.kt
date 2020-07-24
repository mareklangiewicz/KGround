import pl.mareklangiewicz.kommand.Ls.Option.*
import pl.mareklangiewicz.kommand.Ls.Option.sortType.*
import pl.mareklangiewicz.kommand.ls
import pl.mareklangiewicz.kommand.printLine
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

        assertEquals("ls -a --author -l --sort=time .. /usr", line.toString())
    }
}
