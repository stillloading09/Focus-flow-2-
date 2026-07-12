package com.focusflow.app.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class TimeBlock(
    val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val category: BlockCategory,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val isRecurring: Boolean = false
) {
    val duration: Duration get() = Duration.between(startTime, endTime)

    fun isActiveAt(time: LocalTime): Boolean =
        !time.isBefore(startTime) && time.isBefore(endTime)

    fun progressAt(time: LocalTime): Float {
        if (time.isBefore(startTime)) return 0f
        if (!time.isBefore(endTime)) return 1f
        val total = Duration.between(startTime, endTime).toMinutes().toFloat()
        val elapsed = Duration.between(startTime, time).toMinutes().toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }
}
