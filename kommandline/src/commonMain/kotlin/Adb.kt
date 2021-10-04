package pl.mareklangiewicz.kommand

fun adb(command: Adb.Command, init: Adb.() -> Unit = {}) = Adb(command).apply(init)

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
    var command: Command = Command.help,
    val options: MutableList<Option> = mutableListOf() // these are "global" options - TODO_later other options
): Kommand {
    override val name get() = "adb"
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