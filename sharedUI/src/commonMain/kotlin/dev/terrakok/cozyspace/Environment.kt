package dev.terrakok.cozyspace

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import cozyspace.sharedui.generated.resources.Res
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Stable
data class Environment(
    val name: String,
    val icon: ImageVector,
    val uri: String,
    private val _volume: MutableIntState,
    private val _enabled: MutableState<Boolean>
) {
    var volume by _volume
    var enabled by _enabled
}

val environments = listOf(
    Environment("Bird", Icons.Bird, Res.getUri("files/birds.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Boat", Icons.Boat, Res.getUri("files/boat.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("City", Icons.City, Res.getUri("files/city.ogg"), mutableIntStateOf(25), mutableStateOf(true)),
    Environment("Coffee", Icons.Coffee, Res.getUri("files/coffee-shop.ogg"), mutableIntStateOf(35), mutableStateOf(true)),
    Environment("CampFire", Icons.CampFire, Res.getUri("files/fireplace.ogg"), mutableIntStateOf(60), mutableStateOf(true)),
    Environment("SineWave", Icons.SineWave, Res.getUri("files/pink-noise.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Rain", Icons.Rain, Res.getUri("files/rain.ogg"), mutableIntStateOf(30), mutableStateOf(true)),
    Environment("Storm", Icons.Storm, Res.getUri("files/storm.ogg"), mutableIntStateOf(40), mutableStateOf(true)),
    Environment("Fountain", Icons.Fountain, Res.getUri("files/stream.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Moon", Icons.Moon, Res.getUri("files/summer-night.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Train", Icons.Train, Res.getUri("files/train.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Waves", Icons.Waves, Res.getUri("files/waves.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("SquareWave", Icons.SquareWave, Res.getUri("files/white-noise.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
    Environment("Wind", Icons.Wind, Res.getUri("files/wind.ogg"), mutableIntStateOf(50), mutableStateOf(false)),
)

@Serializable
data class Preset(
    val name: String,
    val map: Map<String, Int>
) {

    fun apply() {
        environments.forEach { environment ->
            val p = map[environment.name]
            environment.volume = p ?: 50
            environment.enabled = p != null
        }
    }

    companion object {
        fun create(
            name: String = Instant.fromEpochSeconds(Clock.System.now().epochSeconds).toString()
        ) = Preset(
            name = name,
            map = environments.filter { it.enabled }.associate { it.name to it.volume }
        )

        val default = Preset(
            name = "Default",
            map = mapOf(
                "City" to 25,
                "Coffee" to 35,
                "CampFire" to 60,
                "Rain" to 30,
                "Storm" to 40,
            )
        )
    }
}