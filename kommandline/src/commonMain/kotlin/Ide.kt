package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Ide.Cmd
import pl.mareklangiewicz.kommand.Ide.Type

/** https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html */
fun idea(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.idea, cmd).apply(init)
fun ideap(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.ideap, cmd).apply(init)
fun studio(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.studio, cmd).apply(init)

fun idea(file: String, init: Ide.() -> Unit = {}) = idea { +file; init() }
fun ideap(file: String, init: Ide.() -> Unit = {}) = ideap { +file; init() }
fun studio(file: String, init: Ide.() -> Unit = {}) = studio { +file; init() }

data class Ide(
    var type: Type,
    var cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val stuff: MutableList<String> = mutableListOf(),
): Kommand {
    override val name get() = type.name
    override val args get() = cmd?.str.orEmpty() + options.flatMap { it.str } + stuff

    enum class Type { idea, ideap, studio }

    sealed class Cmd(val name: String, val arg: String? = null) {

        open val str get() = listOf(name) plusIfNN arg

        data object diff : Cmd("diff")
        data object merge : Cmd("merge")
        data object format : Cmd("format")
        data object inspect : Cmd("inspect")
    }

    sealed class Option(val name: String, val arg: String? = null) {
        open val str get() = listOf(name) plusIfNN arg
        data object nosplash : Option("nosplash")
        data object dontReopenProjects : Option("nosplash")
        data object disableNonBundledPlugins : Option("disableNonBundledPlugins")
        data object wait : Option("--wait")
        data class ln(val l: Int) : Option("--line", l.toString())
        data class col(val c: Int) : Option("--column", c.toString())

        // options only for Cmd.format:
        data object formathelp : Option("-h")
        data object formatmask : Option("-m")
        data object formatrecursive : Option("-r")
        data object formatsettings : Option("-s")

        // options only for Cmd.inspect:
        data object inspectchanges : Option("-changes")
        data object inspectsubdir : Option("-d")
        data object inspectformat : Option("-format")
        data class inspectverbosity(val v: Int) : Option("-v", v.toString())

    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}