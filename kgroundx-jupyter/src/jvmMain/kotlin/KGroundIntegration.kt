package pl.mareklangiewicz.kground

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

internal class KGroundIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
        import("kotlinx.coroutines.*")
        import("kotlinx.coroutines.flow.*")
        import("pl.mareklangiewicz.kground.*")
    }
}

