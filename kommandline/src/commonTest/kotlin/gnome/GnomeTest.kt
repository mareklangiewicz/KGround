package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.gnome.GnomeTerm.Option.title
import pl.mareklangiewicz.kommand.gnome.GnomeTerm.Option.verbose
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import pl.mareklangiewicz.kommand.checkWithUser
import pl.mareklangiewicz.kommand.kommand
import pl.mareklangiewicz.kommand.notify
import pl.mareklangiewicz.kommand.systemd.*
import pl.mareklangiewicz.kommand.systemd.JournalCtl.Option.*
import kotlin.test.Test


class GnomeTest {
    @Test fun testJournalCtl() = journalctl { -follow; -cat; +"/usr/bin/gnome-shell" }
        .checkWithUser("journalctl -f -ocat /usr/bin/gnome-shell")

    @Test fun testGnomeTerminal() = gnometerm(kommand("vim")) { -verbose; -title("strange terminal title") }
        .checkWithUser("gnome-terminal --verbose --title=strange\\ terminal\\ title -- vim")

    @Test fun testGLibCompileSchemas() = kommand("glib-compile-schemas", "schemas/")
        .checkWithUser("glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext")

    @Test fun testNotify() = notify("aa", "some longer body") { -urgency("critical") }
        .checkWithUser("notify-send --urgency=critical aa some\\ longer\\ body")
}
