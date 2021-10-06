package pl.mareklangiewicz.kommand

fun xdgopen(file: String, init: XdgOpen.() -> Unit = {}) = XdgOpen(file).apply(init)

data class XdgOpen(
    var file: String? = null,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "xdg-open"
    override val args get() = options.map { it.str } plusIfNotNull file

    sealed class Option(val str: String) {
        object help : Option("--help")
        object manual : Option("--manual")
        object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}