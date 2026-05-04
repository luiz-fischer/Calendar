package com.example.calendar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val googleId: String? = null,
    val title: String,
    val description: String,
    val date: String, // ISO-8601 format
    val lastUpdated: Long,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

enum class SyncStatus {
    LOCAL_ONLY, SYNCED, PENDING_UPDATE, CONFLICT
}
