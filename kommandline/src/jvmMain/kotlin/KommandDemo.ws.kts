import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.Ls.Option.all
import pl.mareklangiewicz.kommand.Ls.Option.long
import pl.mareklangiewicz.kommand.gnome.gnomeext
import pl.mareklangiewicz.kommand.gnome.gnometerm

println("blaaa")

Platform.SYS.start(gnometerm(bash(gnomeext(list), pause = true)))
Platform.SYS.start(gnometerm(bash(ls { -long; -all }, pause = true)))