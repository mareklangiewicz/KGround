package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.regex.*
import kotlin.test.*

class UtilsTest {
    @Test fun findSingleTest() {
        val input = "xabcxabcy"
        val result = Regex("abc").findSingle(input, 2)
        assertEquals(5..7, result.range)
    }
}