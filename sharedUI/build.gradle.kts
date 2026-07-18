plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            implementation(libs.rikkaui.foundation)
            implementation(libs.rikkaui.components)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.multiplatformSettings)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.composemediaplayer.audio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.nucleus.composenativetray)
            api(libs.nucleus.application)
            implementation(libs.nucleus.window.core)
            implementation(libs.nucleus.window.tao)
            implementation(libs.nucleus.darkmode.detector)
        }

    }
}
