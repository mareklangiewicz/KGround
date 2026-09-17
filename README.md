# KGround

Kotlin Common Ground

Common code that should be useful for many kotlin projects.

Mainly for MPP.

Modules starting with: kgroundx have less common stuff (more opinionated / dirty / experimental).
When in doubt, try cleaner ones first: kground and kground-io.

Also now merged with kommand-line (+ kommand-samples): Kotlin DSL for popular CLI commands.

Also now merged with abcdk (tiny unions lib) and tuplek (tiny tuples lib with cool infix syntax),
folded in from their own repos. They keep their own artifact ids, so
`pl.mareklangiewicz:abcdk` and `pl.mareklangiewicz:tuplek` still work - but they now ride KGround's
version, so they jump from the 0.0.x line straight to KGround's current one. `kground` api-exposes
both, so depending on `kground` alone is enough.
