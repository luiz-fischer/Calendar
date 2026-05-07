package com.example.calendar.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = try {
        SyncStatus.valueOf(value)
    } catch (e: Exception) {
        SyncStatus.LOCAL_ONLY
    }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = try {
        RecurrenceType.valueOf(value)
    } catch (e: Exception) {
        RecurrenceType.NONE
    }
}
