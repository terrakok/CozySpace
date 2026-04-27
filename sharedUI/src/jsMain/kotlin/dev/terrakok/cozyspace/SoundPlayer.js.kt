package dev.terrakok.cozyspace

import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

actual fun createSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
): SoundPlayer = WebSoundPlayer(tracks, volumes)

private external open class AudioNode {
    fun connect(destination: AudioNode)
}

private external class AudioDestinationNode : AudioNode

private external class AudioParam {
    var value: Float
}

private external class GainNode : AudioNode {
    val gain: AudioParam
}

private external class AudioBuffer

private external class AudioBufferSourceNode : AudioNode {
    var buffer: AudioBuffer?
    var loop: Boolean
    fun start()
    fun stop()
}

private external class AudioContext {
    val destination: AudioDestinationNode
    fun createGain(): GainNode
    fun createBufferSource(): AudioBufferSourceNode
    fun decodeAudioData(audioData: ArrayBuffer): Promise<AudioBuffer>
    fun suspend(): Promise<Unit>
    fun resume(): Promise<Unit>
    fun close(): Promise<Unit>
}

private class WebSoundPlayer(
    tracks: List<String>,
    volumes: List<Float>
) : SoundPlayer {

    private val context = AudioContext()
    private val gainNodes: List<GainNode> = volumes.map { volume ->
        context.createGain().also { node ->
            node.gain.value = volume
            node.connect(context.destination)
        }
    }
    private val buffers = arrayOfNulls<AudioBuffer>(tracks.size)
    private val sources = arrayOfNulls<AudioBufferSourceNode>(tracks.size)
    private var playing = false

    init {
        tracks.forEachIndexed { index, uri -> loadTrack(index, uri) }
    }

    private fun loadTrack(index: Int, uri: String) {
        window.fetch(uri).then { response ->
            response.arrayBuffer().then { arrayBuffer ->
                context.decodeAudioData(arrayBuffer).then { buffer ->
                    buffers[index] = buffer
                    if (playing) startSource(index)
                }
            }
        }
    }

    private fun startSource(index: Int) {
        val buffer = buffers[index] ?: return
        sources[index] = context.createBufferSource().also { source ->
            source.buffer = buffer
            source.loop = true
            source.connect(gainNodes[index])
            source.start()
        }
    }

    override fun updateVolume(index: Int, value: Float) {
        gainNodes[index].gain.value = value
    }

    override fun play() {
        playing = true
        context.resume()
        sources.forEachIndexed { index, source ->
            if (source == null) startSource(index)
        }
    }

    override fun pause() {
        playing = false
        context.suspend()
    }

    override fun shutdown() {
        sources.forEach { it?.stop() }
        context.close()
    }
}
