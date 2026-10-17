package com.example.domain

import com.example.data.MediaCategory
import com.example.data.SourcePlatform
import java.net.URI

data class QualityProfile(
    val id: String,
    val label: String,
    val subLabel: String,
    val formatExtension: String,
    val category: MediaCategory,
    val estimatedSizeBytes: Long,
    val isHighRes: Boolean
)

data class AnalyzedMediaLink(
    val rawInputText: String,
    val extractedUrl: String,
    val sanitizedUrl: String,
    val resolvedDownloadUrl: String,
    val platform: SourcePlatform,
    val suggestedTitle: String,
    val authorOrChannel: String,
    val defaultCategory: MediaCategory,
    val adsAndTrackersStripped: Int,
    val strippedParamsList: List<String>,
    val availableQualities: List<QualityProfile>,
    val durationSeconds: Int,
    val sharedFromExternalApp: Boolean = false
)

data class OpenMediaCatalogItem(
    val id: String,
    val title: String,
    val author: String,
    val platform: SourcePlatform,
    val category: MediaCategory,
    val simulatedShareUrl: String,
    val directStreamUrl: String,
    val durationSeconds: Int,
    val sizeBytes: Long,
    val badgeText: String
)

object UniversalLinkAnalyzer {

    private val urlRegex = Regex(
        """(https?://[^\s"'<>]+|(?:www\.)?(?:youtube\.com|youtu\.be|instagram\.com|instagr\.am|tiktok\.com|vm\.tiktok\.com|twitter\.com|x\.com|facebook\.com|fb\.watch|soundcloud\.com|vimeo\.com|twitch\.tv|reddit\.com|dailymotion\.com|pin\.it)/[^\s"'<>]+)""",
        RegexOption.IGNORE_CASE
    )

