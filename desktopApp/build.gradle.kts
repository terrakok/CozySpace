import io.github.kdroidfilter.nucleus.desktop.application.dsl.CompressionLevel
import io.github.kdroidfilter.nucleus.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nucleus)
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(libs.nucleus.aot.runtime)
}

nucleus.application {
    mainClass = "MainKt"

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "CozySpace"
        packageVersion = project.findProperty("appVersion")?.toString() ?: "1.0.0"
        homepage = "https://terrakok.github.io/CozySpace/"

        buildTypes.release.proguard {
            obfuscate = false
            optimize = true
            joinOutputJars = true
            version = "7.8.1"
            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        compressionLevel = CompressionLevel.Maximum
        cleanupNativeLibs = true
        enableAotCache = false //fix ci

        modules("java.instrument", "java.prefs", "jdk.unsupported")

        linux {
            iconFile.set(project.file("appIcons/LinuxIcon.png"))
            shortcut = true
            packageName = "dev.terrakok.cozyspace.desktopApp"
            appRelease = "1"
            appCategory = "Utility"
            menuGroup = "Development"
            debMaintainer = "Konstantin Tskhovrebov <terrakok@gmail.com>"
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
