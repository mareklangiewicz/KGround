import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.gnome.GnomeExt.Cmd.list
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.all
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.long
import pl.mareklangiewicz.kommand.gnome.gnomeext
import pl.mareklangiewicz.kommand.gnome.gnometerm

println("start")

// FIXME: this worksheet doesn't work for me. For now I use unit tests instead but I should fix it.
// Exception in thread "main" java.lang.NoSuchMethodError: 'pl.mareklangiewicz.kommand.SysPlatform pl.mareklangiewicz.kommand.Platform$Companion.getSYS()'
// UPDATE: also I get warning during normal project build, so let's move it outside source:
//   w: Script 'KommandDemo.ws.kts' is not supposed to be used along with regular Kotlin sources, and will be ignored in the future versions by default. (Use -Xallow-any-scripts-in-source-roots command line option to opt-in for the old behavior.)
// UPDATE: check out Kotlin Notebooks! as new exciting way of doing such stuff.
// https://plugins.jetbrains.com/plugin/16340-kotlin-notebook/versions

Platform.SYS.start(gnometerm(bash(gnomeext(list), pause = true)))
Platform.SYS.start(gnometerm(bash(ls { -long; -all }, pause = true)))

println("end")
