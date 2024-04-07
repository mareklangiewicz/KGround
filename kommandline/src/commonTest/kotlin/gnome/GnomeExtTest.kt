package pl.mareklangiewicz.kommand.gnome

import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheck
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Option.*


@OptIn(DelicateApi::class) // TODO NOW: move to samples or sth
class GnomeExtTest {

  @Test fun testGnomeExtList() = gnomeext(List)
    .tryInteractivelyCheck("gnome-extensions list")

  @Test fun testGnomeExtListDisabled() = gnomeext(List) { -Disabled }
    .tryInteractivelyCheck("gnome-extensions list --disabled")

  @Test fun testGnomeExtPrefs() = gnomeext(Cmd.Prefs("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheck("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtEnable() = gnomeext(Enable("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheck("gnome-extensions enable mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtDisable() = gnomeext(Disable("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheck("gnome-extensions disable mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtCreateInteractive() = gnomeext(Create) { -Interactive }
    .tryInteractivelyCheck("gnome-extensions create --interactive")
}
