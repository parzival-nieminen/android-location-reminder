package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var db: RemindersDatabase
    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun when_save_and_read_reminder_then_the_reminder_id_is_equal() = runBlockingTest {
        val expected = ReminderDTO("Hello", "World", "Home", 42.0, 42.0)
        db.reminderDao().saveReminder(expected)

        val testee = db.reminderDao().getReminderById(expected.id)

        assertThat(testee?.id).isEqualTo(expected.id)
    }

    @Test
    fun when_save_and_read_two_reminders_then_get_a_list_two_reminders() = runBlockingTest {
        val expectedOne = ReminderDTO("Hello 1", "World 1", "Home 1", 42.0, 42.0)
        val expectedTow = ReminderDTO("Hello 2", "World 2", "Home 2", 42.0, 42.0)
        db.reminderDao().saveReminder(expectedOne)
        db.reminderDao().saveReminder(expectedTow)

        val testee = db.reminderDao().getReminders()

        assertThat(testee?.size).isEqualTo(2)
    }

    @Test
    fun when_save_and_delete_two_reminders_then_get_a_list_zero_reminders() = runBlockingTest {
        val expectedOne = ReminderDTO("Hello 1", "World 1", "Home 1", 42.0, 42.0)
        val expectedTow = ReminderDTO("Hello 2", "World 2", "Home 2", 42.0, 42.0)
        db.reminderDao().saveReminder(expectedOne)
        db.reminderDao().saveReminder(expectedTow)
        db.reminderDao().deleteAllReminders()

        val testee = db.reminderDao().getReminders()

        assertThat(testee?.size).isZero
    }

    @Test
    fun when_read_reminder_with_wrong_id_then_result_is_an_error() = runBlockingTest {

        val testee = db.reminderDao().getReminderById("42")

        assertThat(testee).isNull()
    }
}