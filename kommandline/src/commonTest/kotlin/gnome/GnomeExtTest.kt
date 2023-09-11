package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.chkWithUser
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Option.*
import kotlin.test.Test


class GnomeExtTest {

    @Test fun testGnomeExtList() = gnomeext(List)
        .chkWithUser("gnome-extensions list")

    @Test fun testGnomeExtListDisabled() = gnomeext(List) { -Disabled }
        .chkWithUser("gnome-extensions list --disabled")

    @Test fun testGnomeExtPrefs() = gnomeext(Cmd.Prefs("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtEnable() = gnomeext(Enable("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions enable mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtDisable() = gnomeext(Disable("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions disable mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtCreateInteractive() = gnomeext(Create) { -Interactive }
        .chkWithUser("gnome-extensions create --interactive")
}
