import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.terrakok.cozyspace.DesktopApp

fun main(arg: Array<String>) {
    val isWindowApp = arg.contains("--window")
    nucleusApplication(backend = NucleusBackend.Tao) {
        DesktopApp(!isWindowApp)
    }
}
