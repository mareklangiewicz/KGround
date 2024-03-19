package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import kotlin.test.Ignore
import kotlin.test.Test


@OptIn(DelicateApi::class) // TODO NOW: move to samples or sth
class GnomeAppTest {

    @Test fun testGnomeAppListApps() = gnomeapp(ListApps)
        .chkWithUser("gapplication list-apps")

    @Test fun testGnomeAppListGEditActions() = gnomeapp(ListActions("org.gnome.gedit"))
        .chkWithUser("gapplication list-actions org.gnome.gedit")

    @Ignore // jitpack
    @Test fun testGnomeAppListAllAppActions() = SYS.run {
        gnomeapp(ListApps).execb(this).forEach {
            println("Application $it:")
            gnomeapp(ListActions(it)).execb(this).forEach {
                println("   action: $it")
            }
        }
    }

    @Test fun testGnomeAppHelp() = gnomeapp(Help()).chkWithUser()
    @Test fun testGnomeAppLaunchGEdit() = gnomeapp(Launch("org.gnome.gedit")).chkWithUser()
    @Test fun testGnomeAppGEditNewWindow() = gnomeapp(Action("org.gnome.gedit", "new-window")).chkWithUser()
    @Test fun testGnomeAppGEditNewDocument() = gnomeapp(Action("org.gnome.gedit", "new-document")).chkWithUser()
}
