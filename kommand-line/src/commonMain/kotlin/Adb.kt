package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.udata.MutLO

fun adb(command: Adb.Command, init: Adb.() -> Unit = {}) = Adb(command).apply(init)

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
  var command: Command = Command.Help,
  val options: MutableList<Option> = MutLO(), // these are "global" options - TODO_later other options
) : Kommand {
  override val name get() = "adb"
  override val args get() = options.map { it.str } + command.str

  sealed class Command(val name: String, val arg: String? = null) {
    open val str get() = LO(name) plusIfNN arg

    data object Help : Command("help")
    data object Devices : Command("devices") // TODO: -l
    data object Version : Command("version")
    data class Pair(val ip: String, val port: Int) :
      Command("pair", "$ip:$port") // TODO: arg with optional pair code

    data class Connect(val ip: String, val port: Int = 5555) : Command("connect", "$ip:$port")
    data object Shell : Command("shell")
  }

  sealed class Option(val str: String) {
    /** Listen on all network interfaces, not just localhost */
    data object All : Option("-a")
    /** Use USB device (error if multiple devices connected) */
    data object Usb : Option("-d")
    /** Use TCP/IP device (error if multiple TCP/IP devices available) */
    data object Tcp : Option("-e")
  }

  operator fun Option.unaryMinus() = options.add(this)
}
