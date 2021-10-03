package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.GnomeExt.Cmd.disable
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.enable
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.prefs
import pl.mareklangiewicz.kommand.GnomeExt.Option.disabled
import kotlin.test.Test


class GnomeExtTest {

    @Test fun testGnomeExtList() = gnomeext(list)
        .checkWithUser("gnome-extensions list")

    @Test fun testGnomeExtListDisabled() = gnomeext(list) { -disabled }
        .checkWithUser("gnome-extensions list --disabled")

    @Test fun testGnomeExtPrefs() = gnomeext(prefs("mygnomeext@mareklangiewicz.pl"))
        .checkWithUser("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtEnable() = gnomeext(enable("mygnomeext@mareklangiewicz.pl"))
        .checkWithUser("gnome-extensions enable mygnomeext@mareklangiewicz.pl")

    @Test fun testGnomeExtDisable() = gnomeext(disable("mygnomeext@mareklangiewicz.pl"))
        .checkWithUser("gnome-extensions disable mygnomeext@mareklangiewicz.pl")
}
