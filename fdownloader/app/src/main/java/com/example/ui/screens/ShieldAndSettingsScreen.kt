package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AdBlockRuleEntity
import com.example.ui.components.formatFileSize
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.EngineSettingsState

private val speedChoices = listOf(
    0 to "Sınırsız",
    256 to "256 KB/s",
    512 to "512 KB/s",
    1024 to "1 MB/s",
    2048 to "2 MB/s",
    5120 to "5 MB/s"
)

private val themeChoices = listOf(
    ThemeMode.DARK to "Stüdyo Karanlık",
    ThemeMode.AMOLED to "Saf AMOLED Siyah",
    ThemeMode.LIGHT to "Açık Mod",
    ThemeMode.SYSTEM to "Sistem Varsayılanı"
)

@Composable
fun ShieldAndSettingsScreen(
    settings: EngineSettingsState,
    adBlockRules: List<AdBlockRuleEntity>,
    onToggleAdBlock: (Boolean) -> Unit,
    onToggleStrictAdBlock: (Boolean) -> Unit,
    onToggleRule: (AdBlockRuleEntity) -> Unit,
    onAddCustomRule: (String, String) -> Unit,
    onChangeSpeedLimit: (Int) -> Unit,
    onChangeConcurrentDownloads: (Int) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onChangeThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddRuleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("shield_settings_screen")
    ) {
        // 1. Ad-Block & Tracker Shield Hero Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (settings.adBlockEnabled) NeonEmerald.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(22.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (settings.adBlockEnabled) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ad-Block & İzleyici Kalkanı",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (settings.adBlockEnabled) {
                                        "Aktif • Video önü reklamlar ve takip kodları temizleniyor"
                                    } else {
                                        "Devre Dışı"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (settings.adBlockEnabled) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.adBlockEnabled,
                            onCheckedChange = onToggleAdBlock,
                            modifier = Modifier.testTag("adblock_master_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${settings.totalAdsBlockedCount}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = NeonEmerald
                                )
                                Text(
                                    text = "Engellenen Reklam & İzleyici",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = formatFileSize(settings.totalDataSavedBytes),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tasarruf Edilen Veri",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Katı Kalkan Modu (Sponsorlu & Pop-up Engelleme)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Paylaşılan linklerdeki yönlendirme reklamlarını ve gömülü izleyicileri otomatik ayıklar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.adBlockStrictMode,
                            onCheckedChange = onToggleStrictAdBlock
                        )
                    }
                }
            }
        }

        // 2. Dark Mode & Appearance Settings
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Karanlık Mod & Görünüm Teması",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        themeChoices.forEach { (mode, label) ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { onChangeThemeMode(mode) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("theme_mode_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Download Speed Limit & Multi-Download Engine Configuration
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "İndirme Hızı Sınırlama & Çoklu İndirme Motoru",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bant Genişliği Hız Sınırı:",
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
                        speedChoices.forEach { (kbps, label) ->
                            FilterChip(
                                selected = settings.speedLimitKbps == kbps,
                                onClick = { onChangeSpeedLimit(kbps) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Eş Zamanlı (Paralel) Maksimum İndirme Sayısı:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { count ->
                            FilterChip(
                                selected = settings.maxConcurrentDownloads == count,
                                onClick = { onChangeConcurrentDownloads(count) },
                                label = { Text("$count Eşzamanlı") }
                            )
                        }
                    }
                }
            }
        }

        // 4. Offline Mode, Background Play, and Completion Notifications
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Offline Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Çevrimdışı Mod (Offline Mode)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ağ kullanımını tamamen durdurur ve yalnızca indirilen yerel medyaları oynatır",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.offlineModeEnabled,
                            onCheckedChange = onToggleOfflineMode,
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }

                    // Background Playback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Arka Planda Çalma Desteği",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Videoların sesini ve müzikleri ekran kapalıyken kesintisiz çal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.backgroundPlayEnabled,
                            onCheckedChange = onToggleBackgroundPlay,
                            modifier = Modifier.testTag("background_play_switch")
                        )
                    }

                    // Completion Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "İndirme Tamamlandı Bildirimleri",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Her indirme bittiğinde sistem bildirim çubuğuna anında haber gönder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                onToggleNotifications(enabled)
                                if (enabled) onRequestNotificationPermission()
                            },
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }
                }
            }
        }

        // 5. Active Ad-Block Filter Rules List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aktif Reklam & İzleyici Filtre Kuralları",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = { showAddRuleDialog = true },
                    modifier = Modifier.testTag("add_adblock_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kural Ekle")
                }
            }
        }

        items(adBlockRules, key = { it.id }) { rule ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleRule(rule) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.pattern,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${rule.category} • ${rule.hitCount} kez engellendi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = rule.isEnabled,
                        onCheckedChange = { onToggleRule(rule) }
                    )
                }
            }
        }
    }

    if (showAddRuleDialog) {
        var patternText by remember { mutableStateOf("") }
        var categoryText by remember { mutableStateOf("Özel Reklam / Alan Adı Filtresi") }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Özel Reklam / İzleyici Kuralı Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = patternText,
                        onValueChange = { patternText = it },
                        label = { Text("Alan Adı veya Parametre (ör. *.adserver.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (patternText.isNotBlank()) {
                            onAddCustomRule(patternText, categoryText)
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
