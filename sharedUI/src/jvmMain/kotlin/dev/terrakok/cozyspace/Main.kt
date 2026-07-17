package dev.terrakok.cozyspace

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.composenativetray.tray.api.TrayApp
import dev.nucleusframework.composenativetray.tray.api.rememberTrayAppState
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode

@Composable
fun NucleusApplicationScope.DesktopApp(trayApp: Boolean) {
    if (trayApp) {
        val trayAppState = rememberTrayAppState(
            initialWindowSize = DpSize(400.dp, 470.dp),
            initiallyVisible = true
        )
        TrayApp(
            state = trayAppState,
            icon = Icons.Bird,
            tooltip = "CozySpace",
            menu = {
                Item("Toggle popup") { trayAppState.toggle() }
                Divider()
                Item("Quit") { exitApplication() }
            }
        ) {
            App(isSystemInDarkMode())
        }
    } else {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            title = "CozySpace",
            state = WindowState(size = DpSize(400.dp, 470.dp))
        ) {
            App(isSystemInDarkMode())
        }
    }
}