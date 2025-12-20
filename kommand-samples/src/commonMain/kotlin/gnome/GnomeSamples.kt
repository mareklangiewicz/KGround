package pl.mareklangiewicz.kommand.gnome

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.systemd.*
import pl.mareklangiewicz.kommand.term.*
import pl.mareklangiewicz.kommand.vim.*
import pl.mareklangiewicz.ulog.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateApi::class)
data object GnomeSamples {

  val help = gnomeapp(Help()) s "gapplication help"

  val launchTextEditor = gnomeapp(Launch("org.gnome.TextEditor")) s "gapplication launch org.gnome.TextEditor"

  val listApps = gnomeapp(ListApps) s "gapplication list-apps"

  val listCalendarActions = gnomeapp(ListActions("org.gnome.Calendar")) s "gapplication list-actions org.gnome.Calendar"

  val listAllAppsActions = ReducedScript {
    val log = localULog()
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


  val journalCtlFollowGnomeShell = journalctl {
    -JournalCtl.Option.Follow
    -JournalCtl.Option.Cat
    +"/usr/bin/gnome-shell"
  } s "journalctl -f -ocat /usr/bin/gnome-shell"

  val termGnomeVim = termGnome(vim()) {
    -TermGnomeOpt.Verbose
    -TermGnomeOpt.Title("strange terminal title")
  } s "gnome-terminal --verbose --title=strange terminal title -- vim"

  // FIXME_later
  // val testGLibCompileSchemas = kommand("glib-compile-schemas", "schemas/") s "glib-compile-schemas schemas/", "/home/marek/code/kotlin/kokpit667/mygnomeext".P

  val notifySomeCriticalStuff = notify("aa", "some longer body") {
    -NotifySend.Option.Urgency("critical")
  } s "notify-send --urgency=critical aa some longer body"

}
