@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Bash.Option.command

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)
fun vim(vararg files: String, init: Vim.() -> Unit = {}) = Vim(files.toMutableList()).apply(init)
fun adb(command: Adb.Command, init: Adb.() -> Unit = {}) = Adb(command).apply(init)
fun audacious(vararg files: String, init: Audacious.() -> Unit = {}) = Audacious(files.toMutableList()).apply(init)
fun bash(script: String, pause: Boolean = false, init: Bash.() -> Unit = {}) =
    Bash(mutableListOf(if (pause) "$script ; echo END.ENTER; read" else script)).apply { -command; init() }
fun bash(kommand: Kommand, pause: Boolean = false, init: Bash.() -> Unit = {}) = bash(kommand.line(), pause, init)
    // FIXME_someday: I assumed kommand.line() is correct script and will not interfere with surrounding stuff

/** anonymous kommand to use only if no actual Kommand class defined */
fun kommand(name: String, vararg args: String) = object : Kommand {
    override val name get() = name
    override val args get() = args.toList()
}

// TODO_later: full documentation in kdoc (all commands, options, etc)
//  (check in practice to make sure it's optimal for IDE users)

interface Kommand {
    val name: String
    val args: List<String>
}

fun Kommand.line() = (listOf(name) + args.map { it.quoteBashMetaChars() }).joinToString(" ")
fun Kommand.println() = println(line())
fun String.quoteBashMetaChars() = replace(Regex("([|&;<>() \\\\\"\\t\\n])"), "\\\\$1")

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element

fun List<String>.printlns() = forEach(::println)

// TODO_someday: access to input/output streams wrapped in okio Source/Sink
expect class ExecProcess(kommand: Kommand, dir: String? = null) {
    fun waitFor(): ExecResult
}

data class ExecResult(val exitValue: Int, val stdOutAndErr: List<String>)

/**
 * Returns the output but ensures the exit value was 0 first
 * @throws IllegalStateException if exit value is not 0
 */
val ExecResult.out: List<String> get() =
    if (exitValue != 0) throw IllegalStateException("Exit value: $exitValue") else stdOutAndErr

fun Kommand.execStart(dir: String? = null) = ExecProcess(this, dir)

fun Kommand.execBlock(dir: String? = null): ExecResult = execStart(dir).waitFor()


/**
 * Execute given command (with optional args) in separate subprocess. Does not wait for it to end.
 * (the command should not expect any input or give any output or error)
 */
fun Kommand.exec(dir: String? = null) = execStart(dir).unit


/**
 * Runs given command in bash shell;
 * captures all its output (with error output merged in);
 * waits for the subprocess to finish;
 */
fun Kommand.shell(dir: String? = null) = bash(this).execBlock(dir)

/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "ls"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object all : Option("-a")
        object almostAll : Option("-A")
        object author : Option("--author")
        object directory : Option("-d")
        object humanReadable : Option("-h")
        object long : Option("-l")
        object recursive : Option("-R")
        object reverse : Option("-r")
        object size : Option("-s")
        data class sort(val type: sortType): Option("--sort=$type")
        enum class sortType { NONE, SIZE, TIME, VERSION, EXTENSION;
            override fun toString() = super.toString().lowercase()
        }
        data class time(val type: timeType): Option("--time=$type")
        enum class timeType { ATIME, ACCESS, CTIME, STATUS;
            override fun toString() = super.toString().lowercase()
        }
        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}


data class Vim(
    val files: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "vim"
    override val args get() = options.flatMap { it.str } + files

    sealed class Option(val name: String, val arg: String? = null) {

        // important: name and arg has to be separate in Vim.args - for Kommand.exec to work correctly
        val str get() = arg?.let { listOf(name, it) } ?: listOf(name)

        object gui : Option("-g")
        object diff : Option("-d")
        object help : Option("-h")
        /**
         * Connect  to  a Vim server and make it edit the files given in the rest of the arguments.
         * If no server is found a warning is given and the files are edited in the current Vim.
         */
        object remote : Option("--remote")
        /** List the names of all Vim servers that can be found. */
        object serverlist : Option("--serverlist")
        /**
         * Use {server} as the server name.  Used for the current Vim, unless used with a --remote  argument,
         * then  it's  the name of the server to connect to.
         */
        data class servername(val server: String) : Option("--servername", server)
        /** GTK GUI only: Use the GtkPlug mechanism to run gvim in another window. */
        data class socketid(val id: String) : Option("--socketid", id)
        /** During startup write timing messages to the file {fname}. */
        data class startuptime(val file: String) : Option("--startuptime", file)
        object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}

/** [Android Debug Bridge User Guide](https://developer.android.com/studio/command-line/adb) */
data class Adb(
    var command: Command = Command.help,
    val options: MutableList<Option> = mutableListOf() // these are "global" options - TODO_later other options
): Kommand {
    override val name get() = "adb"
    override val args get() = options.map { it.str } + listOf(command.name)

    sealed class Command(val name: String) {
        object help : Command("help")
        object devices : Command("devices")
        object version : Command("version")
    }

    sealed class Option(val str: String) {
        /** Listen on all network interfaces, not just localhost */
        object all : Option("-a")
        /** Use USB device (error if multiple devices connected) */
        object usb : Option("-d")
        /** Use TCP/IP device (error if multiple TCP/IP devices available) */
        object tcp : Option("-e")
    }
    operator fun Option.unaryMinus() = options.add(this)
}

data class Audacious(
    val files: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "audacious"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {
        object help : Option("--help")
        object enqueue : Option("--enqueue")
        object play : Option("--play")
        object pause : Option("--pause")
        object stop : Option("--stop")
        object rew : Option("--rew")
        object fwd : Option("--fwd")
        object version : Option("--version")
        object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}

// TODO_someday: better bash composition support; make sure I correctly 'quote' stuff when composing Kommands with Bash
// https://www.gnu.org/savannah-checkouts/gnu/bash/manual/bash.html#Quoting
// TODO_maybe: typesafe DSL for composing bash scripts? (similar to URE)
data class Bash(
    /** a command string (usually just one string with or without spaces) or a file (when no -c option provided) */
    val nonopts: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
): Kommand {
    override val name get() = "bash"
    override val args get() = options.map { it.str } + nonopts

    sealed class Option(val str: String) {
        /** interpret nonopts as a command to run */
        object command : Option("-c")
        object interactive : Option("-i")
        object login : Option("-l")
        object restricted : Option("-r")
        object posix : Option("--posix")
        object help : Option("--help")
        object version : Option("--version")
        object verbose : Option("--verbose")
    }
    operator fun Option.unaryMinus() = options.add(this)
}

// TODO: "export" command - with output parsing for better composability
