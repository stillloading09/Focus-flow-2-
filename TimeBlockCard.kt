package com.focusflow.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.TimeBlock
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

/** Vertical drag pixels needed to shift the end time by one minute. Larger = coarser control. */
private const val PIXELS_PER_MINUTE = 6f

@Composable
fun TimeBlockCard(
    block: TimeBlock,
    isActive: Boolean,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onResizeMinutes: (Long) -> Unit,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val accent = block.category.color
    val shadowColor = if (isActive) accent.copy(alpha = glowAlpha) else Color.Transparent
    var resizeDragAccumPx by remember(block.id) { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isActive) 16.dp else 2.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f))
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    }
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.outline,
                modifier = dragHandleModifier.padding(end = 8.dp)
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = block.category.icon, contentDescription = block.category.label, tint = accent)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = "${block.startTime.format(timeFormatter)} \u2013 ${block.endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (block.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (block.isCompleted) "Completed" else "Mark complete",
                    tint = if (block.isCompleted) accent else MaterialTheme.colorScheme.outline
                )
            }
        }

        // Resize handle: drag vertically to extend/shrink the block's duration.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .pointerInput(block.id) {
                    detectVerticalDragGestures(
                        onDragEnd = { resizeDragAccumPx = 0f },
                        onDragCancel = { resizeDragAccumPx = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            resizeDragAccumPx += dragAmount
                            val minuteDelta = (resizeDragAccumPx / PIXELS_PER_MINUTE).toLong()
                            if (minuteDelta != 0L) {
                                onResizeMinutes(minuteDelta)
                                resizeDragAccumPx -= minuteDelta * PIXELS_PER_MINUTE
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}
