package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.action
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.help
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.launch
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.listactions
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.listapps
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.checkWithUser
import pl.mareklangiewicz.kommand.unwrap
import kotlin.test.Ignore
import kotlin.test.Test


class GnomeAppTest {

    @Test fun testGnomeAppListApps() = gnomeapp(listapps)
        .checkWithUser("gapplication list-apps")

    @Test fun testGnomeAppListGEditActions() = gnomeapp(listactions("org.gnome.gedit"))
        .checkWithUser("gapplication list-actions org.gnome.gedit")

    @Ignore // jitpack
    @Test fun testGnomeAppListAllAppActions() = SYS.run {
        gnomeapp(listapps)().forEach {
            println("Application $it:")
            gnomeapp(listactions(it))().forEach {
                println("   action: $it")
            }
        }
    }

    @Test fun testGnomeAppHelp() = gnomeapp(help()).checkWithUser()
    @Test fun testGnomeAppLaunchGEdit() = gnomeapp(launch("org.gnome.gedit")).checkWithUser()
    @Test fun testGnomeAppGEditNewWindow() = gnomeapp(action("org.gnome.gedit", "new-window")).checkWithUser()
    @Test fun testGnomeAppGEditNewDocument() = gnomeapp(action("org.gnome.gedit", "new-document")).checkWithUser()
}
