package com.focusflow.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Minimal long-press drag-to-reorder for a LazyColumn. Tracks the item currently being
 * dragged and its cumulative vertical offset, and reports a (fromIndex, toIndex) move
 * once the dragged item's midpoint crosses into a neighboring item's bounds.
 */
class DragDropListState(
    private val listState: LazyListState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var draggingItemOffset by mutableStateOf(0f)
        private set

    private var initialDraggingIndex: Int? = null

    private val currentItemInfo
        get() = draggingItemIndex?.let { index ->
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        }

    fun onDragStart(index: Int) {
        initialDraggingIndex = index
        draggingItemIndex = index
        draggingItemOffset = 0f
    }

    fun onDrag(offset: Float) {
        draggingItemOffset += offset
        val itemInfo = currentItemInfo ?: return
        val startIndex = draggingItemIndex ?: return

        val draggedMidpoint = itemInfo.offset + itemInfo.size / 2 + draggingItemOffset

        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { candidate ->
            candidate.index != startIndex &&
                draggedMidpoint.toInt() in candidate.offset..(candidate.offset + candidate.size)
        }

        if (target != null) {
            onMove(startIndex, target.index)
            draggingItemIndex = target.index
            // Preserve visual continuity: adjust offset so the item doesn't jump.
            draggingItemOffset += (itemInfo.offset - target.offset).toFloat()
        }
    }

    fun onDragEnd() {
        draggingItemIndex = null
        draggingItemOffset = 0f
        initialDraggingIndex = null
    }
}

@Composable
fun rememberDragDropListState(
    listState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropListState {
    return remember(listState) { DragDropListState(listState, onMove) }
}

fun Modifier.dragHandle(
    index: Int,
    state: DragDropListState
): Modifier = this.pointerInput(state) {
    detectDragGesturesAfterLongPress(
        onDragStart = { state.onDragStart(index) },
        onDrag = { change, dragAmount -> change.consume(); state.onDrag(dragAmount.y) },
        onDragEnd = { state.onDragEnd() },
        onDragCancel = { state.onDragEnd() }
    )
}
