@file:OptIn(NotPortableApi::class, DelicateApi::class, ExperimentalApi::class)

package pl.mareklangiewicz.kground

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.boolean
import kotlinx.coroutines.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.uctxWithIO
import pl.mareklangiewicz.kgroundx.maintenance.ZenitySupervisor
import pl.mareklangiewicz.kommand.getSysCLI
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.localCLI
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.localULog

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this fun (called from main fun) allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.app.Playground#play
 * So this way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 */
fun main(args: Array<String>) = kgroundx(args)

@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
fun kgroundx(args: Array<String>) = KGroundXCommand().main(args)

@DelicateApi("Very opinionated setup for launching main stuff. Usually better to copy and adjust to own needs.")
fun runBlockingMain(name: String, block: suspend CoroutineScope.() -> Unit) =
  runBlocking {
    val log = UHackySharedFlowLog(
      minLevel = ULogLevel.INFO,
      // minLevel = ULogLevel.DEBUG,
    ) { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
    // FIXME_later: Maybe I should log with Clikt "echo"? is it thread-safe??
    uctxWithIO(
      context = log + ZenitySupervisor() + getSysCLI(),
      name = name,
      // dispatcher = null, // FIXME_later: rethink default dispatcher
      block = block,
    )
  }



private class KGroundXCommand() : CliktCommand(name = "kgroundx") {
  init {
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

  override fun run() = Unit

  override fun helpEpilog(context: Context): String {
    return super.helpEpilog(context) + """
      Examples:
        $commandName get-user-flag code.interactive
        $commandName set-user-flag code.interactive true
        $commandName set-user-flag code.interactive false
        $commandName try-code-xclip
        $commandName try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToAbcdK
        $commandName try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToKGround
        $commandName try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectAllMyProjects
        $commandName try-code pl.mareklangiewicz.kgroundx.maintenance.MyOtherExamples#updateGradlewInMyProjects
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
  val flag by argument(help = "user flag name")
  override fun run() = runBlockingMain(commandName) {
    localULog().i(getUserFlagFullStr(localCLI(), flag))
  }
}

private class SetUserFlagCommand() : CliktCommand() {
  val flag by argument(help = "user flag name")
  val value by argument(help = "user flag value").boolean()
  override fun run() = runBlockingMain(commandName) {
    setUserFlag(localCLI(), flag, value)
  }
}

private class TryCodeCommand() : CliktCommand() {
  val codeRef by argument(help = "code reference")
  override fun run() = runBlockingMain(commandName) {
    tryInteractivelyCodeRefWithLogging(codeRef)
  }
}

private class TryCodeXclipCommand() : CliktCommand() {
  override fun run() = runBlockingMain(commandName) {
    tryInteractivelyCodeRefWithLogging("xclip")
  }
}
