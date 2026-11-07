package com.nexus.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexus.app.ui.screens.AcademyScreen
import com.nexus.app.ui.screens.DevicesScreen
import com.nexus.app.ui.screens.FilesScreen
import com.nexus.app.ui.screens.MapScreen
import com.nexus.app.ui.screens.SettingsScreen

@Composable
fun NexusApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = null,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Devices.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Devices.route) { DevicesScreen() }
            composable(Destination.Map.route) { MapScreen() }
            composable(Destination.Files.route) { FilesScreen() }
            composable(Destination.Academy.route) { AcademyScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
