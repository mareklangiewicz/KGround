package pl.mareklangiewicz.kommand.gnome

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import pl.mareklangiewicz.kground.onEachLog
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.*
import pl.mareklangiewicz.kommand.reducedOutToFlow
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.implictx

@OptIn(ExperimentalCoroutinesApi::class)
data object GnomeSamples {

  val help = gnomeapp(Help()) s "gapplication help"

  val launchTextEditor = gnomeapp(Launch("org.gnome.TextEditor")) s "gapplication launch org.gnome.TextEditor"

  val listApps = gnomeapp(ListApps) s "gapplication list-apps"

  val listCalendarActions = gnomeapp(ListActions("org.gnome.Calendar")) s "gapplication list-actions org.gnome.Calendar"

  val listAllAppsActions = ReducedScript {
    val log = implictx<ULog>()
    gnomeapp(ListApps)
      .reducedOutToFlow().ax()
      .onEachLog(log, timed = false)
      .flatMapConcat { app -> gnomeapp(ListActions(app)).reducedOutToFlow().ax().map { "$app: $it" } }
      .collect { log.i(it) }
  }

  val consoleNewTab = gnomeapp(Action("org.gnome.Console", "new-tab")) s
    "gapplication action org.gnome.Console new-tab"

  val consoleNew5Tabs = ReducedScript {
    repeat(5) {
      delay(1000)
      consoleNewTab.ax()
    }
  }
}
