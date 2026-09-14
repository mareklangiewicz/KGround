package pl.mareklangiewicz.templatelogic

import pl.mareklangiewicz.deps.*

// region [[Lib Details TMP — sibling model prototype]]

/**
 * TEMPORARY local prototype of the de-nested DepsKt model. See
 * `~/code/kotlin/DepsKt/docs/design/lib-details-denesting.md`.
 *
 * Why here and not in DepsKt: DepsKt is published and consumed (KGround is on 0.4.25), so changing
 * `LibDetails` there is a breaking change to a public model. The design note says the shape must be
 * proven before consumers migrate — so it is prototyped in the consumer, against a working control
 * (four assembling templates), with no version burn. Same convention as [AndroSdkCompileMinorTMP].
 *
 * The change is NOT flattening. The five types stay; they stop being fields of each other, so each
 * can be supplied independently as its own context parameter, and the nullable ones
 * ([LibComposeSettingsTMP], [LibAndroSettingsTMP]) encode presence as *scope* instead of as `null`.
 */

/** Identity and coordinates. Everything that was NOT `settings` in the nested [LibDetails]. */
data class LibDetailsTMP(
  val name: String,
  val group: String,
  val description: String,
  val authorId: String,
  val authorName: String,
  val authorEmail: String,
  val githubUrl: String,
  val licenceName: String,
  val licenceUrl: String,
  val version: Ver,
  val namespace: String = "$group.$name".lowercase(),
  val appId: String = "$namespace.app",
  val appMainPackage: String = namespace,
  val appMainClass: String = "App_jvmKt",
  val appMainFun: String = "main",
  val appVerCode: Int = version.code,
  val appVerName: String = version.str,
) {
  fun withVer(version: Ver) = copy(version = version)
}

/**
 * Platform/testing flags only. Note what is GONE compared to [LibSettings]: `compose`, `andro` and
 * `repos` fields, and with them `withCompose`/`withAndro`. Presence is answered by whether a scope
 * is open, not by a property on this object.
 */
data class LibSettingsTMP(
  val withJvm: Boolean = true,
  val withJvmVer: String? = Vers.JvmDefaultVer.takeIf { withJvm },
  val withJs: Boolean = true,
  val withLinuxX64: Boolean = false,
  val withKotlinxHtml: Boolean = false,
  val withTestJUnit5: Boolean = withJvm,
  val withTestJUnit4: Boolean = false,
  /** Needed because JUnit5 is STILL not supported for android on device tests.. */
  val withTestJUnit4OnAndroidDevice: Boolean = false,
  val withTestUSpekX: Boolean = true,
  val withTestGoogleTruth: Boolean = false,
  val withTestMockitoKotlin: Boolean = false,
  val withCentralPublish: Boolean = false,
)

/** Same fields as [LibComposeSettings]; no longer a field of anything. */
data class LibComposeSettingsTMP(
  val withComposeUi: Boolean = true,
  val withComposeFoundation: Boolean = true,
  val withComposeMaterial2: Boolean = true,
  val withComposeMaterial3: Boolean = true,
  val withComposeMaterialIconsExtended: Boolean = false,
  val withComposeFullAnimation: Boolean = true,
  val withComposeDesktop: Boolean = true,
  val withComposeDesktopComponents: Boolean = false,
  val withComposeHtmlCore: Boolean = false,
  val withComposeHtmlSvg: Boolean = false,
  val withComposeTestUi: Boolean = false,
  val withComposeTestUiJUnit4: Boolean = false,
  val withComposeTestUiJUnit5: Boolean = false,
  val withComposeTestHtmlUtils: Boolean = false,
)

/** Same fields as [LibAndroSettings], plus the minor compile level that [AndroSdkCompileMinorTMP] stands in for. */
data class LibAndroSettingsTMP(
  val sdkCompilePreview: String? = null,
  val sdkCompile: Int = Vers.AndroSdkCompile,
  /** The field DepsKt lacks today; [AndroSdkCompileMinorTMP] exists only because there is nowhere to put this. */
  val sdkCompileMinor: Int = AndroSdkCompileMinorTMP,
  val sdkTargetPreview: String? = null,
  val sdkTarget: Int = Vers.AndroSdkTarget,
  val sdkMin: Int = Vers.AndroSdkMin,
  val withAppCompat: Boolean = true,
  val withLifecycle: Boolean = true,
  val withActivityCompose: Boolean = true,
  val withMDC: Boolean = false,
  val withTestEspresso: Boolean = true,
  val withTestRunner: String? = Vers.AndroTestRunner,
  val publishVariant: String = "",
) {
  val publishAllVariants get() = publishVariant == AllVariants
  val publishNoVariants get() = publishVariant == NoVariants

  /** Note: [LibAndroSettings.publishOneVariant] has a typo — it ands `publishNoVariants` with itself. Fixed here. */
  val publishOneVariant get() = !publishNoVariants && !publishAllVariants
  val AllVariants get() = "*"
  val NoVariants get() = ""
}

/** Same fields as [LibReposSettings]; no longer derived from [LibSettings.withKotlinxHtml]. */
data class LibReposSettingsTMP(
  @Suppress("DEPRECATION")
  val withMavenLocal: Boolean = false,
  val withMavenCentral: Boolean = true,
  val withGradle: Boolean = false,
  val withGoogle: Boolean = true,
  val withKotlinx: Boolean = true,
  val withKotlinxHtml: Boolean = false,
  val withComposeJbDev: Boolean = false,
  val withKtorEap: Boolean = false,
  val withJitpack: Boolean = false,
)

// endregion [[Lib Details TMP — sibling model prototype]]
