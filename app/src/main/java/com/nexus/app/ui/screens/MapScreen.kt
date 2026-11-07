package com.nexus.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.nexus.app.core.Device
import java.util.Date

private data class DisplayLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val timeMillis: Long,
    val isCurrentFix: Boolean,
)

@Composable
fun MapScreen(devices: List<Device>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<DisplayLocation?>(null) }
    var selectedDeviceId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun readLastKnown() {
        try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val known = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .filter(manager::isProviderEnabled)
                .mapNotNull(manager::getLastKnownLocation)
                .maxByOrNull(Location::getTime)
            if (known != null) location = known.toDisplayLocation(isCurrentFix = false)
        } catch (_: SecurityException) {
            error = "Location permission is needed to read this phone's location."
        } catch (_: IllegalArgumentException) {
            error = "Location is unavailable from the current providers."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { it }) readLastKnown()
        else error = "Location access was not granted. You can enable it later in Android Settings."
    }

    @SuppressLint("MissingPermission")
    fun refreshCurrentLocation() {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }
        readLastKnown()
        loading = true
        error = null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider ->
                try {
                    manager.isProviderEnabled(provider)
                } catch (_: SecurityException) {
                    false
                }
            }
        if (providers.isEmpty()) {
            loading = false
            error = "Turn on Android location services and try again."
            return
        }
        val provider = providers.first()
        try {
            manager.requestSingleUpdate(provider, LocationListener { fix ->
                location = fix.toDisplayLocation(isCurrentFix = true)
                loading = false
                error = null
            }, Looper.getMainLooper())
            Handler(Looper.getMainLooper()).postDelayed({
                if (loading) {
                    loading = false
                    error = "No fresh location fix arrived. The last-known fix, if shown, has not been refreshed."
                }
            }, 20_000)
        } catch (_: SecurityException) {
            loading = false
            error = "Location permission was revoked. Allow access and try again."
        } catch (_: IllegalArgumentException) {
            loading = false
            error = "No location provider could return a fix."
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) readLastKnown()
    }

    val selectedDevice = devices.firstOrNull { it.id == selectedDeviceId }
    val showingPhoneLocation = selectedDeviceId == null
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Map", style = MaterialTheme.typography.headlineMedium)
        Text(
            "OpenStreetMap · current location is shown only for this phone, after your permission.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedDevice?.let { "Selected: ${it.name}" } ?: "Selected: This phone")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("This phone · current location") },
                    onClick = { selectedDeviceId = null; expanded = false },
                )
                devices.forEach { device ->
                    DropdownMenuItem(
                        text = { Text("${device.name} · ${device.role.name}") },
                        onClick = { selectedDeviceId = device.id; expanded = false },
                    )
                }
            }
        }
        if (showingPhoneLocation) {
            RowMapActions(
                hasPermission = hasLocationPermission(),
                loading = loading,
                onRequest = { refreshCurrentLocation() },
            )
        } else {
            Text(
                "No shared location is available for ${selectedDevice?.name}. This build has no paired-device location service.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showingPhoneLocation && location != null) {
            val fix = location!!
            Text(
                "${if (fix.isCurrentFix) "Current fix" else "Last-known fix"} · ${Date(fix.timeMillis)}" +
                    (fix.accuracyMeters?.let { " · ±${it.toInt()} m" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
            )
            Text("${fix.latitude}, ${fix.longitude}", style = MaterialTheme.typography.bodySmall)
        } else if (showingPhoneLocation && !hasLocationPermission()) {
            Text("No location is shown until you explicitly grant access.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (showingPhoneLocation && location != null) {
            OpenStreetMap(location = location!!, modifier = Modifier.fillMaxWidth().weight(1f))
        } else {
            NexusCard {
                Text(
                    if (showingPhoneLocation) "Location not available" else "No shared device location",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    if (showingPhoneLocation) {
                        "Grant permission and refresh to place this phone on the map. A stale fix is labeled as last known."
                    } else {
                        "No marker is displayed because no actual location data has been shared by this device."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text("Map data © OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun RowMapActions(hasPermission: Boolean, loading: Boolean, onRequest: () -> Unit) {
    if (loading) {
        CircularProgressIndicator()
    } else {
        Button(onClick = onRequest) {
            Text(if (hasPermission) "Refresh location" else "Allow location access")
        }
    }
}

@Composable
private fun OpenStreetMap(location: DisplayLocation, modifier: Modifier = Modifier) {
    val url = remember(location.latitude, location.longitude) {
        val lat = location.latitude.coerceIn(-85.0, 85.0)
        val lon = location.longitude.coerceIn(-180.0, 180.0)
        val delta = 0.02
        "https://www.openstreetmap.org/export/embed.html?" +
            "bbox=${lon - delta}%2C${lat - delta}%2C${lon + delta}%2C${lat + delta}" +
            "&layer=mapnik&marker=$lat%2C$lon"
    }
    AndroidView(
        modifier = modifier.height(340.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        update = { webView -> if (webView.url != url) webView.loadUrl(url) },
    )
}

private fun Location.toDisplayLocation(isCurrentFix: Boolean) = DisplayLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = if (hasAccuracy()) accuracy else null,
    timeMillis = time,
    isCurrentFix = isCurrentFix,
)
