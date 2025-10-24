package pl.mareklangiewicz.kommand.ide

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.ide.Ide.Type.*
import pl.mareklangiewicz.kommand.samples.*

@OptIn(DelicateApi::class)
data object IdeSamples {

  @OptIn(NotPortableApi::class)
  private val FS = getSysUFileSys()
  private val PHome = FS.pathToUserHome!!
  private val PTmpNotes = FS.pathToTmpNotes
  private val PCodeKt = PHome / "code/kotlin"
  private val PKGround = PCodeKt / "KGround"
  private val PTemplateBasicBuild = PKGround / "template-basic/build.gradle.kts"
  private val PTemplateFullBuild = PKGround / "template-full/build.gradle.kts"

  val ideapHelp = ideHelp(IdeaP) s "ideap --help"

  val ideapVersion = ideVersion(IdeaP) s "ideap --version"

  val ideaVersion = ideVersion(Ide.Type.Idea) s "idea --version"

  val ideaslimVersion = ideVersion(IdeaSlim) s "ideaslim --version"

  val studioVersion = ideVersion(Studio) s "studio --version"

  // BTW It will start IdeaP if not already running
  val ideapOpenTmpNotes = ideOpen(IdeaP, PTmpNotes) s "ideap $PTmpNotes"

  // BTW It will just fail if no running IDE found
  val ideOpenTmpNotes = ideOpen(PTmpNotes)

  // BTW It will start IdeaP if not already running
  val ideapDiffTemplates = ideDiff(IdeaP, PTemplateBasicBuild, PTemplateFullBuild) s
    "ideap diff $PTemplateBasicBuild $PTemplateFullBuild"

  // BTW It will just fail if no running IDE found
  val ideDiffTemplates = ideDiff(PTemplateBasicBuild, PTemplateFullBuild)


  // TODO_someday_maybe: samples for merging?
  //   although it's similar to diffing, also I don't want to prepare files for sample merging

  // TODO: samples for Format, Inspect? Install?


  // BTW See also [[MyDemoSamples]]
}

