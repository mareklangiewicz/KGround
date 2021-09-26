@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

fun ls(init: Ls.() -> Unit) = Ls().apply(init)
fun adb(command: Adb.Command, init: Adb.() -> Unit) = Adb(command).apply(init)

/** anonymous kommand to use only if no actual Kommand class defined */
fun kommand(name: String, vararg args: String) = object : Kommand {
    override val name get() = name
    override val args get() = args.toList()
}

// TODO: full documentation in kdoc (all commands, options, etc)
//  (check in practice to make sure it's optimal for IDE users)

interface Kommand {
    val name: String
    val args: List<String>
    fun line() = (listOf(name) + args).joinToString(" ")
    fun println() = println(line())
}


/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name = "ls"
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

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
    var command: Command = Command.help,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name = "adb"
    override val args get() = options.map { it.str } + listOf(command.name)

        sealed class Command(val name: String) {
        object help : Command("help")
        object devices : Command("devices")
        object version : Command("version")
    }

    sealed class Option(val str: String) {
        /** Listen on all network interfaces, not just localhost */
        object all : Option("-a")
        /** Use USB device (error if multiple devices connected) */
        object usb : Option("-d")
        /** Use TCP/IP device (error if multiple TCP/IP devices available) */
        object tcp : Option("-e")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
