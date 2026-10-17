package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CustomFolderEntity
import com.example.data.MediaCategory
import com.example.data.SourcePlatform
import com.example.domain.AnalyzedMediaLink
import com.example.domain.QualityProfile
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SoundCloudOrange
import com.example.ui.theme.TiktokCyan
import com.example.ui.theme.TwitchPurple
import com.example.ui.theme.TwitterSky
import com.example.ui.theme.VimeoBlue
import com.example.ui.theme.YoutubeRed
import java.util.Locale

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0L) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.2f MB/s", mb)
    } else {
        String.format(Locale.US, "%.0f KB/s", kb)
    }
}

fun formatDuration(seconds: Int): String {
    val safeSec = seconds.coerceAtLeast(0)
    val mins = safeSec / 60
    val secs = safeSec % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

fun platformBrandColor(platform: SourcePlatform): Color {
    return when (platform) {
        SourcePlatform.YOUTUBE -> YoutubeRed
        SourcePlatform.INSTAGRAM -> InstagramPink
        SourcePlatform.TIKTOK -> TiktokCyan
        SourcePlatform.TWITTER -> TwitterSky
        SourcePlatform.FACEBOOK -> FacebookBlue
        SourcePlatform.SOUNDCLOUD -> SoundCloudOrange
        SourcePlatform.VIMEO -> VimeoBlue
        SourcePlatform.TWITCH -> TwitchPurple
        SourcePlatform.REDDIT -> Color(0xFFFF4500)
        SourcePlatform.DAILYMOTION -> Color(0xFF0066DC)
        SourcePlatform.PINTEREST -> Color(0xFFE60023)
        SourcePlatform.DIRECT -> Color(0xFF00E5FF)
    }
}

@Composable
fun PlatformBadge(
    platform: SourcePlatform,
    modifier: Modifier = Modifier
) {
    val brandColor = platformBrandColor(platform)
    Surface(
        color = brandColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, brandColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(brandColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = platform.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryBadge(
    category: MediaCategory,
    modifier: Modifier = Modifier
) {
    val icon = when (category) {
        MediaCategory.VIDEO -> Icons.Default.Movie
        MediaCategory.AUDIO -> Icons.Default.AudioFile
        MediaCategory.SHORT_CLIP -> Icons.Default.VideoFile
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.displayName,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * İndirme Kalitesi Seçim Diyaloğu (Download Quality Selection Dialog):
 * Allows the user to select 360p, 480p, 720p, 1080p, 4K video resolutions or MP3/M4A audio formats
 * before starting any download.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadQualitySelectionDialog(
    analyzedLink: AnalyzedMediaLink,
    selectedQuality: QualityProfile?,
    selectedFolderId: Long?,
    folders: List<CustomFolderEntity>,
    onSelectQuality: (QualityProfile) -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onStartDownload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editableTitle by remember(analyzedLink.suggestedTitle) {
        mutableStateOf(analyzedLink.suggestedTitle)
    }

    val quickResolutionBadges = listOf(
        "vid_360p" to "360p",
        "vid_720p" to "720p",
        "vid_1080p" to "1080p",
        "vid_4k" to "4K",
        "audio_320" to "MP3 Ses"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("download_quality_selection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // Dialog Header: İndirme Kalitesi Seçim Diyaloğu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "İndirme Kalitesi Seçimi",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "360p, 720p, 1080p, 4K veya Ses formatını seçin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_quality_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Dialog Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Platform + External Share Badge + Ad-Block status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PlatformBadge(platform = analyzedLink.platform)
                        if (analyzedLink.sharedFromExternalApp) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Paylaşım Kısayolu",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Paylaş Kısayolu",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Surface(
                            color = NeonEmerald.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${analyzedLink.adsAndTrackersStripped} Reklam Temizlendi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Editable Media Title
                    OutlinedTextField(
                        value = editableTitle,
                        onValueChange = { editableTitle = it },
                        label = { Text("Medya Başlığı") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyzed_title_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kanal: ${analyzedLink.authorOrChannel} • Süre: ${formatDuration(analyzedLink.durationSeconds)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick 1-Tap Resolution Pill Strip (360p | 720p | 1080p | 4K | MP3)
                    Text(
                        text = "Hızlı Çözünürlük Seçimi:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        quickResolutionBadges.forEach { (profileId, shortLabel) ->
                            val matchingProfile = analyzedLink.availableQualities.find { it.id == profileId }
                            if (matchingProfile != null) {
                                val isSelected = selectedQuality?.id == matchingProfile.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectQuality(matchingProfile) },
                                    label = {
                                        Text(
                                            text = shortLabel,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.testTag("quick_quality_chip_$profileId")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tüm Kalite ve Format Seçenekleri:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    analyzedLink.availableQualities.forEach { profile ->
                        val isSelected = selectedQuality?.id == profile.id
                        val borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        }
                        val bgColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }

                        Surface(
                            color = bgColor,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectQuality(profile) }
                                .testTag("quality_option_${profile.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectQuality(profile) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Icon(
                                    imageVector = if (profile.category == MediaCategory.AUDIO) {
                                        Icons.Default.AudioFile
                                    } else {
                                        Icons.Default.HighQuality
                                    },
                                    contentDescription = profile.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (profile.isHighRes) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ULTRA/HD",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = profile.subLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatFileSize(profile.estimatedSizeBytes),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = profile.formatExtension.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Smart Folder / Collection Selection
                    Text(
                        text = "Kaydedilecek Klasör / Koleksiyon:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFolderId == null,
                            onClick = { onSelectFolder(null) },
                            label = { Text("Otomatik Kategorize Et") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        folders.forEach { folder ->
                            FilterChip(
                                selected = selectedFolderId == folder.id,
                                onClick = { onSelectFolder(folder.id) },
                                label = { Text(folder.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dialog Action Footer (İptal & İndirmeyi Başlat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(0.36f)
                            .height(50.dp)
                            .testTag("cancel_quality_dialog_button")
                    ) {
                        Text("İptal", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { onStartDownload(editableTitle) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .weight(0.64f)
                            .height(50.dp)
                            .testTag("confirm_start_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "İndir (${selectedQuality?.label?.substringBefore(" ") ?: "1080p"})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
