package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.FileSystem.Companion.RESOURCES
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kground.io.pathToTmpNotes
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.kommand.zenity.zenityShowWarning
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*


// TODO_someday: support for ~~>...<~~...<~~

// It will NOT inject special regions with tildes; these magic arrow regions differ in each file anyway.
suspend fun tryInjectMyTemplatesToProject(
  projectPath: Path,
  collectedTemplates: Map<String, String>? = null,
  askInteractively: Boolean = true,
) {
  val templates = collectedTemplates ?: collectMyTemplates()
  findAllFiles(projectPath).filterExt("kts") // TODO_someday: support templates in .kt files too
    .forEachSpecialRegionFound(allowTildes = false) { path, label, content, region ->
      val log = localULog()
      val templateRegion = templates[label] ?: run {
        log.i("Found unknown region [[$label]] in $path (length ${region.length}). Ignoring.")
        return@forEachSpecialRegionFound
      }
      if(region == templateRegion) {
        log.i("Found known template [[$label]] in $path (length ${region.length}). Matching -> Ignoring.")
        return@forEachSpecialRegionFound
      }
      suspend fun inject() {
        log.i("Injecting   template [[$label]] to $path:")
        path.injectSpecialRegion(label, templateRegion, addIfNotFound = false)
      }
      when {
        !askInteractively -> inject()
        zenityAskIf("Automatically inject template [[$label]]? to file:\n$path").ax() -> inject()
        zenityAskIf("Try opening diff with [[$label]] in IDE? (put to tmp.notes) with file:\n$path").ax() -> {
          val notes = localUFileSys().pathToTmpNotes
          writeFileWithDD(templateRegion.lines(), notes).ax()
          ideDiff(notes, path).ax()
        }
      }
      if (askInteractively)
        zenityAskIf("Continue injecting templates? (No -> abort)").ax().chkTrue { "Abort injecting templates." }
    }
}

@Deprecated("Temporary fun to fix templates from single to double square brackets marks")
@OptIn(DelicateApi::class, ExperimentalApi::class, NotPortableApi::class)
suspend fun tryFixMyTemplatesInProject(
  projectPath: Path,
  askInteractively: Boolean = true,
) {
  val log = localULog()
  val ure = ureSpecialRegion(
    content = ureWhateva().withName("regionContent"),
    specialLabel = ureAnyRegionLabel(allowTildes = true, allowBrackets = false).withName("specialLabel"),
    regionLabelPrefix = "[",
    regionLabelPostfix = "]",
  ).withName("oldRegion")
  findAllFiles(projectPath).filterExt("kts") // TODO_someday: support templates in .kt files too
    .forEach { path ->
      processFile(path, path) { oldContent ->
        val newContent = oldContent.replaceAll(ure) { mr ->
          val oldRegion by mr
          val regionContent by mr
          val specialLabel by mr
          val newRegion = "// region [[$specialLabel]]\n$regionContent// endregion [[$specialLabel]]\n"
          log.i("found special region $specialLabel (len ${oldRegion.length}->${newRegion.length})")
          newRegion
        }
        when {
          !askInteractively || newContent == oldContent -> newContent
          zenityAskIf("Fix regions in $path (len ${oldContent.length}->${newContent.length})?").ax() -> newContent
          else -> null
        }
      }
      // if (askInteractively) zenityAskIf("Continue fixing templates? (No -> abort)").ax().chkTrue { "Abort fixing templates." }
    }
}

