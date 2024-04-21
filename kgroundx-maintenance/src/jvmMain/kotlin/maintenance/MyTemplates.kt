package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.FileSystem.Companion.RESOURCES
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.kommand.zenity.zenityShowWarning
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ulog.hack.ulog


// TODO_someday: support for ~~>...<~~...<~~

// It will NOT inject special regions with tildes; these magic arrow regions differ in each file anyway.
suspend fun tryInjectMyTemplatesToProject(
  projectPath: Path,
  collectedTemplates: Map<String, String>? = null,
  askInteractively: Boolean = true,
) {
  val templates = collectedTemplates ?: collectMyTemplates()
  SYSTEM.findAllFiles(projectPath).filterExt("kts") // TODO_someday: support templates in .kt files too
    .forEachSpecialRegionFound(SYSTEM, labelAllowTildes = false) { path, label, content, region ->
      val templateRegion = templates[label] ?: run {
        ulog.i("Found unknown region [$label] in $path (length ${region.length}). Ignoring.")
        return@forEachSpecialRegionFound
      }
      if(region == templateRegion) {
        ulog.i("Found known template [$label] in $path (length ${region.length}). Matching -> Ignoring.")
        return@forEachSpecialRegionFound
      }
      fun inject() {
        ulog.i("Injecting   template [$label] to $path:")
        SYSTEM.injectSpecialRegion(label, templateRegion, path, addIfNotFound = false)
      }
      when {
        !askInteractively -> inject()
        zenityAskIf("Automatically inject template [$label]? to file:\n$path").ax() -> inject()
        zenityAskIf("Try opening diff with [$label] in IDE? (put to tmp.notes) with file:\n$path").ax() -> {
          writeFileWithDD(templateRegion.lines(), SYS.pathToTmpNotes).ax()
          ideDiff(SYS.pathToTmpNotes, path.toString()).ax()
        }
      }
      if (askInteractively)
        zenityAskIf("Continue injecting templates? (No -> abort)").ax().chkTrue { "Abort injecting templates." }
    }
}

@DelicateApi("This needs KGround source code, it's interactive and mostly for myself.")
suspend fun tryDiffMyConflictingTemplatesSrc() {
  val templates = mutableMapOf<String, String>()
  val templatesSrc = mutableMapOf<String, Path>()
  SYSTEM.findAllFiles(PathToKGroundProject).filterExt("kts") // TODO_someday: support future templates in .kt files
    .collectSpecialRegionsTo(templates, templatesSrc, SYSTEM) { path, label, content, region -> // onConflict
      val oldPath = SYSTEM.canonicalize(templatesSrc[label]!!).toString()
      val newPath = SYSTEM.canonicalize(path).toString()
      val warning = "Conflicting   region [$label] in files:" // aligned spaces with other logs
      ulog.w(warning)
      ulog.w(oldPath)
      ulog.w(newPath)
      zenityShowWarning("$warning\n$oldPath\n$newPath").ax()
      val question = "Try opening diff in IDE?\nideDiff(\n  \"$oldPath\",\n  \"$newPath\"\n)"
      if (zenityAskIf(question).ax()) ideDiff(oldPath, newPath).ax()
      zenityAskIf("Continue diffing templates? (No -> abort)").ax().chkTrue { "Abort diffing templates." }
    }
}


suspend fun collectMyTemplates(): Map<String, String> {
  val preferred = "template-mpp" // in case of conflict
  val templates = mutableMapOf<String, String>()
  val templatesRes = mutableMapOf<String, Path>()
  RESOURCES.findAllFiles("templates".toPath()).filterExt("kts.tmpl")
    .collectSpecialRegionsTo(templates, templatesRes, RESOURCES) { path, label, content, region -> // onConflict
      val oldPath = SYSTEM.canonicalize(templatesRes[label]!!).toString()
      val newPath = SYSTEM.canonicalize(path).toString()
      ulog.w("Conflicting [$label] in files:")
      ulog.w(oldPath)
      ulog.w(newPath)
      if (preferred in newPath && preferred !in oldPath) {
        ulog.w("Overriding with the new one from $path")
        templates[label] = region
        templatesRes[label] = path
      }
      else ulog.w("Keeping the one from $oldPath")
    }
  return templates
}

// It will NOT collect special regions with tildes; these magic arrow regions differ in each file anyway.
private suspend fun Sequence<Path>.collectSpecialRegionsTo(
  specialRegions: MutableMap<String, String>,
  specialRegionsSrc: MutableMap<String, Path>,
  fs: FileSystem,
  onConflict: suspend (path: Path, label: String, content: String, region: String) -> Unit =
    { path, label, content, region -> bad { "Different special region labeled $label already found." } },
) = forEachSpecialRegionFound(fs, labelAllowTildes = false) { path, label, content, region ->
  ulog.d("Found special region [$label] in $path (length ${region.length})")
  when (specialRegions[label]) {
    null -> { specialRegions[label] = region; specialRegionsSrc[label] = path }
    region -> ulog.d("Same  special region [$label] in ${specialRegionsSrc[label]}") // aligned spaces with other logs
    else -> onConflict(path, label, content, region)
  }
}


// TODO_maybe: generalize to other regions too?
@OptIn(NotPortableApi::class)
suspend fun Sequence<Path>.forEachSpecialRegionFound(
  fs: FileSystem,
  labelAllowTildes: Boolean = true,
  action: suspend (path: Path, label: String, content: String, region: String) -> Unit = { path, label, content, region ->
    ulog.i("Found special region [$label] in $path (length ${region.length})")
  },
) = forEach { path ->
    ulog.d("Searching special regions in file $path")
    ureAnySpecialRegion(
      contentName = "content",
      labelName = "label",
      labelAllowTildes = labelAllowTildes,
    ).withName("region")
      .findAll(fs.readUtf8(path))
      .forEach {
        val label by it
        val content by it
        val region by it
        action(path, label, content, region)
      }
  }

