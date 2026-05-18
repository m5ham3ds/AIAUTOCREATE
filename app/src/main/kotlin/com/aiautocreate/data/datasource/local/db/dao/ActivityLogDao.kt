package com.aiautocreate.data.datasource.local.db.dao

import androidx.room.*
import com.aiautocreate.data.datasource.local.db.entities.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE type = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE project_id = :projectId ORDER BY timestamp DESC")
    fun getLogsByProjectId(projectId: Long): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ActivityLogEntity>)

    @Delete
    suspend fun deleteLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllLogs()
}