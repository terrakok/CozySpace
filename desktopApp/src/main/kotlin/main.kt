import androidx.compose.ui.window.application
import dev.terrakok.cozyspace.DesktopApp
import io.github.kdroidfilter.nucleus.aot.runtime.AotRuntime
import kotlin.system.exitProcess

fun main() {
    aotTraining()
    application {
        DesktopApp()
    }
}


private fun aotTraining() {
    if (AotRuntime.isTraining()) {
        Thread({
            Thread.sleep(5000)
            exitProcess(0)
        }, "aot-timer").apply {
            isDaemon = false
            start()
        }
    }
}