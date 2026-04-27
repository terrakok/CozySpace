package dev.terrakok.cozyspace

interface SoundPlayer {
    fun updateVolume(index: Int, value: Float)
    fun play()
    fun pause()
    fun shutdown()
}

expect fun createSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
): SoundPlayer