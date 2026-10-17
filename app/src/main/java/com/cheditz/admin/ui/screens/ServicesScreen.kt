package com.cheditz.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.Service
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBack: () -> Unit, vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val services = config?.services ?: emptyList()
    var edits by remember(services) { mutableStateOf(services) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services & Pricing", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = {
                        vm.updateServices(edits)
                        saved = true
                    }) { Text("Save", color = VioletPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (saved) Snackbar { Text("Services saved to Firestore") }

            if (edits.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No services configured", color = TextSecondary)
                }
            } else {
                edits.forEachIndexed { i, service ->
                    ServiceEditCard(service) { new ->
                        edits = edits.toMutableList().also { l -> l[i] = new }
                        saved = false
                    }
                }
            }

            Text(
                "Changes update cheditz/config.services — the website shows them live.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ServiceEditCard(service: Service, onChange: (Service) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Service", style = MaterialTheme.typography.labelLarge, color = CyanAccent)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = service.name,
                    onValueChange = { onChange(service.copy(name = it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                )
                OutlinedTextField(
                    value = service.price,
                    onValueChange = { onChange(service.copy(price = it)) },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.width(140.dp),
                    shape = MaterialTheme.shapes.small
                )
            }
            OutlinedTextField(
                value = service.desc,
                onValueChange = { onChange(service.copy(desc = it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5,
                shape = MaterialTheme.shapes.small
            )
            OutlinedTextField(
                value = service.feats,
                onValueChange = { onChange(service.copy(feats = it)) },
                label = { Text("Features (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 7,
                shape = MaterialTheme.shapes.small
            )
        }
    }
}
