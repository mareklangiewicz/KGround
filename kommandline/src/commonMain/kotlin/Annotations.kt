package pl.mareklangiewicz.kommand

/**
 * Marks declarations in the kommand line library that are **delicate**;
 * usually they are low level and allow to generate incorrect kommands,
 * which leads to bugs which can be very difficult to find later.
 * Try to use safer non-delicate wrappers instead, which either do not allow to create incorrect kommand lines,
 * or at least they try to "fail fast" in runtime instead of running some suspicious kommands on CliPlatform.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is a delicate KommandLine API and its use requires care."
)
public annotation class DelicateKommandApi

/** Marks declarations that are very **experimental** in kommand line API. */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPEALIAS
)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
public annotation class ExperimentalKommandApi
