package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var db: RemindersDatabase

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        remindersLocalRepository = RemindersLocalRepository(db.reminderDao(), Dispatchers.Main)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun when_save_and_read_reminder_then_the_reminder_id_is_equal(): Unit = runBlocking {
        val expected = ReminderDTO("Hello", "World", "Home", 42.0, 42.0)

        remindersLocalRepository.saveReminder(expected)

        val testee = remindersLocalRepository.getReminder(expected.id) as Result.Success

        assertThat(testee).isNotNull
        assertThat(testee.data.id).isEqualTo(expected.id)
    }

    @Test
    fun when_save_and_read_two_reminders_then_get_a_list_two_reminders(): Unit = runBlocking {
        val expectedOne = ReminderDTO("Hello 1", "World 1", "Home 1", 42.0, 42.0)
        val expectedTow = ReminderDTO("Hello 2", "World 2", "Home 2", 42.0, 42.0)

        remindersLocalRepository.saveReminder(expectedOne)
        remindersLocalRepository.saveReminder(expectedTow)

        val testee = remindersLocalRepository.getReminders() as Result.Success

        assertThat(testee).isNotNull
        assertThat(testee.data.size).isEqualTo(2)
    }

    @Test
    fun when_save_and_delete_two_reminders_then_get_a_list_zero_reminders(): Unit = runBlocking {
        val expectedOne = ReminderDTO("Hello 1", "World 1", "Home 1", 42.0, 42.0)
        val expectedTow = ReminderDTO("Hello 2", "World 2", "Home 2", 42.0, 42.0)

        remindersLocalRepository.saveReminder(expectedOne)
        remindersLocalRepository.saveReminder(expectedTow)
        remindersLocalRepository.deleteAllReminders()

        val testee = remindersLocalRepository.getReminders() as Result.Success

        assertThat(testee).isNotNull
        assertThat(testee.data.size).isZero
    }

    @Test
    fun when_read_reminder_with_wrong_id_then_result_is_an_error(): Unit = runBlocking {

        val testee = remindersLocalRepository.getReminder("42") as Result.Error

        assertThat(testee).isNotNull
        assertThat(testee.message).isEqualTo("Reminder not found!")
    }
}
