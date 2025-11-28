import pl.mareklangiewicz.uspek.*
import kotlin.test.*


class TestHelloJs {

  // now it's the same configuration as in commonTest (using just uspek fun)
  // but maybe I will provide some uspek wrapper for better integration with some js testing framework
  // (analogous to jvm junit5 wrapper: uspekTestFactory)
  // gutter play icon is not here probably because gradle "jsTest" task doesn't have --tests option.
  @Test
  fun testHelloJs() = uspek {
    "On testHelloJs" o { onHelloStuff() }
  }
}
