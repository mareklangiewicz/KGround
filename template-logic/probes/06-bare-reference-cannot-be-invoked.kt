//? name: a bare reference resolves but CANNOT be invoked without the flag
//? expect: fail
//? fails-in: consumer
//? message: To call contextual declarations, specify the '-Xcontext-parameters' compiler option.
//? ---- lib ----
context(d: LibDetails) fun probe(): String = d.name
//? ---- consumer ----
// This is why probe 05 needs the explicit type: the reference trick alone is not enough.
fun consume() {
  val ref = ::probe
  println(ref())
}
