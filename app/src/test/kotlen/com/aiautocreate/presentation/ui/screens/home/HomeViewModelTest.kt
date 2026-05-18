package com.aiautocreate.presentation.ui.screens.home

import app.cash.turbine.test
import com.aiautocreate.domain.model.Project
import com.aiautocreate.domain.repository.IProjectRepository
import com.aiautocreate.domain.usecase.model.CheckApiModelsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var projectRepository: IProjectRepository
    private lateinit var checkApiModelsUseCase: CheckApiModelsUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        projectRepository = mockk {
            every { getAllProjects() } returns flowOf(
                listOf(
                    Project(id = 1, title = "Project A", status = "draft"),
                    Project(id = 2, title = "Project B", status = "completed")
                )
            )
        }

        checkApiModelsUseCase = mockk {
            coEvery { hasAnyApiKey() } returns true
        }

        viewModel = HomeViewModel(projectRepository, checkApiModelsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads projects and api status`() = runTest {
        viewModel.state.test {
            // تخطي حالة التحميل الأولى
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(2, loadedState.projectCount)
            assertEquals(1, loadedState.completedProjects)
            assertEquals(true, loadedState.hasApiKeys)
            assertEquals(2, loadedState.recentProjects.size)
        }
    }
}