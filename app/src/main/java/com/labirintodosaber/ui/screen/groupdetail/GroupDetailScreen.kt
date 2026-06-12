package com.labirintodosaber.ui.screen.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labirintodosaber.R
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.ui.screen.activities.ActivitiesMockData
import com.labirintodosaber.ui.screen.activities.MockNotebook
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onNotebookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = ActivitiesMockData.groupById(groupId)
    val notebooks = ActivitiesMockData.notebooksForGroup(groupId)
    val iconColor = Color(group?.iconColorHex ?: 0xFF5CC8C0)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = group?.name ?: "Grupo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back_button), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color(0xFFF5F5F5),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (group != null) {
                item {
                    Text(group.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${notebooks.size} cadernos", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            items(notebooks) { notebook ->
                NotebookCard(notebook = notebook, iconColor = iconColor, onClick = { onNotebookClick(notebook.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotebookCard(
    notebook: MockNotebook,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Book, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notebook.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(notebook.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    notebook.categories.forEach { CategoryPill(it.displayName()) }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color(0xFFF0F0F0)).padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text("${notebook.taskIds.size} atividades", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color(0xFFF0F0F0)).padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
    }
}

private fun TaskCategory.displayName() = when (this) {
    TaskCategory.READING -> "Leitura"
    TaskCategory.WRITING -> "Escrita"
    TaskCategory.VOCABULARY -> "Vocabulário"
    TaskCategory.COMPREHENSION -> "Compreensão"
}
