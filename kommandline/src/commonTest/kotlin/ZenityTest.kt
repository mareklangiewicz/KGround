package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.Zenity.DialogType.*
import pl.mareklangiewicz.kommand.Zenity.Option.*
import pl.mareklangiewicz.kommand.core.*
import kotlin.test.Test


class ZenityTest {
    @Test fun testZenityEntryCheck() = zenity(entry) { -text("some question") }.checkWithUser()
    @Test fun testZenityEntryStart() = ifInteractive { SYS.start(zenity(entry) { -entrytext("suggested text") }) }

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
            +(it % 3 == 0).toString()
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
            +(it == 1).toString()
            +"label $it"
            +"desc $it"
        }
    }.checkWithUser()
    @Test fun testZenityListFromLs() = SYS.run { // TODO_someday: nice parsing for ls output columns etc..
        val lines = ls { -All; -LongFormat; -HumanReadable }.exec()
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
