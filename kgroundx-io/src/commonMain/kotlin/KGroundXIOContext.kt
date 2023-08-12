package pl.mareklangiewicz.kground.io

import pl.mareklangiewicz.kommand.CliPlatform

/** Sth like this be used as a context receiver when Kotlin supports it. For now just pass it as "kg" parameter.*/
interface KGroundXIOContext: KGroundIOContext, KGCliPlatformContext

interface KGCliPlatformContext {
    val kgCliPlatform: CliPlatform
}
