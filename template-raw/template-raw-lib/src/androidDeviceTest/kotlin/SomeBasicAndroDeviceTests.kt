import android.os.Build
import org.junit.runner.*
import pl.mareklangiewicz.uspek.*
import kotlin.test.*
import pl.mareklangiewicz.ktsample.*


private val String.teePP get() = apply { println(this) }


@RunWith(USpekJUnit4Runner::class)
class MicroCalcAndroidDeviceJUnit4Test {
  init {
    "INIT ${this::class.simpleName}".teePP
    "INIT ${Build.MODEL} ${Build.DEVICE}".teePP
  }

  @USpekTestTree(18) fun microCalcTest() = testSomeMicroCalc()
  @USpekTestTree(2) fun loggingTest() = testSomeLogging()
}
