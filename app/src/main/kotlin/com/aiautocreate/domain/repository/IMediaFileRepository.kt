package com.aiautocreate.domain.repository

import com.aiautocreate.domain.model.MediaFile
import kotlinx.coroutines.flow.Flow

interface IMediaFileRepository {
    fun getAllMediaFiles(): Flow<List<MediaFile>>
    fun getMediaFilesByProjectId(projectId: Long): Flow<List<MediaFile>>
    suspend fun getMediaFileById(id: Long): MediaFile?
    fun getMediaFilesByType(fileType: String): Flow<List<MediaFile>>
    suspend fun insertMediaFile(file: MediaFile): Long
    suspend fun insertMediaFiles(files: List<MediaFile>)
    suspend fun updateMediaFile(file: MediaFile)
    suspend fun deleteMediaFile(file: MediaFile)
    suspend fun deleteMediaFilesByProjectId(projectId: Long)
}