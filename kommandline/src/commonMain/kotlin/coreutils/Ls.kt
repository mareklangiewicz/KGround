package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "ls"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object all : Option("-a")
        object almostAll : Option("-A")
        object author : Option("--author")
        object directory : Option("-d")
        object humanReadable : Option("-h")
        object long : Option("-l")
        object recursive : Option("-R")
        object reverse : Option("-r")
        object size : Option("-s")
        data class sort(val type: sortType): Option("--sort=$type")
        enum class sortType { NONE, SIZE, TIME, VERSION, EXTENSION;
            override fun toString() = super.toString().lowercase()
        }
        data class time(val type: timeType): Option("--time=$type")
        enum class timeType { ATIME, ACCESS, CTIME, STATUS;
            override fun toString() = super.toString().lowercase()
        }
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
