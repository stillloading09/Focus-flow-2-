package com.focusflow.app.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.repository.TimeBlockRepository
import com.focusflow.app.domain.model.BlockCategory
import com.focusflow.app.domain.model.TimeBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEditBlockUiState(
    val id: Long = 0,
    val isNew: Boolean = true,
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0),
    val endTime: LocalTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1),
    val category: BlockCategory = BlockCategory.WORK,
    val notes: String = "",
    val isRecurring: Boolean = false,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && endTime.isAfter(startTime)
}

private const val NEW_BLOCK_ARG = "new"

@HiltViewModel
class AddEditBlockViewModel @Inject constructor(
    private val repository: TimeBlockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val blockIdArg: String? = savedStateHandle["blockId"]
    private val preselectedDateArg: String? = savedStateHandle["date"]

    private val _uiState = MutableStateFlow(AddEditBlockUiState())
    val uiState: StateFlow<AddEditBlockUiState> = _uiState.asStateFlow()

    init {
        if (blockIdArg == null || blockIdArg == NEW_BLOCK_ARG) {
            val date = preselectedDateArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now()
            _uiState.value = AddEditBlockUiState(isNew = true, date = date, isLoading = false)
        } else {
            loadBlock(blockIdArg.toLong())
        }
    }

    private fun loadBlock(id: Long) {
        viewModelScope.launch {
            val block = repository.getBlock(id)
            _uiState.value = if (block != null) {
                AddEditBlockUiState(
                    id = block.id,
                    isNew = false,
                    title = block.title,
                    date = block.date,
                    startTime = block.startTime,
                    endTime = block.endTime,
                    category = block.category,
                    notes = block.notes,
                    isRecurring = block.isRecurring,
                    isLoading = false
                )
            } else {
                _uiState.value.copy(isLoading = false, error = "Block not found")
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateCategory(value: BlockCategory) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun updateNotes(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun updateRecurring(value: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = value)
    }

    fun updateStartTime(value: LocalTime) {
        val current = _uiState.value
        // Keep duration constant when the user shifts the start time.
        val duration = java.time.Duration.between(current.startTime, current.endTime)
        val newEnd = if (value.isBefore(current.endTime) || duration.isZero) {
            value.plus(if (duration.isNegative || duration.isZero) java.time.Duration.ofMinutes(30) else duration)
        } else {
            current.endTime
        }
        _uiState.value = current.copy(startTime = value, endTime = newEnd)
    }

    fun updateEndTime(value: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = value)
    }

    /** Adjusts duration by a number of minutes, used by the +/- resize steppers. */
    fun adjustDurationMinutes(deltaMinutes: Long) {
        val current = _uiState.value
        val newEnd = current.endTime.plusMinutes(deltaMinutes)
        if (newEnd.isAfter(current.startTime)) {
            _uiState.value = current.copy(endTime = newEnd)
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.value = state.copy(error = "Give the block a title and make sure it ends after it starts.")
            return
        }
        viewModelScope.launch {
            repository.saveBlock(
                TimeBlock(
                    id = state.id,
                    title = state.title.trim(),
                    date = state.date,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    category = state.category,
                    notes = state.notes.trim(),
                    isRecurring = state.isRecurring
                )
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun duplicate() {
        val state = _uiState.value
        if (state.isNew) return
        viewModelScope.launch {
            val duration = java.time.Duration.between(state.startTime, state.endTime)
            repository.saveBlock(
                TimeBlock(
                    id = 0, // 0 forces Room to auto-generate a new row
                    title = "${state.title} (copy)",
                    date = state.date,
                    startTime = state.endTime,
                    endTime = state.endTime.plus(duration),
                    category = state.category,
                    notes = state.notes,
                    isRecurring = state.isRecurring
                )
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state.isNew) return
        viewModelScope.launch {
            repository.deleteBlock(
                TimeBlock(
                    id = state.id,
                    title = state.title,
                    date = state.date,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    category = state.category
                )
            )
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
