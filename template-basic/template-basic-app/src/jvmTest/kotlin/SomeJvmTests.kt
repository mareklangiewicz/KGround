import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.templatebasic.*
import pl.mareklangiewicz.uspek.*

internal infix fun <T> T.chkEq(expected: T) = check(this == expected) { "$this != $expected" }

class SomeJvmTests {

  @TestFactory fun helloAllCliJvmTest() = uspekTestFactory {
    "On template cli-like project" o {
      "Check helloCommon output" o { helloCommon() chkEq "Hello Pure Common World!" }
      "Check helloCommon output - should fail" ox { helloCommon() chkEq "Incorrect output" }
      "Check helloPlatform output" o { helloPlatform().startsWith("Hello JVM World") chkEq true }
      "Check helloPlatform output - should fail" ox { helloPlatform() chkEq "Incorrect output" }
      "Just run fun helloAllTogetherForBasicCli" o { helloAllTogetherForBasicCli("SomeJvmTests") }
    }
  }
}
