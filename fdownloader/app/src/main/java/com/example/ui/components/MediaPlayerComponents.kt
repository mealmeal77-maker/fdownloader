package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DownloadTaskEntity
import com.example.data.MediaCategory
import com.example.domain.PlaybackState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import kotlin.math.sin

@Composable
fun MiniPlayerBar(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack ?: return
    val progress = if (playbackState.totalDurationSec > 0) {
        (playbackState.currentPositionSec.toFloat() / playbackState.totalDurationSec.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
            .clickable { onExpand() }
            .testTag("mini_player_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if (track.categoryEnum == MediaCategory.AUDIO) {
                            Icons.Default.AudioFile
                        } else {
                            Icons.Default.Movie
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (playbackState.isBackgroundPlayEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NeonEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ARKA PLAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${track.authorOrChannel} • ${formatDuration(playbackState.currentPositionSec)} / ${formatDuration(playbackState.totalDurationSec)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.testTag("mini_player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.testTag("mini_player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Sonraki",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("mini_player_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Oynatıcıyı Kapat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenMediaPlayerOverlay(
    playbackState: PlaybackState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleSleepTimer: () -> Unit,
    onCycleEqualizerPreset: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    onSelectQueueItem: (DownloadTaskEntity) -> Unit
) {
    val track = playbackState.currentTrack ?: return

    BackHandler(onBack = onCollapse)

    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_screen_media_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("collapse_full_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Küçült",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FDOWNLOADER STÜDYO OYNATICI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = track.qualityLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PlatformBadge(platform = track.platformEnum)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cinema Video Surface / Audio Spectrum Visualizer Card
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        RoundedCornerShape(22.dp)
                    )
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary

                Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    val barCount = 28
                    val spacing = size.width / barCount
                    val barWidth = spacing * 0.58f
                    val centerY = size.height * 0.55f

                    for (i in 0 until barCount) {
                        val animFactor = if (playbackState.isPlaying) {
                            (sin(wavePhase + i * 0.45f) * 0.4f + 0.55f).coerceIn(0.15f, 1f)
                        } else {
                            0.18f
                        }
                        val barHeight = size.height * 0.65f * animFactor
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            ),
                            topLeft = Offset(
                                x = i * spacing + (spacing - barWidth) / 2f,
                                y = centerY - barHeight / 2f
                            ),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                // Overlay badge
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${track.categoryEnum.displayName} • ${playbackState.equalizerPreset.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Info
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${track.authorOrChannel} • ${formatFileSize(track.totalBytes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Seek Slider
            Slider(
                value = playbackState.currentPositionSec.toFloat(),
                onValueChange = { onSeek(it.toInt()) },
                valueRange = 0f..playbackState.totalDurationSec.coerceAtLeast(1).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_seek_slider")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(playbackState.currentPositionSec),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(playbackState.totalDurationSec),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Karışık Çal",
                        tint = if (playbackState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onPlayPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Önceki",
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(onClick = onSkipBackward) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "10 Saniye Geri",
                        modifier = Modifier.size(28.dp)
                    )
                }
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("full_player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Duraklat" else "Oynat",
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onSkipForward) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "10 Saniye İleri",
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onPlayNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Sonraki",
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Tekrarla",
                        tint = if (playbackState.isRepeatEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Background Playback & Studio Tools (Speed, Sleep Timer, Equalizer Preset)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Arka Planda Çalma Modu",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ekran kapalıyken kesintisiz çalmaya devam et",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = playbackState.isBackgroundPlayEnabled,
                        onCheckedChange = onToggleBackgroundPlay
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Studio Control Chips: Speed | Sleep Timer | Equalizer Preset
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // 1. Speed Chip
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCycleSpeed() }
                        .testTag("playback_speed_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hız: ${playbackState.playbackSpeed}x",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. Sleep Timer Chip
                val sleepActive = playbackState.sleepTimerRemainingSec > 0
                Surface(
                    color = if (sleepActive) AmberWarning.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCycleSleepTimer() }
                        .testTag("sleep_timer_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = if (sleepActive) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sleepActive) {
                                "Uyku: ${formatDuration(playbackState.sleepTimerRemainingSec)}"
                            } else {
                                "Uyku Zamanlayıcı: Kapalı"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sleepActive) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Equalizer Preset Chip
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCycleEqualizerPreset() }
                        .testTag("equalizer_preset_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ses: ${playbackState.equalizerPreset.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Çevrimdışı Çalma Listesi (${playbackState.playlist.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(playbackState.playlist, key = { it.id }) { item ->
                    val isCurrent = item.id == track.id
                    Surface(
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectQueueItem(item) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (item.categoryEnum == MediaCategory.AUDIO) Icons.Default.AudioFile else Icons.Default.Movie,
                                contentDescription = null,
                                tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.platformEnum.displayName} • ${item.qualityLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatDuration(item.durationSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
