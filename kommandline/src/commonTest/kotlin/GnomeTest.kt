package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeTerm.Option as GO
import pl.mareklangiewicz.kommand.JournalCtl.Option as JO
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import pl.mareklangiewicz.kommand.Zenity.DialogType.calendar
import pl.mareklangiewicz.kommand.Zenity.DialogType.entry
import pl.mareklangiewicz.kommand.Zenity.DialogType.error
import pl.mareklangiewicz.kommand.Zenity.DialogType.fileselection
import pl.mareklangiewicz.kommand.Zenity.DialogType.info
import pl.mareklangiewicz.kommand.Zenity.DialogType.warning
import pl.mareklangiewicz.kommand.Zenity.Option.*
import kotlin.test.Test
import kotlin.test.assertEquals


class GnomeTest {

    @Test fun testJournalCtl() = journalctl { -JO.follow; -JO.cat; +"/usr/bin/gnome-shell" }
        .checkWithUser("journalctl -f -ocat /usr/bin/gnome-shell")
    @Test fun testGnomeTerminal() = gnometerm(kommand("vim")) { -GO.verbose; -GO.title("strange terminal title") }
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

    @Test fun testZenityEntryCheck() = zenity(entry) { -text("some question") }.checkWithUser()
    @Test fun testZenityEntryStart() = ifInteractive { zenity(entry) { -entrytext("suggested text") }.exec() }

    @Test fun testZenityCalendar() = zenity(calendar) { -title("some title"); -text("some text") }
        .checkWithUser("zenity --calendar --title=some\\ title --text=some\\ text")

    @Test fun testZenityCalendarFormat() = zenity(calendar) { -dateformat("%y-%m-%d") }.checkWithUser()
    @Test fun testZenityInfo() = zenity(info) { -text("Some info (timeout 5s)"); -timeout(5) }.checkWithUser()
    @Test fun testZenityWarning() = zenity(warning) { -text("Some Warning (timeout 3s)"); -timeout(3) }.checkWithUser()
    @Test fun testZenityError() = zenity(error) { -text("Some loooooong looong ERROR!"); -nowrap }.checkWithUser()
    @Test fun testZenityFileSelection() = zenity(fileselection) { -title("Select some file") }.checkWithUser()
    @Test fun testZenityFileMultiple() = zenity(fileselection) { -title("Select some files"); -multiple }.checkWithUser()
    @Test fun testZenityFileDirectory() = zenity(fileselection) { -title("Select some dir"); -directory }.checkWithUser()
    @Test fun testZenityFileSave() = zenity(fileselection) { -save; -confirmoverwrite }.checkWithUser()
}
