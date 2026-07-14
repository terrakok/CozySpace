import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.compose.reload.gradle.ComposeHotRun

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nucleus)
}

dependencies {
    implementation(project(":sharedUI"))
}

nucleus.application {
    mainClass = "MainKt"
    jvmArgs("-Dsun.java2d.metal=false")

    nativeDistributions {
        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Msi,
            TargetFormat.Deb,
            TargetFormat.Rpm,
            TargetFormat.Flatpak,
        )
        packageName = "CozySpace"
        packageVersion = project.findProperty("appVersion")?.toString() ?: "1.1.0"
        homepage = "https://terrakok.github.io/CozySpace/"

        compressionLevel = CompressionLevel.Maximum
        cleanupNativeLibs = true

        graalvm {
            isEnabled = true
            imageName = "CozySpace"
            javaLanguageVersion = 25

            buildArgs.addAll(
                "-H:+AddAllCharsets",
                "-Djava.awt.headless=false",
                "-Os",
                "-H:-IncludeMethodData",
            )
        }

        linux {
            iconFile.set(project.file("appIcons/LinuxIcon.png"))
            shortcut = true
            packageName = "dev.terrakok.cozyspace.desktopApp"
            appRelease = "3"
            appCategory = "Utility"
            menuGroup = "Development"
            debMaintainer = "Konstantin Tskhovrebov <terrakok@gmail.com>"
            rpmLicenseType = "MIT"
            flatpak {
                branch = "main"
            }
        }
        windows {
            iconFile.set(project.file("appIcons/WindowsIcon.ico"))
            upgradeUuid = "93e841a6-7652-441b-b48e-88eefe40b055"
        }
        macOS {
            iconFile.set(project.file("appIcons/MacosIcon.icns"))
            bundleID = "dev.terrakok.cozyspace.desktopApp"
            infoPlist {
                extraKeysRawXml = """
                        <key>LSUIElement</key>
                        <true/>
                    """.trimIndent()
            }
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    args = listOf("--window")
}

// Java Sound native library (libjsound) is required by TinySound to open an audio
// output line. Nucleus copies a curated set of JDK native libs next to the GraalVM
// native image but omits libjsound, so in the native build AudioSystem finds no
// mixers ("Unsupported output format!" / "TinySound not initialized!") and there is
// no sound. We piggy-back on Nucleus' existing per-OS "copy native libs" task by
// adding jsound to its include list — it then flows through the same strip/patch/
// codesign pipeline as the AWT libs. Source dir matches Nucleus' own graalvmHome
// (the configured Java 25 toolchain).
// Nucleus registers its GraalVM tasks in afterEvaluate, so wire this up there too.
afterEvaluate {
    val graalvmLibDir =
        javaToolchains
            .launcherFor { languageVersion.set(JavaLanguageVersion.of(25)) }
            .map { it.metadata.installationPath }

    fun addJsoundTo(taskName: String, subDir: String, libName: String) {
        (tasks.findByName(taskName) as? Copy)?.apply {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(graalvmLibDir.map { it.dir(subDir) }) { include(libName) }
        }
    }

    val osName = System.getProperty("os.name").lowercase()
    when {
        osName.contains("mac") -> addJsoundTo("copyGraalvmAwtDylibs", "lib", "libjsound.dylib")
        osName.contains("win") -> addJsoundTo("copyGraalvmAwtDlls", "bin", "jsound.dll")
        else -> addJsoundTo("copyGraalvmAwtSoLibs", "lib", "libjsound.so")
    }
}
