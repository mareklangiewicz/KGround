@file:Suppress("FunctionName", "unused")

package pl.mareklangiewicz.uwidgets

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.utheme.*
import pl.mareklangiewicz.uwidgets.UAlignmentType.*
import pl.mareklangiewicz.uwidgets.UBinType.*

enum class UBinType { UBOX, UROW, UCOLUMN }

enum class UAlignmentType(val css: String) {
    USTART("start"), UEND("end"), UCENTER("center"), USTRETCH("stretch");

    companion object {
        fun css(css: String) = UAlignmentType.values().first { it.css == css }
    }
}

@Composable fun UBin(
    type: UBinType,
    mod: Mod = Mod,
    selected: Boolean = false, // TODO blaNOW: also mods?
    content: @Composable () -> Unit,
) {
    val childrenMod = LocalUChildrenMod.current
    @Suppress("RemoveRedundantQualifierName") // IDE issue
    UCoreBin(type, if (childrenMod == null) mod else Mod.childrenMod().then(mod)) {
        UDepth { CompositionLocalProvider(LocalUChildrenMod provides null, content = content) }
    }
}

@Composable fun UChildrenMod(
    mod: Mod.() -> Mod,
    content: @Composable () -> Unit,
) {
    val current = LocalUChildrenMod.current
    val new = if (current == null) mod else {
        { current().mod() }
    }
    CompositionLocalProvider(LocalUChildrenMod provides new, content = content)
}
