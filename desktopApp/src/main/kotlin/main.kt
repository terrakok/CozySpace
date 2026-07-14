import dev.nucleusframework.aot.runtime.AotRuntime
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.terrakok.cozyspace.DesktopApp
import kotlin.system.exitProcess

fun main(arg: Array<String>) {
    val isWindowApp = arg.contains("--window")
    aotTraining()
    nucleusApplication(backend = NucleusBackend.Tao) {
        DesktopApp(!isWindowApp)
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