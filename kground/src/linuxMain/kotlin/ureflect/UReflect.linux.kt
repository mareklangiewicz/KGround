package pl.mareklangiewicz.ureflect

import kotlin.reflect.KFunction
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.udata.LO

@DelicateApi
@NotPortableApi("Only JVM supported; null will be returned on other platforms")
actual fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)? = null

@DelicateApi("Generally delicate, but in particular it changes getter.isAccessible")
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
actual fun <T : Any> T.getReflectNamedPropsValues(): List<Pair<String, Any?>> = LO<Pair<String, Any?>>()


@DelicateApi
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
actual fun <T : Any> T.getReflectSomeMemberFunctions(except: Set<String>): List<KFunction<*>> = LO()
