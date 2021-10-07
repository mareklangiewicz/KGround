package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeTerm.Option.title
import pl.mareklangiewicz.kommand.GnomeTerm.Option.verbose
import pl.mareklangiewicz.kommand.JournalCtl.Option.cat
import pl.mareklangiewicz.kommand.JournalCtl.Option.follow
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
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
