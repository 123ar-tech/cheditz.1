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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.Testimonial
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.TestimonialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(vm: TestimonialViewModel = viewModel()) {
    val testimonials by vm.testimonials.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (testimonials.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reviews yet", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(testimonials, key = { it.id }) { t ->
                    ReviewCard(t, onDelete = { vm.delete(t.id) })
                }
            }
        }
    }
}

@Composable
fun ReviewCard(t: Testimonial, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t.name, fontWeight = FontWeight.SemiBold)
                    Text(t.role, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row {
                    repeat(5) { i ->
                        Icon(
                            if (i < t.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            "Star",
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { showDelete = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(t.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(t.date, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete review?") },
            text = { Text("This review by ${t.name} will be permanently removed.") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }) { Text("Delete", color = ErrorRed) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }
}
