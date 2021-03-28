package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    private val fakeDb = FakeDataSource()
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupTest() {
        Dispatchers.setMain(testDispatcher)
        stopKoin()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDb)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun when_create_render_list_view_model_then_return_not_null() {
        val testee = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDb)

        assertThat(testee).isNotNull
        assertThat(testee).isInstanceOf(RemindersListViewModel::class.java)
    }

    @Test
    fun no_reminder_found_when_list_is_empty() = runBlockingTest {
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size).isEqualTo(0)
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isTrue()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun one_reminder_found_when_list_has_one_entry() = runBlockingTest {
        fakeDb.saveReminder(ReminderDTO(null, null, null, null, null))
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size).isEqualTo(1)
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun shows_loading_state_is_true_when_load_task_is_running() = runBlockingTest {
        testDispatcher.pauseDispatcher()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
    }
}