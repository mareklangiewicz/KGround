package pl.mareklangiewicz.kommand.dbus

import pl.mareklangiewicz.kommand.gnome.GnomeShell
import pl.mareklangiewicz.kommand.gnome.gnomeshell
import pl.mareklangiewicz.kommand.samples.s

data object DBusSamples {

  val dbusRunSession1 = dbusrunsession { -DBusRunSession.Option.Version } s
    "dbus-run-session --version"

  val dbusRunSession2 = dbusrunsession(gnomeshell(GnomeShell.Option.Nested, GnomeShell.Option.Wayland)) s
    "dbus-run-session -- gnome-shell --nested --wayland"
}
