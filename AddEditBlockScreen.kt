package com.focusflow.app.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusflow.app.domain.model.BlockCategory
import com.focusflow.app.ui.components.TimeField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBlockScreen(
    onDone: () -> Unit,
    viewModel: AddEditBlockViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New Block" else "Edit Block") },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { viewModel.duplicate() }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(BlockCategory.entries) { category ->
                        CategoryChip(
                            category = category,
                            selected = category == state.category,
                            onClick = { viewModel.updateCategory(category) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeField(
                    label = "Starts",
                    value = state.startTime,
                    onValueChange = viewModel::updateStartTime,
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    label = "Ends",
                    value = state.endTime,
                    onValueChange = viewModel::updateEndTime,
                    modifier = Modifier.weight(1f)
                )
            }

            // Resize stepper — quick duration adjustment in 5-minute increments.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Duration", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(onClick = { viewModel.adjustDurationMinutes(-5) }) {
                    Text("\u2212", fontWeight = FontWeight.Bold)
                }
                val minutes = java.time.Duration.between(state.startTime, state.endTime).toMinutes()
                Text("${minutes} min", style = MaterialTheme.typography.titleMedium)
                FilledTonalIconButton(onClick = { viewModel.adjustDurationMinutes(5) }) {
                    Text("+", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Repeats", modifier = Modifier.weight(1f))
                Switch(checked = state.isRecurring, onCheckedChange = viewModel::updateRecurring)
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.save() },
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isNew) "Add Block" else "Save Changes")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this block?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryChip(
    category: BlockCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) category.color else category.color.copy(alpha = 0.12f)
    val fg = if (selected) Color.White else category.color

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(category.icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(category.label, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}
