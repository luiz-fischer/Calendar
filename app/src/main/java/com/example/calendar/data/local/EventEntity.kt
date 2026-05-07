package com.example.calendar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val googleId: String? = null,
    val title: String,
    val description: String = "",
    val date: String, // ISO-8601 format (YYYY-MM-DD)
    val startTime: String? = null, // Format HH:mm
    val endTime: String? = null, // Format HH:mm
    val reminderMinutes: Int = 0, // 0 = no reminder
    val color: Int? = null, // Hex color
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    
    // Recurrence fields
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndDate: String? = null
)

enum class SyncStatus {
    LOCAL_ONLY, SYNCED, PENDING_UPDATE, CONFLICT
}

enum class RecurrenceType(val label: String) {
    NONE("Não se repete"), 
    DAILY("Diariamente"), 
    WEEKLY("Semanalmente"), 
    MONTHLY("Mensalmente"), 
    YEARLY("Anualmente")
}
