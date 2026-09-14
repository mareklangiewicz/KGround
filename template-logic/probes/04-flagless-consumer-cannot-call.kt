//? name: a FLAGLESS consumer cannot call a context fun naturally
//? expect: fail
//? fails-in: consumer
//? message: To call contextual declarations, specify the '-Xcontext-parameters' compiler option.
//? ---- lib ----
// Receiverless on purpose, so "receiver type mismatch" cannot be the confound.
context(d: LibDetails) fun probe(): String = d.name
//? ---- consumer ----
fun consume(d: LibDetails): String = probe(d)
