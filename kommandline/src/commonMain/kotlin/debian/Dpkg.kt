@file:Suppress("unused")

package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.kommand.*

fun CliPlatform.dpkgSearchOneCommandExec(command: String) =
    whichOneExec(command)?.let { dpkg(DpkgAct.Search(it)).exec() }

/** There has to be exactly one action in each invocation */
fun dpkg(act: DpkgAct, init: Dpkg.() -> Unit = {}) = dpkg { init(); -act }

/** There has to be exactly one option: action (DpkgAct) in each invocation */
fun dpkg(init: Dpkg.() -> Unit = {}) = Dpkg().apply(init)


/** [linux man](https://man7.org/linux/man-pages/man1/dpkg.1.html) */
data class Dpkg(
    val opts: MutableList<DpkgOpt> = mutableListOf(),
) : Kommand {
    override val name get() = "dpkg"
    override val args get() = opts.flatMap { it.args }
    operator fun DpkgOpt.unaryMinus() = opts.add(this)
}

interface DpkgAct : DpkgOpt {
    object Help : KOptL("help"), DpkgAct
    object Version : KOptL("version"), DpkgAct

    /**
     * Install the package.
     * @param debFileOrDir Single .deb file, or directory when the Recursive option enabled
     */
    data class Install(val debFileOrDir: String) : KOptS("i", debFileOrDir), DpkgAct

    /**
     * Unpack the package, but don't configure it.
     * @param debFileOrDir Single .deb file, or directory when the Recursive option enabled.
     */
    data class Unpack(val debFileOrDir: String) : KOptL("unpack", debFileOrDir), DpkgAct

    /**
     * Configure a package which has been unpacked but not yet configured.
     * @param pkgName Single package, or null if Pending option enabled.
     */
    data class Configure(val pkgName: String? = null) : KOptL("configure", separator = " "), DpkgAct

    /**
     * Remove an installed package.
     * @param pkgName Single package, or null if Pending option enabled.
     */
    data class Remove(val pkgName: String? = null) : KOptS("r", pkgName), DpkgAct

    /**
     * Purge an installed or already removed package.
     * @param pkgName Single package, or null if Pending option enabled.
     */
    data class Purge(val pkgName: String? = null) : KOptS("P", pkgName), DpkgAct

    /**
     * Verifies the integrity of package-name or all packages if omitted.
     * @param pkgName Single package, or null to verify all packages.
     */
    data class Verify(val pkgName: String? = null) : KOptS("V", pkgName), DpkgAct

    object PrintArch : KOptL("print-architecture"), DpkgAct
    data class ListPackages(val pkgNamePattern: String) : KOptS("l", pkgNamePattern), DpkgAct
    data class ListFiles(val pkgName: String) : KOptS("L", pkgName), DpkgAct
    data class Status(val pkgName: String) : KOptS("l", pkgName), DpkgAct
    data class Details(val pkgName: String) : KOptS("p", pkgName), DpkgAct
    data class Search(val fileSearchPattern: String) : KOptS("S", fileSearchPattern), DpkgAct
}

interface DpkgOpt: KOpt {
    object Recursive : KOptS("R"), DpkgOpt
    object Pending : KOptL("pending"), DpkgOpt
    object DryRun : KOptL("dry-run"), DpkgOpt
    object RefuseDowngrade : KOptL("refuse-downgrade"), DpkgOpt
    object SkipSameVersion : KOptL("skip-same-version"), DpkgOpt
    /** Use a machine-readable output format. */
    object Robot : KOptL("robot"), DpkgOpt
    object NoPager : KOptL("no-pager"), DpkgOpt
}
