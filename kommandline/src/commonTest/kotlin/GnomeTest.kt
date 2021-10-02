package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeTerm.Option.title
import pl.mareklangiewicz.kommand.GnomeTerm.Option.verbose
import pl.mareklangiewicz.kommand.JournalCtl.Option.cat
import pl.mareklangiewicz.kommand.JournalCtl.Option.follow
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import pl.mareklangiewicz.kommand.Zenity.DialogType.entry
import pl.mareklangiewicz.kommand.Zenity.DialogType.fileselection
import kotlin.test.Test
import kotlin.test.assertEquals


// TODO_someday: intellij plugin with @param UI similar to colab notebooks
private const val USER_ENABLED = true
//private const val USER_ENABLED = false

private fun Kommand.checkWithUser(expectedKommandLine: String, execInDir: String? = null) {
    this.println()
    assertEquals(expectedKommandLine, line())
    if (USER_ENABLED) execInGnomeTermIfUserConfirms(execInDir = execInDir)
}

class GnomeTest {

    @Test fun testQuoteShSpecials() {
        val str = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
        val out = str.quoteBashMetaChars()
        println(str)
        println(out)
        assertEquals("abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno", out)
    }
    @Test fun testJournalCtl() = journalctl { -follow; -cat; +"/usr/bin/gnome-shell" }
        .checkWithUser("journalctl -f -ocat /usr/bin/gnome-shell")
    @Test fun testGnomeTerminal() = gnometerm(kommand("vim")) { -verbose; -title("strange terminal title") }
        .checkWithUser("gnome-terminal --verbose --title=strange\\ terminal\\ title -- vim")
    @Test fun testGnomeExt() = gnomeext_list()
        .checkWithUser("gnome-extensions list")
    @Test fun testGnomeExtPrefs() = gnomeext_prefs("mygnomeext@mareklangiewicz.pl")
        .checkWithUser("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")
    @Test fun testGnomeMagic() = kommand("dbus-run-session", "--", "gnome-shell", "--nested", "--wayland")
        .checkWithUser("dbus-run-session -- gnome-shell --nested --wayland")
    @Test fun testGLibCompileSchemas() = kommand("glib-compile-schemas", "schemas/")
        .checkWithUser("glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext")
    @Test fun testNotify() = notify("aa", "some longer body") { -urgency("critical") }
        .checkWithUser("notify-send --urgency=critical aa some\\ longer\\ body")

    @Test
    fun testZenity() {
        val z1 = zenity(entry)
        assertEquals("zenity --entry", z1.line())
        val e = z1.shell()
        println(e)
        val e2 = zenity(fileselection).shell()
        println(e2)
    }
}
