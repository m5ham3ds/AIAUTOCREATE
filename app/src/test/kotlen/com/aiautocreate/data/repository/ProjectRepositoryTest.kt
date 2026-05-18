package com.aiautocreate.data.repository

import app.cash.turbine.test
import com.aiautocreate.data.datasource.local.db.dao.ProjectDao
import com.aiautocreate.data.datasource.local.db.entities.ProjectEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectRepositoryTest {

    private val projectDao = mockk<ProjectDao>()
    private val repository = ProjectRepository(projectDao)

    @Test
    fun `getAllProjects returns domain models`() = runTest {
        val entity = ProjectEntity(1, "Test", status = "draft")
        every { projectDao.getAllProjects() } returns flowOf(listOf(entity))

        repository.getAllProjects().test {
            val projects = awaitItem()
            assertEquals(1, projects.size)
            assertEquals("Test", projects[0].title)
            assertEquals("draft", projects[0].status)
        }
    }

    @Test
    fun `insertProject calls dao insert`() = runTest {
        val domain = com.aiautocreate.domain.model.Project(title = "New")
        coEvery { projectDao.insertProject(any()) } returns 1L

        val id = repository.insertProject(domain)
        assertEquals(1L, id)
    }

    @Test
    fun `getProjectById returns null when not found`() = runTest {
        coEvery { projectDao.getProjectById(999) } returns null
        val result = repository.getProjectById(999)
        assertEquals(null, result)
    }
}