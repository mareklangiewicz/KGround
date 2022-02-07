import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.core.Ls.Option.all
import pl.mareklangiewicz.kommand.core.Ls.Option.long
import pl.mareklangiewicz.kommand.gnome.gnomeext
import pl.mareklangiewicz.kommand.gnome.gnometerm

println("start")

// FIXME: this worksheet doesn't work for me. For now I use unit tests instead but I should fix it.
// Exception in thread "main" java.lang.NoSuchMethodError: 'pl.mareklangiewicz.kommand.SysPlatform pl.mareklangiewicz.kommand.Platform$Companion.getSYS()'

Platform.SYS.start(gnometerm(bash(gnomeext(list), pause = true)))
Platform.SYS.start(gnometerm(bash(ls { -long; -all }, pause = true)))

println("end")
