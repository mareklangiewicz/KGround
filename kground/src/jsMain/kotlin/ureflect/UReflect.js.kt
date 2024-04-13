package pl.mareklangiewicz.ureflect

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.e

@NotPortableApi
@DelicateApi
actual fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)? = null.also {
  ulog.e("getReflectCallOrNull not implemented on JS yet")
}

