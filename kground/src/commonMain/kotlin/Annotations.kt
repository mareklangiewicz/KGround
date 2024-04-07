package pl.mareklangiewicz.annotations


// TODO:
//   Annotations here are mostly meant to be different flavors of "soft" deprecation.
//   I don't want standard @Deprecated because it annoys user too much, and it changes autocompletion too much.
//   It would be awesome to have IntelliJ support that suggest changes according to [replaceWith] params,
//   and that underlines usages in specific delicate manners (like green grammatical errors etc).
//   But I still want autocomplete not to hide these APIs too much
//   I want IDE to hint the user about these APIs after he types specific prefix,
//   (showing "soft-deprecated" completions is important to give user a clue what other completions are NOT doing)
//   and I want IDE to show provided messages and propose changes according to [replaceWith]
//   and I want to have (Require)OptIn machinery/propagation still available (BTW can IDE propagate message too?),
//   For now I'll add two parameters (just as documentation) to each such annotation class: message, replaceWith;
//   even though IntelliJ is unfortunately not using them as in @Deprecated case - maybe someday it will.
//   There are some JetBrains plans and issues on youtrack - monitor them:
//     https://youtrack.jetbrains.com/issue/KT-54106/Provide-API-for-perpetual-soft-deprecation-and-endorsing-uses-of-more-appropriate-API
//     https://youtrack.jetbrains.com/issue/IJPL-74


/** API that can behave differently on different platforms, or even work only on specific platforms. */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@MustBeDocumented
annotation class NotPortableApi(val message: String = "", val replaceWith: ReplaceWith = ReplaceWith(""))

/** API that has to be used more carefully. Usually low-level stuff. */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@MustBeDocumented
annotation class DelicateApi(val message: String = "", val replaceWith: ReplaceWith = ReplaceWith(""))

/** API that is a bit less preferred than standard API that should be demonstrated in [replaceWith]. */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@MustBeDocumented
annotation class SecondaryApi(val message: String = "", val replaceWith: ReplaceWith = ReplaceWith(""))



/** API that will most probably be changed a lot in the future. */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@MustBeDocumented
annotation class ExperimentalApi(val message: String = "")

/** API that is included mostly as doc/sample/example usage of underlying API. Not so much for general usage. */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@MustBeDocumented
annotation class ExampleApi(val message: String = "")
