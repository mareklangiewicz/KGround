package pl.mareklangiewicz.kommand.ide

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.BadStateErr
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkNN
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.ide.Ide.*
import pl.mareklangiewicz.kommand.admin.psAllFull
import pl.mareklangiewicz.kommand.debian.whichFirstOrNull
import pl.mareklangiewicz.kommand.ide.Ide.Cmd.Open.Opt
import pl.mareklangiewicz.kommand.vim.gvimOpen
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.bad.*

fun ideOpen(
  path1: Path? = null,
  path2: Path? = null,
  path3: Path? = null,
  vararg useNamedArgs: Unit,
  line: Int? = null,
  column: Int? = null,
  ifNoIdeRunningStart: Type? = null,
) = ide(Cmd.Open(path1, path2, path3, line = line, column = column), ifNoIdeRunningStart)

@OptIn(DelicateApi::class)
fun ideOrGVimOpen(
  path1: Path? = null,
  path2: Path? = null,
  path3: Path? = null,
  vararg useNamedArgs: Unit,
  line: Int? = null,
  column: Int? = null,
) = ReducedScript {
  try {
    ideOpen(path1, path2, path3, line = line, column = column).ax() // throws BadStateErr when no Ide is running
  } catch (_: BadStateErr) {
    gvimOpen(path1, path2, path3, line = line, column = column).ax()
  }
}

/** https://www.jetbrains.com/help/idea/command-line-differences-viewer.html */
fun ideDiff(path1: Path, path2: Path, path3: Path? = null, ifNoIdeRunningStart: Type? = null) =
  ide(Cmd.Diff(path1, path2, path3), ifNoIdeRunningStart)

/** https://www.jetbrains.com/help/idea/command-line-merge-tool.html */
fun ideMerge(path1: Path, path2: Path, pathOut: Path, pathBase: Path? = null, ifNoIdeRunningStart: Type? = null) =
  ide(Cmd.Merge(path1, path2, pathOut, pathBase), ifNoIdeRunningStart)

fun ideHelp(type: Type) = ideOpen(type) { -Opt.Help }

fun ideVersion(type: Type) = ideOpen(type) { -Opt.Version }

/**
 * https://www.jetbrains.com/help/idea/command-line-merge-tool.html
 * BTW If Ide of given type is not running, it will start it.
 */
fun ideMerge(type: Type, path1: Path, path2: Path, pathOut: Path, pathBase: Path? = null) =
  ide(type,Cmd.Merge(path1, path2, pathOut, pathBase))

/**
 * https://www.jetbrains.com/help/idea/command-line-differences-viewer.html
 * BTW If Ide of given type is not running, it will start it.
 */
fun ideDiff(type: Type, path1: Path, path2: Path, path3: Path? = null) = ide(type, Cmd.Diff(path1, path2, path3))

/** BTW If Ide of given type is not running, it will start it (unless -Opt.Help or -Opt.Version (and no paths)). */
fun ideOpen(
  type: Type,
  path1: Path? = null,
  path2: Path? = null,
  path3: Path? = null,
  init: Cmd.Open.() -> Unit = {}
): Ide = ide(type, Cmd.Open(path1, path2, path3).apply(init))

fun <CmdT : Cmd> ide(cmd: CmdT, ifNoIdeRunningStart: Type? = null, init: CmdT.() -> Unit = {}) =
  ReducedScript {
    val type = ideFindFirstRunning() ?: ifNoIdeRunningStart ?: bad { "No known IDE is running." }
    ide(type, cmd, init).ax()
  }


fun <CmdT : Cmd> ide(type: Type, cmd: CmdT, init: CmdT.() -> Unit = {}) = Ide(type, cmd.apply(init))

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
data class Ide(var type: Type, var cmd: Cmd) : Kommand {
  override val name get() = type.cmdName
  override val args get() = cmd.toArgs()

  /**
   * The "ideap" and "ideaslim" are my conventions for shell scripts launching: Idea EAP version,
   * and separate Idea Slim installation (without most plugins installed, configured to be fast)
   * https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html#d4a34497_155
   */
  enum class Type { Idea, IdeaP, IdeaSlim, Studio; val cmdName get() = namelowords("") }

  sealed class Cmd(val name: String?) : ToArgs {

