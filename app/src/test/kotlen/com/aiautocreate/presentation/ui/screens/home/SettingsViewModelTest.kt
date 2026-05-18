package com.aiautocreate.presentation.ui.screens.settings

import app.cash.turbine.test
import com.aiautocreate.data.datasource.local.datastore.DataStoreManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        dataStoreManager = mockk {
            every { language } returns flowOf("ar")
            every { themeMode } returns flowOf("system")
            every { dynamicColor } returns flowOf(true)
            coEvery { setLanguage(any()) } returns Unit
            coEvery { setThemeMode(any()) } returns Unit
            coEvery { setDynamicColor(any()) } returns Unit
        }

        viewModel = SettingsViewModel(
            dataStoreManager = dataStoreManager,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads preferences correctly`() = runTest {
        viewModel.state.test {
            val first = awaitItem()
            assertEquals(true, first.isLoading)

            val second = awaitItem()
            assertEquals(false, second.isLoading)
            assertEquals("ar", second.language)
            assertEquals("system", second.themeMode)
            assertEquals(true, second.dynamicColor)
        }
    }

    @Test
    fun `changing language updates datastore`() = runTest {
        viewModel.state.test {
            awaitItem() // isLoading
            awaitItem() // loaded

            viewModel.onLanguageChanged("en")
            advanceUntilIdle()
            coVerify { dataStoreManager.setLanguage("en") }
        }
    }
}