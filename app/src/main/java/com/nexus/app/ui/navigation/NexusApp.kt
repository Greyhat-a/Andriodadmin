package com.nexus.app.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.nexus.app.core.LocalDeviceRepository
import com.nexus.app.core.LocalTransferRepository
import com.nexus.app.core.PairingCoordinator
import com.nexus.app.data.SharedPreferencesDeviceDataStore
import com.nexus.app.data.SharedPreferencesTransferHistoryStore

@Composable
fun NexusApp() {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) {
        context.getSharedPreferences("nexus_local_data", Context.MODE_PRIVATE)
    }
    val deviceRepository = remember(preferences) {
        LocalDeviceRepository(SharedPreferencesDeviceDataStore(preferences))
    }
    val transferRepository = remember(preferences) {
        LocalTransferRepository(SharedPreferencesTransferHistoryStore(preferences))
    }
    val pairingCoordinator = remember { PairingCoordinator() }
    val devices by deviceRepository.devices.collectAsState()
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
            composable(Destination.Devices.route) {
                DevicesScreen(deviceRepository, pairingCoordinator)
            }
            composable(Destination.Map.route) { MapScreen(devices) }
            composable(Destination.Files.route) { FilesScreen(transferRepository) }
            composable(Destination.Academy.route) { AcademyScreen(preferences) }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
