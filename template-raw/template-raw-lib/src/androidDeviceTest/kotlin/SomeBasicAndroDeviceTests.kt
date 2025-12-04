import android.os.Build
import org.junit.runner.*
import pl.mareklangiewicz.uspek.*
import kotlin.test.*
import pl.mareklangiewicz.ktsample.*


private val String.teePP get() = apply { println(this) }


@RunWith(USpekJUnit4Runner::class)
class MicroCalcAndroidDeviceJUnit4Test {
  init {
    // NOTE It should be printed in logcat in android studio, but probably not on host side.
    "INIT ${this::class.simpleName}".teePP
    "INIT ${Build.MODEL} ${Build.DEVICE}".teePP
  }

  // @USpekTestTree(19) fun microCalcTest() {
  //   "In Andro Device microCalcTest tree" o {
  //     testSomeMicroCalc()
  //   }
  // }
  //
  // @USpekTestTree(3) fun loggingTest() {
  //   "In Andro Device loggingTest tree" o {
  //     testSomeLogging()
  //   }
  // }
}
