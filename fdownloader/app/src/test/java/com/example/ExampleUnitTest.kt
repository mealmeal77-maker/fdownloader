package com.example

import com.example.data.MediaCategory
import com.example.data.SourcePlatform
import com.example.domain.UniversalLinkAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun universalLinkAnalyzer_extractsAndSanitizesInstagramReelShare() {
        val rawSharePayload = "Bu Reels videosuna göz at! https://www.instagram.com/reel/C8x9LmNopQr/?igsh=MzRlODBiNWFlZA==&utm_source=ig_web_copy_link"
        val analyzed = UniversalLinkAnalyzer.analyzeInput(rawSharePayload, sharedFromExternalApp = true)
        assertNotNull(analyzed)
        assertEquals(SourcePlatform.INSTAGRAM, analyzed?.platform)
        assertEquals(MediaCategory.SHORT_CLIP, analyzed?.defaultCategory)
        assertTrue(analyzed!!.sanitizedUrl.startsWith("https://www.instagram.com/reel/C8x9LmNopQr"))
        assertTrue(!analyzed.sanitizedUrl.contains("igsh="))
        assertTrue(analyzed.adsAndTrackersStripped >= 2)

        val qualityIds = analyzed.availableQualities.map { it.id }
        assertTrue(qualityIds.contains("vid_360p"))
        assertTrue(qualityIds.contains("vid_720p"))
        assertTrue(qualityIds.contains("vid_1080p"))
        assertTrue(qualityIds.contains("vid_4k"))
    }

    @Test
    fun universalLinkAnalyzer_detectsAllMajorPlatforms() {
        assertEquals(SourcePlatform.YOUTUBE, UniversalLinkAnalyzer.detectPlatform("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals(SourcePlatform.TIKTOK, UniversalLinkAnalyzer.detectPlatform("https://www.tiktok.com/@user/video/123"))
        assertEquals(SourcePlatform.TWITTER, UniversalLinkAnalyzer.detectPlatform("https://x.com/user/status/999"))
        assertEquals(SourcePlatform.FACEBOOK, UniversalLinkAnalyzer.detectPlatform("https://fb.watch/abc123/"))
        assertEquals(SourcePlatform.SOUNDCLOUD, UniversalLinkAnalyzer.detectPlatform("https://soundcloud.com/artist/track"))
    }
}
