package com.cheditz.admin.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.defaultSiteConfig
import com.cheditz.admin.data.model.siteConfigFromJson
import com.cheditz.admin.data.model.toJson
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: ConfigViewModel = viewModel()) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val config by vm.config.collectAsStateWithLifecycle()

    var notifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifPermission = granted
    }

    var status by remember { mutableStateOf<String?>(null) }
    var showImport by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            if (status != null) Snackbar { Text(status!!) }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, "Notifications", tint = VioletPrimary)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Order Notifications", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (notifPermission) "Enabled — new orders will push a notification" else "Disabled — enable to receive new order alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (!notifPermission) {
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) { Text("Enable") }
                    }
                }
            }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Config Backup (like web Export / Import)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Export copies the whole site config (works, content, services, contact, accent) as JSON — paste it back anytime to restore, or reset to defaults.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val cfg = config
                            if (cfg == null) status = "Config not loaded yet"
                            else {
                                clipboard.setText(AnnotatedString(cfg.toJson()))
                                status = "Config JSON copied to clipboard"
                            }
                        }, enabled = config != null) { Text("Export") }

                        OutlinedButton(onClick = { showImport = true }) { Text("Import") }

                        OutlinedButton(onClick = { showReset = true }) { Text("Reset") }
                    }
                }
            }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Firebase Connection", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Project", "cheditz-dee25")
                    InfoRow("Orders Collection", "cheditz_orders")
                    InfoRow("Testimonials", "cheditz_testimonials")
                    InfoRow("Config Doc", "cheditz/config")
                }
            }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Data Sources", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Orders sync live from Firestore. Status changes propagate to the website instantly. New orders alert you via background polling (no Blaze plan needed).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("App", "Cheditz Admin v1.0.2")
                    InfoRow("Website", "cheditz.online")
                    InfoRow("Min SDK", "Android 8.0 (API 26)")
                }
            }
        }
    }

    if (showImport) {
        ImportDialog(
            onDismiss = { showImport = false },
            onImport = { json ->
                try {
                    val cfg = siteConfigFromJson(json)
                    vm.saveWholeConfig(cfg)
                    showImport = false
                    status = "Config imported — saved to Firestore"
                } catch (e: Exception) {
                    status = "Invalid JSON: ${e.message}"
                }
            }
        )
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset config?") },
            text = { Text("Works, content, services, contact and accent will be replaced with the default Cheditz config. Reviews and orders are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.saveWholeConfig(defaultSiteConfig())
                    showReset = false
                    status = "Config reset to defaults"
                }) { Text("Reset", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Config JSON") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 320.dp),
                placeholder = { Text("Paste config JSON here") },
                shape = MaterialTheme.shapes.small
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onImport(text) }, enabled = text.isNotBlank()) {
                Text("Import", color = VioletPrimary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
