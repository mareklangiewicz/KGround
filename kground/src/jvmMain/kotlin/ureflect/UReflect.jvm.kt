package pl.mareklangiewicz.ureflect

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@DelicateApi
@NotPortableApi("Only JVM supported; null will be returned on other platforms")
actual fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)? {

  val kClass: KClass<*> = Class.forName(className).kotlin
  val objectOrNull: Any? = kClass.objectInstance
  val kMember: KCallable<*>? = kClass.members.firstOrNull { it.name == memberName }
  when {
    kMember == null -> try {
      val jMethod: Method = kClass.java.getDeclaredMethod(memberName)
      return { jMethod.invoke((objectOrNull)) }
    } catch (_: NoSuchMethodException) { return null }
    kMember.isSuspend -> return { kMember.callSuspend(objectOrNull) }
    else -> return { kMember.call(objectOrNull) }
  }
}

@DelicateApi("Generally delicate, but in particular it changes getter.isAccessible")
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
actual fun <T : Any> T.getReflectNamedPropsValues(): List<Pair<String, Any?>> {
  return (this::class as KClass<T>).declaredMemberProperties
    .filter { it.visibility == KVisibility.PUBLIC }
    .map { it.getter.isAccessible = true; it.name to it(this) }
}


@DelicateApi // BTW this hacky/temporary fun is here mostly to avoid importing full jvm reflection in my tests, etc.
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
actual fun <T : Any> T.getReflectSomeMemberFunctions(except: Set<String>): List<KFunction<*>> =
  (this::class as KClass<T>).declaredMemberFunctions.filter { it.name !in except }
