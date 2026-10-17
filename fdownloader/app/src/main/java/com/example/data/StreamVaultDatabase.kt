package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import android.content.Context
import kotlinx.coroutines.flow.Flow

enum class SourcePlatform(val displayName: String, val badgeCode: String) {
    YOUTUBE("YouTube", "YT"),
    INSTAGRAM("Instagram", "IG"),
    TIKTOK("TikTok", "TT"),
    TWITTER("X / Twitter", "X"),
    FACEBOOK("Facebook", "FB"),
    SOUNDCLOUD("SoundCloud", "SC"),
    VIMEO("Vimeo", "VM"),
    TWITCH("Twitch", "TW"),
    REDDIT("Reddit", "RD"),
    DAILYMOTION("Dailymotion", "DM"),
    PINTEREST("Pinterest", "PN"),
    DIRECT("Doğrudan Web/Akış", "WEB")
}

enum class MediaCategory(val displayName: String) {
    VIDEO("Video"),
    AUDIO("Müzik & Ses"),
    SHORT_CLIP("Kısa Klip / Reels")
}

enum class DownloadStatus(val label: String) {
    QUEUED("Sırada"),
    DOWNLOADING("İndiriliyor"),
    PAUSED("Duraklatıldı"),
    COMPLETED("Tamamlandı"),
    FAILED("Hata")
}

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val authorOrChannel: String,
    val originalUrl: String,
    val cleanStreamUrl: String,
    val platform: String,          // SourcePlatform.name
    val mediaCategory: String,     // MediaCategory.name
    val qualityLabel: String,      // e.g. "4K 2160p Ultra HD", "1080p Full HD", "MP3 320kbps"
    val formatExtension: String,   // "mp4", "mp3", "m4a", "webm"
    val status: String,            // DownloadStatus.name
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val localFilePath: String = "",
    val folderId: Long? = null,
    val isFavorite: Boolean = false,
    val adsRemovedCount: Int = 0,
    val sharedFromExternalApp: Boolean = false,
    val durationSeconds: Int = 180,
    val createdAt: Long = System.currentTimeMillis()
) {
    val platformEnum: SourcePlatform
        get() = runCatching { SourcePlatform.valueOf(platform) }.getOrDefault(SourcePlatform.DIRECT)

    val categoryEnum: MediaCategory
        get() = runCatching { MediaCategory.valueOf(mediaCategory) }.getOrDefault(MediaCategory.VIDEO)

    val statusEnum: DownloadStatus
        get() = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.QUEUED)

    val progressFraction: Float
        get() = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

@Entity(tableName = "custom_folders")
data class CustomFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "adblock_rules")
data class AdBlockRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val category: String, // "Reklam Sunucusu", "İzleyici Parametresi", "Sponsorlu Yönlendirme"
    val isEnabled: Boolean = true,
    val hitCount: Int = 0
)

@Dao
interface StreamVaultDao {
    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    suspend fun getQueuedTasks(): List<DownloadTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity): Long

    @Update
    suspend fun updateTask(task: DownloadTaskEntity)

    @Query(
        "UPDATE download_tasks SET status = :status, downloadedBytes = :downloadedBytes, " +
            "totalBytes = :totalBytes, speedBytesPerSec = :speedBytesPerSec, localFilePath = :localFilePath " +
            "WHERE id = :id"
    )
    suspend fun updateTaskProgress(
        id: Long,
        status: String,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        localFilePath: String
    )

    @Query("UPDATE download_tasks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE download_tasks SET folderId = :folderId WHERE id = :id")
    suspend fun updateTaskFolder(id: Long, folderId: Long?)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM download_tasks WHERE status = 'COMPLETED'")
    suspend fun clearCompletedHistory()

    // Folders
    @Query("SELECT * FROM custom_folders ORDER BY createdAt ASC")
    fun observeFolders(): Flow<List<CustomFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: CustomFolderEntity): Long

    @Query("DELETE FROM custom_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    // AdBlock Rules
    @Query("SELECT * FROM adblock_rules ORDER BY hitCount DESC")
    fun observeAdBlockRules(): Flow<List<AdBlockRuleEntity>>

    @Query("SELECT * FROM adblock_rules")
    suspend fun getAllAdBlockRules(): List<AdBlockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdBlockRule(rule: AdBlockRuleEntity): Long

    @Query("UPDATE adblock_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleAdBlockRule(id: Long, enabled: Boolean)

    @Query("UPDATE adblock_rules SET hitCount = hitCount + 1 WHERE id = :id")
    suspend fun incrementRuleHit(id: Long)
}

@Database(
    entities = [
        DownloadTaskEntity::class,
        CustomFolderEntity::class,
        AdBlockRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StreamVaultDatabase : RoomDatabase() {
    abstract fun dao(): StreamVaultDao

    companion object {
        @Volatile
        private var INSTANCE: StreamVaultDatabase? = null

        fun getInstance(context: Context): StreamVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StreamVaultDatabase::class.java,
                    "streamvault_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
