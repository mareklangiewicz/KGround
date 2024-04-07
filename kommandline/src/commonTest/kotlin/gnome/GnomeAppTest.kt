package pl.mareklangiewicz.kommand.gnome

import kotlin.test.Ignore
import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheck
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.*
import pl.mareklangiewicz.ulog.i


@OptIn(DelicateApi::class) // TODO NOW: move to samples or sth
class GnomeAppTest {

  @Test fun testGnomeAppListApps() = gnomeapp(ListApps)
    .tryInteractivelyCheck("gapplication list-apps")

  @Test fun testGnomeAppListGEditActions() = gnomeapp(ListActions("org.gnome.gedit"))
    .tryInteractivelyCheck("gapplication list-actions org.gnome.gedit")

  @Ignore // jitpack
  @Test fun testGnomeAppListAllAppActions() = SYS.run {
    gnomeapp(ListApps).axb(this).forEach {
      ulog.i("Application $it:")
      gnomeapp(ListActions(it)).axb(this).forEach {
        ulog.i("   action: $it")
      }
    }
  }

  @Test fun testGnomeAppHelp() = gnomeapp(Help()).tryInteractivelyCheck()
  @Test fun testGnomeAppLaunchGEdit() = gnomeapp(Launch("org.gnome.gedit")).tryInteractivelyCheck()
  @Test fun testGnomeAppGEditNewWindow() = gnomeapp(Action("org.gnome.gedit", "new-window")).tryInteractivelyCheck()
  @Test fun testGnomeAppGEditNewDocument() =
    gnomeapp(Action("org.gnome.gedit", "new-document")).tryInteractivelyCheck()
}
