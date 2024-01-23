package pl.mareklangiewicz.ure

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.kground.teePP
import pl.mareklangiewicz.uspek.*

/**
 * Temporary jvm factory, because reporting in UreTestsCmn.kt is lame.
 * JUnit5 based @TestFactory integrates with IntelliJ much better for now.
 * But I plan to implement my own mpp "runner" with better reporting, so someday this file will be deleted.
 */
class UreTestsJvmTemp {
    init { "INIT ${this::class.simpleName}".teePP }
    @TestFactory fun testUreStuffJvmFactory() = uspekTestFactory { testUreCmn() }
}

