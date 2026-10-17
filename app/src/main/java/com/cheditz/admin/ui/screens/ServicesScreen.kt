package com.cheditz.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.cheditz.admin.data.model.Service
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBack: () -> Unit, vm: ConfigViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val services = config?.services ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services & Pricing", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
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
            if (services.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("No services configured", color = TextSecondary)
                }
            } else {
                services.forEach { service ->
                    ServiceCard(service)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Edit prices/descriptions on the website admin panel, or extend this screen with Firestore write support for services[].",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ServiceCard(service: Service) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(service.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Surface(color = CyanAccent.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        service.price,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanAccent
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(service.desc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (service.featureList.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                service.featureList.forEach { feat ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Filled.CheckCircle, "Feature", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(feat, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
