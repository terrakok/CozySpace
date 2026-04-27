package dev.terrakok.cozyspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardContent
import zed.rainxch.rikkaui.components.ui.card.CardVariant
import zed.rainxch.rikkaui.components.ui.dialog.Dialog
import zed.rainxch.rikkaui.components.ui.dialog.DialogFooter
import zed.rainxch.rikkaui.components.ui.dialog.DialogHeader
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenu
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenuLabel
import zed.rainxch.rikkaui.components.ui.icon.Icon
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.slider.Slider
import zed.rainxch.rikkaui.components.ui.slider.SliderAnimation
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.foundation.RikkaPalette
import zed.rainxch.rikkaui.foundation.RikkaTheme
import zed.rainxch.rikkaui.foundation.RikkaTheme.colors

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }


@Composable
internal fun AppTheme(
    systemIsDark: Boolean,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val isDarkState = remember(systemIsDark) { mutableStateOf(systemIsDark) }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        onThemeChanged(!isDark)
        val palette = RikkaPalette.Zinc.resolve(isDark = isDark)
        RikkaTheme(colors = palette, content = content)
    }
}

@Composable
fun App(
    systemIsDark: Boolean = isSystemInDarkTheme(),
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) = AppTheme(systemIsDark, onThemeChanged) {
    val storage = remember { Storage() }
    DisposableEffect(Unit) {
        storage.latestPreset.apply()
        onDispose { storage.latestPreset = Preset.create() }
    }

    BoxWithConstraints(modifier) {
        val isWide = maxWidth >= 400.dp
        var isPlaying by remember { mutableStateOf(false) }
        val player = remember {
            createSoundPlayer(
                tracks = environments.map { it.uri },
                volumes = environments.map { it.volume / 100f }
            )
        }
        DisposableEffect(Unit) {
            onDispose { player.shutdown() }
        }

        var presetToSave: Preset? by remember { mutableStateOf(null) }
        Dialog(
            open = presetToSave != null,
            onDismiss = { presetToSave = null },
            maxWidth = 300.dp
        ) {
            val newPreset = presetToSave ?: return@Dialog
            DialogHeader(
                title = "Save a new preset",
                description = "Enter a name and save it."
            )
            var presetName by remember { mutableStateOf(newPreset.name) }
            Input(
                value = presetName,
                onValueChange = { presetName = it.trim().take(20) },
                label = "Preset name"
            )
            DialogFooter {
                Button(
                    "Cancel",
                    onClick = { presetToSave = null },
                    variant = ButtonVariant.Outline,
                )
                Button(
                    "Save",
                    onClick = {
                        storage.saveNewPreset(newPreset.copy(name = presetName))
                        presetToSave = null
                    }
                )
            }
        }

        var presetToDelete: Preset? by remember { mutableStateOf(null) }
        Dialog(
            open = presetToDelete != null,
            onDismiss = { presetToDelete = null },
            maxWidth = 300.dp
        ) {
            val oldPreset = presetToDelete ?: return@Dialog
            DialogHeader(
                title = "Delete preset",
                description = "Are you sure\nyou want to delete this preset?"
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "\"${oldPreset.name}\""
            )
            DialogFooter {
                Button(
                    "Cancel",
                    onClick = { presetToDelete = null },
                    variant = ButtonVariant.Outline,
                )
                Button(
                    "Delete",
                    onClick = {
                        storage.deletePreset(oldPreset)
                        presetToDelete = null
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val content = remember {
                movableContentOf {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val half = environments.size / 2
                        for (i in 0..<half) {
                            val left = environments[i]
                            val right = environments[i + half]
                            LaunchedEffect(left.volume, left.enabled) {
                                val v = if (left.enabled) left.volume / 100f else 0f
                                player.updateVolume(i, v)
                            }
                            LaunchedEffect(right.volume, right.enabled) {
                                val v = if (right.enabled) right.volume / 100f else 0f
                                player.updateVolume(i + half, v)
                            }
                            Row {
                                EnvironmentItem(left, Modifier.weight(1f))
                                EnvironmentItem(right, Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Button(
                                variant = ButtonVariant.Ghost,
                                size = ButtonSize.Icon,
                                onClick = {
                                    presetToSave = Preset.create()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Floppy,
                                    contentDescription = "",
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(
                                variant = ButtonVariant.Default,
                                size = ButtonSize.Lg,
                                onClick = {
                                    isPlaying = !isPlaying
                                    if (isPlaying) player.play() else player.pause()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Pause else Icons.Play,
                                    contentDescription = "",
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))

                            var showPresetsList by remember { mutableStateOf(false) }
                            DropdownMenu(
                                expanded = showPresetsList,
                                onDismiss = { showPresetsList = false },
                                maxWidth = 250.dp,
                                trigger = {
                                    Button(
                                        variant = ButtonVariant.Ghost,
                                        size = ButtonSize.Icon,
                                        onClick = { showPresetsList = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Database,
                                            contentDescription = "",
                                        )
                                    }
                                },
                            ) {
                                val list = remember { storage.getSavedPresets() }
                                DropdownMenuLabel("Saved presets")
                                list.forEach { preset ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showPresetsList = false
                                                preset.apply()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(preset.name, modifier = Modifier.weight(1f))
                                        if (preset != Preset.default) {
                                            Icon(
                                                imageVector = Icons.Trash,
                                                contentDescription = "",
                                                modifier = Modifier.size(20.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        showPresetsList = false
                                                        presetToDelete = preset
                                                    }
                                                    .padding(4.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isWide) {
                Card(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.width(400.dp).height(IntrinsicSize.Min).align(Alignment.Center)
                ) {
                    CardContent { content() }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surface)
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun EnvironmentItem(
    env: Environment,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            variant = ButtonVariant.Ghost,
            size = ButtonSize.Icon,
            enabled = env.volume > 0,
            onClick = { env.enabled = !env.enabled }
        ) {
            Icon(
                imageVector = env.icon,
                contentDescription = "",
                tint = when {
                    !env.enabled || env.volume == 0 -> colors.primary.copy(alpha = 0.3f)
                    else -> colors.primary
                },
            )
        }
        Slider(
            value = env.volume / 100f,
            onValueChange = { env.volume = (it * 100f).toInt().coerceIn(0, 100) },
            enabled = env.enabled,
            trackColor = colors.muted,
            fillColor = colors.primary,
            trackHeight = 4.dp,
            thumbSize = 16.dp,
            animation = SliderAnimation.None,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(6.dp))
    }
}
