import pl.mareklangiewicz.templateraw.*
import pl.mareklangiewicz.uspek.*
import kotlin.test.*

class TestHelloCmn {
  @Test fun testHelloCmn() = uspek {
    "On testHelloCmn" o { onHelloStuff() }
  }
}

fun onHelloStuff() {
  "On helloStuff" o {
    "On helloCommon" o { helloCommon() }
    "On helloPlatform" o { helloPlatform() }
  }
}
