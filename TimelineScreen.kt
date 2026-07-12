package com.focusflow.app.ui.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusflow.app.ui.components.TimeBlockCard
import com.focusflow.app.ui.components.dragHandle
import com.focusflow.app.ui.components.rememberDragDropListState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onAddBlock: (LocalDate) -> Unit,
    onOpenBlock: (Long) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropListState(listState) { from, to ->
        viewModel.reorderBlocks(from, to)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FocusFlow", fontWeight = FontWeight.Bold)
                        Text(
                            text = state.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddBlock(state.date) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add block")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.blocks.isEmpty()) {
            EmptyTimeline(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.blocks, key = { _, block -> block.id }) { index, block ->
                val isDragging = dragDropState.draggingItemIndex == index
                TimeBlockCard(
                    block = block,
                    isActive = block.id == state.activeBlock?.id,
                    onToggleComplete = { viewModel.toggleCompleted(block) },
                    onClick = { onOpenBlock(block.id) },
                    onResizeMinutes = { delta -> viewModel.resizeBlock(block, delta) },
                    dragHandleModifier = Modifier.dragHandle(index, dragDropState),
                    modifier = Modifier.graphicsLayer {
                        translationY = if (isDragging) dragDropState.draggingItemOffset else 0f
                        shadowElevation = if (isDragging) 8f else 0f
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyTimeline(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No blocks planned yet", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap + to add your first time block",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
