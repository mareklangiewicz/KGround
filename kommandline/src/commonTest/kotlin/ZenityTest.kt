package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ZenityOpt.*
import pl.mareklangiewicz.kommand.ZenityOpt.Type.*
import pl.mareklangiewicz.kommand.core.*
import kotlin.test.Test


@OptIn(DelicateApi::class)
class ZenityTest {
    @Test fun testZenityEntryCheck() = zenity(Entry) { -Text("some question") }.chkWithUser()
    @Test fun testZenityEntryStart() = ifInteractiveCodeEnabled {
        SYS.start(zenity(Entry) { -EntryText("suggested text") })
    }

    @Test fun testZenityCalendar() = zenity(Calendar) { -Title("some title"); -Text("some text") }
        .chkWithUser("zenity --calendar --title=some title --text=some text")

    @Test fun testZenityCalendarFormat() = zenity(Calendar) { -DateFormat("%y-%m-%d") }.chkWithUser()
    @Test fun testZenityInfo() = zenity(Info) { -Text("Some info (timeout 5s)"); -Timeout(5) }.chkWithUser()
    @Test fun testZenityWarning() = zenity(Warning) { -Text("Some Warning (timeout 3s)"); -Timeout(3) }.chkWithUser()
    @Test fun testZenityError() = zenity(Error) { -Text("Some loooooong looong ERROR!"); -NoWrap }.chkWithUser()
    @Test fun testZenityFileSelection() = zenity(FileSelection) { -Title("Select some file") }.chkWithUser()
    @Test fun testZenityFileMultiple() = zenity(FileSelection) { -Title("Select some files"); -Multiple }.chkWithUser()
    @Test fun testZenityFileDirectory() = zenity(FileSelection) { -Title("Select some dir"); -ZenityOpt.Directory }.chkWithUser()
    @Test fun testZenityFileSave() = zenity(FileSelection) { -Save; -ConfirmOverwrite }.chkWithUser()
    @Test fun testZenityNotification() = zenity(Notification) { -Text("Some notification") }.chkWithUser()
    @Test fun testZenityProgress() = zenity(Progress) { -Text("Some progress"); -Pulsate }.chkWithUser()
    @Test fun testZenityQuestion() = zenity(Question) { -Text("Some wierdddddd question"); NoWrap }.chkWithUser()
    @Test fun testZenityTextInfo() = zenity(TextInfo) { -FileName("build.gradle.kts") }.chkWithUser()
    @Test fun testZenityScale() = zenity(Scale) { -InitValue(2); -MinValue(1); -MaxValue(8) }.chkWithUser()

    @Test fun testZenityList() = zenity(List) {
        -Text("a list")
        -Column("col 1")
        -Column("col 2")
        repeat(10) {
            +"col 1 row $it"
            +"col 2 row $it"
        }
    }.chkWithUser()
    @Test fun testZenityCheckList() = zenity(List) {
        -CheckList
        -Text("a list")
        -Column("chk")
        -Column("labels")
        repeat(10) {
            +(it % 3 == 0).toString()
            +"label $it"
        }
    }.chkWithUser()
    @Test fun testZenityRadioList() = zenity(List) {
        -RadioList
        -Text("a list")
        -Column("radio")
        -Column("labels")
        -Column("descs")
        repeat(6) {
            +(it == 1).toString()
            +"label $it"
            +"desc $it"
        }
    }.chkWithUser()
    @Test fun testZenityListFromLs() = ifInteractiveCodeEnabled {
        SYS.run { // TODO_someday: nice parsing for ls output columns etc..
            val lines = ls { -All; -LongFormat; -HumanReadable }.execb(this)
            zenity(List) {
                -Text("ls output")
                -Column("ls output")
                for (l in lines) +"line $l"
                // some prefix like "line" is needed,
                // so it doesn't confuse line starting with "-" with zenity option
            }.chkWithUser()
        }
    }

    // TODO_someday bash (& nobash) pipes (both typesafe!). Best if I can compose in kotlin (without bash) sth like:
    //find . -name '*.h' | zenity --list --title "Search Results" --text "Finding all header files.." --column "Files"
}
