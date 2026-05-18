package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.db.dao.ProjectDao
import com.aiautocreate.data.datasource.local.db.entities.ProjectEntity
import com.aiautocreate.domain.model.Project
import com.aiautocreate.domain.repository.IProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) : IProjectRepository {

    override fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAllProjects().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProjectById(id: Long): Project? =
        projectDao.getProjectById(id)?.toDomain()

    override fun getProjectsByStatus(status: String): Flow<List<Project>> =
        projectDao.getProjectsByStatus(status).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertProject(project: Project): Long =
        projectDao.insertProject(project.toEntity())

    override suspend fun updateProject(project: Project) =
        projectDao.updateProject(project.toEntity())

    override suspend fun deleteProject(project: Project) =
        projectDao.deleteProject(project.toEntity())

    override suspend fun deleteProjectById(id: Long) =
        projectDao.deleteProjectById(id)

    override suspend fun getProjectCount(): Int =
        projectDao.getProjectCount()
}

// ========== دوال التحويل ==========
private fun ProjectEntity.toDomain() = Project(
    id = id,
    title = title,
    description = description,
    scriptText = scriptText,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputVideoPath = outputVideoPath,
    thumbnailPath = thumbnailPath
)

private fun Project.toEntity() = ProjectEntity(
    id = id,
    title = title,
    description = description,
    scriptText = scriptText,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputVideoPath = outputVideoPath,
    thumbnailPath = thumbnailPath
)