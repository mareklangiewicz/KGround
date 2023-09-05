package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*

@OptIn(DelicateKommandApi::class)
fun mktemp(
    vararg useNamedArgs: Unit,
    path: String = ".",
    prefix: String = "tmp.",
    suffix: String = ".tmp",
) = mktemp("$path/${prefix}XXXXXX${suffix}").reducedOut { single() }

@DelicateKommandApi
fun mktemp(template: String, init: MkTemp.() -> Unit = {}) = mktemp { +template; init() }

@DelicateKommandApi
fun mktemp(init: MkTemp.() -> Unit) = MkTemp().apply(init)

@DelicateKommandApi
data class MkTemp(
    override val opts: MutableList<MkTempOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<MkTempOpt> { override val name get() = "mktemp" }

@DelicateKommandApi
interface MkTempOpt: KOptTypical {
    data object Directory : KOptS("d"), MkTempOpt
    data object DryRun : KOptS("u"), MkTempOpt
    data object Quiet : KOptS("q"), MkTempOpt
    data class Suffix(val suffix: String) : KOptL("suffix", suffix), MkTempOpt
    data class TmpDir(val dir: String? = null) : KOptS("p", dir), MkTempOpt
    data object Help : KOptL("help"), MkTempOpt
    data object Version : KOptL("version"), MkTempOpt
}
