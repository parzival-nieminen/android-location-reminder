package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var remindersServiceData: HashMap<String, ReminderDTO> = HashMap()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(remindersServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        remindersServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }
}
