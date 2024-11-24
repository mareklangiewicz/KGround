package pl.mareklangiewicz.ureflect

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

// TODO later: better designed, more universal API. Current one is just "MVP" for use cases I need now.

@DelicateApi
@NotPortableApi("Only JVM supported; null will be returned on other platforms")
expect fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)?

@DelicateApi("Generally delicate, but in particular it changes getter.isAccessible on JVM")
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
expect fun <T : Any> T.getReflectNamedPropsValues(): List<Pair<String, Any?>>


@DelicateApi
@NotPortableApi("Only JVM supported; empty list will be returned on other platforms")
expect fun <T : Any> T.getReflectSomeMemberFunctions(except: Set<String> = setOf("equals", "hashCode", "toString")): List<KFunction<*>>

