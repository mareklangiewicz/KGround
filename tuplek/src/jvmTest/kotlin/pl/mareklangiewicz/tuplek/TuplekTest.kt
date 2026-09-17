package pl.mareklangiewicz.tuplek

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory

internal infix fun Any?.eq(expected: Any?) = check(this == expected) { "TEST FAIL: $this != $expected" }

class TuplekTest {

    @TestFactory
    fun tuplekTest() = uspekTestFactory {

        "On Triple" o {
            val triple = Triple("a", "b", 'c')

            "eq infix tre" o { "a" to "b" tre 'c' eq triple }
            "is covariant" o { val b: Triple<CharSequence, CharSequence, Comparable<Char>> = triple }

            "On Quad" o {
                val quad = Quad("a", "b", 'c', 7)

                "eq infix fo" o { triple fo 7 eq quad }
                "is covariant" o { val b: Quad<CharSequence, CharSequence, Comparable<Char>, Number> = quad }
            }
        }
    }
}
