package pl.mareklangiewicz.kgroundx.maintenance

import org.junit.jupiter.api.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.uspek.*

class MyGradleWTests {

  @TestFactory
  fun gradleWTestFactory() = uspekTestFactory {
    "On services.gradle.org versions json (trimmed real response)" o {
      val json = """
        {
          "version" : "9.8.0",
          "buildTime" : "20260924134000+0000",
          "current" : true,
          "snapshot" : false,
          "downloadUrl" : "https://services.gradle.org/distributions/gradle-9.8.0-bin.zip",
          "checksumUrl" : "https://services.gradle.org/distributions/gradle-9.8.0-bin.zip.sha256",
          "checksum" : "bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c",
          "wrapperChecksumUrl" : "https://services.gradle.org/distributions/gradle-9.8.0-wrapper.jar.sha256",
          "wrapperChecksum" : "0000000000000000000000000000000000000000000000000000000000000000"
        }
      """.trimIndent()
      "parses version and DISTRIBUTION checksum (not the wrapper jar one)" o {
        parseGradleRelease(json).chkEq(
          GradleRelease("9.8.0", "bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c"),
        )
      }
      "fails on json without checksum" o {
        runCatching { parseGradleRelease(json.replace("\"checksum\"", "\"nope\"")) }
          .isFailure.chkTrue { "Parsed json without checksum" }
      }
    }
    "On gradle-wrapper.properties" o {
      "reads bin version" o {
        gradleVersionInWrapperProperties(
          "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.7.1-bin.zip\nretries=0\n",
        ).chkEq("9.7.1")
      }
      "reads all version" o {
        gradleVersionInWrapperProperties(
          "distributionUrl=https\\://services.gradle.org/distributions/gradle-4.10-all.zip",
        ).chkEq("4.10")
      }
      "no url means null" o { gradleVersionInWrapperProperties("retries=0").chkEq(null) }
    }
  }
}
