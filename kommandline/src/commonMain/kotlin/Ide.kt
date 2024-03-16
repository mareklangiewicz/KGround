package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.BadStateErr
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkNN
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.Ide.Cmd
import pl.mareklangiewicz.kommand.Ide.Type
import pl.mareklangiewicz.kommand.admin.psAllFull
import pl.mareklangiewicz.kommand.debian.whichFirstOrNull
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.bad.*


fun ideOpen(path: String, ifNoIdeRunningStart: Type? = null) = ide(Cmd.Open, ifNoIdeRunningStart) { +path }

fun ideOrGVimOpen(path: String) = ReducedScript<Unit> { platform, dir ->
    try { ideOpen(path).exec(platform, dir = dir) }
    catch (e: BadStateErr) { gvim(path).exec(platform, dir = dir) }
}

/** https://www.jetbrains.com/help/idea/command-line-differences-viewer.html */
fun ideDiff(path1: String, path2: String, path3: String? = null, ifNoIdeRunningStart: Type? = null) =
    ide(Cmd.Diff, ifNoIdeRunningStart) { +path1; +path2; path3?.let { +it } }

/** https://www.jetbrains.com/help/idea/command-line-merge-tool.html */
fun ideMerge(path1: String, path2: String, output: String, base: String? = null, ifNoIdeRunningStart: Type? = null) =
    ide(Cmd.Merge, ifNoIdeRunningStart) { +path1; +path2; base?.let { +it }; +output }

// TODO: more wrappers for most common simple usages?

fun ide(cmd: Cmd, ifNoIdeRunningStart: Type? = null, init: Ide.() -> Unit = {}) =
    ReducedScript<Unit> { platform, dir ->
        val type = getFirstRunningIdeType(platform) ?: ifNoIdeRunningStart ?: bad { "No known IDE is running." }
        ide(type, cmd, init).exec(platform, dir = dir)
    }



fun ide(type: Type, cmd: Cmd, init: Ide.() -> Unit = {}) = Ide(type, cmd).apply(init)

/**
 * https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html
 *
 * WARNING: It can launch a whole IDE and even open a whole project automatically.
 * So it can be really slow and memory hungry.
 *
 * From official docs:
 * When you specify the path to a file, IntelliJ IDEA opens it in the LightEdit mode,
 * unless it belongs to a project that is already open
 * or there is special logic to automatically open or create a project
 * (for example, in case of Maven or Gradle files).
 * If you specify a directory with an existing project,
 * IntelliJ IDEA opens this project.
 * If you open a directory that is not a part of a project,
 * IntelliJ IDEA adds the .idea directory to it, making it a project.
 */
data class Ide(
    var type: Type,
    var cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val stuff: MutableList<String> = mutableListOf(),
): Kommand {
    override val name get() = type.name
    override val args get() = cmd?.str.orEmpty() + options.flatMap { it.str } + stuff

    /**
     * The "ideap" and "ideaslim" are my conventions for shell scripts launching: Idea EAP version,
     * and separate Idea Slim installation (without most plugins installed, configured to be fast)
     * https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html#d4a34497_155
     */
    enum class Type { idea, ideap, ideaslim, studio }

    // TODO NOW: rewrite Cmd to each contain its local options in right places
    sealed class Cmd(val name: String?, val arg: String? = null) {

        open val str get() = listOfNotNull(name, arg)

        data object Open : Cmd(null)
        data object Diff : Cmd("diff")
        data object Merge : Cmd("merge")
        data object Format : Cmd("format")
        data object Inspect : Cmd("inspect")
        data object Install : Cmd("installPlugins")
    }

    sealed class Option(val name: String, val arg: String? = null) {
        open val str get() = listOf(name) plusIfNN arg
        data object NoSplash : Option("nosplash")
        data object DontReopenProjects : Option("dontReopenProjects")
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


@OptIn(NotPortableApi::class, DelicateApi::class)
private suspend fun getFirstRunningIdeType(platform: CliPlatform): Type? {

    val ureToolboxApp = ure {
        +ureText("Toolbox/apps/")
        +ureIdent(allowDashesInside = true).withName("app")
    }

    suspend fun getRunningIdesRealNames(): Set<String> = psAllFull().exec(platform)
        .filter<String> { "Toolbox/apps" in it }
        .map<String, String> { ureToolboxApp.findFirst(it).namedValues["app"]!! }
        .toSet()

    suspend fun Type.getRealName(): String {
        val path = whichFirstOrNull(name).exec(platform).chkNN { "Command $name not found." }
        kommand("file", path).exec(platform).single().chkFindSingle(ureText("shell script"))
        for (line in readFileHead(path).exec(platform))
            return ureToolboxApp.findFirstOrNull(line)?.namedValues["app"] ?: continue
        bad { "Real name of $this not found in script $path" }
    }

    val running = getRunningIdesRealNames()

    for (i in Type.entries)
        if (i.getRealName() in running) return i
    return null
}


