@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)
fun vim(vararg files: String, init: Vim.() -> Unit = {}) = Vim(files.toMutableList()).apply(init)
fun adb(command: Adb.Command, init: Adb.() -> Unit = {}) = Adb(command).apply(init)
fun audacious(vararg files: String, init: Audacious.() -> Unit = {}) = Audacious(files.toMutableList()).apply(init)
fun mktemp(template: String? = null, init: MkTemp.() -> Unit = {}) = MkTemp(template).apply(init)
fun Platform.createTempFile(prefix: String = "tmp.", suffix: String = ".tmp") =
    shell(mktemp("$pathToUserTmp/${prefix}XXXXXX${suffix}")).output()[0]

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

fun Kommand.line() = (listOf(name) + args.map { bashQuoteMetaChars(it) }).joinToString(" ")
fun Kommand.println() = println(line())


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
        val str get() = listOf(name) plusIfNotNull arg

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

data class MkTemp(
    var template: String? = null,
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "mktemp"
    override val args get() = options.flatMap { it.str } plusIfNotNull template

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNotNull arg
        object directory : Option("--directory")
        object dryrun : Option("--dry-run")
        object quiet : Option("--quiet")
        data class suffix(val s: String) : Option("--suffix", s)
        data class tmpdir(val dir: String) : Option("--tmpdir", dir)
        object help : Option("--help")
        object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
