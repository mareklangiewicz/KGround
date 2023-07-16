package pl.mareklangiewicz.kommand.git

import pl.mareklangiewicz.kommand.samples.*

data object GitSamples {
    val hash = gitHash() s "git rev-parse HEAD"
    val help = gitHelp() s "git help"
    val helpLog = gitHelp("log") s "git help log"
    val helpOption = git { -Git.Option.help } s "git --help"
    val version = git { -Git.Option.version } s "git --version"
    val status = gitStatus() s "git status"
}