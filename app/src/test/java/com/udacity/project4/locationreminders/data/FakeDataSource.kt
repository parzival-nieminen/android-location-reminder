package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/**
 * FakeDataSource for unit testing
 */
class FakeDataSource : ReminderDataSource {

    private var remindersServiceData = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(remindersServiceData)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remindersServiceData.firstOrNull { it.id == id }
            ?: return Result.Error("404 Not Found")
        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }
}
