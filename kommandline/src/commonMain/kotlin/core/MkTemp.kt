package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

fun mktemp(template: String? = null, init: MkTemp.() -> Unit = {}) = MkTemp(template).apply(init)

fun CliPlatform.mktempExec(prefix: String = "tmp.", suffix: String = ".tmp") =
    mktemp("$pathToUserTmp/${prefix}XXXXXX${suffix}").exec().single()

data class MkTemp(
    var template: String? = null,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "mktemp"
    override val args get() = options.flatMap { it.str } plusIfNotNull template

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNotNull arg
        object directory : Option("--directory")
        object dryrun : Option("--dry-run")
        object quiet : Option("--quiet")
        data class suffix(val s: String) : Option("--suffix", s)
        data class tmpdir(val dir: String) : Option("--tmpdir", dir)
        object help : Option("--help")
        object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
