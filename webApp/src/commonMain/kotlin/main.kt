import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.terrakok.cozyspace.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport(document.getElementById("composeApp")!!) { App() }
