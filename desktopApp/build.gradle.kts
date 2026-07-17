import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.NativeImageOptimization
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

    nativeDistributions {
        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Nsis,
            TargetFormat.Deb,
            TargetFormat.Rpm,
        )
        packageName = "CozySpace"
        packageVersion = project.findProperty("appVersion")?.toString() ?: "1.2.2"
        homepage = "https://terrakok.github.io/CozySpace/"

        compressionLevel = CompressionLevel.Ultra
        cleanupNativeLibs = true

        graalvm {
            isEnabled = true
            imageName = "CozySpace"
            optimization = NativeImageOptimization.SIZE
        }

        linux {
            iconFile.set(project.file("appIcons/LinuxIcon.png"))
            shortcut = true
            packageName = "dev.terrakok.cozyspace.desktopApp"
            appRelease = "4"
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
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    args = listOf("--window")
}

val packageGraal = tasks.register("packageGraalForCurrentOS") {
    group = "nucleus"
    description = "Build and package the GraalVM native image installer(s) for the current OS."

    val osName = System.getProperty("os.name").lowercase()
    val graalPackageTasks = when {
        osName.contains("mac") -> listOf("packageGraalvmDmg")
        osName.contains("win") -> listOf("packageGraalvmNsis")
        else -> listOf("packageGraalvmDeb", "packageGraalvmRpm")
    }
    dependsOn(graalPackageTasks)
}
