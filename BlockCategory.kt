package com.focusflow.app.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class BlockCategory(
    val label: String,
    val color: Color,
    val icon: ImageVector
) {
    WORK("Work", Color(0xFF5B8DEF), Icons.Filled.Work),
    STUDY("Study", Color(0xFF9B6BFF), Icons.Filled.School),
    FITNESS("Fitness", Color(0xFFFF6B6B), Icons.Filled.FitnessCenter),
    HEALTH("Health", Color(0xFF2ED9C3), Icons.Filled.Favorite),
    READING("Reading", Color(0xFFF6A94E), Icons.Filled.MenuBook),
    FAMILY("Family", Color(0xFFFF8FB1), Icons.Filled.FamilyRestroom),
    BREAK("Break", Color(0xFF8D9AAF), Icons.Filled.Coffee),
    ENTERTAINMENT("Entertainment", Color(0xFFB57CFF), Icons.Filled.Movie),
    SLEEP("Sleep", Color(0xFF4A5A8A), Icons.Filled.Bedtime),
    CUSTOM("Custom", Color(0xFF6FCF97), Icons.Filled.Star)
}
