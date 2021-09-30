import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.Ls.Option.all
import pl.mareklangiewicz.kommand.Ls.Option.long

println("blaaa")

gnometerm(bash(gnomeext_list(), pause = true)).exec()
gnometerm(bash(ls { -long; -all }, pause = true)).exec()