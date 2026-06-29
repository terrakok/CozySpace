package dev.terrakok.cozyspace

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class Storage {
    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    private val _savedPresets = MutableStateFlow(loadPresets())
    val savedPresets: StateFlow<List<Preset>> = _savedPresets.asStateFlow()

    private fun loadPresets(): List<Preset> {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        return json.decodeFromString<List<Preset>>(presetsJson) + Preset.default
    }

    var latestPreset: Preset
        get() = settings.getStringOrNull("latest_preset").let { presetJson ->
            if (presetJson.isNullOrBlank()) {
                Preset.default
            } else {
                json.decodeFromString(presetJson)
            }
        }
        set(value) {
            settings.putString("latest_preset", json.encodeToString(value))
        }

    fun saveNewPreset(preset: Preset) {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        val currentPresets = json.decodeFromString<List<Preset>>(presetsJson)
        val updatedPresets = currentPresets + preset
        settings.putString("saved_presets", json.encodeToString(updatedPresets))
        _savedPresets.value = loadPresets()
    }

    fun deletePreset(preset: Preset) {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        val currentPresets = json.decodeFromString<List<Preset>>(presetsJson)
        val updatedPresets = currentPresets.filter { it != preset }
        settings.putString("saved_presets", json.encodeToString(updatedPresets))
        _savedPresets.value = loadPresets()
    }
}