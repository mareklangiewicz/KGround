@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kground.logEach
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.Type.*
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.udata.strf


// TODO: probably most of examples here should rather be in KommandLine/kommandsamples
//   and here only some examples specific for my own peculiar use cases.

@ExampleApi
@OptIn(DelicateApi::class)
object MyZenityExamples {

  // https://help.gnome.org/users/zenity/stable/message.html.en
  suspend fun showSomeWarningWithTimeout3s() = zenityShowWarning("Warning", labelOk = "OOK", withTimeoutSec = 3).ax()

  // https://help.gnome.org/users/zenity/stable/entry.html.en
  suspend fun askForEntryWithTimeout3s() = zenityAskForEntry("Enter something", withTimeoutSec = 3).ax()

  // https://help.gnome.org/users/zenity/stable/entry.html.en
  suspend fun askForPassword() = zenityAskForPassword("Enter super secret code").ax()

  // https://help.gnome.org/users/zenity/stable/progress.html.en
  suspend fun showSomeProgress1() = zenity(Progress) { -Text("Some progress"); -Pulsate }.ax().logEach()

  // https://help.gnome.org/users/zenity/stable/progress.html.en
  suspend fun showSomeLongProgress1() {
    val inLineS = (1..10).asFlow().map { delay(1000); (it * 10).strf }
    zenity(Progress) { -Text("Some long progress") }
      .ax(inLineS = inLineS).logEach()
  }
  // TODO_someday: investigate failure when user cancels in the middle (maybe it's correct behavior but not sure).

  // https://help.gnome.org/users/zenity/stable/progress.html.en
  suspend fun showSomeLongProgress2() {
    val inLineS = (1..10).asFlow().transform {
      delay(500)
      emit((it * 10).strf)
      // zenity interprets lines with only numbers as update of progress percentage
      delay(500)
      emit("# Some lo${"o".repeat(it)}ng progress")
      // zenity interprets lines starting with "# " as text updates
    }
    zenity(Progress) { -Text("Some long progress") }
      .ax(inLineS = inLineS).logEach()
  }

  // https://help.gnome.org/users/zenity/stable/notification.html.en
  suspend fun showSimpleNotification() = zenity(Notification) { -Text("Simple notification"); -Icon("question") }.ax()

  // FIXME_later: this "--listen" magic doesn't work reliably, maybe system policy forbids too many notifications?
  // also it doesn't interpret stdin commands as I would expect by reading docs... (let's not use it for now)
  // also looks like it never stops even that flow/stdin ends...
  // https://help.gnome.org/users/zenity/stable/notification.html.en
  suspend fun showUpdatingNotification() = zenity(Notification) { -Listen }.ax(
    inLineS = flow {
      emit("message: notification 0"); delay(1000)
      emit("message: notification 1, icon: warning, tooltip: tooltip1"); delay(1000)
      emit("message: notification 2, icon: error, tooltip: tooltip2"); delay(1000)
      emit("message: notification 3, icon: info, tooltip: tooltip3, visible: true")
    }
  )

  // https://help.gnome.org/users/zenity/stable/list.html.en
  suspend fun showSomeSimpleList() = zenity(List) {
    -Title("Some simple list title")
    -OkLabel("OOOKKKK")
    -CancelLabel("Nooo")
    -Column("Answer")
    +"BLA"
    +"BLE"
  }.ax().logEach()

  suspend fun showSomeSimpleList2() = zenityAskForOneOf(
    "a", "bb", "ccc", "dddd", "eeeee", "ffffff",
    prompt = "jo, select sth",
    labelOk = "M'key...",
    labelCancel = "Noooo!",
  ).ax()

  // https://help.gnome.org/users/zenity/stable/list.html.en
  suspend fun showSomeRadioList() = zenity(List) {
    -Title("Some radio list title")
    -CancelLabel("Nooo")
    -RadioList
    -Column("") // for radio widget (TRUE/FALSE) itself
    -Column("Answer")
    -Column("Details")
    -OkLabel("OOOKKKK")
    +"FALSE"; +"BLA"; +"Some BLA details"
    +"FALSE"; +"BLE"; +"Some BLE details"
  }.ax().logEach()
}
