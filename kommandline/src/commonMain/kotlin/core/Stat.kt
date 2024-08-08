package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class)
fun statFileSizeBytes(fileP: Path) = kommand("stat", "-c%s", fileP.strf).reducedOut { single().toLong() }

// TODO_later: real Kommand for stat
