package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DownloadStatus
import com.example.data.DownloadTaskEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatSpeed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.viewmodel.DownloadsFilter
import com.example.viewmodel.EngineSettingsState

private val quickSpeedOptions = listOf(
    0 to "Sınırsız",
    256 to "256 KB/s",
    512 to "512 KB/s",
    1024 to "1 MB/s",
    2048 to "2 MB/s"
)

@Composable
fun DownloadsHubScreen(
    allTasks: List<DownloadTaskEntity>,
    currentFilter: DownloadsFilter,
    settings: EngineSettingsState,
    onFilterChange: (DownloadsFilter) -> Unit,
    onPauseTask: (Long) -> Unit,
    onResumeTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onPlayTask: (DownloadTaskEntity) -> Unit,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onClearCompleted: () -> Unit,
    onSpeedLimitChange: (Int) -> Unit,
    onConcurrentLimitChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTasks = allTasks.filter { task ->
        when (currentFilter) {
            DownloadsFilter.ALL -> true
            DownloadsFilter.ACTIVE -> task.statusEnum == DownloadStatus.DOWNLOADING || task.statusEnum == DownloadStatus.QUEUED
            DownloadsFilter.PAUSED -> task.statusEnum == DownloadStatus.PAUSED || task.statusEnum == DownloadStatus.FAILED
            DownloadsFilter.COMPLETED -> task.statusEnum == DownloadStatus.COMPLETED
        }
    }

    val activeSpeedTotal = allTasks
        .filter { it.statusEnum == DownloadStatus.DOWNLOADING }
        .sumOf { it.speedBytesPerSec }

    val activeOrQueuedCount = allTasks.count {
        it.statusEnum == DownloadStatus.DOWNLOADING || it.statusEnum == DownloadStatus.QUEUED
    }
    val pausedCount = allTasks.count { it.statusEnum == DownloadStatus.PAUSED }
    val completedCount = allTasks.count { it.statusEnum == DownloadStatus.COMPLETED }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("downloads_hub_screen")
    ) {
        // 1. Unified Download Control & Speed Limiter Panel
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Birleşik İndirme Merkezi & Geçmiş",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$activeOrQueuedCount Aktif/Sırada • $pausedCount Duraklatıldı • $completedCount Tamamlandı",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatSpeed(activeSpeedTotal),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Speed Limiter Chips
                    Text(
                        text = "Anlık İndirme Hızı Sınırlandırma (Throttling):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        quickSpeedOptions.forEach { (kbps, label) ->
                            FilterChip(
                                selected = settings.speedLimitKbps == kbps,
                                onClick = { onSpeedLimitChange(kbps) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("speed_limit_chip_$kbps")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Concurrent downloads selector row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Eş Zamanlı Çoklu İndirme Kapasitesi:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { count ->
                                val selected = settings.maxConcurrentDownloads == count
                                Surface(
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onConcurrentLimitChange(count) }
                                        .testTag("concurrent_limit_$count")
                                ) {
                                    Text(
                                        text = "${count}x",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Batch Actions: Pause All, Resume All, Clear Completed
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onPauseAll,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pause_all_downloads_button")
                        ) {
                            Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tümünü Duraklat", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onResumeAll,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("resume_all_downloads_button")
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tümünü Devam Ettir", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 2. Filter Tabs + Clear Completed
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    DownloadsFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            modifier = Modifier.testTag("downloads_filter_${filter.name.lowercase()}")
                        )
                    }
                }
                if (completedCount > 0) {
                    IconButton(
                        onClick = onClearCompleted,
                        modifier = Modifier.testTag("clear_completed_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Tamamlanan Geçmişi Temizle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Task Cards List
        if (filteredTasks.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Bu filtrede indirme kaydı bulunmuyor",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keşfet sekmesinden veya herhangi bir uygulamada 'Paylaş -> StreamVault' seçerek yeni bir indirme başlatabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                DownloadTaskCard(
                    task = task,
                    onPause = { onPauseTask(task.id) },
                    onResume = { onResumeTask(task.id) },
                    onDelete = { onDeleteTask(task.id) },
                    onPlay = { onPlayTask(task) }
                )
            }
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    val status = task.statusEnum
    val statusColor = when (status) {
        DownloadStatus.DOWNLOADING -> ElectricCyan
        DownloadStatus.PAUSED -> AmberWarning
        DownloadStatus.QUEUED -> MaterialTheme.colorScheme.secondary
        DownloadStatus.COMPLETED -> NeonEmerald
        DownloadStatus.FAILED -> CoralAlert
    }

    val statusIcon = when (status) {
        DownloadStatus.DOWNLOADING -> Icons.Default.Download
        DownloadStatus.PAUSED -> Icons.Default.Pause
        DownloadStatus.QUEUED -> Icons.Default.Schedule
        DownloadStatus.COMPLETED -> Icons.Default.CheckCircle
        DownloadStatus.FAILED -> Icons.Default.ErrorOutline
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                statusColor.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .testTag("download_task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = task.platformEnum)
                    CategoryBadge(category = task.categoryEnum)
                    if (task.sharedFromExternalApp) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Paylaşıldı",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Paylaş",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = status.label,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${task.authorOrChannel} • ${task.qualityLabel} (${task.formatExtension.uppercase()})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar & telemetry
            LinearProgressIndicator(
                progress = { if (status == DownloadStatus.COMPLETED) 1f else task.progressFraction },
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pct = ((if (status == DownloadStatus.COMPLETED) 1f else task.progressFraction) * 100).toInt()
                Text(
                    text = "%$pct • ${formatFileSize(task.downloadedBytes)} / ${formatFileSize(task.totalBytes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val rightTelemetry = when (status) {
                    DownloadStatus.DOWNLOADING -> {
                        val remainingBytes = (task.totalBytes - task.downloadedBytes).coerceAtLeast(0L)
                        val etaSec = if (task.speedBytesPerSec > 0) remainingBytes / task.speedBytesPerSec else 0L
                        "${formatSpeed(task.speedBytesPerSec)} • ~${etaSec}sn kaldı"
                    }
                    DownloadStatus.PAUSED -> "Kaldığı yerden devam edebilir"
                    DownloadStatus.QUEUED -> "Çoklu indirme sırasında"
                    DownloadStatus.COMPLETED -> "Çevrimdışı Hazır"
                    DownloadStatus.FAILED -> "Bağlantı kesildi • Tekrar dene"
                }

                Text(
                    text = rightTelemetry,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Pause / Resume / Play / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (status) {
                    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        OutlinedButton(
                            onClick = onPause,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("pause_task_button_${task.id}")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Duraklat", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duraklat", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    DownloadStatus.PAUSED, DownloadStatus.FAILED -> {
                        Button(
                            onClick = onResume,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberWarning,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("resume_task_button_${task.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Devam Ettir", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Devam Ettir", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        Button(
                            onClick = onPlay,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("play_task_button_${task.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Çevrimdışı Oynat", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Çevrimdışı Oynat", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_task_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
