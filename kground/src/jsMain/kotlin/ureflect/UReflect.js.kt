package pl.mareklangiewicz.ureflect

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

@NotPortableApi
@DelicateApi
actual fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)? = null.also {
    println("getReflectCallOrNull not implemented on JS yet")
}

