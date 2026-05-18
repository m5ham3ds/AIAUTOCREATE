package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.db.dao.MediaFileDao
import com.aiautocreate.data.datasource.local.db.entities.MediaFileEntity
import com.aiautocreate.domain.model.MediaFile
import com.aiautocreate.domain.repository.IMediaFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaFileRepository @Inject constructor(
    private val mediaFileDao: MediaFileDao
) : IMediaFileRepository {

    override fun getAllMediaFiles(): Flow<List<MediaFile>> =
        mediaFileDao.getAllMediaFiles().map { it.map { entity -> entity.toDomain() } }

    override fun getMediaFilesByProjectId(projectId: Long): Flow<List<MediaFile>> =
        mediaFileDao.getMediaFilesByProjectId(projectId).map { it.map { entity -> entity.toDomain() } }

    override suspend fun getMediaFileById(id: Long): MediaFile? =
        mediaFileDao.getMediaFileById(id)?.toDomain()

    override fun getMediaFilesByType(fileType: String): Flow<List<MediaFile>> =
        mediaFileDao.getMediaFilesByType(fileType).map { it.map { entity -> entity.toDomain() } }

    override suspend fun insertMediaFile(file: MediaFile): Long =
        mediaFileDao.insertMediaFile(file.toEntity())

    override suspend fun insertMediaFiles(files: List<MediaFile>) =
        mediaFileDao.insertMediaFiles(files.map { it.toEntity() })

    override suspend fun updateMediaFile(file: MediaFile) =
        mediaFileDao.updateMediaFile(file.toEntity())

    override suspend fun deleteMediaFile(file: MediaFile) =
        mediaFileDao.deleteMediaFile(file.toEntity())

    override suspend fun deleteMediaFilesByProjectId(projectId: Long) =
        mediaFileDao.deleteMediaFilesByProjectId(projectId)
}

private fun MediaFileEntity.toDomain() = MediaFile(
    id = id,
    projectId = projectId,
    fileType = fileType,
    filePath = filePath,
    originalName = originalName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMs = durationMs,
    createdAt = createdAt
)

private fun MediaFile.toEntity() = MediaFileEntity(
    id = id,
    projectId = projectId,
    fileType = fileType,
    filePath = filePath,
    originalName = originalName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMs = durationMs,
    createdAt = createdAt
)