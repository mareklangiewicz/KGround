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
import kotlin.Deprecated
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * ```text
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !!!                             WARNING                             !!!
 * !!!                                                                 !!!
 * !!! This action binding has no typings provided. All inputs will    !!!
 * !!! have a default type of String.                                  !!!
 * !!! To be able to use this action in a type-safe way, ask the       !!!
 * !!! action's owner to provide the typings using                     !!!
 * !!!                                                                 !!!
 * !!! https://github.com/typesafegithub/github-actions-typing         !!!
 * !!!                                                                 !!!
 * !!! or if it's impossible, contribute typings to a community-driven !!!
 * !!!                                                                 !!!
 * !!! https://github.com/typesafegithub/github-actions-typing-catalog !!!
 * !!!                                                                 !!!
 * !!! This '_Untyped' binding will be available even once the typings !!!
 * !!! are added.                                                      !!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ```
 *
 * Action: Setup Java JDK
 *
 * Set up a specific version of the Java JDK and add the command-line tools to the PATH
 *
 * [Action on GitHub](https://github.com/actions/setup-java)
 *
 * @param javaVersion_Untyped The Java version to set up. Takes a whole or semver Java version. See
 * examples of supported syntax in README file
 * @param javaVersionFile_Untyped The path to the `.java-version` file. See examples of supported
 * syntax in README file
 * @param distribution_Untyped Java distribution. See the list of supported distributions in README
 * file
 * @param javaPackage_Untyped The package type (jdk, jre, jdk+fx, jre+fx)
 * @param architecture_Untyped The architecture of the package (defaults to the action runner's
 * architecture)
 * @param jdkFile_Untyped Path to where the compressed JDK is located
 * @param checkLatest_Untyped Set this option if you want the action to check for the latest
 * available version that satisfies the version spec
 * @param serverId_Untyped ID of the distributionManagement repository in the pom.xml file. Default
 * is `github`
 * @param serverUsername_Untyped Environment variable name for the username for authentication to
 * the Apache Maven repository. Default is $GITHUB_ACTOR
 * @param serverPassword_Untyped Environment variable name for password or token for authentication
 * to the Apache Maven repository. Default is $GITHUB_TOKEN
 * @param settingsPath_Untyped Path to where the settings.xml file will be written. Default is
 * ~/.m2.
 * @param overwriteSettings_Untyped Overwrite the settings.xml file if it exists. Default is "true".
 * @param gpgPrivateKey_Untyped GPG private key to import. Default is empty string.
 * @param gpgPassphrase_Untyped Environment variable name for the GPG private key passphrase.
 * Default is $GPG_PASSPHRASE.
 * @param cache_Untyped Name of the build platform to cache dependencies. It can be "maven",
 * "gradle" or "sbt".
 * @param cacheDependencyPath_Untyped The path to a dependency file: pom.xml, build.gradle,
 * build.sbt, etc. This option can be used with the `cache` option. If this option is omitted, the
 * action searches for the dependency file in the entire repository. This option supports wildcards and
 * a list of file names for caching multiple dependencies.
 * @param jobStatus_Untyped Workaround to pass job status to post job step. This variable is not
 * intended for manual setting
 * @param token_Untyped The token used to authenticate when fetching version manifests hosted on
 * github.com, such as for the Microsoft Build of OpenJDK. When running this action on github.com, the
 * default value is sufficient. When running on GHES, you can pass a personal access token for
 * github.com if you are experiencing rate limiting.
 * @param mvnToolchainId_Untyped Name of Maven Toolchain ID if the default name of
 * "${distribution}_${java-version}" is not wanted. See examples of supported syntax in Advanced Usage
 * file
 * @param mvnToolchainVendor_Untyped Name of Maven Toolchain Vendor if the default name of
 * "${distribution}" is not wanted. See examples of supported syntax in Advanced Usage file
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("SetupJava"),
)
@ExposedCopyVisibility
public data class SetupJava_Untyped private constructor(
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
     * Java distribution. See the list of supported distributions in README file
     */
    public val distribution_Untyped: String,
    /**
     * The package type (jdk, jre, jdk+fx, jre+fx)
     */
    public val javaPackage_Untyped: String? = null,
    /**
     * The architecture of the package (defaults to the action runner's architecture)
     */
    public val architecture_Untyped: String? = null,
    /**
     * Path to where the compressed JDK is located
     */
    public val jdkFile_Untyped: String? = null,
    /**
     * Set this option if you want the action to check for the latest available version that
     * satisfies the version spec
     */
    public val checkLatest_Untyped: String? = null,
    /**
     * ID of the distributionManagement repository in the pom.xml file. Default is `github`
     */
    public val serverId_Untyped: String? = null,
    /**
     * Environment variable name for the username for authentication to the Apache Maven repository.
     * Default is $GITHUB_ACTOR
     */
    public val serverUsername_Untyped: String? = null,
    /**
     * Environment variable name for password or token for authentication to the Apache Maven
     * repository. Default is $GITHUB_TOKEN
     */
    public val serverPassword_Untyped: String? = null,
    /**
     * Path to where the settings.xml file will be written. Default is ~/.m2.
     */
    public val settingsPath_Untyped: String? = null,
    /**
     * Overwrite the settings.xml file if it exists. Default is "true".
     */
    public val overwriteSettings_Untyped: String? = null,
    /**
     * GPG private key to import. Default is empty string.
     */
    public val gpgPrivateKey_Untyped: String? = null,
    /**
     * Environment variable name for the GPG private key passphrase. Default is $GPG_PASSPHRASE.
     */
    public val gpgPassphrase_Untyped: String? = null,
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
    public val cacheDependencyPath_Untyped: String? = null,
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
    public val token_Untyped: String? = null,
    /**
     * Name of Maven Toolchain ID if the default name of "${distribution}_${java-version}" is not
     * wanted. See examples of supported syntax in Advanced Usage file
     */
    public val mvnToolchainId_Untyped: String? = null,
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
) : RegularAction<SetupJava_Untyped.Outputs>("actions", "setup-java", _customVersion ?: "v4") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        javaVersion_Untyped: String? = null,
        javaVersionFile_Untyped: String? = null,
        distribution_Untyped: String,
        javaPackage_Untyped: String? = null,
        architecture_Untyped: String? = null,
        jdkFile_Untyped: String? = null,
        checkLatest_Untyped: String? = null,
        serverId_Untyped: String? = null,
        serverUsername_Untyped: String? = null,
        serverPassword_Untyped: String? = null,
        settingsPath_Untyped: String? = null,
        overwriteSettings_Untyped: String? = null,
        gpgPrivateKey_Untyped: String? = null,
        gpgPassphrase_Untyped: String? = null,
        cache_Untyped: String? = null,
        cacheDependencyPath_Untyped: String? = null,
        jobStatus_Untyped: String? = null,
        token_Untyped: String? = null,
        mvnToolchainId_Untyped: String? = null,
        mvnToolchainVendor_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(javaVersion_Untyped = javaVersion_Untyped, javaVersionFile_Untyped =
            javaVersionFile_Untyped, distribution_Untyped = distribution_Untyped,
            javaPackage_Untyped = javaPackage_Untyped, architecture_Untyped = architecture_Untyped,
            jdkFile_Untyped = jdkFile_Untyped, checkLatest_Untyped = checkLatest_Untyped,
            serverId_Untyped = serverId_Untyped, serverUsername_Untyped = serverUsername_Untyped,
            serverPassword_Untyped = serverPassword_Untyped, settingsPath_Untyped =
            settingsPath_Untyped, overwriteSettings_Untyped = overwriteSettings_Untyped,
            gpgPrivateKey_Untyped = gpgPrivateKey_Untyped, gpgPassphrase_Untyped =
            gpgPassphrase_Untyped, cache_Untyped = cache_Untyped, cacheDependencyPath_Untyped =
            cacheDependencyPath_Untyped, jobStatus_Untyped = jobStatus_Untyped, token_Untyped =
            token_Untyped, mvnToolchainId_Untyped = mvnToolchainId_Untyped,
            mvnToolchainVendor_Untyped = mvnToolchainVendor_Untyped, _customInputs = _customInputs,
            _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            javaVersion_Untyped?.let { "java-version" to it },
            javaVersionFile_Untyped?.let { "java-version-file" to it },
            "distribution" to distribution_Untyped,
            javaPackage_Untyped?.let { "java-package" to it },
            architecture_Untyped?.let { "architecture" to it },
            jdkFile_Untyped?.let { "jdkFile" to it },
            checkLatest_Untyped?.let { "check-latest" to it },
            serverId_Untyped?.let { "server-id" to it },
            serverUsername_Untyped?.let { "server-username" to it },
            serverPassword_Untyped?.let { "server-password" to it },
            settingsPath_Untyped?.let { "settings-path" to it },
            overwriteSettings_Untyped?.let { "overwrite-settings" to it },
            gpgPrivateKey_Untyped?.let { "gpg-private-key" to it },
            gpgPassphrase_Untyped?.let { "gpg-passphrase" to it },
            cache_Untyped?.let { "cache" to it },
            cacheDependencyPath_Untyped?.let { "cache-dependency-path" to it },
            jobStatus_Untyped?.let { "job-status" to it },
            token_Untyped?.let { "token" to it },
            mvnToolchainId_Untyped?.let { "mvn-toolchain-id" to it },
            mvnToolchainVendor_Untyped?.let { "mvn-toolchain-vendor" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

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

