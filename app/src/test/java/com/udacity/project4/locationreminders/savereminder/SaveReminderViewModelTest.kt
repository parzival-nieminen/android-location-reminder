package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    private val fakeDb = FakeDataSource()
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupTest() {
        Dispatchers.setMain(testDispatcher)
        stopKoin()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDb)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun when_create_save_render_view_model_then_return_not_null() {
        val testee = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDb)

        assertThat(testee).isNotNull
        assertThat(testee).isInstanceOf(SaveReminderViewModel::class.java)
    }

    @Test
    fun when_validate_and_Save_reminder_then_success_id_are_equal() = runBlockingTest {
        val expected = ReminderDataItem(
            "Hello",
            "World",
            "Home",
            42.0,
            42.0
        )
        saveReminderViewModel.validateAndSaveReminder(expected)

        val testee = fakeDb.getReminder(expected.id) as Result.Success

        assertThat(testee.data.id).isEqualTo(expected.id)
    }


}