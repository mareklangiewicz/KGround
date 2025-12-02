import android.os.Build
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.ktsample.*
import pl.mareklangiewicz.uspek.*


private val String.teePP get() = apply { println(this) }

class SomeBasicAndroidHostTests {
  init {
    "INIT ${this::class.simpleName}".teePP
    "INIT ${Build.MODEL} ${Build.DEVICE}".teePP
  }

  @TestFactory fun microCalcJUnit5Test() = uspekTestFactory { testSomeMicroCalc() }
  @TestFactory fun loggingTest() = uspekTestFactory { testSomeLogging() }
}
