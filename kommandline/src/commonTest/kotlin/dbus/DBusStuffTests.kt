package pl.mareklangiewicz.kommand.dbus

import pl.mareklangiewicz.kommand.chkWithUser
import pl.mareklangiewicz.kommand.dbus.DBusRunSession.Option.Version
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Nested
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.Wayland
import pl.mareklangiewicz.kommand.gnome.gnomeshell
import kotlin.test.Test


class DBusStuffTests {
    @Test fun testDBusRunSession1() = dbusrunsession { -Version }.chkWithUser()
    @Test fun testDBusRunSession2() = dbusrunsession(gnomeshell(Nested, Wayland)).chkWithUser()
}
