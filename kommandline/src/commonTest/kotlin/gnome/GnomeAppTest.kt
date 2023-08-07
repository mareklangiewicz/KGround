package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.action
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.help
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.launch
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.listactions
import pl.mareklangiewicz.kommand.gnome.GnomeApp.Cmd.listapps
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import kotlin.test.Ignore
import kotlin.test.Test


class GnomeAppTest {

    @Test fun testGnomeAppListApps() = gnomeapp(listapps)
        .chkWithUser("gapplication list-apps")

    @Test fun testGnomeAppListGEditActions() = gnomeapp(listactions("org.gnome.gedit"))
        .chkWithUser("gapplication list-actions org.gnome.gedit")

    @Ignore // jitpack
    @Test fun testGnomeAppListAllAppActions() = SYS.run {
        gnomeapp(listapps).execb(this).forEach {
            println("Application $it:")
            gnomeapp(listactions(it)).execb(this).forEach {
                println("   action: $it")
            }
        }
    }

    @Test fun testGnomeAppHelp() = gnomeapp(help()).chkWithUser()
    @Test fun testGnomeAppLaunchGEdit() = gnomeapp(launch("org.gnome.gedit")).chkWithUser()
    @Test fun testGnomeAppGEditNewWindow() = gnomeapp(action("org.gnome.gedit", "new-window")).chkWithUser()
    @Test fun testGnomeAppGEditNewDocument() = gnomeapp(action("org.gnome.gedit", "new-document")).chkWithUser()
}
