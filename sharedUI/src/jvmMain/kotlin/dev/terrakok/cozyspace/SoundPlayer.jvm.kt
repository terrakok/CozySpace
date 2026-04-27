package dev.terrakok.cozyspace

import kuusisto.tinysound.Music
import kuusisto.tinysound.TinySound
import java.net.URI

actual fun createSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
): SoundPlayer = JavaSoundPlayer(tracks, volumes)

private class JavaSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
) : SoundPlayer {
    private val musics: List<Music?>
    init {
        if (!TinySound.isInitialized()) TinySound.init()
        musics = tracks.mapIndexed { index, uri ->
            val url = URI.create(uri).toURL()
            TinySound.loadMusic(url)?.apply {
                volume = volumes[index].toDouble()
                setLoop(true)
            }
        }
    }

    override fun updateVolume(index: Int, value: Float) {
        musics[index]?.volume = value.toDouble()
    }

    override fun play() = musics.forEach { it?.play(true) }

    override fun pause() = musics.forEach { it?.pause() }

    override fun shutdown() {
        musics.forEach { it?.unload() }
        TinySound.shutdown()
    }
}
