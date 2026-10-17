package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdBlockRuleEntity
import com.example.data.CustomFolderEntity
import com.example.data.DownloadStatus
import com.example.data.DownloadTaskEntity
import com.example.data.MediaCategory
import com.example.data.SourcePlatform
import com.example.data.StreamVaultDatabase
import com.example.domain.AnalyzedMediaLink
import com.example.domain.BackgroundMediaPlayer
import com.example.domain.DownloadEngine
import com.example.domain.NotificationHelper
import com.example.domain.PlaybackState
import com.example.domain.QualityProfile
import com.example.domain.UniversalLinkAnalyzer
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class MainNavTab(val title: String) {
    HOME("Keşfet & İndir"),
    DOWNLOADS("İndirmeler"),
    LIBRARY("Kütüphane"),
    SHIELD_SETTINGS("Kalkan & Ayarlar")
}

enum class DownloadsFilter(val label: String) {
    ALL("Tümü"),
    ACTIVE("İndiriliyor"),
    PAUSED("Duraklatılan"),
    COMPLETED("Tamamlanan")
}

enum class LibrarySortOption(val label: String) {
    NEWEST("En Yeni"),
    LARGEST("Boyut (Büyük)"),
    TITLE("Ada Göre (A-Z)")
}

data class EngineSettingsState(
    val maxConcurrentDownloads: Int = 2,
    val speedLimitKbps: Int = 0, // 0 = Unlimited, 256, 512, 1024, 2048, 5120
    val adBlockEnabled: Boolean = true,
    val adBlockStrictMode: Boolean = true,
    val autoCleanShareLinks: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val backgroundPlayEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val totalAdsBlockedCount: Int = 148,
    val totalDataSavedBytes: Long = 38_450_000L
)

class StreamVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("streamvault_prefs", Context.MODE_PRIVATE)
    private val database = StreamVaultDatabase.getInstance(application)
    private val dao = database.dao()
    val notificationHelper = NotificationHelper(application)

    private val _settings = MutableStateFlow(loadInitialSettings())
    val settings: StateFlow<EngineSettingsState> = _settings.asStateFlow()

    private val _selectedTab = MutableStateFlow(MainNavTab.HOME)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _analyzedLink = MutableStateFlow<AnalyzedMediaLink?>(null)
    val analyzedLink: StateFlow<AnalyzedMediaLink?> = _analyzedLink.asStateFlow()

    private val _selectedQuality = MutableStateFlow<QualityProfile?>(null)
    val selectedQuality: StateFlow<QualityProfile?> = _selectedQuality.asStateFlow()

    private val _selectedTargetFolderId = MutableStateFlow<Long?>(null)
    val selectedTargetFolderId: StateFlow<Long?> = _selectedTargetFolderId.asStateFlow()

    private val _downloadsFilter = MutableStateFlow(DownloadsFilter.ALL)
    val downloadsFilter: StateFlow<DownloadsFilter> = _downloadsFilter.asStateFlow()

    private val _libraryCategoryFilter = MutableStateFlow<MediaCategory?>(null)
    val libraryCategoryFilter: StateFlow<MediaCategory?> = _libraryCategoryFilter.asStateFlow()

    private val _libraryPlatformFilter = MutableStateFlow<SourcePlatform?>(null)
    val libraryPlatformFilter: StateFlow<SourcePlatform?> = _libraryPlatformFilter.asStateFlow()

    private val _libraryFolderFilter = MutableStateFlow<Long?>(null)
    val libraryFolderFilter: StateFlow<Long?> = _libraryFolderFilter.asStateFlow()

    private val _libraryFavoritesOnly = MutableStateFlow(false)
    val libraryFavoritesOnly: StateFlow<Boolean> = _libraryFavoritesOnly.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _librarySortOption = MutableStateFlow(LibrarySortOption.NEWEST)
    val librarySortOption: StateFlow<LibrarySortOption> = _librarySortOption.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    val allTasks: StateFlow<List<DownloadTaskEntity>> = dao.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customFolders: StateFlow<List<CustomFolderEntity>> = dao.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adBlockRules: StateFlow<List<AdBlockRuleEntity>> = dao.observeAdBlockRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mediaPlayer = BackgroundMediaPlayer(application, notificationHelper)
    val playbackState: StateFlow<PlaybackState> = mediaPlayer.playbackState

    val downloadEngine = DownloadEngine(
        context = application,
        dao = dao,
        notificationHelper = notificationHelper,
        onDownloadFinishedUiEvent = { finishedTask ->
            showBanner("✓ İndirme Tamamlandı: ${finishedTask.title} (${finishedTask.qualityLabel})")
        }
    )

    init {
        val currentSettings = _settings.value
        downloadEngine.updateEngineConfig(
            maxConcurrent = currentSettings.maxConcurrentDownloads,
            speedLimitKbps = currentSettings.speedLimitKbps,
            notifyOnComplete = currentSettings.notificationsEnabled
        )
        mediaPlayer.setBackgroundPlayEnabled(currentSettings.backgroundPlayEnabled)
        seedInitialDataIfNeeded()
    }

    private fun loadInitialSettings(): EngineSettingsState {
        val themeStr = prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.DARK)
        return EngineSettingsState(
            maxConcurrentDownloads = prefs.getInt("max_concurrent", 2),
            speedLimitKbps = prefs.getInt("speed_limit_kbps", 0),
            adBlockEnabled = prefs.getBoolean("adblock_enabled", true),
            adBlockStrictMode = prefs.getBoolean("adblock_strict", true),
            autoCleanShareLinks = prefs.getBoolean("auto_clean_share", true),
            offlineModeEnabled = prefs.getBoolean("offline_mode", false),
            backgroundPlayEnabled = prefs.getBoolean("bg_play", true),
            notificationsEnabled = prefs.getBoolean("notifications", true),
            themeMode = themeMode,
            totalAdsBlockedCount = prefs.getInt("total_ads_blocked", 148),
            totalDataSavedBytes = prefs.getLong("total_data_saved", 38_450_000L)
        )
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            if ( prefs.getBoolean("seeded_v1", false) ) return@launch
            prefs.edit().putBoolean("seeded_v1", true).apply()

            // Seed default custom folders
            val folderMusicId = dao.insertFolder(
                CustomFolderEntity(
                    name = "Favori Müziklerim",
                    description = "320kbps MP3 ve çevrimdışı çalma listeleri",
                    colorHex = "#00E5FF"
                )
            )
            val folderReelsId = dao.insertFolder(
                CustomFolderEntity(
                    name = "Instagram & TikTok Kayıtları",
                    description = "Paylaş menüsünden indirilen filigransız kısa klipler",
                    colorHex = "#E1306C"
                )
            )
            val folder4kId = dao.insertFolder(
                CustomFolderEntity(
                    name = "4K Belgesel & Sinema",
                    description = "Ultra HD yüksek çözünürlüklü videolar",
                    colorHex = "#10B981"
                )
            )

            // Seed default Ad-Block rules
            val defaultRules = listOf(
                AdBlockRuleEntity(
                    pattern = "*.doubleclick.net & googleads.g.doubleclick.net",
                    category = "Video Önü Reklam Sunucusu",
                    isEnabled = true,
                    hitCount = 64
                ),
                AdBlockRuleEntity(
                    pattern = "igsh=*, fbclid=*, utm_source=*, si=*",
                    category = "Sosyal Medya İzleyici Parametresi",
                    isEnabled = true,
                    hitCount = 52
                ),
                AdBlockRuleEntity(
                    pattern = "ads.tiktok.com & business-api.tiktok.com",
                    category = "Sponsorlu Akış Enjeksiyonu",
                    isEnabled = true,
                    hitCount = 19
                ),
                AdBlockRuleEntity(
                    pattern = "pagead2.googlesyndication.com / popunder",
                    category = "Açılır Pencere (Pop-up) Kalkanı",
                    isEnabled = true,
                    hitCount = 13
                )
            )
            defaultRules.forEach { dao.insertAdBlockRule(it) }

            // Create playable local WAV files for pre-downloaded showcase items so user can immediately test offline playback
            val downloadsDir = File(getApplication<Application>().filesDir, "streamvault_media").apply {
                if (!exists()) mkdirs()
            }
            val sampleAudioFile = File(downloadsDir, "vault_seed_audio.mp3")
            val sampleVideoFile = File(downloadsDir, "vault_seed_video.mp4")
            val sampleReelsFile = File(downloadsDir, "vault_seed_reels.mp4")

            downloadEngine.writePlayableAmbientWavFile(sampleAudioFile, durationSeconds = 12)
            downloadEngine.writePlayableAmbientWavFile(sampleVideoFile, durationSeconds = 10)
            downloadEngine.writePlayableAmbientWavFile(sampleReelsFile, durationSeconds = 9)

            // 1. Completed YouTube 4K Video
            dao.insertTask(
                DownloadTaskEntity(
                    title = "Cyberpunk Neon Şehri — 4K HDR Sinematik Klip",
                    authorOrChannel = "Blender Studio Open Movie",
                    originalUrl = "https://youtu.be/dQw4w9WgXcQ?si=tracker_9812",
                    cleanStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    platform = SourcePlatform.YOUTUBE.name,
                    mediaCategory = MediaCategory.VIDEO.name,
                    qualityLabel = "4K Ultra HD (2160p 60fps)",
                    formatExtension = "mp4",
                    status = DownloadStatus.COMPLETED.name,
                    downloadedBytes = 14_680_064L,
                    totalBytes = 14_680_064L,
                    speedBytesPerSec = 0L,
                    localFilePath = sampleVideoFile.absolutePath,
                    folderId = folder4kId,
                    isFavorite = true,
                    adsRemovedCount = 4,
                    sharedFromExternalApp = false,
                    durationSeconds = 145,
                    createdAt = System.currentTimeMillis() - 3600_000L
                )
            )

            // 2. Completed SoundCloud / YouTube MP3 320kbps Audio
            dao.insertTask(
                DownloadTaskEntity(
                    title = "Midnight Synthwave Drive (Original Mix - 320kbps)",
                    authorOrChannel = "AstraAudio Collective",
                    originalUrl = "https://soundcloud.com/astra-audio/midnight-synthwave-drive",
                    cleanStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    platform = SourcePlatform.SOUNDCLOUD.name,
                    mediaCategory = MediaCategory.AUDIO.name,
                    qualityLabel = "MP3 320kbps Studio",
                    formatExtension = "mp3",
                    status = DownloadStatus.COMPLETED.name,
                    downloadedBytes = 8_945_120L,
                    totalBytes = 8_945_120L,
                    speedBytesPerSec = 0L,
                    localFilePath = sampleAudioFile.absolutePath,
                    folderId = folderMusicId,
                    isFavorite = true,
                    adsRemovedCount = 3,
                    sharedFromExternalApp = false,
                    durationSeconds = 218,
                    createdAt = System.currentTimeMillis() - 1800_000L
                )
            )

            // 3. Completed Instagram Reels (Shared via Android Share Sheet)
            dao.insertTask(
                DownloadTaskEntity(
                    title = "Alplerde Kış Kaçamağı — Doğa & Drone Reels",
                    authorOrChannel = "@nature.explorers",
                    originalUrl = "https://www.instagram.com/reel/C8x9LmNopQr/?igsh=MzRlODBiNWFlZA==",
                    cleanStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    platform = SourcePlatform.INSTAGRAM.name,
                    mediaCategory = MediaCategory.SHORT_CLIP.name,
                    qualityLabel = "1080p Full HD",
                    formatExtension = "mp4",
                    status = DownloadStatus.COMPLETED.name,
                    downloadedBytes = 6_420_000L,
                    totalBytes = 6_420_000L,
                    speedBytesPerSec = 0L,
                    localFilePath = sampleReelsFile.absolutePath,
                    folderId = folderReelsId,
                    isFavorite = false,
                    adsRemovedCount = 5,
                    sharedFromExternalApp = true,
                    durationSeconds = 32,
                    createdAt = System.currentTimeMillis() - 900_000L
                )
            )

            // 4. Paused TikTok / X Download ready to test Resume!
            val pausedFile = File(downloadsDir, "vault_seed_paused.mp4")
            dao.insertTask(
                DownloadTaskEntity(
                    title = "Yapay Zeka ve Geleceğin Medya Teknolojileri Belgeseli",
                    authorOrChannel = "@techpulse_tr",
                    originalUrl = "https://x.com/techpulse_tr/status/1845912837192?s=46",
                    cleanStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                    platform = SourcePlatform.TWITTER.name,
                    mediaCategory = MediaCategory.VIDEO.name,
                    qualityLabel = "1080p Full HD",
                    formatExtension = "mp4",
                    status = DownloadStatus.PAUSED.name,
                    downloadedBytes = 1_850_000L,
                    totalBytes = 4_800_000L,
                    speedBytesPerSec = 0L,
                    localFilePath = pausedFile.absolutePath,
                    folderId = folder4kId,
                    isFavorite = false,
                    adsRemovedCount = 3,
                    sharedFromExternalApp = true,
                    durationSeconds = 95,
                    createdAt = System.currentTimeMillis() - 300_000L
                )
            )
        }
    }

    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
    }

    fun updateUrlInput(text: String) {
        _urlInput.value = text
    }

    /**
     * Handles incoming share intents from ANY Android app (Instagram, YouTube, TikTok, X/Twitter,
     * Facebook, Chrome, WhatsApp, Reddit, etc.) via ACTION_SEND or ACTION_VIEW.
     */
    fun handleIncomingExternalShare(sharedText: String) {
        if (sharedText.isBlank()) return
        _urlInput.value = sharedText
        val result = UniversalLinkAnalyzer.analyzeInput(sharedText, sharedFromExternalApp = true)
        if (result != null) {
            _analyzedLink.value = result
            _selectedQuality.value = result.availableQualities.firstOrNull()
            incrementAdBlockStats(result.adsAndTrackersStripped)
            showBanner("Paylaşılan ${result.platform.displayName} bağlantısı yakalandı ve izleyicilerden temizlendi!")
        } else {
            showBanner("Paylaşılan metin içinde geçerli bir medya bağlantısı bulunamadı.")
        }
    }

    fun analyzeCurrentInput(fromExternalShareSimulation: Boolean = false) {
        val raw = _urlInput.value.trim()
        if (raw.isEmpty()) {
            showBanner("Lütfen bir video, müzik veya sosyal medya bağlantısı girin.")
            return
        }
        val result = UniversalLinkAnalyzer.analyzeInput(raw, sharedFromExternalApp = fromExternalShareSimulation)
        if (result != null) {
            _analyzedLink.value = result
            _selectedQuality.value = result.availableQualities.firstOrNull()
            incrementAdBlockStats(result.adsAndTrackersStripped)
        } else {
            showBanner("Geçerli bir bağlantı algılanamadı. Lütfen URL'yi kontrol edin.")
        }
    }

    fun analyzeAndOpenSheetForUrl(url: String, sharedSimulation: Boolean = false) {
        _urlInput.value = url
        val result = UniversalLinkAnalyzer.analyzeInput(url, sharedFromExternalApp = sharedSimulation)
        if (result != null) {
            _analyzedLink.value = result
            _selectedQuality.value = result.availableQualities.firstOrNull()
            incrementAdBlockStats(result.adsAndTrackersStripped)
        }
    }

    fun selectQualityProfile(profile: QualityProfile) {
        _selectedQuality.value = profile
    }

    fun selectTargetFolder(folderId: Long?) {
        _selectedTargetFolderId.value = folderId
    }

    fun dismissAnalyzedSheet() {
        _analyzedLink.value = null
    }

    fun startDownloadFromAnalyzedLink(customTitle: String? = null) {
        val analyzed = _analyzedLink.value ?: return
        val quality = _selectedQuality.value ?: analyzed.availableQualities.firstOrNull() ?: return
        val folderId = _selectedTargetFolderId.value

        if (_settings.value.offlineModeEnabled) {
            showBanner("Çevrimdışı Mod aktif! İndirme sıraya eklendi; çevrimdışı mod kapatıldığında başlayacak.")
        }

        viewModelScope.launch(Dispatchers.IO) {
            val finalTitle = customTitle?.takeIf { it.isNotBlank() } ?: analyzed.suggestedTitle
            val newTask = DownloadTaskEntity(
                title = finalTitle,
                authorOrChannel = analyzed.authorOrChannel,
                originalUrl = analyzed.extractedUrl,
                cleanStreamUrl = analyzed.resolvedDownloadUrl,
                platform = analyzed.platform.name,
                mediaCategory = quality.category.name,
                qualityLabel = quality.label,
                formatExtension = quality.formatExtension,
                status = if (_settings.value.offlineModeEnabled) DownloadStatus.PAUSED.name else DownloadStatus.QUEUED.name,
                downloadedBytes = 0L,
                totalBytes = quality.estimatedSizeBytes,
                speedBytesPerSec = 0L,
                folderId = folderId,
                adsRemovedCount = analyzed.adsAndTrackersStripped,
                sharedFromExternalApp = analyzed.sharedFromExternalApp,
                durationSeconds = analyzed.durationSeconds
            )
            val id = dao.insertTask(newTask)
            if (!_settings.value.offlineModeEnabled) {
                downloadEngine.enqueueOrResume(id)
            }
        }

        _analyzedLink.value = null
        _urlInput.value = ""
        _selectedTab.value = MainNavTab.DOWNLOADS
        showBanner("İndirme başlatıldı: ${quality.label}")
    }

    /**
     * Toplu Çoklu Link İndirici (Batch Multi-URL Downloader):
     * Parses multiple pasted links at once, strips trackers from all of them, and queues them in parallel.
     */
    fun enqueueBatchMultiLinkDownloads(rawBatchText: String, preferAudio: Boolean) {
        val lines = rawBatchText
            .split("\n", ",", " ")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val analyzedItems = lines.mapNotNull { token ->
            UniversalLinkAnalyzer.analyzeInput(token, sharedFromExternalApp = false)
        }.distinctBy { it.sanitizedUrl }

        if (analyzedItems.isEmpty()) {
            showBanner("Toplu metin içinde geçerli bir bağlantı bulunamadı.")
            return
        }

        val totalAdsStripped = analyzedItems.sumOf { it.adsAndTrackersStripped }
        incrementAdBlockStats(totalAdsStripped)

        viewModelScope.launch(Dispatchers.IO) {
            analyzedItems.forEach { analyzed ->
                val chosenProfile = if (preferAudio) {
                    analyzed.availableQualities.find { it.category == MediaCategory.AUDIO }
                        ?: analyzed.availableQualities.first()
                } else {
                    analyzed.availableQualities.find { it.id == "vid_1080p" }
                        ?: analyzed.availableQualities.first()
                }

                val task = DownloadTaskEntity(
                    title = analyzed.suggestedTitle,
                    authorOrChannel = analyzed.authorOrChannel,
                    originalUrl = analyzed.extractedUrl,
                    cleanStreamUrl = analyzed.resolvedDownloadUrl,
                    platform = analyzed.platform.name,
                    mediaCategory = chosenProfile.category.name,
                    qualityLabel = chosenProfile.label,
                    formatExtension = chosenProfile.formatExtension,
                    status = if (_settings.value.offlineModeEnabled) DownloadStatus.PAUSED.name else DownloadStatus.QUEUED.name,
                    downloadedBytes = 0L,
                    totalBytes = chosenProfile.estimatedSizeBytes,
                    speedBytesPerSec = 0L,
                    folderId = _selectedTargetFolderId.value,
                    adsRemovedCount = analyzed.adsAndTrackersStripped,
                    sharedFromExternalApp = false,
                    durationSeconds = analyzed.durationSeconds
                )
                val id = dao.insertTask(task)
                if (!_settings.value.offlineModeEnabled) {
                    downloadEngine.enqueueOrResume(id)
                }
            }
        }

        _selectedTab.value = MainNavTab.DOWNLOADS
        showBanner("${analyzedItems.size} bağlantı toplu indirme kuyruğuna eklendi!")
    }

    fun pauseTask(taskId: Long) {
        downloadEngine.pauseDownload(taskId)
        showBanner("İndirme duraklatıldı. İstediğiniz an kaldığı yerden devam ettirebilirsiniz.")
    }

    fun resumeTask(taskId: Long) {
        if (_settings.value.offlineModeEnabled) {
            showBanner("Çevrimdışı Mod açıkken indirme yapılamaz. Üst çubuktan Çevrimdışı Modu kapatın.")
            return
        }
        downloadEngine.enqueueOrResume(taskId)
        showBanner("İndirme kaldığı yerden devam ediyor…")
    }

    fun pauseAllDownloads() {
        downloadEngine.pauseAllActive()
        showBanner("Tüm aktif indirmeler duraklatıldı.")
    }

    fun resumeAllDownloads() {
        if (_settings.value.offlineModeEnabled) {
            showBanner("Çevrimdışı Mod açık! Lütfen önce Çevrimdışı Modu devre dışı bırakın.")
            return
        }
        downloadEngine.resumeAllPaused(allTasks.value)
        showBanner("Duraklatılmış tüm indirmeler devam ettiriliyor.")
    }

    fun deleteTask(taskId: Long) {
        if (playbackState.value.currentTrack?.id == taskId) {
            mediaPlayer.stopAndClosePlayer()
        }
        downloadEngine.cancelAndDelete(taskId)
        showBanner("Dosya ve indirme kaydı silindi.")
    }

    fun clearCompletedHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearCompletedHistory()
            showBanner("Tamamlanan indirme geçmişi temizlendi.")
        }
    }

    fun toggleFavorite(task: DownloadTaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateFavorite(task.id, !task.isFavorite)
        }
    }

    fun assignTaskToFolder(taskId: Long, folderId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateTaskFolder(taskId, folderId)
            showBanner("Dosya klasör/koleksiyon bilgisi güncellendi.")
        }
    }

    fun createCustomFolder(name: String, description: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertFolder(
                CustomFolderEntity(
                    name = name.trim(),
                    description = description.trim().ifBlank { "Özel medya koleksiyonu" },
                    colorHex = colorHex
                )
            )
            showBanner("Yeni özel klasör oluşturuldu: ${name.trim()}")
        }
    }

    fun deleteCustomFolder(folderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteFolder(folderId)
            if (_libraryFolderFilter.value == folderId) {
                _libraryFolderFilter.value = null
            }
            showBanner("Özel klasör silindi.")
        }
    }

    // Filters & Sorting
    fun setDownloadsFilter(filter: DownloadsFilter) {
        _downloadsFilter.value = filter
    }

    fun setLibraryCategoryFilter(category: MediaCategory?) {
        _libraryCategoryFilter.value = category
    }

    fun setLibraryPlatformFilter(platform: SourcePlatform?) {
        _libraryPlatformFilter.value = platform
    }

    fun setLibraryFolderFilter(folderId: Long?) {
        _libraryFolderFilter.value = folderId
    }

    fun toggleLibraryFavoritesOnly() {
        _libraryFavoritesOnly.update { !it }
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun setLibrarySortOption(option: LibrarySortOption) {
        _librarySortOption.value = option
    }

    // Engine & Ad-Block Settings
    fun setMaxConcurrentDownloads(count: Int) {
        val clamped = count.coerceIn(1, 5)
        _settings.update { it.copy(maxConcurrentDownloads = clamped) }
        prefs.edit().putInt("max_concurrent", clamped).apply()
        downloadEngine.updateEngineConfig(
            maxConcurrent = clamped,
            speedLimitKbps = _settings.value.speedLimitKbps,
            notifyOnComplete = _settings.value.notificationsEnabled
        )
        showBanner("Eş zamanlı çoklu indirme sınırı: $clamped görev")
    }

    fun setSpeedLimitKbps(kbps: Int) {
        _settings.update { it.copy(speedLimitKbps = kbps) }
        prefs.edit().putInt("speed_limit_kbps", kbps).apply()
        downloadEngine.updateEngineConfig(
            maxConcurrent = _settings.value.maxConcurrentDownloads,
            speedLimitKbps = kbps,
            notifyOnComplete = _settings.value.notificationsEnabled
        )
        val label = if (kbps <= 0) "Sınırsız (Maksimum Hız)" else "$kbps KB/s"
        showBanner("İndirme hızı sınırı güncellendi: $label")
    }

    fun toggleAdBlockEnabled(enabled: Boolean) {
        _settings.update { it.copy(adBlockEnabled = enabled) }
        prefs.edit().putBoolean("adblock_enabled", enabled).apply()
    }

    fun toggleAdBlockStrictMode(strict: Boolean) {
        _settings.update { it.copy(adBlockStrictMode = strict) }
        prefs.edit().putBoolean("adblock_strict", strict).apply()
    }

    fun toggleOfflineMode(offline: Boolean) {
        _settings.update { it.copy(offlineModeEnabled = offline) }
        prefs.edit().putBoolean("offline_mode", offline).apply()
        if (offline) {
            downloadEngine.pauseAllActive()
            showBanner("Çevrimdışı Mod aktif: Ağ trafiği durduruldu, indirilen medyaları çevrimdışı oynatabilirsiniz.")
        } else {
            showBanner("Çevrimdışı Mod kapatıldı: İndirmelere devam edebilirsiniz.")
        }
    }

    fun toggleBackgroundPlayback(enabled: Boolean) {
        _settings.update { it.copy(backgroundPlayEnabled = enabled) }
        prefs.edit().putBoolean("bg_play", enabled).apply()
        mediaPlayer.setBackgroundPlayEnabled(enabled)
    }

    fun toggleNotificationsEnabled(enabled: Boolean) {
        _settings.update { it.copy(notificationsEnabled = enabled) }
        prefs.edit().putBoolean("notifications", enabled).apply()
        downloadEngine.updateEngineConfig(
            maxConcurrent = _settings.value.maxConcurrentDownloads,
            speedLimitKbps = _settings.value.speedLimitKbps,
            notifyOnComplete = enabled
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleAdBlockRule(rule: AdBlockRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.toggleAdBlockRule(rule.id, !rule.isEnabled)
        }
    }

    fun addCustomAdBlockRule(pattern: String, category: String) {
        if (pattern.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertAdBlockRule(
                AdBlockRuleEntity(
                    pattern = pattern.trim(),
                    category = category.trim().ifBlank { "Özel Kullanıcı Kuralı" },
                    isEnabled = true,
                    hitCount = 1
                )
            )
            showBanner("Yeni reklam/izleyici engelleme kuralı eklendi.")
        }
    }

    private fun incrementAdBlockStats(strippedCount: Int) {
        if (!_settings.value.adBlockEnabled || strippedCount <= 0) return
        val newTotal = _settings.value.totalAdsBlockedCount + strippedCount
        val newSaved = _settings.value.totalDataSavedBytes + (strippedCount * 240_000L)
        _settings.update {
            it.copy(
                totalAdsBlockedCount = newTotal,
                totalDataSavedBytes = newSaved
            )
        }
        prefs.edit()
            .putInt("total_ads_blocked", newTotal)
            .putLong("total_data_saved", newSaved)
            .apply()
    }

    fun playMediaTask(task: DownloadTaskEntity) {
        val completedLibrary = allTasks.value.filter { it.statusEnum == DownloadStatus.COMPLETED }
        val queue = if (completedLibrary.any { it.id == task.id }) completedLibrary else listOf(task)
        mediaPlayer.playTask(task, queue)
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun showBanner(msg: String) {
        _bannerMessage.value = msg
    }
}
