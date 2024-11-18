package pl.mareklangiewicz.kommand.dbus

import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kommand.dbus.DBusRunSession.Option.Version
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Nested
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Wayland
import pl.mareklangiewicz.kommand.gnome.gnomeshell


@OptIn(DelicateApi::class, NotPortableApi::class) // TODO NOW: move to samples or sth
class DBusStuffTests {
  @Test fun testDBusRunSession1() = dbusrunsession { -Version }.tryInteractivelyCheckBlockingOrErr()
  @Test fun testDBusRunSession2() = dbusrunsession(gnomeshell(Nested, Wayland)).tryInteractivelyCheckBlockingOrErr()
}
