package com.focusflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusflow.app.domain.model.BlockCategory
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val category: BlockCategory,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null // e.g. "MON,WED,FRI" - extended in a later milestone
)
