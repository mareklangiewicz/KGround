@file:Suppress("UNUSED")

import java.io.File
import java.nio.file.Files

val ROOT = File("/home/marek/code/kotlin/KGround")
val TEMPLATE_RAW = ROOT.resolve("template-raw")

val SOURCE_PRIORITY = listOf(
    "template-raw-lib",
    "template-raw-app",
    "template-raw-andro-app",
    "template-raw-jvm-cli-app",
    ""
)

fun main() {
    val parsed = parseArgs(args)
    val dryRun = parsed["dryRun"] as Boolean
    val apply = parsed["apply"] as Boolean
    val includeMain = parsed["includeMain"] as Boolean
    val listRegions = parsed["listRegions"] as Boolean
    val syncAll = parsed["syncAll"] as Boolean
    val backup = parsed["backup"] as Boolean
    @Suppress("UNCHECKED_CAST")
    val regionsToSync = parsed["regions"] as List<String>

    if (dryRun && !apply) {
        println("Dry run mode - no changes will be written\n")
    }

    if (listRegions) {
        listAllRegions()
        return
    }

    if (regionsToSync.isEmpty() && !syncAll) {
        println("No regions specified. Use --all to sync all regions, or specify region names.")
        println("Use --list-regions to see available regions.")
        return
    }

    val allRegions = findAllRegionsInTemplateRaw()

    val regionsToProcess = if (syncAll) {
        allRegions.keys.toList()
    } else {
        regionsToSync.toList()
    }

    val unknownRegions = regionsToProcess.filter { it !in allRegions.keys }
    if (unknownRegions.isNotEmpty()) {
        println("Unknown regions: ${unknownRegions.joinToString(", ")}")
        println("Use --list-regions to see available regions.")
        return
    }

    for (regionName in regionsToProcess) {
        syncRegion(regionName, allRegions[regionName]!!, dryRun, apply, includeMain, backup)
    }

    println("\nDone.")
}

fun parseArgs(args: Array<String>): Map<String, Any> {
    var dry = true
    var app = false
    var incMain = false
    var list = false
    var all = false
    var bak = false
    val regions = mutableListOf<String>()
    
    for (a in args) {
        when (a) {
            "--dry-run" -> dry = true
            "--apply" -> { dry = false; app = true }
            "--include-main" -> incMain = true
            "--list-regions" -> list = true
            "--all" -> all = true
            "--backup" -> bak = true
            else -> regions.add(a)
        }
    }
    
    return mapOf(
        "dryRun" to dry,
        "apply" to app,
        "includeMain" to incMain,
        "listRegions" to list,
        "syncAll" to all,
        "backup" to bak,
        "regions" to regions.toList()
    )
}

fun listAllRegions() {
    val allRegions = findAllRegionsInTemplateRaw()
    println("Available regions in template-raw/:\n")
    for ((name, sources) in allRegions) {
        val sourcePaths = sources.joinToString(", ") { it.relativeTo(ROOT).path }
        println("  * $name")
        println("    Source: $sourcePaths")
        val targets = findTargetFiles(name)
        if (targets.isNotEmpty()) {
            println("    Targets: ${targets.size} file(s)")
        } else {
            println("    Targets: (none - unique region)")
        }
        println()
    }
}

fun findAllRegionsInTemplateRaw(): Map<String, List<File>> {
    val result = mutableMapOf<String, MutableList<File>>()
    val gradleFiles = TEMPLATE_RAW.walkTopDown()
        .filter { it.isFile && (it.name.endsWith(".gradle.kts") || it.name == "settings.gradle.kts") }
    
    for (file in gradleFiles) {
        val regions = extractRegionNames(file)
        for (regionName in regions) {
            result.getOrPut(regionName) { mutableListOf() }.add(file)
        }
    }
    return result
}

fun extractRegionNames(file: File): List<String> {
    val regionStart = Regex("""// region \[\[(.+?)\]\]""")
    return regionStart.findAll(file.readText())
        .map { it.groupValues[1] }
        .toList()
}

fun syncRegion(
    regionName: String, 
    sourceFiles: List<File>,
    dryRun: Boolean,
    apply: Boolean,
    includeMain: Boolean,
    backup: Boolean
) {
    println("=== Syncing \"$regionName\" ===")
    
    val sourceFile = selectSourceFile(sourceFiles)
    if (sourceFile == null) {
        println("  No source file found")
        return
    }
    
    val sourceContent = extractRegionContent(sourceFile, regionName)
    if (sourceContent == null) {
        println("  Could not extract region from source: ${sourceFile.relativeTo(ROOT)}")
        return
    }
    
    val targets = findTargetFiles(regionName)
    if (!includeMain) {
        targets.removeIf { it.path.startsWith("kground") && !it.path.contains("template-") }
    }
    
    println("  Source: ${sourceFile.relativeTo(ROOT)}")
    if (targets.isEmpty()) {
        println("  Targets: (none)")
        return
    }
    println("  Targets: ${targets.size} file(s)\n")
    
    for (target in targets) {
        syncRegionInFile(target, regionName, sourceContent, dryRun, apply, backup)
    }
}

