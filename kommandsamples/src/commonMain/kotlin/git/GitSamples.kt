package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.samples.*

@OptIn(DelicateKommandApi::class)
data object GitSamples {
    val hash = gitHash() s "git rev-parse HEAD"
    val help = gitHelp() s "git help"
    val helpLog = gitHelp("log") s "git help log"
    val helpOption = git { -GitOpt.Help } s "git --help"
    val version = git { -GitOpt.Version } s "git --version"
    val status = gitStatus() s "git status"
}