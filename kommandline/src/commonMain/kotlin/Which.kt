package pl.mareklangiewicz.kommand

fun which(all: Boolean = false, init: Which.() -> Unit = {}) = Which(all).apply(init)

data class Which(
    var all: Boolean = false,
    val names: MutableList<String> = mutableListOf()
): Kommand {
    override val name get() = "which"
    override val args get() = names prependIfNotNull "-a".takeIf { all }
    operator fun String.unaryPlus() = names.add(this)
}