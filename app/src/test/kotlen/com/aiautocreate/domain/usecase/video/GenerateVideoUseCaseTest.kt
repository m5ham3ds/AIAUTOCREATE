package com.aiautocreate.domain.usecase.video

import com.aiautocreate.domain.model.Project
import com.aiautocreate.domain.repository.IProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateVideoUseCaseTest {

    private val projectRepository = mockk<IProjectRepository>()
    private val useCase = GenerateVideoUseCase(projectRepository)

    @Test
    fun `invoke updates project status to generating`() = runTest {
        val project = Project(id = 1, title = "Test Project", status = "draft")
        coEvery { projectRepository.getProjectById(1) } returns project
        coEvery { projectRepository.updateProject(any()) } returns Unit

        val result = useCase(1)
        assertTrue(result.isSuccess)
        coVerify { projectRepository.updateProject(match { it.status == "generating" }) }
    }

    @Test
    fun `invoke returns failure for non-existent project`() = runTest {
        coEvery { projectRepository.getProjectById(99) } returns null
        val result = useCase(99)
        assertTrue(result.isFailure)
    }
}