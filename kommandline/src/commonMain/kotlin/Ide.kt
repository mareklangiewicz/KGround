package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.Ide.Cmd
import pl.mareklangiewicz.kommand.Ide.Type

/** https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html */
fun idea(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.idea, cmd).apply(init)
fun ideap(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.ideap, cmd).apply(init)
fun ideaslim(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.ideaslim, cmd).apply(init)
fun studio(cmd: Cmd? = null, init: Ide.() -> Unit = {}) = Ide(Type.studio, cmd).apply(init)

fun idea(file: String, init: Ide.() -> Unit = {}) = idea { +file; init() }
fun ideap(file: String, init: Ide.() -> Unit = {}) = ideap { +file; init() }
fun ideaslim(file: String, init: Ide.() -> Unit = {}) = ideaslim { +file; init() }
fun studio(file: String, init: Ide.() -> Unit = {}) = studio { +file; init() }

data class Ide(
    var type: Type,
    var cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val stuff: MutableList<String> = mutableListOf(),
): Kommand {
    override val name get() = type.name
    override val args get() = cmd?.str.orEmpty() + options.flatMap { it.str } + stuff

    enum class Type { idea, ideap, ideaslim, studio }

    sealed class Cmd(val name: String, val arg: String? = null) {

        open val str get() = listOf(name) plusIfNN arg

        data object Diff : Cmd("diff")
        data object Merge : Cmd("merge")
        data object Format : Cmd("format")
        data object Inspect : Cmd("inspect")
    }

    sealed class Option(val name: String, val arg: String? = null) {
        open val str get() = listOf(name) plusIfNN arg
        data object NoSplash : Option("nosplash")
        data object DontReopenProjects : Option("nosplash")
        data object DisableNonBundledPlugins : Option("disableNonBundledPlugins")
        data object Wait : Option("--wait")
        data class Line(val l: Int) : Option("--line", l.toString())
        data class Column(val c: Int) : Option("--column", c.toString())

        // options only for Cmd.format:
        data object FormatHelp : Option("-h")
        data object FormatMask : Option("-m")
        data object FormatRecursive : Option("-r")
        data object FormatSettings : Option("-s")

        // options only for Cmd.inspect:
        data object InspectChanges : Option("-changes")
        data object InspectSubDir : Option("-d")
        data object InspectFormat : Option("-format")
        data class InspectVerbosity(val v: Int) : Option("-v", v.toString())

    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}