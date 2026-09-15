
// region [[Full Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun") apply false
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.KotlinMultiCompose) apply false
  plug(plugs.ComposeJb) apply false // ComposeJb(Edge) is very slow to sync, clean, build (jb dev repo issue)
  plug(plugs.AndroLib) apply false
  plug(plugs.AndroApp) apply false
  plug(plugs.VannikPublish) apply false
}

// endregion [[Full Root Build Imports and Plugs]]

defaultGroupAndVerAndDescription(gradle.extLib)
