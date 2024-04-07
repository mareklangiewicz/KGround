package pl.mareklangiewicz.kommand

import kotlin.test.*
import pl.mareklangiewicz.regex.*

class UtilsTest {
  @Test fun findSingleTest() {
    val input = "xabcxabcy"
    val result = Regex("abc").findSingle(input, 2)
    assertEquals(5..7, result.range)
  }
}
