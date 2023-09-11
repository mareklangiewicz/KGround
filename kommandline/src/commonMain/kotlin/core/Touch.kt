package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*

/** Update the access and modification times of each file to the current time. Create empty files if necessary. */
@OptIn(DelicateKommandApi::class)
fun touch(vararg files: String) = touch { nonopts.addAll(files) }.reducedOutToUnit()

@DelicateKommandApi
fun touch(init: Touch.() -> Unit) = Touch().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/touch.1.html) */
@DelicateKommandApi
data class Touch(
    override val opts: MutableList<TouchOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<TouchOpt> { override val name get() = "touch" }



@DelicateKommandApi
interface TouchOpt: KOptTypical {
    data object TimeOfAccessOnly : TouchOpt, KOptS("a")
    data object TimeOfChangeOnly : TouchOpt, KOptS("m")
    data object DisableCreation : TouchOpt, KOptS("c")
    data class Date(val date: String): TouchOpt, KOptL("date", date)
    data object Help : TouchOpt, KOptL("help")
    data object Version : TouchOpt, KOptL("version")
}
