package pl.mareklangiewicz.upue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ColsTests {

    @Test fun arrayLikeTest() {
        val jsobj = js("{}")
        jsobj.length = 3
        jsobj[1] = "x"

        assertEquals(null, jsobj[0])
        assertEquals("x", jsobj[1])
        assertEquals(null, jsobj[2])

        val arr = JsMutArr<String?>(jsobj)

        assertEquals(3, arr.len)
        assertEquals(null, arr[0])
        assertEquals("x", arr[1])
        assertEquals(null, arr[2])

        arr[1] = "zzz"
        arr[2] = "last"

        assertEquals(3, arr.len)
        assertEquals(null, arr[0])
        assertEquals("zzz", arr[1])
        assertEquals("last", arr[2])

        assertEquals(null, jsobj[0])
        assertEquals("zzz", jsobj[1])
        assertEquals("last", jsobj[2])

        jsobj[2] = "newlast"
        assertEquals("newlast", arr[2])
    }

    @Test fun arrayLikeInitTest() {
        assertEquals(0, JsArr<String?>(js("{length:0}")).len)
        // Missing length throws NullPointerException on Kotlin 2.5; older compilers threw
        // ClassCastException. The contract is fail-fast rejection, not the cast's exception type.
        assertFailsWith<RuntimeException> { JsArr<String?>(js("{}")) }
        assertFailsWith<RuntimeException> { JsArr<String?>(js("{length:'invalid'}")) }
    }
}
