import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.templatempp.*
import pl.mareklangiewicz.uspek.*

class SomeJvmTests {

    @TestFactory fun mainCliJvmTest() = uspekTestFactory {
        "On template cli-like project" o {
            "Check helloCommon output" o { helloCommon() eq "Hello Pure Common World!" }
            "Check helloCommon output - should fail" ox { helloCommon() eq "Incorrect output" }
            "Check helloPlatform output" o { helloPlatform().startsWith("Hello JVM World") eq true }
            "Check helloPlatform output - should fail" ox { helloPlatform() eq "Incorrect output" }
            "Just run whole mainCli fun" o { mainCli() }
        }
    }
    @TestFactory fun mainComposeAppJvmTest() = uspekTestFactory {
        "Launch main window (user has to close it)" o {
            mainComposeApp()
        }
    }
}