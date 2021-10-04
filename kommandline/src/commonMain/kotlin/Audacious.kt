package pl.mareklangiewicz.kommand

fun audacious(vararg files: String, init: Audacious.() -> Unit = {}) = Audacious(files.toMutableList()).apply(init)

data class Audacious(
    val files: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "audacious"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object help : Option("--help")
        object enqueue : Option("--enqueue")
        object play : Option("--play")
        object pause : Option("--pause")
        object stop : Option("--stop")
        object rew : Option("--rew")
        object fwd : Option("--fwd")
        object version : Option("--version")
        object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}