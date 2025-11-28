import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.*

class TestHelloJvm {

  @TestFactory
  fun testHelloJvm() = uspekTestFactory {
    "On testHelloJvm" o { onHelloStuff() }
  }
}