    data class Open(
      val opts: MutableList<Opt> = MutLO(),
      val paths: MutableList<Path> = MutLO(),
    ) : Cmd(null) {
      constructor(
        path1: Path? = null,
        path2: Path? = null,
        path3: Path? = null,
        vararg useNamedArgs: Unit,
        line: Int? = null,
        column: Int? = null,
      ) : this(
        LONN(line?.let(Opt::Line), column?.let(Opt::Column)).toMutL,
        LONN(path1, path2, path3).toMutL,
      )

      override fun toArgs() = opts.flatMap { it.toArgs() } + paths.map { it.strf }

      sealed class Opt(val name: String, val arg: String? = null) : KOpt {
        override fun toArgs() = listOf(name) plusIfNN arg

        data object NoSplash : Opt("nosplash")
        data object NoProjects : Opt("dontReopenProjects")
        data object NoPlugins : Opt("disableNonBundledPlugins")
        data object Wait : Opt("--wait")
        data object Help : Opt("--help") // not really "Open" cmd (paths should be empty)
        data object Version : Opt("--version") // not really "Open" cmd (paths should be empty)
        data class Line(val l: Int) : Opt("--line", l.strf)
        data class Column(val c: Int) : Opt("--column", c.strf)
      }

      operator fun Opt.unaryMinus() = opts.add(this)
      operator fun Path.unaryPlus() = paths.add(this)
    }

    data class Diff(var path1: Path, var path2: Path, var path3: Path? = null) : Cmd("diff") {
      override fun toArgs() = LONN(name, path1.strf, path2.strf, path3?.strf)
    }

    data class Merge(var path1: Path, var path2: Path, var pathOut: Path, var pathBase: Path? = null) : Cmd("merge") {
      override fun toArgs() = LONN(name, path1.strf, path2.strf, pathBase?.strf, pathOut.strf)
    }

    /**
     * Formatter launches an instance of IntelliJ IDEA in the background and applies the formatting.
     * It will not work if another instance of IntelliJ IDEA is already running.
     * See: https://www.jetbrains.com/help/idea/command-line-formatter.html
     */
    data class Format(
      val opts: MutableList<Opt> = MutLO(),
      val paths: MutableList<String> = MutLO(),
    ) : Cmd("format") {
      override fun toArgs() = listOf(name!!) + opts.flatMap { it.toArgs() } + paths

      @OptIn(DelicateApi::class)
      sealed class Opt(name: String, args: List<String> = LO()) : KOptS(name, args, argsSeparator = ",") {
        data object Help : Opt("h")
        data object Dry : Opt("d")
        data object Recursive : Opt("R") // Note: long flavor: --recursive is NOT supported.
        data object AllowDefaults : Opt("allowDefaults")
        class Mask(vararg patterns: String) : Opt("m", patterns.toL)
        class Settings(path: String) : Opt("s", listOf(path))
        class Charset(charset: String) : Opt("charset", listOf(charset))
      }

      operator fun Opt.unaryMinus() = opts.add(this)
      operator fun String.unaryPlus() = paths.add(this)
    }

    /**
     * Inspector launches an instance of IntelliJ IDEA in the background where it runs the inspections.
     * It will not work if another instance of IntelliJ IDEA is already running.
     * See: https://www.jetbrains.com/help/idea/command-line-code-inspector.html
     */
    data class Inspect(
      var project: String,
      var profile: String,
      var output: String,
      val opts: MutableList<Opt> = MutLO(),
    ) : Cmd("inspect") {
      override fun toArgs() = listOf(name!!, project, profile, output) + opts.flatMap { it.toArgs() }

      @OptIn(DelicateApi::class)
      sealed class Opt(name: String, arg: String? = null) : KOptS(name, arg) {
        data object Changes : Opt("changes")
        class Dir(dir: String) : Opt("d", dir)
        /** "xml" (default) or "json" or "plain" */
        class Format(format: String) : Opt("d", format)
        /** Low: 0 (default) or Medium: 1 or Maximum: 2 */
        class Verbosity(v: Int) : Opt("v$v")
      }
    }

    /** https://www.jetbrains.com/help/idea/install-plugins-from-the-command-line.html */
    data class Install(
      val plugins: MutableList<String> = MutLO(),
      val repos: MutableList<String> = MutLO(),
    ) : Cmd("installPlugins") {
      override fun toArgs() = listOf(name!!) + plugins + repos
      operator fun String.unaryPlus() = plugins.add(this)
    }
  }
}


@OptIn(NotPortableApi::class, DelicateApi::class)
suspend fun ideFindFirstRunning(): Type? {

  val ureToolboxApp = ure {
    +ureText("Toolbox/apps/")
    +ureIdent(allowDashesInside = true).withName("app")
  }

  suspend fun getRunningIdesRealNames(): Set<String> = psAllFull().ax()
    .filter<String> { "Toolbox/apps" in it }
    .map<String, String> { ureToolboxApp.findFirst(it).namedValues["app"]!! }
    .toSet()

  suspend fun Type.getRealName(): String {
    val path = whichFirstOrNull(cmdName).ax().chkNN { "Command $cmdName not found." }
    kommand("file", path.strf).ax().single().chkFindSingle(ureText("shell script"))
    for (line in readFileHead(path).ax())
      return ureToolboxApp.findFirstOrNull(line)?.namedValues["app"] ?: continue
    bad { "Real name of $this not found in script $path" }
  }

  val running = getRunningIdesRealNames()

  for (i in Type.entries)
    if (i.getRealName() in running) return i
  return null
}
