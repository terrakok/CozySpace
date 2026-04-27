package dev.terrakok.cozyspace

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class Storage {
    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

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

    fun getSavedPresets(): List<Preset> {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        return json.decodeFromString<List<Preset>>(presetsJson) + Preset.default
    }

    fun saveNewPreset(preset: Preset) {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        val currentPresets = json.decodeFromString<List<Preset>>(presetsJson)
        val updatedPresets = currentPresets + preset
        settings.putString("saved_presets", json.encodeToString(updatedPresets))
    }

    fun deletePreset(preset: Preset) {
        val presetsJson = settings.getStringOrNull("saved_presets") ?: "[]"
        val currentPresets = json.decodeFromString<List<Preset>>(presetsJson)
        val updatedPresets = currentPresets.filter { it != preset }
        settings.putString("saved_presets", json.encodeToString(updatedPresets))
    }
}