package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.samples.*


@OptIn(DelicateApi::class)
data object GpgSamples {

  val help = gpg { -Help } s ("gpg --help")

  val testGpgListKeys = gpg(ListPublicKeys) s "gpg --list-public-keys"

  val testGpgListKeysVerbose = gpg(ListPublicKeys) { -Verbose } s "gpg --list-public-keys --verbosee"

  val testGpgListSecretKeysVerbose = gpg(ListSecretKeys) { -Verbose } s "gpg --list-secret-keys --verbose"
}
