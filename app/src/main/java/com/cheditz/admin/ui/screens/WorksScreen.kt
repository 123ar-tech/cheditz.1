package com.cheditz.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cheditz.admin.data.model.WorkItem
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

private val WORK_CATEGORIES = listOf("Poster", "Video", "Document")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksScreen(vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val works = config?.works ?: emptyList()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio Works", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Filled.Add, "Add work") },
                text = { Text("Add Work") }
            )
        }
    ) { padding ->
        if (works.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No works yet — tap Add Work", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(works, key = { _, w -> w.id }) { index, work ->
                    WorkCard(
                        work = work,
                        canUp = index > 0,
                        canDown = index < works.lastIndex,
                        onMoveUp = {
                            vm.updateWorks(works.toMutableList().also { l ->
                                val t = l[index]; l[index] = l[index - 1]; l[index - 1] = t
                            })
                        },
                        onMoveDown = {
                            vm.updateWorks(works.toMutableList().also { l ->
                                val t = l[index]; l[index] = l[index + 1]; l[index + 1] = t
                            })
                        },
                        onToggleVisibility = { vm.toggleWorkVisibility(work.id, !work.visible) },
                        onDelete = { vm.removeWork(work.id) }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showAdd) {
        AddWorkDialog(
            onDismiss = { showAdd = false },
            onAdd = { w -> vm.addWork(w); showAdd = false }
        )
    }
}

@Composable
fun WorkCard(
    work: WorkItem,
    canUp: Boolean,
    canDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (work.type == "image" && work.url.startsWith("http")) {
                AsyncImage(
                    model = work.url,
                    contentDescription = work.title,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(DarkCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (work.type == "video") Icons.Filled.PlayCircle else Icons.Filled.Image,
                        contentDescription = work.title,
                        tint = VioletPrimary
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(work.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(work.sub, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = VioletPrimary.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall) {
                        Text(work.cat, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = VioletPrimary)
                    }
                    if (work.meta.isNotBlank()) {
                        Text(work.meta, modifier = Modifier.padding(vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
                    }
                    if (!work.visible) {
                        Surface(color = ErrorRed.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall) {
                            Text("Hidden", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                        }
                    }
                }
            }
            IconButton(onClick = onMoveUp, enabled = canUp, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.KeyboardArrowUp, "Move up", tint = if (canUp) TextSecondary else TextSecondary.copy(alpha = 0.3f))
            }
            IconButton(onClick = onMoveDown, enabled = canDown, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.KeyboardArrowDown, "Move down", tint = if (canDown) TextSecondary else TextSecondary.copy(alpha = 0.3f))
            }
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (work.visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    "Toggle visibility",
                    tint = if (work.visible) SuccessGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkDialog(onDismiss: () -> Unit, onAdd: (WorkItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var sub by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(WORK_CATEGORIES.first()) }
    var meta by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("image") }
    var url by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    var catMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Work") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sub,
                    onValueChange = { sub = it },
                    label = { Text("Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = cat,
                        onValueChange = { cat = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { catMenu = true }) {
                                Icon(Icons.Filled.ArrowDropDown, "Choose category")
                            }
                        }
                    )
                    DropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
                        WORK_CATEGORIES.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { cat = c; catMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = meta,
                    onValueChange = { meta = it },
                    label = { Text("Meta / Tag (e.g. A2 print)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Type", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "image", onClick = { type = "image" })
                    Text("Image", modifier = Modifier.padding(end = 20.dp))
                    RadioButton(selected = type == "video", onClick = { type = "video" })
                    Text("Video")
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = {
                        Text(if (type == "video") "Video URL (YouTube / Drive)" else "Image URL")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (err != null) {
                    Text(err!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    title.isBlank() -> err = "Title is required."
                    url.isBlank() -> err = "Enter a URL."
                    else -> onAdd(
                        WorkItem(
                            id = "w-${System.currentTimeMillis()}",
                            title = title.trim(),
                            sub = sub.trim(),
                            cat = cat,
                            meta = meta.trim(),
                            type = type,
                            url = url.trim(),
                            visible = true
                        )
                    )
                }
            }) { Text("Add", color = VioletPrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
