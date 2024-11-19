@file:OptIn(NotPortableApi::class)

package pl.mareklangiewicz.kommand

import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.ifInteractiveCodeEnabledBlockingOrErr
import pl.mareklangiewicz.interactive.tryInteractivelyCheck
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.Type.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.udata.strf


@OptIn(DelicateApi::class)
class ZenityTest {
  @Test fun testZenityEntryCheck() = zenity(Entry) { -Text("some question") }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityEntryStart() = ifInteractiveCodeEnabledBlockingOrErr {
    val cli = localCLI()
    cli.lx(zenity(Entry) { -EntryText("suggested text") })
  }

  @Test fun testZenityCalendar() = zenity(Calendar) { -Title("some title"); -Text("some text") }
    .tryInteractivelyCheckBlockingOrErr("zenity --calendar --title=some title --text=some text")

  @Test fun testZenityCalendarFormat() = zenity(Calendar) { -DateFormat("%y-%m-%d") }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityInfo() = zenity(Info) { -Text("Some info (timeout 5s)"); -Timeout(5) }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityWarning() =
    zenity(Warning) { -Text("Some Warning (timeout 3s)"); -Timeout(3) }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityError() =
    zenity(Error) { -Text("Some loooooong looong ERROR!"); -NoWrap }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityFileSelection() = zenity(FileSelection) { -Title("Select some file") }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityFileMultiple() =
    zenity(FileSelection) { -Title("Select some files"); -Multiple }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityFileDirectory() =
    zenity(FileSelection) { -Title("Select some dir"); -ZenityOpt.Directory }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityFileSave() = zenity(FileSelection) { -Save; -ConfirmOverwrite }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityNotification() = zenity(Notification) { -Text("Some notification") }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityProgress() = zenity(Progress) { -Text("Some progress"); -Pulsate }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityQuestion() =
    zenity(Question) { -Text("Some wierdddddd question"); NoWrap }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityTextInfo() = zenity(TextInfo) { -FileName("build.gradle.kts") }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testZenityScale() = zenity(Scale) { -InitValue(2); -MinValue(1); -MaxValue(8) }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityList() = zenity(List) {
    -Text("a list")
    -Column("col 1")
    -Column("col 2")
    repeat(10) {
      +"col 1 row $it"
      +"col 2 row $it"
    }
  }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityCheckList() = zenity(List) {
    -CheckList
    -Text("a list")
    -Column("chk")
    -Column("labels")
    repeat(10) {
      +(it % 3 == 0).strf
      +"label $it"
    }
  }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityRadioList() = zenity(List) {
    -RadioList
    -Text("a list")
    -Column("radio")
    -Column("labels")
    -Column("descs")
    repeat(6) {
      +(it == 1).strf
      +"label $it"
      +"desc $it"
    }
  }.tryInteractivelyCheckBlockingOrErr()

  @Test fun testZenityListFromLs() = ifInteractiveCodeEnabledBlockingOrErr {
    val lines = ls { -All; -LongFormat; -BlockHuman }.ax()
    zenity(List) {
      -Text("ls output")
      -Column("ls output")
      for (l in lines) +"line $l"
      // some prefix like "line" is needed,
      // so it doesn't confuse line starting with "-" with zenity option
    }.tryInteractivelyCheck()
  }

  // TODO_someday bash (& nobash) pipes (both typesafe!). Best if I can compose in kotlin (without bash) sth like:
  // find . -name '*.h' | zenity --list --title "Search Results" --text "Finding all header files.." --column "Files"
}
