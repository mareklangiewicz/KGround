package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import pl.mareklangiewicz.kommand.systemd.*
import pl.mareklangiewicz.kommand.systemd.JournalCtl.Option.*
import pl.mareklangiewicz.kommand.term.*
import kotlin.test.Test


@OptIn(DelicateKommandApi::class)
class GnomeTest {
    @Test fun testJournalCtl() = journalctl { -follow; -cat; +"/usr/bin/gnome-shell" }
        .chkWithUser("journalctl -f -ocat /usr/bin/gnome-shell")

    @Test fun testTermGnome() =
        termGnome(kommand("vim")) { -TermGnomeOpt.Verbose; -TermGnomeOpt.Title("strange terminal title") }
        .chkWithUser("gnome-terminal -v --title=strange terminal title -- vim")

    @Test fun testGLibCompileSchemas() = kommand("glib-compile-schemas", "schemas/")
        .chkWithUser("glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext")

    @Test fun testNotify() = notify("aa", "some longer body") { -urgency("critical") }
        .chkWithUser("notify-send --urgency=critical aa some longer body")
}
