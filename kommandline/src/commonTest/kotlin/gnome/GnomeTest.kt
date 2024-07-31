package pl.mareklangiewicz.kommand.gnome

import kotlin.test.Test
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.NotifySend.Option.*
import pl.mareklangiewicz.kommand.systemd.*
import pl.mareklangiewicz.kommand.systemd.JournalCtl.Option.*
import pl.mareklangiewicz.kommand.term.*


@OptIn(DelicateApi::class, NotPortableApi::class)
class GnomeTest {
  @Test fun testJournalCtl() = journalctl { -Follow; -Cat; +"/usr/bin/gnome-shell" }
    .tryInteractivelyCheckBlockingOrErr("journalctl -f -ocat /usr/bin/gnome-shell")

  @Test fun testTermGnome() =
    termGnome(kommand("vim")) { -TermGnomeOpt.Verbose; -TermGnomeOpt.Title("strange terminal title") }
      .tryInteractivelyCheckBlockingOrErr("gnome-terminal -v --title=strange terminal title -- vim")

  @Test fun testGLibCompileSchemas() = kommand("glib-compile-schemas", "schemas/")
    .tryInteractivelyCheckBlockingOrErr("glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext".toPath())

  @Test fun testNotify() = notify("aa", "some longer body") { -Urgency("critical") }
    .tryInteractivelyCheckBlockingOrErr("notify-send --urgency=critical aa some longer body")
}
