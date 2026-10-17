package com.cheditz.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheditz.admin.data.model.Order
import com.cheditz.admin.ui.theme.*
import com.cheditz.admin.ui.viewmodel.DashboardViewModel
import com.cheditz.admin.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateOrders: () -> Unit,
    dashboardVm: DashboardViewModel = viewModel(),
    orderVm: OrderViewModel = viewModel()
) {
    val pending by dashboardVm.pendingCount.collectAsStateWithLifecycle()
    val completed by dashboardVm.completedCount.collectAsStateWithLifecycle()
    val total by dashboardVm.totalCount.collectAsStateWithLifecycle()
    val recentOrders by dashboardVm.orders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Cheditz Admin", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Pending", pending, WarningAmber, Modifier.weight(1f))
                    StatCard("Completed", completed, SuccessGreen, Modifier.weight(1f))
                    StatCard("Total", total, CyanAccent, Modifier.weight(1f))
                }
            }

            // Pending orders quick action
            item {
                Card(
                    onClick = onNavigateOrders,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(VioletPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, "Pending", tint = VioletPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pending Orders", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "$pending order(s) need attention",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, "Go", tint = TextSecondary)
                    }
                }
            }

            item {
                Text(
                    "Recent Orders",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No orders yet", color = TextSecondary)
                    }
                }
            } else {
                items(recentOrders.take(10), key = { it.id }) { order ->
                    OrderCard(order) { onNavigateOrders() }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, count: Int, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(order.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusChip(order.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(order.service, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (order.receiptNumber.isNotBlank()) {
                Text(
                    order.receiptNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "pending" -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
        "processing" -> CyanAccent.copy(alpha = 0.15f) to CyanAccent
        "completed" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        "cancelled" -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
        else -> TextSecondary.copy(alpha = 0.15f) to TextSecondary
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}
