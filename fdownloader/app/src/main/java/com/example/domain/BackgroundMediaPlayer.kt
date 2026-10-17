package com.example.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.DownloadTaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class EqualizerPreset(val label: String) {
    FLAT("Stüdyo Dengeli"),
    BASS_BOOST("Derin Bas"),
    VOCAL("Net Vokal & Podcast"),
    CINEMA("Sinema 3D")
}

data class PlaybackState(
    val currentTrack: DownloadTaskEntity? = null,
    val playlist: List<DownloadTaskEntity> = emptyList(),
    val isPlaying: Boolean = false,
    val isBackgroundPlayEnabled: Boolean = true,
    val isRepeatEnabled: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val currentPositionSec: Int = 0,
    val totalDurationSec: Int = 180,
    val isFullPlayerExpanded: Boolean = false,
    val sleepTimerRemainingSec: Int = 0, // 0 = Off
    val equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT
)

class BackgroundMediaPlayer(
    private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var progressTickerJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun playTask(task: DownloadTaskEntity, queue: List<DownloadTaskEntity>) {
        val effectiveQueue = if (queue.isNotEmpty()) queue else listOf(task)
        releaseMediaPlayer()

        val duration = task.durationSeconds.coerceAtLeast(15)
        _playbackState.update {
            it.copy(
                currentTrack = task,
                playlist = effectiveQueue,
                isPlaying = true,
                currentPositionSec = 0,
                totalDurationSec = duration
            )
        }

        // Try initializing real Android MediaPlayer on local file if available
        runCatching {
            val localFile = File(task.localFilePath)
            if (localFile.exists() && localFile.length() > 0) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(localFile.absolutePath)
                    isLooping = _playbackState.value.isRepeatEnabled
                    setOnPreparedListener { mp ->
                        val realDur = (mp.duration / 1000).coerceAtLeast(1)
                        _playbackState.update { s -> s.copy(totalDurationSec = maxOf(s.totalDurationSec, realDur)) }
                        mp.start()
                    }
                    setOnCompletionListener {
                        onTrackReachedEnd()
                    }
                    prepareAsync()
                }
            }
        }

        startProgressTicker()
        updatePlaybackNotification()
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        if (current.currentTrack == null) return
        val nextPlaying = !current.isPlaying
        _playbackState.update { it.copy(isPlaying = nextPlaying) }

        runCatching {
            mediaPlayer?.let { mp ->
                if (nextPlaying && !mp.isPlaying) {
                    mp.start()
                } else if (!nextPlaying && mp.isPlaying) {
                    mp.pause()
                }
            }
        }

        if (nextPlaying) {
            startProgressTicker()
        } else {
            progressTickerJob?.cancel()
        }
        updatePlaybackNotification()
    }

    fun seekToSeconds(seconds: Int) {
        val current = _playbackState.value
        val target = seconds.coerceIn(0, current.totalDurationSec)
        _playbackState.update { it.copy(currentPositionSec = target) }
        runCatching {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying || target < mp.duration / 1000) {
                    mp.seekTo(target * 1000)
                }
            }
        }
    }

    fun skipForward(deltaSec: Int = 10) {
        val current = _playbackState.value
        seekToSeconds(current.currentPositionSec + deltaSec)
    }

    fun skipBackward(deltaSec: Int = 10) {
        val current = _playbackState.value
        seekToSeconds(current.currentPositionSec - deltaSec)
    }

    fun playNext() {
        val state = _playbackState.value
        val current = state.currentTrack ?: return
        val list = state.playlist
        if (list.isEmpty()) return

        val nextTrack = if (state.isShuffleEnabled && list.size > 1) {
            list.filter { it.id != current.id }.randomOrNull() ?: list.first()
        } else {
            val idx = list.indexOfFirst { it.id == current.id }
            if (idx in 0 until list.lastIndex) list[idx + 1] else list.first()
        }
        playTask(nextTrack, list)
    }

    fun playPrevious() {
        val state = _playbackState.value
        val current = state.currentTrack ?: return
        val list = state.playlist
        if (list.isEmpty()) return

        if (state.currentPositionSec > 5) {
            seekToSeconds(0)
            return
        }
        val idx = list.indexOfFirst { it.id == current.id }
        val prevTrack = if (idx > 0) list[idx - 1] else list.last()
        playTask(prevTrack, list)
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val currentSpeed = _playbackState.value.playbackSpeed
        val nextIdx = (speeds.indexOf(currentSpeed) + 1) % speeds.size
        val nextSpeed = speeds[nextIdx]
        _playbackState.update { it.copy(playbackSpeed = nextSpeed) }
        runCatching {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.playbackParams = mp.playbackParams.setSpeed(nextSpeed)
                }
            }
        }
    }

    fun cycleSleepTimer() {
        val optionsMinutes = listOf(0, 15, 30, 60)
        val currentMin = (_playbackState.value.sleepTimerRemainingSec + 59) / 60
        val nextIdx = (optionsMinutes.indexOfFirst { it >= currentMin }.let { if (it == -1) 0 else it } + 1) % optionsMinutes.size
        val nextSec = optionsMinutes[nextIdx] * 60
        _playbackState.update { it.copy(sleepTimerRemainingSec = nextSec) }
    }

    fun cycleEqualizerPreset() {
        val presets = EqualizerPreset.entries
        val nextIdx = (_playbackState.value.equalizerPreset.ordinal + 1) % presets.size
        _playbackState.update { it.copy(equalizerPreset = presets[nextIdx]) }
    }

    fun toggleRepeat() {
        val nextRepeat = !_playbackState.value.isRepeatEnabled
        _playbackState.update { it.copy(isRepeatEnabled = nextRepeat) }
        runCatching { mediaPlayer?.isLooping = nextRepeat }
    }

    fun toggleShuffle() {
        _playbackState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun setBackgroundPlayEnabled(enabled: Boolean) {
        _playbackState.update { it.copy(isBackgroundPlayEnabled = enabled) }
        updatePlaybackNotification()
    }

    fun setFullPlayerExpanded(expanded: Boolean) {
        _playbackState.update { it.copy(isFullPlayerExpanded = expanded) }
    }

    fun stopAndClosePlayer() {
        progressTickerJob?.cancel()
        releaseMediaPlayer()
        notificationHelper.cancelPlaybackNotification()
        _playbackState.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                currentPositionSec = 0,
                isFullPlayerExpanded = false,
                sleepTimerRemainingSec = 0
            )
        }
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(1000L)
                val state = _playbackState.value
                if (!state.isPlaying) break

                // Check sleep timer countdown
                if (state.sleepTimerRemainingSec > 0) {
                    val rem = state.sleepTimerRemainingSec - 1
                    if (rem <= 0) {
                        _playbackState.update { it.copy(sleepTimerRemainingSec = 0) }
                        togglePlayPause()
                        break
                    } else {
                        _playbackState.update { it.copy(sleepTimerRemainingSec = rem) }
                    }
                }

                val nextPos = state.currentPositionSec + 1
                if (nextPos >= state.totalDurationSec) {
                    onTrackReachedEnd()
                } else {
                    _playbackState.update { it.copy(currentPositionSec = nextPos) }
                }
            }
        }
    }

    private fun onTrackReachedEnd() {
        val state = _playbackState.value
        if (state.isRepeatEnabled) {
            seekToSeconds(0)
            if (!state.isPlaying) togglePlayPause()
        } else if (state.playlist.size > 1) {
            playNext()
        } else {
            _playbackState.update { it.copy(isPlaying = false, currentPositionSec = 0) }
            updatePlaybackNotification()
        }
    }

    private fun updatePlaybackNotification() {
        val state = _playbackState.value
        val track = state.currentTrack
        if (track != null && state.isBackgroundPlayEnabled) {
            notificationHelper.updateBackgroundPlaybackNotification(
                title = track.title,
                author = track.authorOrChannel,
                isPlaying = state.isPlaying
            )
        } else {
            notificationHelper.cancelPlaybackNotification()
        }
    }

    private fun releaseMediaPlayer() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }
}
