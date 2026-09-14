//? name: context(a,b) takes several args; context(_) forwards without naming
//? expect: ok
//? output: PROBE03 ok:KGround
//? ---- lib ----
context(d: LibDetails, s: LibSettings) private fun inner(): String = "ok:${d.name}"
context(_: LibDetails, _: LibSettings) private fun conduit(): String = inner()
// entry point the consumer can call without needing the flag itself
fun probe(d: LibDetails): String = context(d, d.settings) { conduit() }
fun probeDetails(): LibDetails = myLibDetails(name = "KGround", description = "probe")
//? ---- consumer ----
println("PROBE03 " + probe(probeDetails()))
