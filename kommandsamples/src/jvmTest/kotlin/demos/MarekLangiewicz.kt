package pl.mareklangiewicz.kommand.demos

import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.extension.ExtensionContext
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.konfig.*
import pl.mareklangiewicz.kommand.term.*


// TODO NOW: move all this kind of stuff to samples


// unfortunately, this can't be moved to main kommandline jvm code, because it depends on jupiter:ExtensionContext
// maybe it could be moved to uspekx-jvm, but that would require uspekx depend on kommandline
fun isUserTestClassEnabled(context: ExtensionContext) =
    isUserFlagEnabled(SYS, "tests." + context.requiredTestClass.simpleName)

@OptIn(DelicateApi::class)
@EnabledIf(
    value = "pl.mareklangiewicz.kommand.demos.MarekLangiewiczKt#isUserTestClassEnabled",
    disabledReason = "tests.MarekLangiewicz not enabled in user konfig"
)
class MarekLangiewicz {

}
