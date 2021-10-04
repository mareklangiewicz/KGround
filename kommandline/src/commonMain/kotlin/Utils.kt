package pl.mareklangiewicz.kommand

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNotNull(element: T?) = if (element == null) this else this + element

fun List<String>.printlns() = forEach(::println)

