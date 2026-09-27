@file:OptIn(DelicateApi::class, NotPortableApi::class)

package pl.mareklangiewicz.ureflect

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

object ReflectCallTarget {
  fun noArgs() = "none"
  suspend fun oneArg(name: String) = "hi $name"
  fun twoArgs(a: String, b: String) = "$a+$b"
  fun overloaded() = "zero"
  fun overloaded(x: String) = "one:$x"
  fun notStrings(n: Int) = n
}

class UReflectCallTest {

  private val target = "pl.mareklangiewicz.ureflect.ReflectCallTarget"

  private fun call(member: String, vararg args: String) = runBlocking {
    getReflectCallOrNull(target, member, args.toList())?.invoke()
  }

  @Test fun callsWithoutArgsAsBefore() = assertEquals("none", call("noArgs"))

  @Test fun passesStringArgs() {
    assertEquals("hi kthreelhu", call("oneArg", "kthreelhu"))
    assertEquals("a+b", call("twoArgs", "a", "b"))
  }

  @Test fun picksTheOverloadWithMatchingArity() {
    assertEquals("zero", call("overloaded"))
    assertEquals("one:x", call("overloaded", "x"))
  }

  @Test fun noMatchIsNull() {
    assertNull(getReflectCallOrNull(target, "oneArg", listOf()))
    assertNull(getReflectCallOrNull(target, "noArgs", listOf("unexpected")))
    assertNull(getReflectCallOrNull(target, "notStrings", listOf("1")))
    assertNull(getReflectCallOrNull(target, "missing", listOf()))
  }
}
