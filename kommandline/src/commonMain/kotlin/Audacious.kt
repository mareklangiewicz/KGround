package pl.mareklangiewicz.kommand

fun audacious(vararg files: String, init: Audacious.() -> Unit = {}) = Audacious(files.toMutableList()).apply(init)

data class Audacious(
    val files: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "audacious"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        data object help : Option("--help")
        data object enqueue : Option("--enqueue")
        data object play : Option("--play")
        data object pause : Option("--pause")
        data object stop : Option("--stop")
        data object rew : Option("--rew")
        data object fwd : Option("--fwd")
        data object version : Option("--version")
        data object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}