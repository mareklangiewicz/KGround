package pl.mareklangiewicz.kommand.gnome

import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Option.*


@OptIn(DelicateApi::class, NotPortableApi::class) // TODO NOW: move to samples or sth
class GnomeExtTest {

  @Test fun testGnomeExtList() = gnomeext(List)
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions list")

  @Test fun testGnomeExtListDisabled() = gnomeext(List) { -Disabled }
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions list --disabled")

  @Test fun testGnomeExtPrefs() = gnomeext(Cmd.Prefs("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions prefs mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtEnable() = gnomeext(Enable("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions enable mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtDisable() = gnomeext(Disable("mygnomeext@mareklangiewicz.pl"))
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions disable mygnomeext@mareklangiewicz.pl")

  @Test fun testGnomeExtCreateInteractive() = gnomeext(Create) { -Interactive }
    .tryInteractivelyCheckBlockingOrErr("gnome-extensions create --interactive")
}
