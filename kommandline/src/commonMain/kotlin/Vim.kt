package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.Vim.Option.*

fun vim(vararg files: String, init: Vim.() -> Unit = {}) = Vim(files.toMutableList()).apply(init)
fun gvim(vararg files: String, init: Vim.() -> Unit = {}) = vim(*files) { -gui; init() }

data class Vim(
    val files: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf()
): Kommand {
    override val name get() = "vim"
    override val args get() = options.flatMap { it.str } + files

    sealed class Option(val name: String, val arg: String? = null) {

        // important: name and arg has to be separate in Vim.args - for Kommand.exec to work correctly
        val str get() = listOf(name) plusIfNN arg

        data object gui : Option("-g")
        data object diff : Option("-d")
        data object help : Option("-h")
        /**
         * Connect  to  a Vim server and make it edit the files given in the rest of the arguments.
         * If no server is found a warning is given and the files are edited in the current Vim.
         */
        data object remote : Option("--remote")
        /** List the names of all Vim servers that can be found. */
        data object serverlist : Option("--serverlist")
        /**
         * Use {server} as the server name.  Used for the current Vim, unless used with a --remote  argument,
         * then  it's  the name of the server to connect to.
         */
        data class servername(val server: String) : Option("--servername", server)
        /** GTK GUI only: Use the GtkPlug mechanism to run gvim in another window. */
        data class socketid(val id: String) : Option("--socketid", id)
        /** During startup write timing messages to the file {fname}. */
        data class startuptime(val file: String) : Option("--startuptime", file)
        data object version : Option("--version")
    }
    operator fun Option.unaryMinus() = options.add(this)
}
