package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*

fun adb(command: Adb.Command, init: Adb.() -> Unit = {}) = Adb(command).apply(init)

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
    var command: Command = Command.help,
    val options: MutableList<Option> = mutableListOf() // these are "global" options - TODO_later other options
): Kommand {
    override val name get() = "adb"
    override val args get() = options.map { it.str } + command.str

    sealed class Command(val name: String, val arg: String? = null) {
        open val str get() = listOf(name) plusIfNN arg
        data object help : Command("help")
        data object devices : Command("devices") // TODO: -l
        data object version : Command("version")
        data class pair(val ip: String, val port: Int) : Command("pair", "$ip:$port") // TODO: arg with optional pair code
        data class connect(val ip: String, val port: Int = 5555) : Command("connect", "$ip:$port")
        data object shell : Command("shell")
    }

    sealed class Option(val str: String) {
        /** Listen on all network interfaces, not just localhost */
        data object all : Option("-a")
        /** Use USB device (error if multiple devices connected) */
        data object usb : Option("-d")
        /** Use TCP/IP device (error if multiple TCP/IP devices available) */
        data object tcp : Option("-e")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
