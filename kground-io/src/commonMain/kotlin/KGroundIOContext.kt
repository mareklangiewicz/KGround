package pl.mareklangiewicz.kground.io

import okio.FileSystem
import pl.mareklangiewicz.kground.KGroundContext

/** Sth like this be used as a context receiver when Kotlin supports it. For now just pass it as "kg" parameter.*/
interface KGroundIOContext: KGroundContext, KGOkioSystemContext

interface KGOkioSystemContext: KGroundContext {
    val kgOkioSystem: FileSystem
}
