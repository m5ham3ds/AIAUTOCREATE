package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.db.dao.ActivityLogDao
import com.aiautocreate.data.datasource.local.db.entities.ActivityLogEntity
import com.aiautocreate.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLogRepository @Inject constructor(
    private val dao: ActivityLogDao
) {
    fun getAllLogs(): Flow<List<ActivityLog>> =
        dao.getAllLogs().map { list -> list.map { it.toDomain() } }

    suspend fun insertLog(log: ActivityLog) {
        dao.insertLog(log.toEntity())
    }

    private fun ActivityLogEntity.toDomain() = ActivityLog(
        id = id, type = type, title = title, description = description,
        projectId = projectId, timestamp = timestamp, isSuccess = isSuccess
    )

    private fun ActivityLog.toEntity() = ActivityLogEntity(
        id = id, type = type, title = title, description = description,
        projectId = projectId, timestamp = timestamp, isSuccess = isSuccess
    )
}