import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader

plugins {
    `kotlin-dsl`
}


repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:2.4.20-Beta1")
    implementation("com.android.tools.build:gradle:9.3.0-rc02")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.12.0-beta01")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.37.0")
    implementation("pl.mareklangiewicz.deps:DepsKt:0.4.25")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}


// ===== Executable probes for the context-parameter claims ====================
// See migration-status.md. Experimental, this branch only.
//
//   ./gradlew -p template-logic probes
//   ./gradlew -p template-logic probes -Ponly=05
//
// Half those claims are about what does NOT compile, so they cannot be ordinary
// unit tests. Each probes/*.kt is a real snippet fed to the real Kotlin compiler
// (in-process, via kotlin-compiler-embeddable) and the suite asserts the outcome
// AND the exact diagnostic text.
//
// Every claim turns on ONE variable: the -Xcontext-parameters flag. So a probe
// may have two sections, compiled separately, mirroring this very repo:
//
//   ---- lib ----       compiled WITH    the flag  (like template-logic)
//   ---- consumer ----  compiled WITHOUT the flag  (like a build.gradle.kts)
//
// Nothing touches real sources, so there is no residue to clean up.
//
// NOTE: kept as lambdas, not a custom task class. Top-level class/fun
// declarations in a .gradle.kts silently stop the rest of the script from
// running -- measured here: markers before them registered, markers after did
// not, with no error reported.

val probeCompiler = configurations.create("probeCompiler")
val probeClasspath = configurations.create("probeClasspath")

dependencies {
  probeCompiler("org.jetbrains.kotlin:kotlin-compiler-embeddable:$embeddedKotlinVersion")
  probeClasspath("org.jetbrains.kotlin:kotlin-stdlib:$embeddedKotlinVersion")
  probeClasspath("pl.mareklangiewicz.deps:DepsKt:0.4.25")
}

