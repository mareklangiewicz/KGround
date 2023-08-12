package pl.mareklangiewicz.kommand.iproute2

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*

// TODO NOW: add "ss" command correctly. especially with switches: "ss -tulpn" (and parsing output too?)

/** [ss manpage](https://manpages.ubuntu.com/manpages/bionic/en/man8/ss.8.html) */
fun ss(stateFilter: String? = null, init: Ss.() -> Unit = {}) = Ss(stateFilter).apply(init)

data class Ss(
    val stateFilter: String? = null,
    val options: MutableList<Option> = mutableListOf(),
    val nonopts: MutableList<String> = mutableListOf()
        // FIXME_someday: better static types/wrapping for non-options (FILTER := [ state STATE-FILTER ] [ EXPRESSION ])
): Kommand {
    override val name get() = "ss"
    override val args get() = (options.map { it.str } plusIfNN stateFilter?.let { "state $it" }) + nonopts

    sealed class Option(val name: String, val arg: String? = null) {

        val str get() = arg?.let { "$name=$it" } ?: name // TODO_someday: some fun similar to plusIfNotNull for such cases

        data object help : Option("--help")
        data object version : Option("--version")
        data object noheader : Option("--no-header")
        data object oneline : Option("--oneline")
        data object numeric : Option("--numeric")
        data object all : Option("--all")
        data object listening : Option("--listening")
        data object options : Option("--options")
        data object extended : Option("--extended")
        data object memory : Option("--memory")
        data object processes : Option("--processes")
        data object info : Option("--info")
        data object tos : Option("--tos")
        data object cgroup : Option("--cgroup")
        data object kill : Option("--kill")
        data object summary : Option("--summary")
        data object events : Option("--events")
        data object context : Option("--context")
        data object contexts : Option("--contexts")
        data class net(val nsname: String) : Option("--net", nsname)
        data object bpf : Option("--bpf")
        data object ipv4 : Option("--ipv4")
        data object ipv6 : Option("--ipv6")
        data object packet : Option("--packet")
        data object tcp : Option("--tcp")
        data object udp : Option("--udp")
        data object ddcp : Option("--ddcp")
        data object raw : Option("--raw")
        data object unix : Option("--unix")
        data object sctp : Option("--sctp")
        data object vsock : Option("--vsock")
        data object xdp : Option("--xdp")
        data object inetSockopt : Option("--inet-sockopt")
        data class family(val family: String) : Option("--family", family)
        data class query(val query: String) : Option("--query", query)
        data class diag(val file: String) : Option("--diag", file)
        data class filter(val file: String) : Option("--filter", file)

        data object update : Option("--update")
        data object debug : Option("--debug")
        data object default : Option("--default")
        data object warnings : Option("--warnings")
        data object whatis : Option("--whatis")
        data object apropos : Option("--apropos")
        data object globalapropos : Option("--global-apropos")
        data object where : Option("--where")
        data object regex : Option("--regex")
        data object wildcard : Option("--wildcard")
        data object namesonly : Option("--names-only")
        data object usage : Option("--usage")
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}
