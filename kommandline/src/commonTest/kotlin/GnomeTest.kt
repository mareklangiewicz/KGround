package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Ls.Option.all
import pl.mareklangiewicz.kommand.Ls.Option.humanReadable
import pl.mareklangiewicz.kommand.Ls.Option.long
import pl.mareklangiewicz.kommand.GnomeTerm.Option as GO
import pl.mareklangiewicz.kommand.JournalCtl.Option as JO
import pl.mareklangiewicz.kommand.NotifySend.Option.urgency
import pl.mareklangiewicz.kommand.Zenity.DialogType.calendar
import pl.mareklangiewicz.kommand.Zenity.DialogType.entry
import pl.mareklangiewicz.kommand.Zenity.DialogType.error
import pl.mareklangiewicz.kommand.Zenity.DialogType.fileselection
import pl.mareklangiewicz.kommand.Zenity.DialogType.info
import pl.mareklangiewicz.kommand.Zenity.DialogType.list
import pl.mareklangiewicz.kommand.Zenity.DialogType.notification
import pl.mareklangiewicz.kommand.Zenity.DialogType.progress
import pl.mareklangiewicz.kommand.Zenity.DialogType.question
import pl.mareklangiewicz.kommand.Zenity.DialogType.scale
import pl.mareklangiewicz.kommand.Zenity.DialogType.textinfo
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
    @Test fun testZenityNotification() = zenity(notification) { -text("Some notification") }.checkWithUser()
    @Test fun testZenityProgress() = zenity(progress) { -text("Some progress"); -pulsate }.checkWithUser()
    @Test fun testZenityQuestion() = zenity(question) { -text("Some wierdddddd question"); nowrap }.checkWithUser()
    @Test fun testZenityTextInfo() = zenity(textinfo) { -filename("build.gradle.kts") }.checkWithUser()
    @Test fun testZenityScale() = zenity(scale) { -initvalue(2); -minvalue(1); -maxvalue(8) }.checkWithUser()

    @Test fun testZenityList() = zenity(list) {
        -text("a list")
        -column("col 1")
        -column("col 2")
        repeat(10) {
            +"col 1 row $it"
            +"col 2 row $it"
        }
    }.checkWithUser()
    @Test fun testZenityCheckList() = zenity(list) {
        -checklist
        -text("a list")
        -column("chk")
        -column("labels")
        repeat(10) {
            + (it % 3 == 0).toString()
            +"label $it"
        }
    }.checkWithUser()
    @Test fun testZenityRadioList() = zenity(list) {
        -radiolist
        -text("a list")
        -column("radio")
        -column("labels")
        -column("descs")
        repeat(6) {
            + (it == 1).toString()
            +"label $it"
            +"desc $it"
        }
    }.checkWithUser()
    @Test fun testZenityListFromLs() { // TODO_someday: nice parsing for ls output columns etc..
        val lines = ls { -all; -long; -humanReadable }.shell().out
        zenity(list) {
            -text("ls output")
            -column("ls output")
            for (l in lines) +"line $l"
                // some prefix like "line" is needed,
                // so it doesn't confuse line starting with "-" with zenity option
        }.checkWithUser()
    }

    // TODO_someday bash (& nobash) pipes (both typesafe!). Best if I can compose in kotlin (without bash) sth like:
    //find . -name '*.h' | zenity --list --title "Search Results" --text "Finding all header files.." --column "Files"
}
