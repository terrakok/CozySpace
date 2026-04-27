package dev.terrakok.cozyspace

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import com.kdroid.composetray.tray.api.ExperimentalTrayAppApi
import com.kdroid.composetray.tray.api.TrayApp
import com.kdroid.composetray.tray.api.rememberTrayAppState
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import zed.rainxch.rikkaui.foundation.RikkaTheme.colors

@OptIn(ExperimentalTrayAppApi::class)
@Composable
fun ApplicationScope.DesktopApp() {
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
        App(
            isSystemInDarkMode(),
            modifier = Modifier
                .padding(10.dp)
                .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )
    }
}