@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand


// TODO: full documentation in kdoc (all commands, options, etc)
//  (check in practice to make sure it's optimal for IDE users)

interface Kommand

fun Kommand.kommandLine() = toString()

fun Any.printLine() = println("$this")

/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    sealed class Option {
        object all : Option() { override fun toString() = "-a" }
        object almostAll : Option() { override fun toString() = "-A" }
        object author : Option() { override fun toString() = "--author" }
        object directory : Option() { override fun toString() = "-d" }
        object humanReadable : Option() { override fun toString() = "-h" }
        object long : Option() { override fun toString() = "-l" }
        object recursive : Option() { override fun toString() = "-R" }
        object reverse : Option() { override fun toString() = "-r" }
        object size : Option() { override fun toString() = "-s" }
        data class sort(val type: sortType): Option() { override fun toString() = "--sort=$type" }
        enum class sortType { NONE, SIZE, TIME, VERSION, EXTENSION;
            override fun toString() = super.toString().lowercase()
        }
        data class time(val type: timeType): Option() { override fun toString() = "--sort=$type" }
        enum class timeType { ATIME, ACCESS, CTIME, STATUS;
            override fun toString() = super.toString().lowercase()
        }

        object help : Option() { override fun toString() = "--help" }
        object version : Option() { override fun toString() = "--version" }

    }

    override fun toString() = "ls ${options.joinToString(" ")} ${files.joinToString(" ")}"

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}

fun ls(vararg options: Ls.Option, init: Ls.() -> Unit) = Ls(options.toMutableList()).apply(init)

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
    var command: Command = Command.help,
    val options: MutableList<Option> = mutableListOf()
) {

    sealed class Command {
        object help : Command() { override fun toString() = "help" }
        object devices : Command() { override fun toString() = "devices" }
        object version : Command() { override fun toString() = "version" }
    }

    sealed class Option {

        /** Listen on all network interfaces, not just localhost */
        object all : Option() { override fun toString() = "-a" }

        /** Use USB device (error if multiple devices connected) */
        object usb : Option() { override fun toString() = "-d" }

        /** Use TCP/IP device (error if multiple TCP/IP devices available) */
        object tcp : Option() { override fun toString() = "-e" }
    }

    override fun toString() = "adb ${options.joinToString(" ")} $command"

    operator fun Option.unaryMinus() = options.add(this)
}

fun adb(command: Adb.Command, vararg options: Adb.Option, init: Adb.() -> Unit) =
    Adb(command, options.toMutableList()).apply(init)