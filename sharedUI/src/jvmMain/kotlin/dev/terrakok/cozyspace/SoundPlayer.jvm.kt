package dev.terrakok.cozyspace

import kuusisto.tinysound.Music
import kuusisto.tinysound.TinySound
import java.net.URI

actual fun createSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
): SoundPlayer = JavaSoundPlayer(tracks, volumes)

private class JavaSoundPlayer(
    private val tracks: List<String>,
    volumes: List<Float>
) : SoundPlayer {
    private val volumes: MutableList<Float> = volumes.toMutableList()
    private val musics: Array<Music?> = arrayOfNulls(tracks.size)
    private var playing = false

    init {
        if (!TinySound.isInitialized()) TinySound.init()
        System.gc() // Clean up after init
        tracks.indices.forEach { index ->
            if (this.volumes[index] > 0f) loadMusic(index)
        }
    }

    private fun loadMusic(index: Int): Music? {
        musics[index]?.let { return it }
        val url = URI.create(tracks[index]).toURL()
        return TinySound.loadMusic(url, true)?.also { music ->
            music.volume = volumes[index].toDouble()
            music.setLoop(true)
            if (playing) music.play(true)
            musics[index] = music
        }
    }

    private fun unloadMusic(index: Int) {
        musics[index]?.let { music ->
            music.stop()
            music.unload()
            musics[index] = null
            System.gc() // Trigger GC after unloading to free native memory/buffers
        }
    }

    override fun updateVolume(index: Int, value: Float) {
        volumes[index] = value
        if (value > 0f) {
            loadMusic(index)?.volume = value.toDouble()
        } else {
            // Track is muted/disabled — free its memory.
            unloadMusic(index)
        }
    }

    override fun play() {
        playing = true
        musics.forEach { it?.play(true) }
    }

    override fun pause() {
        playing = false
        musics.forEach { it?.pause() }
    }

    override fun shutdown() {
        musics.indices.forEach { unloadMusic(it) }
        TinySound.shutdown()
    }
}
