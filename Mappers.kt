package com.focusflow.app.data.repository

import com.focusflow.app.data.local.entity.TimeBlockEntity
import com.focusflow.app.domain.model.TimeBlock

fun TimeBlockEntity.toDomain(): TimeBlock = TimeBlock(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    endTime = endTime,
    category = category,
    notes = notes,
    isCompleted = isCompleted,
    isMissed = isMissed,
    isRecurring = isRecurring
)

fun TimeBlock.toEntity(): TimeBlockEntity = TimeBlockEntity(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    endTime = endTime,
    category = category,
    notes = notes,
    isCompleted = isCompleted,
    isMissed = isMissed,
    isRecurring = isRecurring
)
