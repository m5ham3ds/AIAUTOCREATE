package com.aiautocreate.domain.repository

import com.aiautocreate.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface IProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    suspend fun getProjectById(id: Long): Project?
    fun getProjectsByStatus(status: String): Flow<List<Project>>
    suspend fun insertProject(project: Project): Long
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(project: Project)
    suspend fun deleteProjectById(id: Long)
    suspend fun getProjectCount(): Int
}