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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CustomFolderEntity
import com.example.data.DownloadStatus
import com.example.data.DownloadTaskEntity
import com.example.data.MediaCategory
import com.example.data.SourcePlatform
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.components.formatDuration
import com.example.ui.components.formatFileSize
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.viewmodel.LibrarySortOption

private val folderColorChoices = listOf(
    "#00E5FF" to Color(0xFF00E5FF),
    "#10B981" to Color(0xFF10B981),
    "#E1306C" to Color(0xFFE1306C),
    "#F59E0B" to Color(0xFFF59E0B),
    "#8B5CF6" to Color(0xFF8B5CF6)
)

@Composable
fun SmartLibraryScreen(
    allTasks: List<DownloadTaskEntity>,
    folders: List<CustomFolderEntity>,
    selectedCategory: MediaCategory?,
    selectedPlatform: SourcePlatform?,
    selectedFolderId: Long?,
    favoritesOnly: Boolean,
    searchQuery: String,
    sortOption: LibrarySortOption,
    onCategorySelect: (MediaCategory?) -> Unit,
    onPlatformSelect: (SourcePlatform?) -> Unit,
    onFolderSelect: (Long?) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (LibrarySortOption) -> Unit,
    onCreateFolder: (String, String, String) -> Unit,
    onDeleteFolder: (Long) -> Unit,
    onToggleFavoriteTask: (DownloadTaskEntity) -> Unit,
    onAssignTaskFolder: (Long, Long?) -> Unit,
    onPlayTask: (DownloadTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var taskForFolderAssignment by remember { mutableStateOf<DownloadTaskEntity?>(null) }

    val completedTasks = allTasks.filter { it.statusEnum == DownloadStatus.COMPLETED }

    val filteredLibrary = completedTasks
        .filter { task ->
            val matchCat = selectedCategory == null || task.categoryEnum == selectedCategory
            val matchPlat = selectedPlatform == null || task.platformEnum == selectedPlatform
            val matchFolder = selectedFolderId == null || task.folderId == selectedFolderId
            val matchFav = !favoritesOnly || task.isFavorite
            val matchSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.authorOrChannel.contains(searchQuery, ignoreCase = true) ||
                task.platformEnum.displayName.contains(searchQuery, ignoreCase = true)
            matchCat && matchPlat && matchFolder && matchFav && matchSearch
        }
        .let { list ->
            when (sortOption) {
                LibrarySortOption.NEWEST -> list.sortedByDescending { it.createdAt }
                LibrarySortOption.LARGEST -> list.sortedByDescending { it.totalBytes }
                LibrarySortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            }
        }

    val videoCount = completedTasks.count { it.categoryEnum == MediaCategory.VIDEO }
    val audioCount = completedTasks.count { it.categoryEnum == MediaCategory.AUDIO }
    val clipCount = completedTasks.count { it.categoryEnum == MediaCategory.SHORT_CLIP }
    val totalStorageBytes = completedTasks.sumOf { it.totalBytes }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_library_screen")
    ) {
        // 1. Smart Storage & Auto-Categorization Summary Cards
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Akıllı Dosya Yönetimi & Kütüphane",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${completedTasks.size} Çevrimdışı Medya • Toplam ${formatFileSize(totalStorageBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCreateFolderDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_create_folder_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yeni Klasör", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Smart Category Cards (Video, Audio, Short Clip)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SmartCategorySummaryCard(
                        title = "Videolar",
                        count = videoCount,
                        icon = Icons.Default.Movie,
                        accent = ElectricCyan,
                        isSelected = selectedCategory == MediaCategory.VIDEO,
                        onClick = {
                            onCategorySelect(if (selectedCategory == MediaCategory.VIDEO) null else MediaCategory.VIDEO)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("smart_cat_video")
                    )
                    SmartCategorySummaryCard(
                        title = "Müzik & Ses",
                        count = audioCount,
                        icon = Icons.Default.AudioFile,
                        accent = NeonEmerald,
                        isSelected = selectedCategory == MediaCategory.AUDIO,
                        onClick = {
                            onCategorySelect(if (selectedCategory == MediaCategory.AUDIO) null else MediaCategory.AUDIO)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("smart_cat_audio")
                    )
                    SmartCategorySummaryCard(
                        title = "Kısa Klipler",
                        count = clipCount,
                        icon = Icons.Default.VideoFile,
                        accent = Color(0xFFE1306C),
                        isSelected = selectedCategory == MediaCategory.SHORT_CLIP,
                        onClick = {
                            onCategorySelect(if (selectedCategory == MediaCategory.SHORT_CLIP) null else MediaCategory.SHORT_CLIP)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("smart_cat_short_clip")
                    )
                }
            }
        }

        // 2. Custom Folders / Collections Strip
        item {
            Column {
                Text(
                    text = "Özel Klasörler & Koleksiyonlar",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedFolderId == null && !favoritesOnly,
                        onClick = {
                            onFolderSelect(null)
                            if (favoritesOnly) onToggleFavoritesOnly()
                        },
                        label = { Text("Tüm Koleksiyonlar") },
                        leadingIcon = {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )

                    FilterChip(
                        selected = favoritesOnly,
                        onClick = onToggleFavoritesOnly,
                        label = { Text("Favorilerim") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = CoralAlert,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_favorites_chip")
                    )

                    folders.forEach { folder ->
                        val isSelected = selectedFolderId == folder.id
                        val countInFolder = completedTasks.count { it.folderId == folder.id }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFolderSelect(if (isSelected) null else folder.id) },
                            label = { Text("${folder.name} ($countInFolder)") },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Klasörü Sil",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onDeleteFolder(folder.id) }
                                )
                            },
                            modifier = Modifier.testTag("folder_chip_${folder.id}")
                        )
                    }
                }
            }
        }

        // 3. Automatic Platform Tag Filter + Search & Sort
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Kütüphanede başlık, kanal veya platform ara…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        FilterChip(
                            selected = selectedPlatform == null,
                            onClick = { onPlatformSelect(null) },
                            label = { Text("Tüm Platformlar") }
                        )
                        listOf(
                            SourcePlatform.YOUTUBE,
                            SourcePlatform.INSTAGRAM,
                            SourcePlatform.TIKTOK,
                            SourcePlatform.TWITTER,
                            SourcePlatform.FACEBOOK,
                            SourcePlatform.SOUNDCLOUD
                        ).forEach { plat ->
                            FilterChip(
                                selected = selectedPlatform == plat,
                                onClick = {
                                    onPlatformSelect(if (selectedPlatform == plat) null else plat)
                                },
                                label = { Text(plat.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val nextOrd = (sortOption.ordinal + 1) % LibrarySortOption.entries.size
                                onSortOptionChange(LibrarySortOption.entries[nextOrd])
                            }
                            .testTag("library_sort_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOption.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // 4. Library Media Items
        if (filteredLibrary.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Eşleşen medya dosyası bulunamadı",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kategori veya platform filtrelerini sıfırlayabilir ya da yeni medya indirebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLibrary, key = { it.id }) { item ->
                val folderName = folders.find { it.id == item.folderId }?.name
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
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onPlayTask(item) }
                        .testTag("library_media_card_${item.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PlatformBadge(platform = item.platformEnum)
                                CategoryBadge(category = item.categoryEnum)
                                if (folderName != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = folderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavoriteTask(item) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("favorite_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favori",
                                    tint = if (item.isFavorite) CoralAlert else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                            text = "${item.authorOrChannel} • ${item.qualityLabel} • ${formatDuration(item.durationSeconds)} • ${formatFileSize(item.totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { taskForFolderAssignment = item },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("assign_folder_button_${item.id}")
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = folderName ?: "Klasöre Taşı",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onPlayTask(item) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("library_play_button_${item.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Oynat", style = MaterialTheme.typography.labelLarge)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onDeleteTask(item.id) }) {
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
            }
        }
    }

    // Create Custom Folder Dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        var folderDesc by remember { mutableStateOf("") }
        var selectedHex by remember { mutableStateOf(folderColorChoices.first().first) }

        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Yeni Özel Klasör / Koleksiyon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Klasör Adı") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_folder_name_input")
                    )
                    OutlinedTextField(
                        value = folderDesc,
                        onValueChange = { folderDesc = it },
                        label = { Text("Açıklama (İsteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Koleksiyon Rengi:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        folderColorChoices.forEach { (hex, color) ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedHex == hex) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            onCreateFolder(folderName, folderDesc, selectedHex)
                            showCreateFolderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Assign Task to Folder Dialog
    val assigningTask = taskForFolderAssignment
    if (assigningTask != null) {
        AlertDialog(
            onDismissRequest = { taskForFolderAssignment = null },
            title = { Text("Klasör / Koleksiyon Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = assigningTask.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    OutlinedButton(
                        onClick = {
                            onAssignTaskFolder(assigningTask.id, null)
                            taskForFolderAssignment = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Klasörden Çıkar (Genel Kütüphane)")
                    }
                    folders.forEach { folder ->
                        Button(
                            onClick = {
                                onAssignTaskFolder(assigningTask.id, folder.id)
                                taskForFolderAssignment = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(folder.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { taskForFolderAssignment = null }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
private fun SmartCategorySummaryCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
