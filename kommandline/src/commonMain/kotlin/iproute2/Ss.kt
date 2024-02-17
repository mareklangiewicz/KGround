package pl.mareklangiewicz.kommand.iproute2

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.iproute2.SsOpt.*

@OptIn(DelicateApi::class)
/**
 * Wrapper for very common invocation ("ss -tulpn")
 * FIXME_someday: better wrappers with flags for more common use-cases and nicer names
 */
fun ssTulpn() = ss { -Tcp; -Udp; -Listening; -Processes; -Numeric }


// FIXME_someday: better static types/wrapping for nonopts (FILTER := [ state STATE-FILTER ] [ EXPRESSION ])

/** [ss manpage](https://manpages.ubuntu.com/manpages/bionic/en/man8/ss.8.html) */
@DelicateApi
fun ss(init: Ss.() -> Unit = {}) = Ss().apply(init)

@DelicateApi
data class Ss(
    override val opts: MutableList<SsOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<SsOpt> { override val name get() = "ss" }

@DelicateApi
open class SsOpt(arg: String? = null): KOptLN(arg) {
    data object Help : SsOpt()
    data object Version : SsOpt()
    data object NoHeader : SsOpt()
    data object Oneline : SsOpt()
    data object Numeric : SsOpt()
    data object All : SsOpt()
    data object Listening : SsOpt()
    data object Options : SsOpt()
    data object Extended : SsOpt()
    data object Memory : SsOpt()
    data object Processes : SsOpt()
    data object Info : SsOpt()
    data object Tos : SsOpt()
    data object Cgroup : SsOpt()
    data object Kill : SsOpt()
    data object Summary : SsOpt()
    data object Events : SsOpt()
    data object Context : SsOpt()
    data object Contexts : SsOpt()
    data class Net(val nsname: String) : SsOpt(nsname)
    data object Bpf : SsOpt()
    data object Ipv4 : SsOpt()
    data object Ipv6 : SsOpt()
    data object Packet : SsOpt()
    data object Tcp : SsOpt()
    data object Udp : SsOpt()
    data object Ddcp : SsOpt()
    data object Raw : SsOpt()
    data object Unix : SsOpt()
    data object Sctp : SsOpt()
    data object Vsock : SsOpt()
    data object Xdp : SsOpt()
    data object InetSockopt : SsOpt()
    data class Family(val family: String) : SsOpt(family)
    data class Query(val query: String) : SsOpt(query)
    data class Diag(val file: String) : SsOpt(file)
    data class Filter(val file: String) : SsOpt(file)

    data object Update : SsOpt()
    data object Debug : SsOpt()
    data object Default : SsOpt()
    data object Warnings : SsOpt()
    data object Whatis : SsOpt()
    data object Apropos : SsOpt()
    data object GlobalApropos : SsOpt()
    data object Where : SsOpt()
    data object Regex : SsOpt()
    data object Wildcard : SsOpt()
    data object NamesOnly : SsOpt()
    data object Usage : SsOpt()
}
