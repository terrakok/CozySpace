package dev.terrakok.cozyspace

import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayer

interface SoundPlayer {
    fun updateVolume(index: Int, value: Float)
    fun play()
    fun pause()
    fun shutdown()
}

fun createSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
): SoundPlayer = AudioPlayerSoundPlayer(tracks, volumes)

private class AudioPlayerSoundPlayer(
    private val tracks: List<String>,
    volumes: List<Float>
) : SoundPlayer {
    private val volumes: MutableList<Float> = volumes.toMutableList()
    private val players: Array<AudioPlayer?> = arrayOfNulls(tracks.size)
    private val started = BooleanArray(tracks.size)
    private var playing = false

    override fun updateVolume(index: Int, value: Float) {
        volumes[index] = value
        if (value > 0f) {
            val player = players[index] ?: AudioPlayer().also { players[index] = it }
            player.setVolume(value)
            if (playing && !started[index]) start(index)
        } else {
            // Track is muted/disabled — free its resources.
            players[index]?.release()
            players[index] = null
            started[index] = false
        }
    }

    override fun play() {
        playing = true
        tracks.indices.forEach { index ->
            if (volumes[index] > 0f) {
                val player = players[index] ?: AudioPlayer().also { players[index] = it }
                if (started[index]) player.play() else start(index)
            }
        }
    }

    override fun pause() {
        playing = false
        players.forEach { it?.pause() }
    }

    override fun shutdown() {
        players.indices.forEach { index ->
            players[index]?.release()
            players[index] = null
            started[index] = false
        }
    }

    private fun start(index: Int) {
        val player = players[index] ?: return
        player.setVolume(volumes[index])
        player.play(tracks[index], loop = true)
        started[index] = true
    }
}
