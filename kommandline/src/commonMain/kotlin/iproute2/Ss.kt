package pl.mareklangiewicz.kommand.iproute2

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

        object help : Option("--help")
        object version : Option("--version")
        object noheader : Option("--no-header")
        object oneline : Option("--oneline")
        object numeric : Option("--numeric")
        object all : Option("--all")
        object listening : Option("--listening")
        object options : Option("--options")
        object extended : Option("--extended")
        object memory : Option("--memory")
        object processes : Option("--processes")
        object info : Option("--info")
        object tos : Option("--tos")
        object cgroup : Option("--cgroup")
        object kill : Option("--kill")
        object summary : Option("--summary")
        object events : Option("--events")
        object context : Option("--context")
        object contexts : Option("--contexts")
        data class net(val nsname: String) : Option("--net", nsname)
        object bpf : Option("--bpf")
        object ipv4 : Option("--ipv4")
        object ipv6 : Option("--ipv6")
        object packet : Option("--packet")
        object tcp : Option("--tcp")
        object udp : Option("--udp")
        object ddcp : Option("--ddcp")
        object raw : Option("--raw")
        object unix : Option("--unix")
        object sctp : Option("--sctp")
        object vsock : Option("--vsock")
        object xdp : Option("--xdp")
        object inetSockopt : Option("--inet-sockopt")
        data class family(val family: String) : Option("--family", family)
        data class query(val query: String) : Option("--query", query)
        data class diag(val file: String) : Option("--diag", file)
        data class filter(val file: String) : Option("--filter", file)

        object update : Option("--update")
        object debug : Option("--debug")
        object default : Option("--default")
        object warnings : Option("--warnings")
        object whatis : Option("--whatis")
        object apropos : Option("--apropos")
        object globalapropos : Option("--global-apropos")
        object where : Option("--where")
        object regex : Option("--regex")
        object wildcard : Option("--wildcard")
        object namesonly : Option("--names-only")
        object usage : Option("--usage")
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}
