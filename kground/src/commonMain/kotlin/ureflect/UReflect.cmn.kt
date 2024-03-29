package pl.mareklangiewicz.ureflect

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi

// TODO later: better, more universal api. This one is just for use case I need now.
@NotPortableApi
@DelicateApi
expect fun getReflectCallOrNull(className: String, memberName: String): (suspend () -> Any?)?

