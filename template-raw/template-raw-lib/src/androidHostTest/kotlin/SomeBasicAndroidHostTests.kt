import android.os.Build
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.ktsample.*
import pl.mareklangiewicz.uspek.*


private val String.teePP get() = apply { println(this) }

class SomeBasicAndroidHostTests {
  init {
    "INIT ${this::class.simpleName}".teePP
    "INIT ${Build.MODEL} ${Build.DEVICE}".teePP
    // Note: it should be just two nulls, because host tests have stubbed andro api.
  }

  @TestFactory fun microCalcTest() = uspekTestFactory {
    "In Andro Host microCalcTest factory" o {
      testSomeMicroCalc()
    }
  }
  @TestFactory fun loggingTest() = uspekTestFactory {
    "In Andro Host loggingTest factory" o {
      testSomeLogging()
    }
  }
}
