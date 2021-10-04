package pl.mareklangiewicz.kommand

interface Platform {
    fun exec(kommand: Kommand)
    // TODO: some platform properties (type, is GUI available..)
}