tasks.register("probes") {
  group = "verification"
  description = "Assert the context-parameter claims in migration-status.md"

  val probesDir = layout.projectDirectory.dir("probes").asFile
  val workRoot = layout.buildDirectory.dir("probes").get().asFile
  val compilerFiles = probeCompiler
  val classpathFiles = probeClasspath
  val only = providers.gradleProperty("only").orNull
  val log = logger

  val gradlew = layout.projectDirectory.asFile.parentFile.resolve("gradlew")

  doLast {
    val compilerLoader = URLClassLoader(
      compilerFiles.files.map { it.toURI().toURL() }.toTypedArray(),
      ClassLoader.getPlatformClassLoader(),
    )
    val compilerClass = compilerLoader.loadClass("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")
    val execMethod = compilerClass.getMethod("exec", PrintStream::class.java, Array<String>::class.java)

    // The lib section only needs to PRODUCE bytecode, so kotlinc with the flag is fine.
    fun compileLib(src: String, outDir: File, cp: List<File>): Pair<Int, String> {
      outDir.mkdirs()
      val srcFile = File(outDir, "Probe.kt").apply { writeText("import pl.mareklangiewicz.deps.*\n" + src) }
      val classes = File(outDir, "classes").apply { mkdirs() }
      val args = arrayOf(
        srcFile.absolutePath, "-d", classes.absolutePath,
        "-classpath", cp.joinToString(File.pathSeparator) { it.absolutePath },
        "-nowarn", "-Xcontext-parameters",
      )
      val buf = ByteArrayOutputStream()
      val code = PrintStream(buf, true).use { ps ->
        execMethod.invoke(compilerClass.getDeclaredConstructor().newInstance(), ps, args)
      }
      return (code.javaClass.getMethod("getCode").invoke(code) as Int) to buf.toString()
    }

    // The consumer section is the whole point, so it MUST go through Gradle's own
    // build-script compiler -- which behaves differently from standalone kotlinc
    // (measured: kotlinc says "no context argument found" and rejects the callable
    // reference, where the Gradle script compiler says "specify -Xcontext-parameters"
    // and accepts the coerced reference). A generated throwaway project keeps this
    // isolated, so no real source is ever touched.
    fun runConsumer(src: String, dir: File, cp: List<File>): Pair<Int, String> {
      dir.mkdirs()
      File(dir, "settings.gradle.kts").writeText("rootProject.name = \"probe\"\n")
      File(dir, "build.gradle.kts").writeText(
        "buildscript { dependencies { classpath(files(" +
          cp.joinToString(", ") { "\"" + it.absolutePath + "\"" } + ")) } }\n" +
          "import pl.mareklangiewicz.deps.*\n" + src,
      )
      val proc = ProcessBuilder(gradlew.absolutePath, "--console=plain", "-q", "help")
        .directory(dir).redirectErrorStream(true).start()
      val out = proc.inputStream.bufferedReader().readText()
      return proc.waitFor() to out
    }

    fun directive(text: String, key: String): String? =
      Regex("^//\\? " + key + ": (.*)$", RegexOption.MULTILINE).find(text)?.groupValues?.get(1)?.trim()

    val files = probesDir.listFiles { f: File -> f.name.endsWith(".kt") }.orEmpty()
      .sortedBy { it.name }.filter { only == null || it.name.startsWith(only) }
    require(files.isNotEmpty()) { "no probes matched" + (only?.let { " -Ponly=" + it } ?: "") }

    workRoot.deleteRecursively(); workRoot.mkdirs()
    val baseCp = classpathFiles.files.toList()
    var passed = 0
    val failed = mutableListOf<String>()
    fun report(name: String, ok: Boolean, detail: String) {
      if (ok) { log.lifecycle("  PASS  " + name); passed++ }
      else { log.lifecycle("  FAIL  " + name + "\n        " + detail); failed += name }
    }
    fun firstError(out: String) = out.lines().firstOrNull { it.contains("error:") || it.startsWith("e: ") }
      ?: out.take(160).trim()
    fun matches(out: String, want: String) = out.contains(want, ignoreCase = true)

    log.lifecycle("Probing context-parameter claims (see template-logic/migration-status.md)")
    for (f in files) {
      val text = f.readText()
      val name = directive(text, "name") ?: f.nameWithoutExtension
      val expectOk = (directive(text, "expect") ?: error(f.name + ": no 'expect'")) == "ok"
      val failsIn = directive(text, "fails-in")
      val message = directive(text, "message")
      val output = directive(text, "output")
      val body = text.substringAfter("//? ---- lib ----\n", "")
      require(body.isNotEmpty()) { f.name + ": no '//? ---- lib ----' section" }
      val libSrc = body.substringBefore("//? ---- consumer ----")
      val consumerSrc = body.substringAfter("//? ---- consumer ----\n", "").takeIf { it.isNotEmpty() }

      val dir = File(workRoot, f.nameWithoutExtension)
      val (libRc, libOut) = compileLib(libSrc, dir, baseCp)
      if (!expectOk && failsIn == "lib") {
        report(name, libRc != 0 && matches(libOut, message!!),
          if (libRc == 0) "expected a compile error, but it COMPILED"
          else "failed, but not with '" + message + "' -- got: " + firstError(libOut))
        continue
      }
      if (libRc != 0) { report(name, false, "lib section failed: " + firstError(libOut)); continue }

      if (consumerSrc == null) { report(name, true, ""); continue }

      val (cRc, cOut) = runConsumer(consumerSrc, File(dir, "consumer"), baseCp + File(dir, "classes"))
      if (!expectOk && failsIn == "consumer") {
        report(name, cRc != 0 && matches(cOut, message!!),
          if (cRc == 0) "expected the script to fail, but it SUCCEEDED"
          else "failed, but not with '" + message + "' -- got: " + firstError(cOut))
        continue
      }
      if (cRc != 0) { report(name, false, "consumer script failed: " + firstError(cOut)); continue }
      report(name, output == null || cOut.contains(output),
        "ran, but output lacked '" + output + "' (got: " + cOut.trim() + ")")
    }
    log.lifecycle("\n  " + passed + " passed, " + failed.size + " failed")
    if (failed.isNotEmpty()) throw GradleException("probes failed: " + failed.joinToString())
  }
}
