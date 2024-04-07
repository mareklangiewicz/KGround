package pl.mareklangiewicz.ureflect

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend

@NotPortableApi
@DelicateApi
actual fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)? {

  val kClass: KClass<*> = Class.forName(className).kotlin
  val objectOrNull: Any? = kClass.objectInstance
  val kMember: KCallable<*>? = kClass.members.firstOrNull { it.name == memberName }
  when {
    kMember == null -> {
      val jMethod: Method = kClass.java.getDeclaredMethod(memberName)
      return { jMethod.invoke((objectOrNull)) }
    }
    kMember.isSuspend -> return { kMember.callSuspend(objectOrNull) }
    else -> return { kMember.call(objectOrNull) }
  }
}

