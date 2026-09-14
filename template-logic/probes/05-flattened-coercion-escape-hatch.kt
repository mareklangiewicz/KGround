//? name: the flattened-coercion escape hatch works from a FLAGLESS consumer, and runs
//? expect: ok
//? output: PROBE05 ok:KGround
//? ---- lib ----
context(d: LibDetails) fun probe(): String = "ok:" + d.name
fun probeDetails(): LibDetails = myLibDetails(name = "KGround", description = "probe")
//? ---- consumer ----
// The explicit FLATTENED function type is the essential step: the context
// parameter arrives as the first ordinary argument. Dropping the type
// annotation reintroduces the flag error -- that is probe 06.
val f: (LibDetails) -> String = ::probe
println("PROBE05 " + f(probeDetails()))
