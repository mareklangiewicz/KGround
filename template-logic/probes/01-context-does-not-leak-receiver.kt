//? name: context(x){} does not introduce an implicit receiver
//? expect: fail
//? fails-in: lib
//? message: Unresolved reference 'githubUrl'
//? ---- lib ----
// githubUrl exists only on LibDetails, so referencing it detects a receiver.
fun probe(d: LibDetails): String = context(d) { githubUrl }
