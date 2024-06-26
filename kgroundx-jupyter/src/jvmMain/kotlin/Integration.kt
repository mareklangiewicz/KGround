package pl.mareklangiewicz.kgroundx.jupyter

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

internal class Integration : JupyterIntegration() {
  override fun Builder.onLoaded() {
    import("kotlinx.coroutines.*")
    import("kotlinx.coroutines.flow.*")
    import("pl.mareklangiewicz.kground.*")
  }
}

