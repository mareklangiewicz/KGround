import pl.mareklangiewicz.templatempp.*
import pl.mareklangiewicz.uspek.*
import kotlin.test.*

class HelloTest {
    @Test fun testHello() = uspek {
        OnHelloStuff()
    }
}

fun OnHelloStuff() {
    "On helloCommon" o {
        helloCommon()
    }
    "On helloPlatform" o {
        helloPlatform()
    }
}
