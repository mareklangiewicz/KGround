@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand.dbus

import pl.mareklangiewicz.kommand.Kommand

/** [dbus-run-session freedesktop man](https://dbus.freedesktop.org/doc/dbus-run-session.1.html) */
fun dbusrunsession(program: Kommand? = null, init: DBusRunSession.() -> Unit = {}) = DBusRunSession(program).apply(init)

/** [dbus-run-session freedesktop man](https://dbus.freedesktop.org/doc/dbus-run-session.1.html) */
data class DBusRunSession(
  var kommand: Kommand? = null,
  val options: MutableList<Option> = mutableListOf(),
) : Kommand {
  override val name get() = "dbus-run-session"
  override val args get() = options.map { it.str } + kommand?.let { listOf("--", it.name) + it.args }.orEmpty()

  sealed class Option(val name: String, val arg: String? = null) {
    val str get() = if (arg == null) name else "$name=$arg"

    data class ConfigFile(val filename: String) : Option("--config-file", filename)
    data class DbusDaemon(val binary: String) : Option("--dbus-daemon", binary)
    data object Help : Option("--help")
    data object Version : Option("--version")
  }

  operator fun Option.unaryMinus() = options.add(this)
}