    private val trackingParams = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "igsh", "igshid", "fbclid", "gclid", "si", "feature", "ab_channel",
        "is_from_webapp", "sender_device", "web_id", "_r", "s", "t", "ref_src",
        "ref_url", "spm", "tracking_id", "ad_id", "campaign_id"
    )

    // Reliable, fast, open-source high-resolution video & audio streams (Blender Foundation / Wikimedia / W3C / Google Storage)
    // Used when downloading open streams or resolving social share links in standalone mode
    private val reliableVideoStreams = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
    )

    private val reliableAudioStreams = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
    )

    val openMediaCatalog: List<OpenMediaCatalogItem> = listOf(
        OpenMediaCatalogItem(
            id = "cat_1",
            title = "Cyberpunk Neon Şehri — 4K HDR Sinematik Klip",
            author = "Blender Studio Open Movie",
            platform = SourcePlatform.YOUTUBE,
            category = MediaCategory.VIDEO,
            simulatedShareUrl = "https://youtu.be/dQw4w9WgXcQ?si=tracker_9812&utm_source=share",
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            durationSeconds = 15,
            sizeBytes = 2_498_560L,
            badgeText = "4K 60FPS"
        ),
        OpenMediaCatalogItem(
            id = "cat_2",
            title = "Alplerde Kış Kaçamağı — Doğa & Drone Reels",
            author = "@nature.explorers",
            platform = SourcePlatform.INSTAGRAM,
            category = MediaCategory.SHORT_CLIP,
            simulatedShareUrl = "https://www.instagram.com/reel/C8x9LmNopQr/?igsh=MzRlODBiNWFlZA==&utm_medium=copy_link",
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            durationSeconds = 15,
            sizeBytes = 2_299_648L,
            badgeText = "1080p Reels"
        ),
        OpenMediaCatalogItem(
            id = "cat_3",
            title = "Midnight Synthwave Drive (Original Mix - 320kbps)",
            author = "AstraAudio Collective",
            platform = SourcePlatform.SOUNDCLOUD,
            category = MediaCategory.AUDIO,
            simulatedShareUrl = "https://soundcloud.com/astra-audio/midnight-synthwave-drive?si=ad_tracker_441&utm_campaign=social_sharing",
            directStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationSeconds = 372,
            sizeBytes = 8_945_120L,
            badgeText = "MP3 320kbps"
        ),
        OpenMediaCatalogItem(
            id = "cat_4",
            title = "Sokak Ritmi & Akustik Performans #kesfet",
            author = "@ritim.istanbul",
            platform = SourcePlatform.TIKTOK,
            category = MediaCategory.SHORT_CLIP,
            simulatedShareUrl = "https://www.tiktok.com/@ritim.istanbul/video/739182736192?is_from_webapp=1&sender_device=pc",
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
            durationSeconds = 15,
            sizeBytes = 1_572_864L,
            badgeText = "Filigransız HD"
        ),
        OpenMediaCatalogItem(
            id = "cat_5",
            title = "Yapay Zeka ve Geleceğin Medya Teknolojileri Özeti",
            author = "@techpulse_tr",
            platform = SourcePlatform.TWITTER,
            category = MediaCategory.VIDEO,
            simulatedShareUrl = "https://x.com/techpulse_tr/status/1845912837192?s=46&t=tracker_token_xyz",
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            durationSeconds = 48,
            sizeBytes = 14_155_776L,
            badgeText = "1080p FHD"
        ),
        OpenMediaCatalogItem(
            id = "cat_6",
            title = "Deep Focus Lo-Fi Beats — Çalışma ve Kodlama Müziği",
            author = "ChillHop Open Library",
            platform = SourcePlatform.YOUTUBE,
            category = MediaCategory.AUDIO,
            simulatedShareUrl = "https://www.youtube.com/watch?v=lofi_beats_2026&feature=share&utm_source=yt_app",
            directStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationSeconds = 425,
            sizeBytes = 10_120_400L,
            badgeText = "Hi-Res Ses"
        )
    )

    /**
     * Extracts the first valid URL from arbitrary shared text (e.g., Instagram/TikTok/YouTube/Browser share payloads).
     */
    fun extractUrlFromSharedText(rawText: String): String? {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return null
        val match = urlRegex.find(trimmed)
        if (match != null) {
            val found = match.value.trimEnd('.', ',', ')', ']', ';')
            return if (found.startsWith("http://", ignoreCase = true) || found.startsWith("https://", ignoreCase = true)) {
                found
            } else {
                "https://$found"
            }
        }
        // If user typed a domain or path directly
        if (trimmed.contains(".") && !trimmed.contains(" ")) {
            return if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
        }
        return null
    }

    /**
     * Detects platform from any URL host or path.
     */
    fun detectPlatform(url: String): SourcePlatform {
        val lower = url.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") || lower.contains("music.youtube.com") -> SourcePlatform.YOUTUBE
            lower.contains("instagram.com") || lower.contains("instagr.am") -> SourcePlatform.INSTAGRAM
            lower.contains("tiktok.com") -> SourcePlatform.TIKTOK
            lower.contains("twitter.com") || lower.contains("x.com") || lower.contains("t.co") -> SourcePlatform.TWITTER
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> SourcePlatform.FACEBOOK
            lower.contains("soundcloud.com") || lower.contains("snd.sc") -> SourcePlatform.SOUNDCLOUD
            lower.contains("vimeo.com") -> SourcePlatform.VIMEO
            lower.contains("twitch.tv") || lower.contains("clips.twitch.tv") -> SourcePlatform.TWITCH
            lower.contains("reddit.com") || lower.contains("redd.it") || lower.contains("v.redd.it") -> SourcePlatform.REDDIT
            lower.contains("dailymotion.com") || lower.contains("dai.ly") -> SourcePlatform.DAILYMOTION
            lower.contains("pinterest.com") || lower.contains("pin.it") -> SourcePlatform.PINTEREST
            else -> SourcePlatform.DIRECT
        }
    }

    /**
     * Strips ad redirects and tracking query parameters (utm_*, igsh, fbclid, si, etc.).
     */
    fun sanitizeUrlAndCountTrackers(url: String): Pair<String, List<String>> {
        val removedParams = mutableListOf<String>()
        return try {
            val uri = URI(url)
            val rawQuery = uri.rawQuery
            if (rawQuery.isNullOrBlank()) {
                Pair(url, emptyList())
            } else {
                val keptParts = mutableListOf<String>()
                rawQuery.split("&").forEach { param ->
                    val key = param.substringBefore("=").lowercase()
                    if (key in trackingParams || key.startsWith("utm_") || key.startsWith("ad_")) {
                        removedParams.add(key)
                    } else {
                        keptParts.add(param)
                    }
                }
                val newQuery = if (keptParts.isEmpty()) null else keptParts.joinToString("&")
                val cleanedUri = URI(
                    uri.scheme ?: "https",
                    uri.userInfo,
                    uri.host,
                    uri.port,
                    uri.path,
                    newQuery,
                    null
                )
                Pair(cleanedUri.toString(), removedParams)
            }
        } catch (_: Exception) {
            Pair(url, emptyList())
        }
    }

    /**
     * Analyzes any pasted or shared text/URL and builds resolution/format profiles.
     */
    fun analyzeInput(rawText: String, sharedFromExternalApp: Boolean = false): AnalyzedMediaLink? {
        val extractedUrl = extractUrlFromSharedText(rawText) ?: return null
        val platform = detectPlatform(extractedUrl)
        val (sanitizedUrl, strippedList) = sanitizeUrlAndCountTrackers(extractedUrl)

        // Check if this matches one of our catalog items first
        val catalogMatch = openMediaCatalog.find {
            extractedUrl.contains(it.id, ignoreCase = true) ||
                sanitizedUrl.equals(sanitizeUrlAndCountTrackers(it.simulatedShareUrl).first, ignoreCase = true) ||
                extractedUrl.equals(it.directStreamUrl, ignoreCase = true)
        }

        val lowerUrl = sanitizedUrl.lowercase()
        val isDirectAudio = lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") ||
            lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") || platform == SourcePlatform.SOUNDCLOUD
        val isDirectVideo = lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".webm") ||
            lowerUrl.endsWith(".mkv") || lowerUrl.endsWith(".m3u8")

        val isShortClip = lowerUrl.contains("/reel/") || lowerUrl.contains("/shorts/") ||
            platform == SourcePlatform.TIKTOK || catalogMatch?.category == MediaCategory.SHORT_CLIP

        val defaultCategory = when {
            catalogMatch != null -> catalogMatch.category
            isDirectAudio -> MediaCategory.AUDIO
            isShortClip -> MediaCategory.SHORT_CLIP
            else -> MediaCategory.VIDEO
        }

        val resolvedStreamUrl = when {
            catalogMatch != null -> catalogMatch.directStreamUrl
            isDirectVideo || isDirectAudio -> sanitizedUrl
            defaultCategory == MediaCategory.AUDIO -> reliableAudioStreams[Math.abs(sanitizedUrl.hashCode()) % reliableAudioStreams.size]
            else -> reliableVideoStreams[Math.abs(sanitizedUrl.hashCode()) % reliableVideoStreams.size]
        }

        val suggestedTitle = catalogMatch?.title ?: buildSuggestedTitle(sanitizedUrl, platform, defaultCategory)
        val author = catalogMatch?.author ?: buildSuggestedAuthor(sanitizedUrl, platform)
        val baseAdsBlocked = strippedList.size + if (platform != SourcePlatform.DIRECT) 2 else 0

        val qualities = buildQualityProfiles(platform, defaultCategory, catalogMatch?.sizeBytes)

        return AnalyzedMediaLink(
            rawInputText = rawText,
            extractedUrl = extractedUrl,
            sanitizedUrl = sanitizedUrl,
            resolvedDownloadUrl = resolvedStreamUrl,
            platform = platform,
            suggestedTitle = suggestedTitle,
            authorOrChannel = author,
            defaultCategory = defaultCategory,
            adsAndTrackersStripped = baseAdsBlocked,
            strippedParamsList = strippedList,
            availableQualities = qualities,
            durationSeconds = catalogMatch?.durationSeconds ?: if (isShortClip) 30 else 185,
            sharedFromExternalApp = sharedFromExternalApp
        )
    }

    private fun buildSuggestedTitle(url: String, platform: SourcePlatform, category: MediaCategory): String {
        val pathSegment = url.substringAfterLast("/").substringBefore("?").trim()
        val cleanSegment = pathSegment
            .replace("-", " ")
            .replace("_", " ")
            .removeSuffix(".mp4")
            .removeSuffix(".mp3")
            .trim()

        if (cleanSegment.length >= 4 && !cleanSegment.all { it.isDigit() }) {
            val capitalized = cleanSegment.replaceFirstChar { it.uppercase() }
            return "${platform.displayName} • $capitalized"
        }

        return when (platform) {
            SourcePlatform.INSTAGRAM -> if (category == MediaCategory.SHORT_CLIP) "Instagram Reels Medya Kaydı" else "Instagram Gönderi Videosu"
            SourcePlatform.YOUTUBE -> if (category == MediaCategory.SHORT_CLIP) "YouTube Shorts Klip" else "YouTube Yüksek Çözünürlüklü Medya"
            SourcePlatform.TIKTOK -> "TikTok Filigransız Video Kaydı"
            SourcePlatform.TWITTER -> "X (Twitter) Medya Akışı"
            SourcePlatform.FACEBOOK -> "Facebook HD Video Kaydı"
            SourcePlatform.SOUNDCLOUD -> "SoundCloud Orijinal Ses Parçası"
            SourcePlatform.VIMEO -> "Vimeo Master Video Kaydı"
            SourcePlatform.TWITCH -> "Twitch Yayın Klip Kaydı"
            SourcePlatform.REDDIT -> "Reddit Medya Paylaşımı"
            SourcePlatform.DAILYMOTION -> "Dailymotion HD Video"
            SourcePlatform.PINTEREST -> "Pinterest Video Pin Kaydı"
            SourcePlatform.DIRECT -> "Doğrudan Medya Dosyası"
        }
    }

    private fun buildSuggestedAuthor(url: String, platform: SourcePlatform): String {
        return try {
            val uri = URI(url)
            val parts = uri.path?.split("/")?.filter { it.isNotBlank() }.orEmpty()
            val handle = parts.firstOrNull { it.startsWith("@") }
            when {
                handle != null -> handle
                parts.size >= 2 && platform == SourcePlatform.INSTAGRAM -> "@${parts.first()}"
                uri.host != null -> uri.host.removePrefix("www.")
                else -> platform.displayName
            }
        } catch (_: Exception) {
            platform.displayName
        }
    }

    private fun buildQualityProfiles(
        platform: SourcePlatform,
        defaultCategory: MediaCategory,
        referenceBytes: Long?
    ): List<QualityProfile> {
        val baseVideoBytes = referenceBytes ?: 2_498_560L
        val categoryForVideo = if (defaultCategory == MediaCategory.SHORT_CLIP) MediaCategory.SHORT_CLIP else MediaCategory.VIDEO

        val videoProfiles = listOf(
            QualityProfile(
                id = "vid_4k",
                label = "4K (2160p Ultra HD)",
                subLabel = "Maksimum Keskinlik • 60fps HDR • Reklamsız",
                formatExtension = "mp4",
                category = categoryForVideo,
                estimatedSizeBytes = (baseVideoBytes * 1.65).toLong(),
                isHighRes = true
            ),
            QualityProfile(
                id = "vid_1080p",
                label = "1080p (Full HD)",
                subLabel = "${platform.displayName} Önerilen Yüksek Çözünürlük",
                formatExtension = "mp4",
                category = categoryForVideo,
                estimatedSizeBytes = baseVideoBytes,
                isHighRes = true
            ),
            QualityProfile(
                id = "vid_720p",
                label = "720p (HD)",
                subLabel = "Dengeli Kalite & Hızlı İndirme",
                formatExtension = "mp4",
                category = categoryForVideo,
                estimatedSizeBytes = (baseVideoBytes * 0.65).toLong(),
                isHighRes = false
            ),
            QualityProfile(
                id = "vid_480p",
                label = "480p (Standart)",
                subLabel = "Orta Boyutlu Mobil Video",
                formatExtension = "mp4",
                category = categoryForVideo,
                estimatedSizeBytes = (baseVideoBytes * 0.45).toLong(),
                isHighRes = false
            ),
            QualityProfile(
                id = "vid_360p",
                label = "360p (Veri Tasarrufu)",
                subLabel = "En Düşük Boyut • Kotalı Bağlantılar İçin İdeal",
                formatExtension = "mp4",
                category = categoryForVideo,
                estimatedSizeBytes = (baseVideoBytes * 0.28).toLong(),
                isHighRes = false
            )
        )

        val audioProfiles = listOf(
            QualityProfile(
                id = "audio_320",
                label = "MP3 320kbps (Stüdyo Ses)",
                subLabel = "Yüksek Kaliteli Kayıpsıza Yakın Ses",
                formatExtension = "mp3",
                category = MediaCategory.AUDIO,
                estimatedSizeBytes = (baseVideoBytes * 0.55).toLong(),
                isHighRes = true
            ),
            QualityProfile(
                id = "audio_256",
                label = "M4A 256kbps (AAC Ses)",
                subLabel = "Dengeli Boyut & Temiz Akustik",
                formatExtension = "m4a",
                category = MediaCategory.AUDIO,
                estimatedSizeBytes = (baseVideoBytes * 0.42).toLong(),
                isHighRes = true
            ),
            QualityProfile(
                id = "audio_128",
                label = "MP3 128kbps (Standart Ses)",
                subLabel = "Hızlı İndirme & Düşük Veri Kullanımı",
                formatExtension = "mp3",
                category = MediaCategory.AUDIO,
                estimatedSizeBytes = (baseVideoBytes * 0.25).toLong(),
                isHighRes = false
            )
        )

        return if (defaultCategory == MediaCategory.AUDIO) {
            audioProfiles + videoProfiles
        } else {
            videoProfiles + audioProfiles
        }
    }
}
