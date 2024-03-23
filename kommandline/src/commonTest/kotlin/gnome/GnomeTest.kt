package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.NotifySend.Option.*
import pl.mareklangiewicz.kommand.systemd.*
import pl.mareklangiewicz.kommand.systemd.JournalCtl.Option.*
import pl.mareklangiewicz.kommand.term.*
import kotlin.test.Test


@OptIn(DelicateApi::class)
class GnomeTest {
    @Test fun testJournalCtl() = journalctl { -Follow; -Cat; +"/usr/bin/gnome-shell" }
        .tryInteractivelyCheck("journalctl -f -ocat /usr/bin/gnome-shell")

    @Test fun testTermGnome() =
        termGnome(kommand("vim")) { -TermGnomeOpt.Verbose; -TermGnomeOpt.Title("strange terminal title") }
        .tryInteractivelyCheck("gnome-terminal -v --title=strange terminal title -- vim")

    @Test fun testGLibCompileSchemas() = kommand("glib-compile-schemas", "schemas/")
        .tryInteractivelyCheck("glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext")

    @Test fun testNotify() = notify("aa", "some longer body") { -Urgency("critical") }
        .tryInteractivelyCheck("notify-send --urgency=critical aa some longer body")
}
