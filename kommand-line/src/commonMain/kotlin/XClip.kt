package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi


// TODO_later: real implementation (class/types/all options)

enum class XClipSelection { Primary, Secondary, Clipboard }

@OptIn(DelicateApi::class)
fun xclipOut(selection: XClipSelection = XClipSelection.Primary) =
  kommand("xclip", "-o", "-selection", selection.name.lowercase())
    .reducedOutToList()

@OptIn(DelicateApi::class)
fun xclipIn(selection: XClipSelection = XClipSelection.Primary, vararg inFiles: String) =
  kommand("xclip", "-i", "-selection", selection.name.lowercase(), *inFiles)
    .reducedOutToList()
