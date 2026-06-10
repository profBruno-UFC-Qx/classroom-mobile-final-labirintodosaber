package com.labirintodosaber.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.labirintodosaber.R
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    selected: TaskCategory?,
    onSelect: (TaskCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskCategory.entries.forEach { category ->
            CategoryChip(
                label = stringResource(category.labelRes()),
                isSelected = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50.dp)
    val background = if (isSelected) TealPrimary else Color.Transparent
    val textColor = if (isSelected) Color.White else TealPrimary
    val borderColor = if (isSelected) TealPrimary else InputBorder

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

private fun TaskCategory.labelRes() = when (this) {
    TaskCategory.READING -> R.string.category_reading
    TaskCategory.WRITING -> R.string.category_writing
    TaskCategory.VOCABULARY -> R.string.category_vocabulary
    TaskCategory.COMPREHENSION -> R.string.category_comprehension
}
