// This file was generated using action-binding-generator. Don't change it by hand, otherwise your
// changes will be overwritten with the next binding code regeneration.
// See https://github.com/typesafegithub/github-workflows-kt for more info.
@file:Suppress(
    "DataClassPrivateConstructor",
    "UNUSED_PARAMETER",
)

package io.github.typesafegithub.workflows.actions.actions

import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.actions.RegularAction
import java.util.LinkedHashMap
import kotlin.Boolean
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: Setup Java JDK
 *
 * Set up a specific version of the Java JDK and add the command-line tools to the PATH
 *
 * [Action on GitHub](https://github.com/actions/setup-java)
 *
 * @param javaVersion The Java version to set up. Takes a whole or semver Java version. See examples
 * of supported syntax in README file
 * @param javaVersion_Untyped The Java version to set up. Takes a whole or semver Java version. See
 * examples of supported syntax in README file
 * @param javaVersionFile_Untyped The path to the `.java-version` file. See examples of supported
 * syntax in README file
 * @param distribution &lt;required&gt; Java distribution. See the list of supported distributions
 * in README file
 * @param distribution_Untyped &lt;required&gt; Java distribution. See the list of supported
 * distributions in README file
 * @param javaPackage The package type (jdk, jre, jdk+fx, jre+fx)
 * @param javaPackage_Untyped The package type (jdk, jre, jdk+fx, jre+fx)
 * @param architecture The architecture of the package (defaults to the action runner's
 * architecture)
 * @param architecture_Untyped The architecture of the package (defaults to the action runner's
 * architecture)
 * @param jdkFile Path to where the compressed JDK is located
 * @param jdkFile_Untyped Path to where the compressed JDK is located
 * @param checkLatest Set this option if you want the action to check for the latest available
 * version that satisfies the version spec
 * @param checkLatest_Untyped Set this option if you want the action to check for the latest
 * available version that satisfies the version spec
 * @param serverId ID of the distributionManagement repository in the pom.xml file. Default is
 * `github`
 * @param serverId_Untyped ID of the distributionManagement repository in the pom.xml file. Default
 * is `github`
 * @param serverUsername Environment variable name for the username for authentication to the Apache
 * Maven repository. Default is $GITHUB_ACTOR
 * @param serverUsername_Untyped Environment variable name for the username for authentication to
 * the Apache Maven repository. Default is $GITHUB_ACTOR
 * @param serverPassword Environment variable name for password or token for authentication to the
 * Apache Maven repository. Default is $GITHUB_TOKEN
 * @param serverPassword_Untyped Environment variable name for password or token for authentication
 * to the Apache Maven repository. Default is $GITHUB_TOKEN
 * @param settingsPath Path to where the settings.xml file will be written. Default is ~/.m2.
 * @param settingsPath_Untyped Path to where the settings.xml file will be written. Default is
 * ~/.m2.
 * @param overwriteSettings Overwrite the settings.xml file if it exists. Default is "true".
 * @param overwriteSettings_Untyped Overwrite the settings.xml file if it exists. Default is "true".
 * @param gpgPrivateKey GPG private key to import. Default is empty string.
 * @param gpgPrivateKey_Untyped GPG private key to import. Default is empty string.
 * @param gpgPassphrase Environment variable name for the GPG private key passphrase. Default is
 * $GPG_PASSPHRASE.
 * @param gpgPassphrase_Untyped Environment variable name for the GPG private key passphrase.
 * Default is $GPG_PASSPHRASE.
 * @param cache Name of the build platform to cache dependencies. It can be "maven", "gradle" or
 * "sbt".
 * @param cache_Untyped Name of the build platform to cache dependencies. It can be "maven",
 * "gradle" or "sbt".
 * @param cacheDependencyPath The path to a dependency file: pom.xml, build.gradle, build.sbt, etc.
 * This option can be used with the `cache` option. If this option is omitted, the action searches for
 * the dependency file in the entire repository. This option supports wildcards and a list of file
 * names for caching multiple dependencies.
 * @param cacheDependencyPath_Untyped The path to a dependency file: pom.xml, build.gradle,
 * build.sbt, etc. This option can be used with the `cache` option. If this option is omitted, the
 * action searches for the dependency file in the entire repository. This option supports wildcards and
 * a list of file names for caching multiple dependencies.
 * @param jobStatus Workaround to pass job status to post job step. This variable is not intended
 * for manual setting
 * @param jobStatus_Untyped Workaround to pass job status to post job step. This variable is not
 * intended for manual setting
 * @param token The token used to authenticate when fetching version manifests hosted on github.com,
 * such as for the Microsoft Build of OpenJDK. When running this action on github.com, the default
 * value is sufficient. When running on GHES, you can pass a personal access token for github.com if
 * you are experiencing rate limiting.
 * @param token_Untyped The token used to authenticate when fetching version manifests hosted on
 * github.com, such as for the Microsoft Build of OpenJDK. When running this action on github.com, the
 * default value is sufficient. When running on GHES, you can pass a personal access token for
 * github.com if you are experiencing rate limiting.
 * @param mvnToolchainId Name of Maven Toolchain ID if the default name of
 * "${distribution}_${java-version}" is not wanted. See examples of supported syntax in Advanced Usage
 * file
 * @param mvnToolchainId_Untyped Name of Maven Toolchain ID if the default name of
 * "${distribution}_${java-version}" is not wanted. See examples of supported syntax in Advanced Usage
 * file
 * @param mvnToolchainVendor Name of Maven Toolchain Vendor if the default name of "${distribution}"
 * is not wanted. See examples of supported syntax in Advanced Usage file
 * @param mvnToolchainVendor_Untyped Name of Maven Toolchain Vendor if the default name of
 * "${distribution}" is not wanted. See examples of supported syntax in Advanced Usage file
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@ExposedCopyVisibility
public data class SetupJava private constructor(
    /**
     * The Java version to set up. Takes a whole or semver Java version. See examples of supported
     * syntax in README file
     */
    public val javaVersion: String? = null,
    /**
     * The Java version to set up. Takes a whole or semver Java version. See examples of supported
     * syntax in README file
     */
    public val javaVersion_Untyped: String? = null,
    /**
     * The path to the `.java-version` file. See examples of supported syntax in README file
     */
    public val javaVersionFile_Untyped: String? = null,
    /**
     * &lt;required&gt; Java distribution. See the list of supported distributions in README file
     */
    public val distribution: SetupJava.Distribution? = null,
    /**
     * &lt;required&gt; Java distribution. See the list of supported distributions in README file
     */
    public val distribution_Untyped: String? = null,
    /**
     * The package type (jdk, jre, jdk+fx, jre+fx)
     */
    public val javaPackage: SetupJava.JavaPackage? = null,
    /**
     * The package type (jdk, jre, jdk+fx, jre+fx)
     */
    public val javaPackage_Untyped: String? = null,
    /**
     * The architecture of the package (defaults to the action runner's architecture)
     */
    public val architecture: String? = null,
    /**
     * The architecture of the package (defaults to the action runner's architecture)
     */
    public val architecture_Untyped: String? = null,
    /**
     * Path to where the compressed JDK is located
     */
    public val jdkFile: String? = null,
    /**
     * Path to where the compressed JDK is located
     */
    public val jdkFile_Untyped: String? = null,
    /**
     * Set this option if you want the action to check for the latest available version that
     * satisfies the version spec
     */
    public val checkLatest: Boolean? = null,
    /**
     * Set this option if you want the action to check for the latest available version that
     * satisfies the version spec
     */
    public val checkLatest_Untyped: String? = null,
    /**
     * ID of the distributionManagement repository in the pom.xml file. Default is `github`
     */
    public val serverId: String? = null,
    /**
     * ID of the distributionManagement repository in the pom.xml file. Default is `github`
     */
    public val serverId_Untyped: String? = null,
    /**
     * Environment variable name for the username for authentication to the Apache Maven repository.
     * Default is $GITHUB_ACTOR
     */
    public val serverUsername: String? = null,
    /**
     * Environment variable name for the username for authentication to the Apache Maven repository.
     * Default is $GITHUB_ACTOR
     */
    public val serverUsername_Untyped: String? = null,
    /**
     * Environment variable name for password or token for authentication to the Apache Maven
     * repository. Default is $GITHUB_TOKEN
     */
    public val serverPassword: String? = null,
    /**
     * Environment variable name for password or token for authentication to the Apache Maven
     * repository. Default is $GITHUB_TOKEN
     */
    public val serverPassword_Untyped: String? = null,
    /**
     * Path to where the settings.xml file will be written. Default is ~/.m2.
     */
    public val settingsPath: String? = null,
    /**
     * Path to where the settings.xml file will be written. Default is ~/.m2.
     */
    public val settingsPath_Untyped: String? = null,
    /**
     * Overwrite the settings.xml file if it exists. Default is "true".
     */
    public val overwriteSettings: Boolean? = null,
    /**
     * Overwrite the settings.xml file if it exists. Default is "true".
     */
    public val overwriteSettings_Untyped: String? = null,
    /**
     * GPG private key to import. Default is empty string.
     */
    public val gpgPrivateKey: String? = null,
    /**
     * GPG private key to import. Default is empty string.
     */
    public val gpgPrivateKey_Untyped: String? = null,
    /**
     * Environment variable name for the GPG private key passphrase. Default is $GPG_PASSPHRASE.
     */
    public val gpgPassphrase: String? = null,
    /**
     * Environment variable name for the GPG private key passphrase. Default is $GPG_PASSPHRASE.
     */
    public val gpgPassphrase_Untyped: String? = null,
    /**
     * Name of the build platform to cache dependencies. It can be "maven", "gradle" or "sbt".
     */
    public val cache: SetupJava.BuildPlatform? = null,
    /**
     * Name of the build platform to cache dependencies. It can be "maven", "gradle" or "sbt".
     */
    public val cache_Untyped: String? = null,
    /**
     * The path to a dependency file: pom.xml, build.gradle, build.sbt, etc. This option can be used
     * with the `cache` option. If this option is omitted, the action searches for the dependency file
     * in the entire repository. This option supports wildcards and a list of file names for caching
     * multiple dependencies.
     */
    public val cacheDependencyPath: String? = null,
    /**
     * The path to a dependency file: pom.xml, build.gradle, build.sbt, etc. This option can be used
     * with the `cache` option. If this option is omitted, the action searches for the dependency file
     * in the entire repository. This option supports wildcards and a list of file names for caching
     * multiple dependencies.
     */
    public val cacheDependencyPath_Untyped: String? = null,
    /**
     * Workaround to pass job status to post job step. This variable is not intended for manual
     * setting
     */
    public val jobStatus: String? = null,
    /**
     * Workaround to pass job status to post job step. This variable is not intended for manual
     * setting
     */
    public val jobStatus_Untyped: String? = null,
    /**
     * The token used to authenticate when fetching version manifests hosted on github.com, such as
     * for the Microsoft Build of OpenJDK. When running this action on github.com, the default value is
     * sufficient. When running on GHES, you can pass a personal access token for github.com if you are
     * experiencing rate limiting.
     */
    public val token: String? = null,
    /**
     * The token used to authenticate when fetching version manifests hosted on github.com, such as
     * for the Microsoft Build of OpenJDK. When running this action on github.com, the default value is
     * sufficient. When running on GHES, you can pass a personal access token for github.com if you are
     * experiencing rate limiting.
     */
    public val token_Untyped: String? = null,
    /**
     * Name of Maven Toolchain ID if the default name of "${distribution}_${java-version}" is not
     * wanted. See examples of supported syntax in Advanced Usage file
     */
    public val mvnToolchainId: String? = null,
    /**
     * Name of Maven Toolchain ID if the default name of "${distribution}_${java-version}" is not
     * wanted. See examples of supported syntax in Advanced Usage file
     */
    public val mvnToolchainId_Untyped: String? = null,
    /**
     * Name of Maven Toolchain Vendor if the default name of "${distribution}" is not wanted. See
     * examples of supported syntax in Advanced Usage file
     */
    public val mvnToolchainVendor: String? = null,
    /**
     * Name of Maven Toolchain Vendor if the default name of "${distribution}" is not wanted. See
     * examples of supported syntax in Advanced Usage file
     */
    public val mvnToolchainVendor_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<SetupJava.Outputs>("actions", "setup-java", _customVersion ?: "v4") {
    init {
        require(!((javaVersion != null) && (javaVersion_Untyped != null))) {
            "Only javaVersion or javaVersion_Untyped must be set, but not both"
        }

        require(!((distribution != null) && (distribution_Untyped != null))) {
            "Only distribution or distribution_Untyped must be set, but not both"
        }
        require((distribution != null) || (distribution_Untyped != null)) {
            "Either distribution or distribution_Untyped must be set, one of them is required"
        }

        require(!((javaPackage != null) && (javaPackage_Untyped != null))) {
            "Only javaPackage or javaPackage_Untyped must be set, but not both"
        }

        require(!((architecture != null) && (architecture_Untyped != null))) {
            "Only architecture or architecture_Untyped must be set, but not both"
        }

        require(!((jdkFile != null) && (jdkFile_Untyped != null))) {
            "Only jdkFile or jdkFile_Untyped must be set, but not both"
        }

        require(!((checkLatest != null) && (checkLatest_Untyped != null))) {
            "Only checkLatest or checkLatest_Untyped must be set, but not both"
        }

        require(!((serverId != null) && (serverId_Untyped != null))) {
            "Only serverId or serverId_Untyped must be set, but not both"
        }

        require(!((serverUsername != null) && (serverUsername_Untyped != null))) {
            "Only serverUsername or serverUsername_Untyped must be set, but not both"
        }

        require(!((serverPassword != null) && (serverPassword_Untyped != null))) {
            "Only serverPassword or serverPassword_Untyped must be set, but not both"
        }

        require(!((settingsPath != null) && (settingsPath_Untyped != null))) {
            "Only settingsPath or settingsPath_Untyped must be set, but not both"
        }

        require(!((overwriteSettings != null) && (overwriteSettings_Untyped != null))) {
            "Only overwriteSettings or overwriteSettings_Untyped must be set, but not both"
        }

        require(!((gpgPrivateKey != null) && (gpgPrivateKey_Untyped != null))) {
            "Only gpgPrivateKey or gpgPrivateKey_Untyped must be set, but not both"
        }

        require(!((gpgPassphrase != null) && (gpgPassphrase_Untyped != null))) {
            "Only gpgPassphrase or gpgPassphrase_Untyped must be set, but not both"
        }

        require(!((cache != null) && (cache_Untyped != null))) {
            "Only cache or cache_Untyped must be set, but not both"
        }

        require(!((cacheDependencyPath != null) && (cacheDependencyPath_Untyped != null))) {
            "Only cacheDependencyPath or cacheDependencyPath_Untyped must be set, but not both"
        }

        require(!((jobStatus != null) && (jobStatus_Untyped != null))) {
            "Only jobStatus or jobStatus_Untyped must be set, but not both"
        }

        require(!((token != null) && (token_Untyped != null))) {
            "Only token or token_Untyped must be set, but not both"
        }

        require(!((mvnToolchainId != null) && (mvnToolchainId_Untyped != null))) {
            "Only mvnToolchainId or mvnToolchainId_Untyped must be set, but not both"
        }

        require(!((mvnToolchainVendor != null) && (mvnToolchainVendor_Untyped != null))) {
            "Only mvnToolchainVendor or mvnToolchainVendor_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        javaVersion: String? = null,
        javaVersion_Untyped: String? = null,
        javaVersionFile_Untyped: String? = null,
        distribution: SetupJava.Distribution? = null,
        distribution_Untyped: String? = null,
        javaPackage: SetupJava.JavaPackage? = null,
        javaPackage_Untyped: String? = null,
        architecture: String? = null,
        architecture_Untyped: String? = null,
        jdkFile: String? = null,
        jdkFile_Untyped: String? = null,
        checkLatest: Boolean? = null,
        checkLatest_Untyped: String? = null,
        serverId: String? = null,
        serverId_Untyped: String? = null,
        serverUsername: String? = null,
        serverUsername_Untyped: String? = null,
        serverPassword: String? = null,
        serverPassword_Untyped: String? = null,
        settingsPath: String? = null,
        settingsPath_Untyped: String? = null,
        overwriteSettings: Boolean? = null,
        overwriteSettings_Untyped: String? = null,
        gpgPrivateKey: String? = null,
        gpgPrivateKey_Untyped: String? = null,
        gpgPassphrase: String? = null,
        gpgPassphrase_Untyped: String? = null,
        cache: SetupJava.BuildPlatform? = null,
        cache_Untyped: String? = null,
        cacheDependencyPath: String? = null,
        cacheDependencyPath_Untyped: String? = null,
        jobStatus: String? = null,
        jobStatus_Untyped: String? = null,
        token: String? = null,
        token_Untyped: String? = null,
        mvnToolchainId: String? = null,
        mvnToolchainId_Untyped: String? = null,
        mvnToolchainVendor: String? = null,
        mvnToolchainVendor_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(javaVersion = javaVersion, javaVersion_Untyped = javaVersion_Untyped,
            javaVersionFile_Untyped = javaVersionFile_Untyped, distribution = distribution,
            distribution_Untyped = distribution_Untyped, javaPackage = javaPackage,
            javaPackage_Untyped = javaPackage_Untyped, architecture = architecture,
            architecture_Untyped = architecture_Untyped, jdkFile = jdkFile, jdkFile_Untyped =
            jdkFile_Untyped, checkLatest = checkLatest, checkLatest_Untyped = checkLatest_Untyped,
            serverId = serverId, serverId_Untyped = serverId_Untyped, serverUsername =
            serverUsername, serverUsername_Untyped = serverUsername_Untyped, serverPassword =
            serverPassword, serverPassword_Untyped = serverPassword_Untyped, settingsPath =
            settingsPath, settingsPath_Untyped = settingsPath_Untyped, overwriteSettings =
            overwriteSettings, overwriteSettings_Untyped = overwriteSettings_Untyped, gpgPrivateKey
            = gpgPrivateKey, gpgPrivateKey_Untyped = gpgPrivateKey_Untyped, gpgPassphrase =
            gpgPassphrase, gpgPassphrase_Untyped = gpgPassphrase_Untyped, cache = cache,
            cache_Untyped = cache_Untyped, cacheDependencyPath = cacheDependencyPath,
            cacheDependencyPath_Untyped = cacheDependencyPath_Untyped, jobStatus = jobStatus,
            jobStatus_Untyped = jobStatus_Untyped, token = token, token_Untyped = token_Untyped,
            mvnToolchainId = mvnToolchainId, mvnToolchainId_Untyped = mvnToolchainId_Untyped,
            mvnToolchainVendor = mvnToolchainVendor, mvnToolchainVendor_Untyped =
            mvnToolchainVendor_Untyped, _customInputs = _customInputs, _customVersion =
            _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            javaVersion?.let { "java-version" to it },
            javaVersion_Untyped?.let { "java-version" to it },
            javaVersionFile_Untyped?.let { "java-version-file" to it },
            distribution?.let { "distribution" to it.stringValue },
            distribution_Untyped?.let { "distribution" to it },
            javaPackage?.let { "java-package" to it.stringValue },
            javaPackage_Untyped?.let { "java-package" to it },
            architecture?.let { "architecture" to it },
            architecture_Untyped?.let { "architecture" to it },
            jdkFile?.let { "jdkFile" to it },
            jdkFile_Untyped?.let { "jdkFile" to it },
            checkLatest?.let { "check-latest" to it.toString() },
            checkLatest_Untyped?.let { "check-latest" to it },
            serverId?.let { "server-id" to it },
            serverId_Untyped?.let { "server-id" to it },
            serverUsername?.let { "server-username" to it },
            serverUsername_Untyped?.let { "server-username" to it },
            serverPassword?.let { "server-password" to it },
            serverPassword_Untyped?.let { "server-password" to it },
            settingsPath?.let { "settings-path" to it },
            settingsPath_Untyped?.let { "settings-path" to it },
            overwriteSettings?.let { "overwrite-settings" to it.toString() },
            overwriteSettings_Untyped?.let { "overwrite-settings" to it },
            gpgPrivateKey?.let { "gpg-private-key" to it },
            gpgPrivateKey_Untyped?.let { "gpg-private-key" to it },
            gpgPassphrase?.let { "gpg-passphrase" to it },
            gpgPassphrase_Untyped?.let { "gpg-passphrase" to it },
            cache?.let { "cache" to it.stringValue },
            cache_Untyped?.let { "cache" to it },
            cacheDependencyPath?.let { "cache-dependency-path" to it },
            cacheDependencyPath_Untyped?.let { "cache-dependency-path" to it },
            jobStatus?.let { "job-status" to it },
            jobStatus_Untyped?.let { "job-status" to it },
            token?.let { "token" to it },
            token_Untyped?.let { "token" to it },
            mvnToolchainId?.let { "mvn-toolchain-id" to it },
            mvnToolchainId_Untyped?.let { "mvn-toolchain-id" to it },
            mvnToolchainVendor?.let { "mvn-toolchain-vendor" to it },
            mvnToolchainVendor_Untyped?.let { "mvn-toolchain-vendor" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public sealed class Distribution(
        public val stringValue: String,
    ) {
        public object Adopt : SetupJava.Distribution("adopt")

        public object AdoptHotspot : SetupJava.Distribution("adopt-hotspot")

        public object AdoptOpenj9 : SetupJava.Distribution("adopt-openj9")

        public object Corretto : SetupJava.Distribution("corretto")

        public object Liberica : SetupJava.Distribution("liberica")

        public object Microsoft : SetupJava.Distribution("microsoft")

        public object Temurin : SetupJava.Distribution("temurin")

        public object Zulu : SetupJava.Distribution("zulu")

        public class Custom(
            customStringValue: String,
        ) : SetupJava.Distribution(customStringValue)
    }

    public sealed class JavaPackage(
        public val stringValue: String,
    ) {
        public object Jdk : SetupJava.JavaPackage("jdk")

        public object Jre : SetupJava.JavaPackage("jre")

        public object JdkPlusFx : SetupJava.JavaPackage("jdk+fx")

        public object JrePlusFx : SetupJava.JavaPackage("jre+fx")

        public class Custom(
            customStringValue: String,
        ) : SetupJava.JavaPackage(customStringValue)
    }

    public sealed class BuildPlatform(
        public val stringValue: String,
    ) {
        public object Maven : SetupJava.BuildPlatform("maven")

        public object Gradle : SetupJava.BuildPlatform("gradle")

        public object Sbt : SetupJava.BuildPlatform("sbt")

        public class Custom(
            customStringValue: String,
        ) : SetupJava.BuildPlatform(customStringValue)
    }

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * Distribution of Java that has been installed
         */
        public val distribution: String = "steps.$stepId.outputs.distribution"

        /**
         * Actual version of the java environment that has been installed
         */
        public val version: String = "steps.$stepId.outputs.version"

        /**
         * Path to where the java environment has been installed (same as $JAVA_HOME)
         */
        public val path: String = "steps.$stepId.outputs.path"

        /**
         * A boolean value to indicate an exact match was found for the primary key
         */
        public val cacheHit: String = "steps.$stepId.outputs.cache-hit"
    }
}

