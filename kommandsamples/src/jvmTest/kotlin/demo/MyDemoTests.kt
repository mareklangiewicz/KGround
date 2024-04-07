package pl.mareklangiewicz.kommand.demo

import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.extension.ExtensionContext
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.uspek.runTestUSpek
import pl.mareklangiewicz.uspek.so
import kotlin.test.Test


// Yet another way to run some experiments from IDE: via conditionally enabled tests
// By default it's disabled; have to setup user konfig: tests.MyDemoTests.enabled to enable it.
// This is older approach. Now I prefer the "Playground.play" way (no confusion with real tests)
// Executed like this: ./gradlew :kommandjupyter:run --args play
// But let's leave this conditional tests here, as secondary api, and as docs how to use junit: @EnabledIf.



// unfortunately, this can't be moved to main kommandline jvm code, because it depends on jupiter:ExtensionContext
// maybe it could be moved to uspekx-jvm, but that would require uspekx depend on kommandline
fun isUserTestClassEnabled(context: ExtensionContext) =
    getUserFlag(SYS, "tests." + context.requiredTestClass.simpleName)

@ExampleApi
@OptIn(DelicateApi::class)
@EnabledIf(
  value = "pl.mareklangiewicz.kommand.demo.MyDemoTestsKt#isUserTestClassEnabled",
  disabledReason = "tests.MyDemoTests not enabled in user konfig",
)
class MyDemoTests {

    @Test fun testExperiment1() = runTestUSpek {
        MyDemoSamples.run {

            "On btopKitty" so { btopK.ax(SYS) }

            "On manAllMan" so { manAllMan.ax(SYS) }
        }
    }

}
