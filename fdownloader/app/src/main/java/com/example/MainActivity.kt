package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DownloadStatus
import com.example.ui.components.DownloadQualitySelectionDialog
import com.example.ui.components.FullScreenMediaPlayerOverlay
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.DownloadsHubScreen
import com.example.ui.screens.HomeDiscoverScreen
import com.example.ui.screens.ShieldAndSettingsScreen
import com.example.ui.screens.SmartLibraryScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StreamVaultTheme
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.StreamVaultViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: StreamVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            StreamVaultTheme(themeMode = settings.themeMode) {
                StreamVaultApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    /**
     * Captures shared links from ANY app (Instagram, YouTube, TikTok, X/Twitter, Facebook,
     * Chrome, WhatsApp, Reddit, etc.) via Android's system Share sheet (ACTION_SEND / ACTION_VIEW).
     */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.handleIncomingExternalShare(sharedText)
                        intent.removeExtra(Intent.EXTRA_TEXT)
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val textList = intent.getStringArrayListExtra(Intent.EXTRA_TEXT)
                val firstUrl = textList?.firstOrNull { it.isNotBlank() }
                if (!firstUrl.isNullOrBlank()) {
                    viewModel.handleIncomingExternalShare(firstUrl)
                    intent.removeExtra(Intent.EXTRA_TEXT)
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUri = intent.dataString
                if (!dataUri.isNullOrBlank()) {
                    viewModel.handleIncomingExternalShare(dataUri)
                    intent.data = null
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamVaultApp(viewModel: StreamVaultViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val analyzedLink by viewModel.analyzedLink.collectAsStateWithLifecycle()
    val selectedQuality by viewModel.selectedQuality.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedTargetFolderId.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val customFolders by viewModel.customFolders.collectAsStateWithLifecycle()
    val adBlockRules by viewModel.adBlockRules.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val downloadsFilter by viewModel.downloadsFilter.collectAsStateWithLifecycle()
    val libraryCategoryFilter by viewModel.libraryCategoryFilter.collectAsStateWithLifecycle()
    val libraryPlatformFilter by viewModel.libraryPlatformFilter.collectAsStateWithLifecycle()
    val libraryFolderFilter by viewModel.libraryFolderFilter.collectAsStateWithLifecycle()
    val libraryFavoritesOnly by viewModel.libraryFavoritesOnly.collectAsStateWithLifecycle()
    val librarySearchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle()
    val librarySortOption by viewModel.librarySortOption.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showBanner("Bildirim izni verildi: Tamamlanan indirmeler bildirim çubuğunda görünecek.")
        }
    }

    // Auto-dismiss status banner after 4 seconds
    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(4000L)
            viewModel.clearBanner()
        }
    }

    // Handle Android Back navigation across non-Home tabs
    if (selectedTab != MainNavTab.HOME && !playbackState.isFullPlayerExpanded) {
        BackHandler {
            viewModel.selectTab(MainNavTab.HOME)
        }
    }

    val activeDownloadCount = allTasks.count {
        it.statusEnum == DownloadStatus.DOWNLOADING || it.statusEnum == DownloadStatus.QUEUED
    }
    val completedLibraryCount = allTasks.count {
        it.statusEnum == DownloadStatus.COMPLETED
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Fdownloader",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (settings.offlineModeEnabled) {
                                    Text(
                                        text = "Çevrimdışı Mod Aktif",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberWarning
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Offline Mode Quick Toggle Pill
                        Surface(
                            color = if (settings.offlineModeEnabled) {
                                AmberWarning.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleOfflineMode(!settings.offlineModeEnabled) }
                                .testTag("top_bar_offline_mode_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Çevrimdışı Mod",
                                    tint = if (settings.offlineModeEnabled) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (settings.offlineModeEnabled) "Çevrimdışı" else "Çevrimiçi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (settings.offlineModeEnabled) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Quick Dark / Light / AMOLED Theme Toggle Button
                        IconButton(
                            onClick = {
                                val nextMode = when (settings.themeMode) {
                                    ThemeMode.DARK -> ThemeMode.AMOLED
                                    ThemeMode.AMOLED -> ThemeMode.LIGHT
                                    ThemeMode.LIGHT -> ThemeMode.DARK
                                    ThemeMode.SYSTEM -> ThemeMode.DARK
                                }
                                viewModel.setThemeMode(nextMode)
                            },
                            modifier = Modifier.testTag("top_bar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (settings.themeMode == ThemeMode.LIGHT) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = "Tema Değiştir",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Persistent Mini Player Bar when a track/video is loaded
                    AnimatedVisibility(
                        visible = playbackState.currentTrack != null && !playbackState.isFullPlayerExpanded,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        MiniPlayerBar(
                            playbackState = playbackState,
                            onTogglePlayPause = { viewModel.mediaPlayer.togglePlayPause() },
                            onSkipNext = { viewModel.mediaPlayer.playNext() },
                            onExpand = { viewModel.mediaPlayer.setFullPlayerExpanded(true) },
                            onClose = { viewModel.mediaPlayer.stopAndClosePlayer() }
                        )
                    }

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == MainNavTab.HOME,
                            onClick = { viewModel.selectTab(MainNavTab.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainNavTab.HOME) Icons.Filled.Explore else Icons.Outlined.Explore,
                                    contentDescription = MainNavTab.HOME.title
                                )
                            },
                            label = { Text(MainNavTab.HOME.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_tab_home")
                        )

                        NavigationBarItem(
                            selected = selectedTab == MainNavTab.DOWNLOADS,
                            onClick = { viewModel.selectTab(MainNavTab.DOWNLOADS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeDownloadCount > 0) {
                                            Badge { Text(activeDownloadCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == MainNavTab.DOWNLOADS) Icons.Filled.Download else Icons.Outlined.Download,
                                        contentDescription = MainNavTab.DOWNLOADS.title
                                    )
                                }
                            },
                            label = { Text(MainNavTab.DOWNLOADS.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_tab_downloads")
                        )

                        NavigationBarItem(
                            selected = selectedTab == MainNavTab.LIBRARY,
                            onClick = { viewModel.selectTab(MainNavTab.LIBRARY) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (completedLibraryCount > 0) {
                                            Badge { Text(completedLibraryCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == MainNavTab.LIBRARY) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                        contentDescription = MainNavTab.LIBRARY.title
                                    )
                                }
                            },
                            label = { Text(MainNavTab.LIBRARY.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_tab_library")
                        )

                        NavigationBarItem(
                            selected = selectedTab == MainNavTab.SHIELD_SETTINGS,
                            onClick = { viewModel.selectTab(MainNavTab.SHIELD_SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainNavTab.SHIELD_SETTINGS) Icons.Filled.Shield else Icons.Outlined.Shield,
                                    contentDescription = MainNavTab.SHIELD_SETTINGS.title
                                )
                            },
                            label = { Text(MainNavTab.SHIELD_SETTINGS.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_tab_shield")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    MainNavTab.HOME -> {
                        HomeDiscoverScreen(
                            urlInput = urlInput,
                            settings = settings,
                            activeCount = activeDownloadCount,
                            onUrlChange = viewModel::updateUrlInput,
                            onAnalyzeClick = { viewModel.analyzeCurrentInput(fromExternalShareSimulation = false) },
                            onBatchDownloadLinks = viewModel::enqueueBatchMultiLinkDownloads,
                            onSimulateExternalShare = { sharedPayload ->
                                viewModel.handleIncomingExternalShare(sharedPayload)
                            },
                            onSelectCatalogItem = { catalogItem ->
                                viewModel.analyzeAndOpenSheetForUrl(
                                    url = catalogItem.simulatedShareUrl,
                                    sharedSimulation = false
                                )
                            }
                        )
                    }

                    MainNavTab.DOWNLOADS -> {
                        DownloadsHubScreen(
                            allTasks = allTasks,
                            currentFilter = downloadsFilter,
                            settings = settings,
                            onFilterChange = viewModel::setDownloadsFilter,
                            onPauseTask = viewModel::pauseTask,
                            onResumeTask = viewModel::resumeTask,
                            onDeleteTask = viewModel::deleteTask,
                            onPlayTask = viewModel::playMediaTask,
                            onPauseAll = viewModel::pauseAllDownloads,
                            onResumeAll = viewModel::resumeAllDownloads,
                            onClearCompleted = viewModel::clearCompletedHistory,
                            onSpeedLimitChange = viewModel::setSpeedLimitKbps,
                            onConcurrentLimitChange = viewModel::setMaxConcurrentDownloads
                        )
                    }

                    MainNavTab.LIBRARY -> {
                        SmartLibraryScreen(
                            allTasks = allTasks,
                            folders = customFolders,
                            selectedCategory = libraryCategoryFilter,
                            selectedPlatform = libraryPlatformFilter,
                            selectedFolderId = libraryFolderFilter,
                            favoritesOnly = libraryFavoritesOnly,
                            searchQuery = librarySearchQuery,
                            sortOption = librarySortOption,
                            onCategorySelect = viewModel::setLibraryCategoryFilter,
                            onPlatformSelect = viewModel::setLibraryPlatformFilter,
                            onFolderSelect = viewModel::setLibraryFolderFilter,
                            onToggleFavoritesOnly = viewModel::toggleLibraryFavoritesOnly,
                            onSearchQueryChange = viewModel::setLibrarySearchQuery,
                            onSortOptionChange = viewModel::setLibrarySortOption,
                            onCreateFolder = viewModel::createCustomFolder,
                            onDeleteFolder = viewModel::deleteCustomFolder,
                            onToggleFavoriteTask = viewModel::toggleFavorite,
                            onAssignTaskFolder = viewModel::assignTaskToFolder,
                            onPlayTask = viewModel::playMediaTask,
                            onDeleteTask = viewModel::deleteTask
                        )
                    }

                    MainNavTab.SHIELD_SETTINGS -> {
                        ShieldAndSettingsScreen(
                            settings = settings,
                            adBlockRules = adBlockRules,
                            onToggleAdBlock = viewModel::toggleAdBlockEnabled,
                            onToggleStrictAdBlock = viewModel::toggleAdBlockStrictMode,
                            onToggleRule = viewModel::toggleAdBlockRule,
                            onAddCustomRule = viewModel::addCustomAdBlockRule,
                            onChangeSpeedLimit = viewModel::setSpeedLimitKbps,
                            onChangeConcurrentDownloads = viewModel::setMaxConcurrentDownloads,
                            onToggleOfflineMode = viewModel::toggleOfflineMode,
                            onToggleBackgroundPlay = viewModel::toggleBackgroundPlayback,
                            onToggleNotifications = viewModel::toggleNotificationsEnabled,
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onChangeThemeMode = viewModel::setThemeMode
                        )
                    }
                }

                // Floating Status / Notification Banner
                AnimatedVisibility(
                    visible = bannerMessage != null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("status_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = bannerMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // İndirme Kalitesi Seçim Diyaloğu (360p, 480p, 720p, 1080p, 4K ve Ses Seçimi)
        val currentAnalyzed = analyzedLink
        if (currentAnalyzed != null) {
            DownloadQualitySelectionDialog(
                analyzedLink = currentAnalyzed,
                selectedQuality = selectedQuality,
                selectedFolderId = selectedFolderId,
                folders = customFolders,
                onSelectQuality = viewModel::selectQualityProfile,
                onSelectFolder = viewModel::selectTargetFolder,
                onStartDownload = { title ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        settings.notificationsEnabled &&
                        !viewModel.notificationHelper.hasNotificationPermission()
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.startDownloadFromAnalyzedLink(title)
                },
                onDismiss = viewModel::dismissAnalyzedSheet
            )
        }

        // Full Screen Media Player Overlay
        AnimatedVisibility(
            visible = playbackState.isFullPlayerExpanded && playbackState.currentTrack != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FullScreenMediaPlayerOverlay(
                playbackState = playbackState,
                onCollapse = { viewModel.mediaPlayer.setFullPlayerExpanded(false) },
                onTogglePlayPause = { viewModel.mediaPlayer.togglePlayPause() },
                onSeek = { sec -> viewModel.mediaPlayer.seekToSeconds(sec) },
                onSkipForward = { viewModel.mediaPlayer.skipForward(10) },
                onSkipBackward = { viewModel.mediaPlayer.skipBackward(10) },
                onPlayNext = { viewModel.mediaPlayer.playNext() },
                onPlayPrevious = { viewModel.mediaPlayer.playPrevious() },
                onCycleSpeed = { viewModel.mediaPlayer.cyclePlaybackSpeed() },
                onCycleSleepTimer = { viewModel.mediaPlayer.cycleSleepTimer() },
                onCycleEqualizerPreset = { viewModel.mediaPlayer.cycleEqualizerPreset() },
                onToggleRepeat = { viewModel.mediaPlayer.toggleRepeat() },
                onToggleShuffle = { viewModel.mediaPlayer.toggleShuffle() },
                onToggleBackgroundPlay = viewModel::toggleBackgroundPlayback,
                onSelectQueueItem = { track -> viewModel.playMediaTask(track) }
            )
        }
    }
}
