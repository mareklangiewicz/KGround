import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.*

class HelloTestJvm {

  @TestFactory
  fun testHelloJvm() = uspekTestFactory {
    OnHelloStuff()
  }
}
