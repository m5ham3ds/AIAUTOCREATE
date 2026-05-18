package com.aiautocreate.domain.repository

import com.aiautocreate.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

interface ISyncRepository {
    val syncState: Flow<SyncState>
    suspend fun triggerSync(): Boolean
    suspend fun resolveConflict(entityType: String, entityId: Long)
    suspend fun clearSyncHistory()
}