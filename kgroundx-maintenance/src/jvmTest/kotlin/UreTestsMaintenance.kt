package pl.mareklangiewicz.ure

import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import org.junit.jupiter.api.*
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.uspek.*

@OptIn(NotPortableApi::class)
class UreTestsMaintenance {

    @TestFactory
    fun ureTestFactory() = uspekTestFactory {
        testUreMultiplatform()
    }

    private fun testUreMultiplatform() {
        "On UWidgetsCmnKt" o {
            val path = "/home/marek/code/kotlin/UWidgets/uwidgets/src/commonMain/kotlin/uwidgets/UWidgets.cmn.kt".toPath()
            val content = SYSTEM.readUtf8(path)
            val output = content.commentOutMultiplatformFun()
            println(output) // TODO_later: better tests
        }
    }

    @TestFactory
    @Disabled("Has side effects in other project.")
    fun testCommentOutMultiplatformStuff() = uspekTestFactory {
        val dir = "/home/marek/code/kotlin/uspek-painters/lib/src"
        "On dir: $dir" o {
            "comment out multiplatform stuff inside" o {
                SYSTEM.commentOutMultiplatformFunInEachKtFile(dir.toPath())
            }
        }
    }

    @TestFactory
    @Disabled("Has side effects in other project.")
    fun testUndoCommentOutMultiplatformStuff() = uspekTestFactory {
        val dir = "/home/marek/code/kotlin/uspek-painters/lib/src"
        "On dir: $dir" o {
            "undo comment out multiplatform stuff inside" o {
                SYSTEM.undoCommentOutMultiplatformFunInEachKtFile(dir.toPath())
            }
        }
    }
}