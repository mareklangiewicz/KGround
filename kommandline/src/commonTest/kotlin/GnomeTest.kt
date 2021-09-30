package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeTerm.Option.title
import pl.mareklangiewicz.kommand.GnomeTerm.Option.verbose
import pl.mareklangiewicz.kommand.JournalCtl.Option.cat
import pl.mareklangiewicz.kommand.JournalCtl.Option.follow
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import kotlin.test.Test
import kotlin.test.assertEquals

class GnomeTest {

    @Test
    fun testJournalCtl() {

        val kommand = journalctl {
            - follow
            - cat
            + "/usr/bin/gnome-shell"
        }

        kommand.println()

        assertEquals("journalctl -f -o cat /usr/bin/gnome-shell", kommand.line())
    }

    @Test
    fun testGnomeTerminal() {
        val kommand = gnometerm(kommand("vim")) {
            - verbose
            - title("oldeditor")
        }
        kommand.println()
        assertEquals("gnome-terminal --verbose --title=oldeditor -- vim", kommand.line())
//        kommand.exec()
    }


    @Test
    fun testGnomeExt() {
        val kommand = gnomeext_list()
        assertEquals("gnome-extensions list", kommand.line())
//        kommand.shell().out.printlns()
    }

    @Test fun testGnomeExtPrefs() = gnomeext_prefs("mygnomeext@mareklangiewicz.pl").exec()

    @Test
    fun testGnomeMagic() {
        val kommand = kommand("dbus-run-session", "--", "gnome-shell", "--nested", "--wayland")
        assertEquals("dbus-run-session -- gnome-shell --nested --wayland", kommand.line())
        gnometerm(bash(kommand, pause = true)).exec()
    }

    @Test
    fun testGLibCompileSchemas() {
        val kommand = kommand("glib-compile-schemas", "schemas/")
//        val kommand = kommand("ls", "-lah", "schemas/")
        assertEquals("glib-compile-schemas schemas/", kommand.line())
//        gnometerm(bash(kommand, pause = true)).exec("/home/marek/code/kotlin/kokpit667/mygnomeext")
    }

    @Test
    fun testNotify() {
        val kommand = notify("aa", "some longer body") { -urgency("critical") }
        assertEquals("notify-send --urgency=critical aa some longer body", kommand.line())
//        kommand.exec()
    }
}
