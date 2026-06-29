package dev.terrakok.cozyspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
internal fun AppTheme(
    systemIsDark: Boolean,
    content: @Composable () -> Unit
) {
    val palette = RikkaPalette.Zinc.resolve(isDark = systemIsDark)
    RikkaTheme(colors = palette, content = content)
}

@Composable
fun App(
    systemIsDark: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) = AppTheme(systemIsDark) {
    val storage = remember { Storage() }
    LaunchedEffect(Unit) {
        storage.latestPreset.apply()
    }
    DisposableEffect(Unit) {
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

        var presetToSave by remember { mutableStateOf<Preset?>(null) }
        var presetToDelete by remember { mutableStateOf<Preset?>(null) }

        SavePresetDialog(
            preset = presetToSave,
            onDismiss = { presetToSave = null },
            onSave = { name ->
                presetToSave?.copy(name = name)?.let(storage::saveNewPreset)
                presetToSave = null
            }
        )

        DeletePresetDialog(
            preset = presetToDelete,
            onDismiss = { presetToDelete = null },
            onDelete = {
                presetToDelete?.let(storage::deletePreset)
                presetToDelete = null
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val content = remember {
                movableContentOf {
                    val list by storage.savedPresets.collectAsState()
                    EnvironmentGrid(
                        player = player,
                        isPlaying = isPlaying,
                        onPlayToggle = {
                            isPlaying = !isPlaying
                            if (isPlaying) player.play() else player.pause()
                        },
                        onShowSavePreset = { presetToSave = Preset.create() },
                        list = list,
                        onPresetSelected = { it.apply() },
                        onDeletePreset = { presetToDelete = it }
                    )
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
                ) { content() }
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    preset: Preset?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    Dialog(
        open = preset != null,
        onDismiss = onDismiss,
        maxWidth = 300.dp
    ) {
        val newPreset = preset ?: return@Dialog
        DialogHeader(
            title = "Save a new preset",
            description = "Enter a name and save it."
        )
        var presetName by remember(newPreset.name) { mutableStateOf(newPreset.name) }
        Input(
            value = presetName,
            onValueChange = { presetName = it.trim().take(20) },
            label = "Preset name"
        )
        DialogFooter {
            Button(
                "Cancel",
                onClick = onDismiss,
                variant = ButtonVariant.Outline,
            )
            Button(
                "Save",
                onClick = { onSave(presetName) }
            )
        }
    }
}

@Composable
private fun DeletePresetDialog(
    preset: Preset?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        open = preset != null,
        onDismiss = onDismiss,
        maxWidth = 300.dp
    ) {
        val oldPreset = preset ?: return@Dialog
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
                onClick = onDismiss,
                variant = ButtonVariant.Outline,
            )
            Button(
                "Delete",
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun PresetsDropdown(
    list: List<Preset>,
    onPresetSelected: (Preset) -> Unit,
    onDeletePreset: (Preset) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismiss = { expanded = false },
        maxWidth = 250.dp,
        trigger = {
            Button(
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Icon,
                onClick = { expanded = true }
            ) {
                Icon(
                    imageVector = Icons.Database,
                    contentDescription = "Saved presets",
                )
            }
        },
    ) {
        DropdownMenuLabel("Saved presets")
        list.forEach { preset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = false
                        onPresetSelected(preset)
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(6.dp))
                Text(preset.name, modifier = Modifier.weight(1f))
                if (preset != Preset.default) {
                    Icon(
                        imageVector = Icons.Trash,
                        contentDescription = "Delete preset",
                        modifier = Modifier.size(20.dp)
                            .clip(CircleShape)
                            .clickable {
                                expanded = false
                                onDeletePreset(preset)
                            }
                            .padding(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentGrid(
    player: SoundPlayer,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onShowSavePreset: () -> Unit,
    list: List<Preset>,
    onPresetSelected: (Preset) -> Unit,
    onDeletePreset: (Preset) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                onClick = onShowSavePreset
            ) {
                Icon(
                    imageVector = Icons.Floppy,
                    contentDescription = "Save preset",
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                variant = ButtonVariant.Default,
                size = ButtonSize.Lg,
                onClick = onPlayToggle
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Pause else Icons.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                )
            }
            Spacer(modifier = Modifier.size(8.dp))

            PresetsDropdown(
                list = list,
                onPresetSelected = onPresetSelected,
                onDeletePreset = onDeletePreset,
            )
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
                contentDescription = env.name,
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
