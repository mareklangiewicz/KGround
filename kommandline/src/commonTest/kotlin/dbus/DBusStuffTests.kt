package pl.mareklangiewicz.kommand.dbus

import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheck
import pl.mareklangiewicz.kommand.dbus.DBusRunSession.Option.Version
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Nested
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Wayland
import pl.mareklangiewicz.kommand.gnome.gnomeshell


@OptIn(DelicateApi::class) // TODO NOW: move to samples or sth
class DBusStuffTests {
  @Test fun testDBusRunSession1() = dbusrunsession { -Version }.tryInteractivelyCheck()
  @Test fun testDBusRunSession2() = dbusrunsession(gnomeshell(Nested, Wayland)).tryInteractivelyCheck()
}
