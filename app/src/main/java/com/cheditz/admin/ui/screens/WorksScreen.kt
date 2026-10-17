package com.cheditz.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cheditz.admin.data.model.WorkItem
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksScreen(vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val works = config?.works ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio Works", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (works.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No works in portfolio", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(works, key = { it.id }) { work ->
                    WorkCard(
                        work = work,
                        onToggleVisibility = { vm.toggleWorkVisibility(work.id, !work.visible) },
                        onDelete = { vm.removeWork(work.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkCard(work: WorkItem, onToggleVisibility: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (work.url.isNotBlank()) {
                AsyncImage(
                    model = work.url,
                    contentDescription = work.title,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(work.title, fontWeight = FontWeight.SemiBold)
                Text(work.sub, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = VioletPrimary.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall) {
                        Text(work.cat, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = VioletPrimary)
                    }
                    if (!work.visible) {
                        Surface(color = ErrorRed.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall) {
                            Text("Hidden", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                        }
                    }
                }
            }
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (work.visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    "Toggle visibility",
                    tint = if (work.visible) SuccessGreen else TextSecondary
                )
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed)
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove work?") },
            text = { Text("\"${work.title}\" will be removed from the portfolio.") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }) { Text("Remove", color = ErrorRed) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }
}
