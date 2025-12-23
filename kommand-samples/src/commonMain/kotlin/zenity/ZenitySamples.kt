package pl.mareklangiewicz.kommand.zenity

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.InteractiveScript
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.Type.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.localCLI
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.udata.strf


// clean allTests; allTests; check if testing all by messing expectations to;

@OptIn(DelicateApi::class, NotPortableApi::class)
data object ZenitySamples {
  val zenityEntryCheck = zenity(Entry) { -Text("some question") } s "zenity --entry --text=some question"
  val zenityEntryStart = InteractiveScript {
    val cli = localCLI()
    cli.lx(zenity(Entry) { -EntryText("suggested text") })
  }

  val zenityCalendar = zenity(Calendar) { -Title("some title"); -Text("some text") } s "zenity --calendar --title=some title --text=some text"

  val zenityCalendarFormat = zenity(Calendar) { -DateFormat("%y-%m-%d") } s "zenity --calendar --date-format=%y-%m-%d"
  val zenityInfo = zenity(Info) { -Text("Some info (timeout 5s)"); -Timeout(5) } s "zenity --info --text=Some info (timeout 5s) --timeout=5"
  val zenityWarning = zenity(Warning) { -Text("Some Warning (timeout 3s)"); -Timeout(3) } s "zenity --warning --text=Some Warning (timeout 3s) --timeout=3"

  val zenityError = zenity(Error) { -Text("Some loooooong looong LOOOOOOOOOOOOOOOONG ERROR!"); -NoWrap } s "zenity --error --text=Some loooooong looong LOOOOOOOOOOOOOOOONG ERROR! --no-wrap"

  val zenityFileSelection = zenity(FileSelection) { -Title("Select some file") } s "zenity --file-selection --title=Select some file"
  val zenityFileMultiple = zenity(FileSelection) { -Title("Select some files"); -Multiple } s "zenity --file-selection --title=Select some files --multiple"

  val zenityFileDirectory = zenity(FileSelection) { -Title("Select some dir"); -ZenityOpt.Directory } s "zenity --file-selection --title=Select some dir --directory"

  val zenityFileSave = zenity(FileSelection) { -Save; -ConfirmOverwrite } s "zenity --file-selection --save --confirm-overwrite"
  val zenityNotification = zenity(Notification) { -Text("Some notification") } s "zenity --notification --text=Some notification"
  val zenityProgress = zenity(Progress) { -Text("Some progress"); -Pulsate } s "zenity --progress --text=Some progress --pulsate"
  val zenityQuestion = zenity(Question) { -Text("Some wierdddddd question"); NoWrap } s "zenity --question --text=Some wierdddddd question"

  val zenityTextInfo = zenity(TextInfo) { -FileName("build.gradle.kts") } s "zenity --text-info --filename=build.gradle.kts"
  val zenityScale = zenity(Scale) { -InitValue(2); -MinValue(1); -MaxValue(8) } s "zenity --scale --value=2 --min-value=1 --max-value=8"

  val zenityList = zenity(List) {
    -Text("a list")
    -Column("col 1")
    -Column("col 2")
    repeat(10) {
      +"col 1 row $it"
      +"col 2 row $it"
    }
  }

  val zenityCheckList = zenity(List) {
    -CheckList
    -Text("a list")
    -Column("chk")
    -Column("labels")
    repeat(10) {
      +(it % 3 == 0).strf
      +"label $it"
    }
  }

  val zenityRadioList = zenity(List) {
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
  }

  val zenityListFromLs = InteractiveScript {
    val lines = ls { -All; -LongFormat; -BlockHuman }.ax()
    zenity(List) {
      -Text("ls output")
      -Column("ls output")
      for (l in lines) +"line $l"
      // some prefix like "line" is needed,
      // so it doesn't confuse line starting with "-" with zenity option
    }.ax()
  }

  // TODO_someday bash (& nobash) pipes (both typesafe!). Best if I can compose in kotlin (without bash) sth like:
  // find . -name '*.h' | zenity --list --title "Search Results" --text "Finding all header files.." --column "Files"
}
