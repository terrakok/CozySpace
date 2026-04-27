import io.github.kdroidfilter.nucleus.desktop.application.dsl.CompressionLevel
import io.github.kdroidfilter.nucleus.desktop.application.dsl.TargetFormat

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

        buildTypes.release.proguard.isEnabled = false

        compressionLevel = CompressionLevel.Maximum
        cleanupNativeLibs = true
        enableAotCache = true

        modules("java.instrument", "java.prefs", "jdk.unsupported")

        linux {
            iconFile.set(project.file("appIcons/LinuxIcon.png"))
        }
        windows {
            iconFile.set(project.file("appIcons/WindowsIcon.ico"))
        }
        macOS {
            iconFile.set(project.file("appIcons/MacosIcon.icns"))
            bundleID = "dev.terrakok.cozyspace.desktopApp"
        }
    }
}
