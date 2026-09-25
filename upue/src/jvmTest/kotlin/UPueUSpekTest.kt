package pl.mareklangiewicz.upue

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.*

class UPueUSpekTest {

    @TestFactory
    fun uspekTest() = uspekTestFactory {
        "On memoize stuff" o {

            "On growing list" o {
                var x = 1
                val list = mutableListOf(x)
                val addNext: () -> Unit = { list += ++x }
                val addNextMemoized = memoize(addNext)

                "list is prepared with one number one" o { list eq listOf(1) }

                "On first addNext" o {
                    addNext()

                    "list contains 1, 2" o { list eq listOf(1, 2) }

                    "On first addNextMemoized" o {
                        addNextMemoized()

                        "list contains 1, 2, 3" o { list eq listOf(1, 2, 3) }

                        "On second addNextMemoized" o {
                            addNextMemoized()

                            "list still contains 1, 2, 3" o { list eq listOf(1, 2, 3) }

                            "On 20 times addNextMemoized" o {
                                for (i in 1..20)
                                    addNextMemoized()

                                "list still contains 1, 2, 3" o { list eq listOf(1, 2, 3) }

                                "On 20 times addNext" o {
                                    for (i in 1..20)
                                        addNext()

                                    "list size is 23" o { list.size eq 23 }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
