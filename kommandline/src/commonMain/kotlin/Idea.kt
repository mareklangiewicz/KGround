package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Idea.Cmd

/** https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html */
fun idea(cmd: Cmd? = null, init: Idea.() -> Unit = {}) = Idea(cmd, eap = false).apply(init)
fun ideap(cmd: Cmd? = null, init: Idea.() -> Unit = {}) = Idea(cmd, eap = true).apply(init)

data class Idea(
    var cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val stuff: MutableList<String> = mutableListOf(),
    var eap: Boolean = false
): Kommand {
    override val name get() = if (eap) "ideap" else "idea"
    override val args get() = cmd?.str.orEmpty() + options.flatMap { it.str } + stuff

    sealed class Cmd(val name: String, val arg: String? = null) {

        open val str get() = listOf(name) plusIfNotNull arg

        object diff : Cmd("diff")
        object merge : Cmd("merge")
        object format : Cmd("format")
        object inspect : Cmd("inspect")
    }

    sealed class Option(val name: String, val arg: String? = null) {
        open val str get() = listOf(name) plusIfNotNull arg
        object nosplash : Option("nosplash")
        object dontReopenProjects : Option("nosplash")
        object disableNonBundledPlugins : Option("disableNonBundledPlugins")
        object wait : Option("--wait")
            // FIXME: in other place they say it's: "-w" https://www.jetbrains.com/help/idea/2021.3/lightedit-mode.html#lightedit_open_file_modes

        data class ln(val l: Int) : Option("--line", l.toString())
        data class col(val c: Int) : Option("--column", c.toString()) // FIXME: is it still available in eap?? (official idea help webpage versions are confusing)

        // options only for Cmd.format:
        object formathelp : Option("-h")
        object formatmask : Option("-m")
        object formatrecursive : Option("-r")
        object formatsettings : Option("-s")

        // options only for Cmd.inspect:
        object inspectchanges : Option("-changes")
        object inspectsubdir : Option("-d")
        object inspectformat : Option("-format")
        data class inspectverbosity(val v: Int) : Option("-v", v.toString())

    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}