import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.Ls.Option.all
import pl.mareklangiewicz.kommand.Ls.Option.long

println("blaaa")

Platform.SYS.exec(gnometerm(bash(gnomeext(list), pause = true)))
Platform.SYS.exec(gnometerm(bash(ls { -long; -all }, pause = true)))