package com.example.calendar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val googleId: String? = null,
    val title: String,
    val description: String,
    val date: String, // ISO-8601 format (YYYY-MM-DD)
    val lastUpdated: Long,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    
    // Recurrence fields (Google Calendar Style)
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndDate: String? = null // Optional end date for recurrence
)

enum class SyncStatus {
    LOCAL_ONLY, SYNCED, PENDING_UPDATE, CONFLICT
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
