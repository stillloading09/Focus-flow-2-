package com.focusflow.app.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.TimeBlockRepository
import com.focusflow.app.domain.model.TimeBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TimelineUiState(
    val date: LocalDate = LocalDate.now(),
    val blocks: List<TimeBlock> = emptyList(),
    val currentTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = true
) {
    val activeBlock: TimeBlock?
        get() = blocks.firstOrNull { it.isActiveAt(currentTime) }
}

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: TimeBlockRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    // Ticks once a second so the active block / progress ring stay live.
    private val clock: Flow<LocalTime> = flow {
        while (true) {
            emit(LocalTime.now())
            delay(1000)
        }
    }

    val uiState: StateFlow<TimelineUiState> = combine(
        selectedDate.flatMapLatest { repository.observeBlocksForDate(it) },
        selectedDate,
        clock
    ) { blocks, date, now ->
        TimelineUiState(date = date, blocks = blocks, currentTime = now, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineUiState()
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun toggleCompleted(block: TimeBlock) {
        viewModelScope.launch { repository.setCompleted(block.id, !block.isCompleted) }
    }

    fun saveBlock(block: TimeBlock) {
        viewModelScope.launch { repository.saveBlock(block) }
    }

    fun deleteBlock(block: TimeBlock) {
        viewModelScope.launch { repository.deleteBlock(block) }
    }

    /**
     * Drag-to-reorder: moves the block at [fromIndex] to [toIndex] within the current
     * day's list, then reflows every block's start/end times sequentially — the first
     * block keeps its original start time, and each subsequent block starts exactly
     * when the previous one ends, preserving each block's own duration.
     */
    fun reorderBlocks(fromIndex: Int, toIndex: Int) {
        val current = uiState.value.blocks
        if (fromIndex == toIndex || fromIndex !in current.indices || toIndex !in current.indices) return

        val reordered = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }

        var cursor = reordered.first().startTime
        val reflowed = reordered.map { block ->
            val duration = block.duration
            val newStart = cursor
            val newEnd = cursor.plus(duration)
            cursor = newEnd
            block.copy(startTime = newStart, endTime = newEnd)
        }

        viewModelScope.launch { repository.saveBlocks(reflowed) }
    }

    /** Drag-to-resize: adjusts a block's end time by [deltaMinutes], snapped by the caller. */
    fun resizeBlock(block: TimeBlock, deltaMinutes: Long) {
        val newEnd = block.endTime.plusMinutes(deltaMinutes)
        if (!newEnd.isAfter(block.startTime)) return
        viewModelScope.launch { repository.saveBlock(block.copy(endTime = newEnd)) }
    }
}
