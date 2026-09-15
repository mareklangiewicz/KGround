// region [[Basic Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun") apply false
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
}

// endregion [[Basic Root Build Imports and Plugs]]

defaultGroupAndVerAndDescription(gradle.extLib)
