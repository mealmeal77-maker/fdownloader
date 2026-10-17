package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SourcePlatform
import com.example.domain.OpenMediaCatalogItem
import com.example.domain.UniversalLinkAnalyzer
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.components.formatDuration
import com.example.ui.components.formatFileSize
import com.example.ui.components.platformBrandColor
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.viewmodel.EngineSettingsState

private data class ShareShortcutDemo(
    val appName: String,
    val platform: SourcePlatform,
    val sharedPayload: String
)

private val shareShortcutDemos = listOf(
    ShareShortcutDemo(
        appName = "Instagram Paylaş",
        platform = SourcePlatform.INSTAGRAM,
        sharedPayload = "Bu Reels videosuna göz at! https://www.instagram.com/reel/C8x9LmNopQr/?igsh=MzRlODBiNWFlZA==&utm_source=ig_web_copy_link"
    ),
    ShareShortcutDemo(
        appName = "YouTube Paylaş",
        platform = SourcePlatform.YOUTUBE,
        sharedPayload = "Cyberpunk 4K 60FPS HDR Klip: https://youtu.be/dQw4w9WgXcQ?si=tracker_9812&feature=shared"
    ),
    ShareShortcutDemo(
        appName = "TikTok Paylaş",
        platform = SourcePlatform.TIKTOK,
        sharedPayload = "Sokak Ritmi #kesfet https://www.tiktok.com/@ritim.istanbul/video/739182736192?is_from_webapp=1&sender_device=pc"
    ),
    ShareShortcutDemo(
        appName = "X / Twitter Paylaş",
        platform = SourcePlatform.TWITTER,
        sharedPayload = "Yapay Zeka Medya Özeti https://x.com/techpulse_tr/status/1845912837192?s=46&t=tracker_token_xyz"
    ),
    ShareShortcutDemo(
        appName = "Facebook Paylaş",
        platform = SourcePlatform.FACEBOOK,
        sharedPayload = "Harika bir belgesel kesiti: https://fb.watch/v9xLmN2pQr/?fbclid=IwAR29812_tracker"
    ),
    ShareShortcutDemo(
        appName = "SoundCloud Paylaş",
        platform = SourcePlatform.SOUNDCLOUD,
        sharedPayload = "Midnight Synthwave Drive dinle: https://soundcloud.com/astra-audio/midnight-synthwave-drive?si=ad_tracker_441"
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDiscoverScreen(
    urlInput: String,
    settings: EngineSettingsState,
    activeCount: Int,
    onUrlChange: (String) -> Unit,
    onAnalyzeClick: () -> Unit,
    onBatchDownloadLinks: (String, Boolean) -> Unit,
    onSimulateExternalShare: (String) -> Unit,
    onSelectCatalogItem: (OpenMediaCatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showBatchDialog by remember { mutableStateOf(false) }

    val detectedLivePlatform = if (urlInput.isNotBlank()) {
        UniversalLinkAnalyzer.detectPlatform(urlInput)
    } else null

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("home_discover_screen")
    ) {
        // 1. Hero Studio Banner & Universal Link Detector Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.6f),
                                NeonEmerald.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FDOWNLOADER MEDYA MOTORU",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = NeonEmerald.copy(alpha = 0.16f),
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
                                    text = "${settings.totalAdsBlockedCount} Reklam Engellendi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Her Platformdan 4K Video & 320kbps Müzik İndir",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "YouTube, Instagram, TikTok, X (Twitter), Facebook, SoundCloud, Vimeo ve tüm web bağlantılarını otomatik algılar, reklamları temizler.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // URL Input with live platform detection
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUrlChange,
                        placeholder = {
                            Text(
                                text = "Video, Reels, Shorts veya MP4/MP3 linki yapıştırın…",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = { onUrlChange("") },
                                    modifier = Modifier.testTag("clear_url_input_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Temizle")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("universal_url_input")
                    )

                    if (detectedLivePlatform != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Otomatik Algılanan Kaynak:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PlatformBadge(platform = detectedLivePlatform)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    onUrlChange(clipText)
                                } else {
                                    onUrlChange("https://www.instagram.com/reel/C8x9LmNopQr/?igsh=MzRlODBiNWFlZA==")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .weight(0.32f)
                                .height(48.dp)
                                .testTag("paste_clipboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yapıştır", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { showBatchDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .weight(0.28f)
                                .height(48.dp)
                                .testTag("batch_multi_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Toplu Link", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onAnalyzeClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .weight(0.40f)
                                .height(48.dp)
                                .testTag("analyze_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kalite Seç", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // 2. Supported Platforms Strip
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Desteklenen Platformlar & Otomatik Algılama",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourcePlatform.entries.forEach { platform ->
                        PlatformBadge(platform = platform)
                    }
                }
            }
        }

        // 3. Universal Android Share Sheet ("Paylaş -> Fdownloader") Shortcut Integration & Quick Simulator
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Tüm Uygulamalardan 'Paylaş' Kısayolu Aktif",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instagram, YouTube, TikTok, X, Facebook veya tarayıcıda 'Paylaş -> Fdownloader ile İndir' seçerek anında indirin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hızlı Paylaşım Kısayolu Simülatörü (Tek Dokunuşla Test Edin):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        shareShortcutDemos.forEach { demo ->
                            val brandColor = platformBrandColor(demo.platform)
                            Surface(
                                color = brandColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .border(1.dp, brandColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSimulateExternalShare(demo.sharedPayload) }
                                    .testTag("simulate_share_${demo.platform.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = brandColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = demo.appName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Live Engine Status Strip (Speed Limit & Multi-Download Status)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Hız Sınırı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (settings.speedLimitKbps <= 0) "Sınırsız Hız" else "${settings.speedLimitKbps} KB/s",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Çoklu İndirme",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${settings.maxConcurrentDownloads} Paralel ($activeCount Aktif)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. Open High-Resolution Media Catalog for 1-Tap Testing
        item {
            Column {
                Text(
                    text = "Hızlı İndirme & Yüksek Çözünürlüklü Medya Katalogu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kalite seçim diyaloğunu (360p, 720p, 1080p, 4K) ve çevrimdışı oynatıcıyı denemek için seçin:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(UniversalLinkAnalyzer.openMediaCatalog, key = { it.id }) { item ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectCatalogItem(item) }
                    .testTag("catalog_item_${item.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PlatformBadge(platform = item.platform)
                            CategoryBadge(category = item.category)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.author} • ${formatDuration(item.durationSeconds)} • ~${formatFileSize(item.sizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onSelectCatalogItem(item) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("download_catalog_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "İndir",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("İndir", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // Batch Multi-Link Queue Dialog
    if (showBatchDialog) {
        var batchInput by remember {
            mutableStateOf(
                "https://youtu.be/dQw4w9WgXcQ?si=tracker_9812\n" +
                    "https://www.instagram.com/reel/C8x9LmNopQr/\n" +
                    "https://www.tiktok.com/@ritim.istanbul/video/739182736192"
            )
        }
        var preferAudioOnly by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text("Toplu Çoklu Link İndirici") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Her satıra bir bağlantı gelecek şekilde birden fazla YouTube, Instagram, TikTok veya X linki yapıştırın. Tümü aynı anda sıraya alınır:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = batchInput,
                        onValueChange = { batchInput = it },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_urls_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !preferAudioOnly,
                            onClick = { preferAudioOnly = false },
                            label = { Text("1080p HD Video Olarak İndir") }
                        )
                        FilterChip(
                            selected = preferAudioOnly,
                            onClick = { preferAudioOnly = true },
                            label = { Text("MP3 320kbps Ses Olarak İndir") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchDownloadLinks(batchInput, preferAudioOnly)
                        showBatchDialog = false
                    },
                    modifier = Modifier.testTag("confirm_batch_download_button")
                ) {
                    Text("Tümünü Kuyruğa Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
