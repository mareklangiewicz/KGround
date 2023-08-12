package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*

fun mktemp(template: String? = null, init: MkTemp.() -> Unit = {}) = MkTemp(template).apply(init)

fun CliPlatform.mktempExec(prefix: String = "tmp.", suffix: String = ".tmp") =
    mktemp("$pathToUserTmp/${prefix}XXXXXX${suffix}").execb(this).single()

data class MkTemp(
    var template: String? = null,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "mktemp"
    override val args get() = options.flatMap { it.str } plusIfNN template

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNN arg
        data object directory : Option("--directory")
        data object dryrun : Option("--dry-run")
        data object quiet : Option("--quiet")
        data class suffix(val s: String) : Option("--suffix", s)
        data class tmpdir(val dir: String) : Option("--tmpdir", dir)
        data object help : Option("--help")
        data object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
