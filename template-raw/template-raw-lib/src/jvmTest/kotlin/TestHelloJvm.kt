import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.*

/**
 * The "tee" like util, to log any value to stdout.
 * @return The same provided value, so it can be chained/injected in expressions as debug tool.
 */
private val <T> T.tee: T get() = apply { println(this.toString()) }

class TestHelloJvm {

  @TestFactory
  fun testHelloJvm() = uspekTestFactory {
    "On testHelloJvm" o {
      "On logDetailedJvmInfo" o {
        "OS Name: ${System.getProperty("os.name")}".tee
        "OS Version: ${System.getProperty("os.version")}".tee
        "OS Arch: ${System.getProperty("os.arch")}".tee
        "Java Version: ${System.getProperty("java.version")}".tee
        "Java Vendor: ${System.getProperty("java.vendor")}".tee
        "JVM Name: ${System.getProperty("java.vm.name")}".tee
        "User Directory: ${System.getProperty("user.dir")}".tee
      }
      onHelloStuff()
    }
  }
}
