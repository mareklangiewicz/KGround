//? name: CONTROL - with(x){} DOES introduce a receiver
//? expect: ok
//? ---- lib ----
// Without this control, probe 01 would pass for a misspelled identifier just as
// happily as for the real behaviour. This proves 01 discriminates.
fun probe(d: LibDetails): String = with(d) { githubUrl }
