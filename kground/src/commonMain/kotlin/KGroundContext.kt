package pl.mareklangiewicz.kground

import kotlinx.coroutines.CoroutineScope
import pl.mareklangiewicz.ulog.ULog

/** Sth like this be used as a context receiver when Kotlin supports it. For now just pass it as "kg" parameter.*/
interface KGroundContext: KGCoroutineScopeContext, ULog

interface KGCoroutineScopeContext {
    val kgCoroutineScope: CoroutineScope
}
