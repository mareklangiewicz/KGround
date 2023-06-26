package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

/** Update the access and modification times of each file to the current time. Create empty files if necessary. */
fun CliPlatform.touchExec(vararg files: String) = touch { this.files.addAll(files) }.exec()

fun touch(init: Touch.() -> Unit = {}) = Touch().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/touch.1.html) */
data class Touch(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "touch"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object timeOfAccessOnly : Option("-a")
        object timeOfChangeOnly : Option("-m")
        object disableCreation : Option("-c")
        data class date(val date: String): Option("--date=$date")
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
