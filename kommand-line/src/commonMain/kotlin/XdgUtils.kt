package pl.mareklangiewicz.kommand

import okio.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.XdgSessionType.*
import pl.mareklangiewicz.udata.*

enum class XdgSessionType { Wayland, X11, TTY }

/** @return Session type based on $XDG_SESSION_TYPE environment variable or null if not recognized. */
@OptIn(NotPortableApi::class)
fun xdgSessionTypeOrNull(): XdgSessionType? = getSysEnv("XDG_SESSION_TYPE")?.let { env ->
  entries.find { it.name.equals(env, ignoreCase = true) }
  // BTW From what I've seen, correct session env values are lowercase, but I ignore case anyway, it's fine.
}

enum class ClipSelection {
  /** Default clipboard. Only one supported by Wayland without extensions. Use this one unless very specific needs. */
  Clipboard,
  /** The "Primary Selection" inherited from X11 (middle-click pasting). In Wayland requires extension protocol. */
  @NotPortableApi("Requires extension in Wayland.")
  Primary,
  @NotPortableApi("Obsolete X11 second selection, not supported in Wayland.")
  @Deprecated("Obsolete X11 second selection, not supported in Wayland.")
  Secondary,
}

// TODO_someday: better clipboards implementation (classes/types/all options)

@OptIn(DelicateApi::class)
fun xclipOut(selection: ClipSelection = ClipSelection.Clipboard) =
  kommand("xclip", "-o", "-selection", selection.name.lowercase())
    .reducedOutToList()

/** Copies stdin content if no files provided. */
@OptIn(DelicateApi::class)
fun xclipIn(selection: ClipSelection = ClipSelection.Clipboard, vararg inFiles: Path) =
  kommand("xclip", "-i", "-selection", selection.name.lowercase(), *inFiles.map { it.toString() }.toTypedArray())
    .reducedOutToList()

// TODO_later: support --primary, --type (mimetype), and more
@OptIn(DelicateApi::class)
fun wlclipOut() = kommand("wl-paste").reducedOutToList()

/** Copies stdin content if no inText provided. */
@OptIn(DelicateApi::class)
fun wlclipIn(inText: String? = null) = AKommand("wl-copy", LONN(inText)).reducedOutToList()

fun clipOut(): ReducedKommand<List<String>> = when(xdgSessionTypeOrNull()) {
  Wayland -> wlclipOut() // BTW assuming wl-paste on Wayland (should throw nice error if not)
  X11 -> xclipOut() // BTW assuming xclip on X11 (should throw nice error if not)
  else -> bad { "Unsupported XDG_SESSION_TYPE" }
}

fun clipIn(): ReducedKommand<List<String>> = when(xdgSessionTypeOrNull()) {
  Wayland -> wlclipIn() // BTW assuming wl-copy on Wayland (should throw nice error if not)
  X11 -> xclipIn() // BTW assuming xclip on X11 (should throw nice error if not)
  else -> bad { "Unsupported XDG_SESSION_TYPE" }
}
