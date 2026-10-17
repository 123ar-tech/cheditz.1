package com.cheditz.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    vm: OrderViewModel = viewModel()
) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    val order = orders.find { it.id == orderId }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(order.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        StatusChip(order.status)
                    }
                    Text(order.email, color = TextSecondary)
                    if (order.phone.isNotBlank()) Text(order.phone, color = TextSecondary)
                    if (order.receiptNumber.isNotBlank()) {
                        Text(order.receiptNumber, color = CyanAccent, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Service", order.service)
                    DetailRow("Budget", order.budget)
                    DetailRow("Deadline", order.deadline)
                    DetailRow("Date", order.date)
                }
            }

            if (order.description.isNotBlank()) {
                Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Customer Brief", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(order.description, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Contact actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.phone}"))
                        context.startActivity(intent)
                    },
                    enabled = order.phone.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Filled.Call, "Call", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Call") }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${order.email}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Filled.Email, "Email", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Email") }

                if (order.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${order.phone.filter { it.isDigit() }}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Filled.Chat, "WhatsApp", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("WhatsApp") }
                }
            }

            // Status actions
            Text("Update Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatusButton("Pending", order.status == "pending", WarningAmber, Modifier.weight(1f)) { vm.updateStatus(orderId, "pending") }
                StatusButton("Processing", order.status == "processing", CyanAccent, Modifier.weight(1f)) { vm.updateStatus(orderId, "processing") }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatusButton("Completed", order.status == "completed", SuccessGreen, Modifier.weight(1f)) { vm.updateStatus(orderId, "completed") }
                StatusButton("Cancelled", order.status == "cancelled", ErrorRed, Modifier.weight(1f)) { vm.updateStatus(orderId, "cancelled") }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Order?") },
            text = { Text("This permanently removes the order from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteOrder(orderId) { onBack() }
                    showDeleteDialog = false
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun StatusButton(label: String, selected: Boolean, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) accent else DarkCard,
            contentColor = if (selected) Color.Black else TextSecondary
        ),
        shape = MaterialTheme.shapes.small
    ) { Text(label, style = MaterialTheme.typography.labelLarge) }
}
