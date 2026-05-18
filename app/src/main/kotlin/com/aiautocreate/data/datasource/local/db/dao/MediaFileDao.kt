package com.aiautocreate.data.datasource.local.db.dao

import androidx.room.*
import com.aiautocreate.data.datasource.local.db.entities.MediaFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {

    @Query("SELECT * FROM media_files ORDER BY created_at DESC")
    fun getAllMediaFiles(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE project_id = :projectId ORDER BY file_type, created_at")
    fun getMediaFilesByProjectId(projectId: Long): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getMediaFileById(id: Long): MediaFileEntity?

    @Query("SELECT * FROM media_files WHERE file_type = :fileType ORDER BY created_at DESC")
    fun getMediaFilesByType(fileType: String): Flow<List<MediaFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFile(file: MediaFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFiles(files: List<MediaFileEntity>)

    @Update
    suspend fun updateMediaFile(file: MediaFileEntity)

    @Delete
    suspend fun deleteMediaFile(file: MediaFileEntity)

    @Query("DELETE FROM media_files WHERE project_id = :projectId")
    suspend fun deleteMediaFilesByProjectId(projectId: Long)
}