@file:OptIn(NotPortableApi::class, DelicateApi::class, ExperimentalApi::class)

package pl.mareklangiewicz.kground

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.boolean
import kotlinx.coroutines.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.uctxWithIO
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.kommand.getSysCLI
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.localCLI
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.localULog
import pl.mareklangiewicz.usubmit.USubmit
import pl.mareklangiewicz.usubmit.xd.CannedSupervisor

fun main(args: Array<String>) = kgroundx(args)

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this fun (called from main fun) allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.app.Playground#play
 * So this way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
fun kgroundx(args: Array<String>) = KGroundXCommand().main(args)

@DelicateApi("Very opinionated setup for launching main stuff. Usually better to copy and adjust to own needs.")
fun runBlockingMain(name: String, submit: USubmit = ZenitySupervisor(), block: suspend CoroutineScope.() -> Unit) =
  runBlocking {
    val log = UHackySharedFlowLog(
      minLevel = ULogLevel.INFO,
      // minLevel = ULogLevel.DEBUG,
    ) { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
    // FIXME_later: Maybe I should log with Clikt "echo"? is it thread-safe??
    uctxWithIO(
      context = log + submit + getSysCLI(),
      name = name,
      // dispatcher = null, // FIXME_later: rethink default dispatcher
      block = block,
    )
  }



/**
 * The version baked into the jar manifest by kgroundx-app/build.gradle.kts.
 *
 * Null when running from loose classes rather than a jar (IDE, tests) -- there is no manifest to
 * read then, so say so instead of inventing a number.
 */
private fun kgroundxVersion(): String =
  KGroundXCommand::class.java.`package`?.implementationVersion ?: "unknown (no jar manifest)"

/** No canned answers: ask a human (zenity). Any: answer from them, and never pop anything up. */
private fun supervisorFor(answers: Map<String, String>): USubmit =
  if (answers.isEmpty()) ZenitySupervisor() else CannedSupervisor(answers)

private class KGroundXCommand() : CliktCommand(name = "kgroundx") {

  val answers by option(
    "--answer",
    metavar = "ID=VALUE",
    help = "Answer the question with this id instead of asking (repeatable). With any --answer given, " +
      "nothing pops up: an unanswered question fails, and messages go to the log.",
  ).associate()

  init {
    versionOption(kgroundxVersion())
    subcommands(
      GetUserFlagCommand(),
      SetUserFlagCommand(),
      TryCodeXclipCommand(),
      TryCodeCommand(),
      CompletionCommand(),
        // use it like: kground generate-completion zsh/bash/fish > ~/.config/myshell/kground-completion-zsh
        // and then set up sourcing generated file in some zsh/bash/fish init script
        // (jvm is too slow to regenerate it each time the shell is starting)
    )
  }

  override fun run() { currentContext.findOrSetObject { answers } }

  override fun helpEpilog(context: Context): String {
    return super.helpEpilog(context) + """
      Examples:
        $commandName get-user-flag code.interactive
        $commandName set-user-flag code.interactive true
        $commandName set-user-flag code.interactive false
        $commandName try-code-xclip
        $commandName try-code tryInjectToProject SMokK
        $commandName try-code tryInjectToKGround
        $commandName try-code tryInjectToAllMyProjects
        $commandName try-code updateGradlewInExampleProject
        $commandName try-code updateGradlewInMyProjects
        $commandName try-code checkGradleEverywhere
        $commandName try-code updateGradleEverywhere
        $commandName try-code checkAllMDW
        $commandName try-code injectMDWToMyProjects
        $commandName try-code injectDWToProject kthreelhu
        $commandName --answer try-code.call=yes --answer try-code.open-log=no try-code checkDWInProject kthreelhu
        $commandName try-code collectGabrysCards
      Also using full "paths", f.e.:
        $commandName try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToKGround
        $commandName try-code pl.mareklangiewicz.kgroundx.experiments.MyExperiments#collectGabrysCards
    """.trimIndent().replace('\n', '\u0085')
      // have to use special "manual" line-breaks
      // see: https://ajalt.github.io/clikt/documenting/#manual-line-breaks

    // TODO: support short coderefs for special/common places/examples/samples/classes
    //   clikt has "aliases" and "transformToken", but better to just use my own "universal" method,
    //   that searches provided coderef in some predefined classes.
  }
}

private class GetUserFlagCommand() : CliktCommand() {
  val answers by requireObject<Map<String, String>>()
  val flag by argument(help = "user flag name")
  override fun run() = runBlockingMain(commandName, supervisorFor(answers)) {
    localULog().i(getUserFlagFullStr(localCLI(), flag))
  }
}

private class SetUserFlagCommand() : CliktCommand() {
  val answers by requireObject<Map<String, String>>()
  val flag by argument(help = "user flag name")
  val value by argument(help = "user flag value").boolean()
  override fun run() = runBlockingMain(commandName, supervisorFor(answers)) {
    setUserFlag(localCLI(), flag, value)
  }
}

private class TryCodeCommand() : CliktCommand() {
  val answers by requireObject<Map<String, String>>()
  val codeRef by argument(help = "code reference")
  val codeArgs by argument(help = "String arguments for the referenced function").multiple()
  override fun run() = runBlockingMain(commandName, supervisorFor(answers)) {
    tryInteractivelyCodeRefWithLogging(codeRef, codeArgs)
  }
}

private class TryCodeXclipCommand() : CliktCommand() {
  val answers by requireObject<Map<String, String>>()
  override fun run() = runBlockingMain(commandName, supervisorFor(answers)) {
    tryInteractivelyCodeRefWithLogging("xclip")
  }
}