@DelicateApi("This needs KGround source code, it's interactive and mostly for myself.")
suspend fun tryDiffMyConflictingTemplatesSrc() {
  val log = localULog()
  val fs = localUFileSys()
  val templates = mutableMapOf<String, String>()
  val templatesSrc = mutableMapOf<String, Path>()
  fs.findAllFiles(PathToKGroundProject).filterExt("kts") // TODO_someday: support future templates in .kt files
    .collectSpecialRegionsTo(templates, templatesSrc) { path, label, content, region -> // onConflict
      val oldPath = fs.canonicalize(templatesSrc[label]!!)
      val newPath = fs.canonicalize(path)
      val warning = "Conflicting   region [[$label]] in files:" // aligned spaces with other logs
      log.w(warning)
      log.w(oldPath)
      log.w(newPath)
      zenityShowWarning("$warning\n$oldPath\n$newPath").ax()
      val question = "Try opening diff in IDE?\nideDiff(\n  \"$oldPath\",\n  \"$newPath\"\n)"
      if (zenityAskIf(question).ax()) ideDiff(oldPath, newPath).ax()
      zenityAskIf("Continue diffing templates? (No -> abort)").ax().chkTrue { "Abort diffing templates." }
    }
}


suspend fun collectMyTemplates(): Map<String, String> {
  val preferred1 = "template-full-lib" // in case of conflict
  val preferred2 = "template-full" // in case of conflict
  val templates = mutableMapOf<String, String>()
  val templatesRes = mutableMapOf<String, Path>()
  val log = localULog()
  val fsres = UFileSys(RESOURCES)
  uctx(fsres) {
    findAllFiles("templates".pth).filterExt("kts.tmpl")
      .collectSpecialRegionsTo(templates, templatesRes) { path, label, content, region -> // onConflict
        val oldPath = fsres.canonicalize(templatesRes[label]!!).strf
        val newPath = fsres.canonicalize(path).strf
        log.w("Conflicting [[$label]] in resources:")
        log.w(oldPath)
        log.w(newPath)
        if ((preferred1 in newPath && preferred1 !in oldPath)||(preferred2 in newPath && preferred2 !in oldPath)) {
          log.w("Overriding with the new one from $path")
          templates[label] = region
          templatesRes[label] = path
        }
        else log.w("Keeping the one from $oldPath")
      }
  }
  return templates
}

// It will NOT collect special regions with tildes; these magic arrow regions differ in each file anyway.
private suspend fun Sequence<Path>.collectSpecialRegionsTo(
  specialRegions: MutableMap<String, String>,
  specialRegionsSrc: MutableMap<String, Path>,
  onConflict: suspend (path: Path, label: String, content: String, region: String) -> Unit =
    { path, label, content, region -> bad { "Different special region labeled $label already found." } },
) = forEachSpecialRegionFound(allowTildes = false) { path, label, content, region ->
  val log = localULog()
  log.d("Found special region [[$label]] in $path (length ${region.length})")
  when (specialRegions[label]) {
    null -> { specialRegions[label] = region; specialRegionsSrc[label] = path }
    region -> log.d("Same  special region [[$label]] in ${specialRegionsSrc[label]}") // aligned spaces with other logs
    else -> onConflict(path, label, content, region)
  }
}


// TODO_maybe: generalize to other regions too?
@OptIn(NotPortableApi::class, DelicateApi::class, ExperimentalApi::class)
/** Note: label in action is the part without surrounding double brackets. */
suspend fun Sequence<Path>.forEachSpecialRegionFound(
  allowTildes: Boolean = true,
  action: suspend (path: Path, label: String, content: String, region: String) -> Unit,
) {
  val log = localULog()
  val fs = localUFileSys()
  forEach { path ->
    log.d("Searching special regions in file $path")
    ureSpecialRegion(
      content = ureWhateva().withName("content"),
      specialLabel = ureAnyRegionLabel(allowTildes = allowTildes, allowBrackets = false).withName("label"),
    ).withName("region")
      .findAll(fs.readUtf8(path))
      .forEach {
        val label by it // without [[]]
        val content by it
        val region by it
        action(path, label, content, region)
      }
  }
}

// Do I even need this?
suspend fun Sequence<Path>.logEachSpecialRegionFound(
  allowTildes: Boolean = true,
  level: ULogLevel = ULogLevel.INFO,
) {
  val log = localULog()
  forEachSpecialRegionFound(allowTildes) { path, label, content, region ->
    log(level, "Found special region [[$label]] in $path (length ${region.length})")
  }
}
