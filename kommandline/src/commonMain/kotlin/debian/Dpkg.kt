@file:Suppress("unused")

package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.kommand.*

@OptIn(DelicateKommandApi::class)
fun searchCommandScript(command: String) =
    ReducedScript { platform, dir ->
        val first = whichFirstOrNull(command).exec(platform, dir) ?: return@ReducedScript null
        dpkg(DpkgAct.Search(first)).exec(platform)
    }

/** There has to be exactly one action in each invocation */
@DelicateKommandApi
fun dpkg(act: DpkgAct, init: Dpkg.() -> Unit = {}) = dpkg { init(); -act }

/** There has to be exactly one option: action (DpkgAct) in each invocation */
@DelicateKommandApi
fun dpkg(init: Dpkg.() -> Unit = {}) = Dpkg().apply(init)


/** [linux man](https://man7.org/linux/man-pages/man1/dpkg.1.html) */
@DelicateKommandApi
data class Dpkg(
    val opts: MutableList<DpkgOpt> = mutableListOf(),
) : Kommand {
    override val name get() = "dpkg"
    override val args get() = opts.flatMap { it.toArgs() }
    operator fun DpkgOpt.unaryMinus() = opts.add(this)
}

@DelicateKommandApi
interface DpkgAct : DpkgOpt {
    data object Help : KOptL("help"), DpkgAct
    data object Version : KOptL("version"), DpkgAct

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
    data class Configure(val pkgName: String? = null) : KOptL("configure", pkgName, nameSeparator = " "), DpkgAct

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

    data object PrintArch : KOptL("print-architecture"), DpkgAct
    data class ListPackages(val pkgNamePattern: String) : KOptS("l", pkgNamePattern), DpkgAct
    data class ListFiles(val pkgName: String) : KOptS("L", pkgName), DpkgAct
    data class Status(val pkgName: String) : KOptS("l", pkgName), DpkgAct

    /**
     * Display details about package-name, as found in /var/lib/dpkg/available.
     * Users of APT-based frontends should use apt show package-name instead.
     */
    data class Details(val pkgName: String) : KOptS("p", pkgName), DpkgAct
    data class Search(val fileSearchPattern: String) : KOptS("S", fileSearchPattern), DpkgAct
}

@DelicateKommandApi
interface DpkgOpt: KOpt {
    data object Recursive : KOptS("R"), DpkgOpt
    data object Pending : KOptL("pending"), DpkgOpt
    data object DryRun : KOptL("dry-run"), DpkgOpt
    data object RefuseDowngrade : KOptL("refuse-downgrade"), DpkgOpt
    data object SkipSameVersion : KOptL("skip-same-version"), DpkgOpt
    /** Use a machine-readable output format. */
    data object Robot : KOptL("robot"), DpkgOpt
    data object NoPager : KOptL("no-pager"), DpkgOpt
}
