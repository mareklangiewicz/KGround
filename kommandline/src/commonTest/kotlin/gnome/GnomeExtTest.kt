package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.chkWithUser
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.create
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.disable
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.enable
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.prefs
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Option.disabled
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Option.interactive
import kotlin.test.Test


class GnomeExtTest {

    @Test fun testGnomeExtList() = gnomeext(list)
        .chkWithUser("gnome-extensions list")

    @Test fun testGnomeExtListDisabled() = gnomeext(list) { -disabled }
        .chkWithUser("gnome-extensions list --disabled")

    @Test fun testGnomeExtPrefs() = gnomeext(prefs("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtEnable() = gnomeext(enable("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions enable mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtDisable() = gnomeext(disable("mygnomeext@mareklangiewicz.pl"))
        .chkWithUser("gnome-extensions disable mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtCreateInteractive() = gnomeext(create) { -interactive }
        .chkWithUser("gnome-extensions create --interactive")
}
