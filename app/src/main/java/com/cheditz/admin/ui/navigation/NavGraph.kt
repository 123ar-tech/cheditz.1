package com.cheditz.admin.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cheditz.admin.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home)
    data object Orders : Screen("orders", "Orders", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
    data object Works : Screen("works", "Works", Icons.Filled.Work, Icons.Outlined.Work)
    data object Reviews : Screen("reviews", "Reviews", Icons.Filled.Star, Icons.Outlined.StarBorder)
    data object Content : Screen("content", "Content", Icons.Filled.Edit, Icons.Outlined.Edit)
    data object More : Screen("more", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Orders,
    Screen.Works,
    Screen.Reviews,
    Screen.Content,
    Screen.More
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheditzNavHost(startOrderId: String? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    LaunchedEffect(startOrderId) {
        if (!startOrderId.isNullOrBlank()) {
            navController.navigate("order_detail/$startOrderId")
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(onNavigateOrders = { navController.navigate(Screen.Orders.route) }) }
            composable(Screen.Orders.route) { OrdersScreen(onOrderClick = { id -> navController.navigate("order_detail/$id") }) }
            composable(Screen.Works.route) { WorksScreen() }
            composable(Screen.Reviews.route) { ReviewsScreen() }
            composable(Screen.Content.route) { ContentScreen() }
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateSettings = { navController.navigate("settings") },
                    onNavigateServices = { navController.navigate("services") },
                    onNavigateContact = { navController.navigate("contact") },
                    onNavigateLogin = { navController.navigate("login") }
                )
            }
            composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable("services") { ServicesScreen(onBack = { navController.popBackStack() }) }
            composable("contact") { ContactScreen(onBack = { navController.popBackStack() }) }
            composable("login") { LoginScreen(onBack = { navController.popBackStack() }) }
            composable(
                "order_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                OrderDetailScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }
        }
    }
}
