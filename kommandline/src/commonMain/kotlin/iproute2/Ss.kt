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

        data object Help : Option("--help")
        data object Version : Option("--version")
        data object NoHeader : Option("--no-header")
        data object OneLine : Option("--oneline")
        data object Numeric : Option("--numeric")
        data object All : Option("--all")
        data object Listening : Option("--listening")
        data object Options : Option("--options")
        data object Extended : Option("--extended")
        data object Memory : Option("--memory")
        data object Processes : Option("--processes")
        data object Info : Option("--info")
        data object Tos : Option("--tos")
        data object CGroup : Option("--cgroup")
        data object Kill : Option("--kill")
        data object Summary : Option("--summary")
        data object Events : Option("--events")
        data object Context : Option("--context")
        data object Contexts : Option("--contexts")
        data class Net(val nsname: String) : Option("--net", nsname)
        data object Bpf : Option("--bpf")
        data object Ipv4 : Option("--ipv4")
        data object Ipv6 : Option("--ipv6")
        data object Packet : Option("--packet")
        data object Tcp : Option("--tcp")
        data object Udp : Option("--udp")
        data object Ddcp : Option("--ddcp")
        data object Raw : Option("--raw")
        data object Unix : Option("--unix")
        data object Sctp : Option("--sctp")
        data object VSock : Option("--vsock")
        data object Xdp : Option("--xdp")
        data object InetSockOpt : Option("--inet-sockopt")
        data class Family(val family: String) : Option("--family", family)
        data class Query(val query: String) : Option("--query", query)
        data class Diag(val file: String) : Option("--diag", file)
        data class Filter(val file: String) : Option("--filter", file)

        data object Update : Option("--update")
        data object Debug : Option("--debug")
        data object Default : Option("--default")
        data object Warnings : Option("--warnings")
        data object WhatIs : Option("--whatis")
        data object Apropos : Option("--apropos")
        data object GlobalApropos : Option("--global-apropos")
        data object Where : Option("--where")
        data object Regex : Option("--regex")
        data object Wildcard : Option("--wildcard")
        data object NamesOnly : Option("--names-only")
        data object Usage : Option("--usage")
    }
    operator fun Option.unaryMinus() = options.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}
