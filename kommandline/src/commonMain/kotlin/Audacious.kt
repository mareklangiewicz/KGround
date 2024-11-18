package pl.mareklangiewicz.kommand

fun audacious(vararg files: String, init: Audacious.() -> Unit = {}) = Audacious(files.toMutableList()).apply(init)

data class Audacious(
  val files: MutableList<String> = mutableListOf(),
  val options: MutableList<Option> = mutableListOf(),
) : Kommand {
  override val name get() = "audacious"
  override val args get() = options.map { it.str } + files

  sealed class Option(val str: String) {
    data object Help : Option("--help") // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
    data object Enqueue : Option("--enqueue")
    data object Play : Option("--play")
    data object Pause : Option("--pause")
    data object Stop : Option("--stop")
    data object Rew : Option("--rew")
    data object Fwd : Option("--fwd")
    data object Version : Option("--version") // Don't risk short -v (ambiguity with "verbose" for many commands)
    data object Verbose : Option("--verbose")
  }

  operator fun Option.unaryMinus() = options.add(this)
}