fun selectSourceFile(sourceFiles: List<File>): File? {
    for (priority in SOURCE_PRIORITY) {
        val matching = sourceFiles.find { 
            if (priority.isEmpty()) {
                it.parentFile == TEMPLATE_RAW
            } else {
                it.path.contains("/$priority/")
            }
        }
        if (matching != null) return matching
    }
    return sourceFiles.firstOrNull()
}

fun extractRegionContent(file: File, regionName: String): String? {
    val text = file.readText()
    val startPattern = Regex("""(?:// region \[\[$regionName\]\])""")
    val endPattern = Regex("""(?:// endregion \[\[$regionName\])""")
    
    val startMatch = startPattern.find(text) ?: return null
    val startEnd = startMatch.range.last + 1
    val afterStart = text.indexOf('\n', startEnd).let { if (it == -1) startEnd else it + 1 }
    
    val endMatch = endPattern.find(text, afterStart) ?: return null
    val beforeEnd = text.lastIndexOf('\n', endMatch.range.first - 1).let { if (it == -1) 0 else it + 1 }
    
    return text.substring(afterStart, beforeEnd)
}

fun findTargetFiles(regionName: String): MutableList<File> {
    val pattern = Regex("""// region \[\[$regionName\]\]""")
    val targets = mutableListOf<File>()
    
    for (file in ROOT.walkTopDown().filter { it.isFile && (it.name.endsWith(".gradle.kts") || it.name == "settings.gradle.kts") }) {
        if (file.path.contains("/template-raw/")) continue
        if (pattern.containsMatchIn(file.readText())) {
            targets.add(file)
        }
    }
    
    return targets
}

fun syncRegionInFile(
    target: File, 
    regionName: String, 
    newContent: String,
    dryRun: Boolean,
    apply: Boolean,
    backup: Boolean
) {
    val text = target.readText()
    val startPattern = Regex("""(?:// region \[\[$regionName\]\])""")
    val endPattern = Regex("""(?:// endregion \[\[$regionName\])""")
    
    val startMatch = startPattern.find(text) ?: run {
        println("  Could not find region start in: ${target.relativeTo(ROOT)}")
        return
    }
    val startEnd = startMatch.range.last + 1
    val afterStart = text.indexOf('\n', startEnd).let { if (it == -1) startEnd else it + 1 }
    
    val endMatch = endPattern.find(text, afterStart) ?: run {
        println("  Could not find region end in: ${target.relativeTo(ROOT)}")
        return
    }
    val beforeEnd = text.lastIndexOf('\n', endMatch.range.first - 1).let { if (it == -1) 0 else it + 1 }
    
    val newText = text.substring(0, afterStart) + newContent + "\n" + text.substring(beforeEnd)
    
    if (dryRun) {
        val relPath = target.relativeTo(ROOT)
        val diffLines = computeUnifiedDiff(text, newText, relPath.path)
        for (line in diffLines) {
            println(line)
        }
    } else if (apply) {
        if (backup) {
            val bakFile = File("$target.bak")
            Files.copy(target.toPath(), bakFile.toPath())
        }
        target.writeText(newText)
        println("  Updated: ${target.relativeTo(ROOT)}")
    }
}

fun computeUnifiedDiff(oldText: String, newText: String, path: String): List<String> {
    val oldLines = oldText.lines()
    val newLines = newText.lines()
    
    val diff = mutableListOf<String>()
    diff.add("  ~ $path")
    
    var line = 0
    val maxLines = maxOf(oldLines.size, newLines.size)
    var hasChanges = false
    var changesShown = 0
    val maxChangesToShow = 10
    
    while (line < maxLines && changesShown < maxChangesToShow) {
        val oldLine = oldLines.getOrNull(line)
        val newLine = newLines.getOrNull(line)
        
        if (oldLine != newLine) {
            if (!hasChanges) {
                diff.add("    --- before")
                diff.add("    +++ after")
                hasChanges = true
            }
            if (oldLine != null) diff.add("    - ${oldLine.take(120)}")
            if (newLine != null) diff.add("    + ${newLine.take(120)}")
            changesShown++
        }
        
        line++
    }
    
    if (hasChanges && line < maxLines) {
        val remaining = (oldLines.size - line) + (newLines.size - line)
        if (remaining > 0) {
            diff.add("    ... ($remaining more lines differ)")
        }
    }
    
    if (!hasChanges) {
        diff.add("    (content changed)")
    }
    
    return diff
}

main()
