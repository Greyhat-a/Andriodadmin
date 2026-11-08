package com.nexus.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nexus.app.core.Device
import com.nexus.app.core.DeviceConnectionStatus
import com.nexus.app.core.DeviceRole
import com.nexus.app.core.DeviceViewModel
import com.nexus.app.core.PairingCoordinator
import com.nexus.app.core.PairingInvitation
import com.nexus.app.core.PairingValidation
import java.text.DateFormat
import java.util.Date

private enum class DeviceSort { NAME, ROLE, STATUS }

@Composable
fun DevicesScreen(
    repository: com.nexus.app.core.DeviceRepository,
    pairingCoordinator: PairingCoordinator,
    modifier: Modifier = Modifier,
) {
    val viewModel: DeviceViewModel = viewModel(factory = DeviceViewModelFactory(repository))
    val devices by viewModel.devices.collectAsState()
    val error by viewModel.error.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<DeviceRole?>(null) }
    var sort by remember { mutableStateOf(DeviceSort.NAME) }
    var showAdd by remember { mutableStateOf(false) }
    var showPairing by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }

    val visibleDevices = devices
        .filter { device ->
            (selectedRole == null || device.role == selectedRole) &&
                (device.name.contains(query.trim(), ignoreCase = true) ||
                    device.model.contains(query.trim(), ignoreCase = true) ||
                    device.id.contains(query.trim(), ignoreCase = true))
        }
        .let { filtered ->
            when (sort) {
                DeviceSort.NAME -> filtered.sortedBy { it.name.lowercase() }
                DeviceSort.ROLE -> filtered.sortedBy { it.role.name }
                DeviceSort.STATUS -> filtered.sortedBy { it.connectionStatus.name }
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Devices", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Local device profiles are saved on this phone. No remote devices are connected in this build.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { showAdd = true }) { Text("Add device") }
            OutlinedButton(onClick = { showPairing = true }) { Text("Pair device") }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search devices") },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedRole == null, onClick = { selectedRole = null }, label = { Text("All roles") })
            DeviceRole.entries.forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = if (selectedRole == role) null else role },
                    label = { Text(role.name) },
                )
            }
            SortMenu(sort = sort, onSort = { sort = it })
        }

        if (visibleDevices.isEmpty()) {
            NexusCard {
                Text(
                    if (devices.isEmpty()) "No devices registered" else "No matching devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (devices.isEmpty()) {
                        "Add a local device profile to get started. Registration does not connect to another device."
                    } else {
                        "Try a different search or role filter."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            visibleDevices.forEach { device ->
                DeviceCard(device = device, onClick = { selectedDevice = device })
            }
        }
        error?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = viewModel::dismissError) { Text("Dismiss") }
        }
    }

    if (showAdd) {
        DeviceFormDialog(
            title = "Add device",
            initialName = "",
            initialModel = "",
            initialRole = DeviceRole.USER,
            onDismiss = { showAdd = false },
            onSave = { name, model, role ->
                if (viewModel.register(name, model, role)) {
                    showAdd = false
                    null
                } else {
                    viewModel.error.value
                }
            },
        )
    }

    selectedDevice?.let { device ->
        DeviceDetailsDialog(
            device = device,
            onDismiss = { selectedDevice = null },
            onSave = { updated ->
                if (viewModel.update(updated)) {
                    selectedDevice = updated
                    null
                } else {
                    viewModel.error.value
                }
            },
            onDelete = { id ->
                if (viewModel.remove(id)) {
                    selectedDevice = null
                    null
                } else {
                    viewModel.error.value
                }
            },
        )
    }

    if (showPairing) {
        PairingDialog(pairingCoordinator = pairingCoordinator, onDismiss = { showPairing = false })
    }
}

private class DeviceViewModelFactory(
    private val repository: com.nexus.app.core.DeviceRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DeviceViewModel::class.java))
        return DeviceViewModel(repository) as T
    }
}

@Composable
private fun DeviceCard(device: Device, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(device.model, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(device.role.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Text(connectionLabel(device.connectionStatus), style = MaterialTheme.typography.labelLarge)
            Text(
                "Battery: ${device.batteryPercent?.let { "$it%" } ?: "Not reported"}  ·  Last seen: ${
                    device.lastSeenEpochMillis?.let(::formatTimestamp) ?: "Not available"
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SortMenu(sort: DeviceSort, onSort: (DeviceSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) { Text("Sort: ${sort.name.lowercase()}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DeviceSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    onClick = { onSort(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun DeviceFormDialog(
    title: String,
    initialName: String,
    initialModel: String,
    initialRole: DeviceRole,
    onDismiss: () -> Unit,
    onSave: (String, String, DeviceRole) -> String?,
) {
    var name by remember { mutableStateOf(initialName) }
    var model by remember { mutableStateOf(initialModel) }
    var role by remember { mutableStateOf(initialRole) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Device name") }, singleLine = true)
                OutlinedTextField(model, { model = it }, label = { Text("Model") }, singleLine = true)
                RoleSelector(role, onRole = { role = it })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = onSave(name, model, role)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeviceDetailsDialog(
    device: Device,
    onDismiss: () -> Unit,
    onSave: (Device) -> String?,
    onDelete: (String) -> String?,
) {
    var editing by remember(device.id) { mutableStateOf(false) }
    var confirmingDelete by remember(device.id) { mutableStateOf(false) }
    var name by remember(device.id) { mutableStateOf(device.name) }
    var model by remember(device.id) { mutableStateOf(device.model) }
    var role by remember(device.id) { mutableStateOf(device.role) }
    var dialogError by remember(device.id) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing) "Edit device" else device.name) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (editing) {
                    OutlinedTextField(name, { name = it }, label = { Text("Device name") }, singleLine = true)
                    OutlinedTextField(model, { model = it }, label = { Text("Model") }, singleLine = true)
                    RoleSelector(role, onRole = { role = it })
                } else {
                    Text(device.model)
                    Text("Role: ${device.role.name}")
                    Text("Status: ${connectionLabel(device.connectionStatus)}")
                    Text("Battery: ${device.batteryPercent?.let { "$it%" } ?: "Not reported"}")
                    Text("Last seen: ${device.lastSeenEpochMillis?.let(::formatTimestamp) ?: "Not available"}")
                    Text("Device ID", style = MaterialTheme.typography.labelLarge)
                    Text(device.id, style = MaterialTheme.typography.bodySmall)
                }
                dialogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            if (editing) {
                Button(onClick = {
                    dialogError = onSave(device.copy(name = name, model = model, role = role))
                    if (dialogError == null) editing = false
                }) { Text("Save") }
            } else {
                TextButton(onClick = { editing = true }) { Text("Edit") }
            }
        },
        dismissButton = {
            Row {
                if (!editing) TextButton(onClick = { confirmingDelete = true }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = { if (editing) editing = false else onDismiss() }) {
                    Text(if (editing) "Cancel edit" else "Close")
                }
            }
        },
    )
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Remove ${device.name}?") },
            text = { Text("This deletes the local device profile from this phone. It does not disconnect a remote device.") },
            confirmButton = {
                Button(onClick = {
                    dialogError = onDelete(device.id)
                    confirmingDelete = false
                }) { Text("Remove device") }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RoleSelector(role: DeviceRole, onRole: (DeviceRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) { Text("Role: ${role.name}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DeviceRole.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = { onRole(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun PairingDialog(pairingCoordinator: PairingCoordinator, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var localRole by remember { mutableStateOf(DeviceRole.ADMIN) }
    var invitation by remember { mutableStateOf<PairingInvitation?>(null) }
    var scannedInvitation by remember { mutableStateOf<PairingInvitation?>(null) }
    var validation by remember { mutableStateOf<PairingValidation?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun acceptScanned(payload: String) {
        val parsed = pairingCoordinator.parse(payload)
        scannedInvitation = parsed
        validation = parsed?.let { pairingCoordinator.validate(it, localRole) } ?: PairingValidation.MALFORMED
        error = null
        message = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pair device") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "This creates an expiring invitation or a local request only. A backend is required to verify and complete pairing.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoleSelector(localRole, onRole = {
                    localRole = it
                    validation = scannedInvitation?.let { invite -> pairingCoordinator.validate(invite, it) }
                })
                OutlinedButton(onClick = {
                    invitation = pairingCoordinator.createInvitation(localRole)
                    error = null
                }) { Text("Generate 10-minute invitation") }
                invitation?.let { invite ->
                    val bitmap = remember(invite.id) { createQrBitmap(invite.toQrPayload()) }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Short-lived NEXUS pairing invitation QR code",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    } ?: Text("Could not encode the invitation as a QR code.", color = MaterialTheme.colorScheme.error)
                    Text("Invitation expires ${formatTimestamp(invite.expiresAtEpochMillis)}")
                    Text("QR invitation only. Do not share screenshots publicly.", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = {
                    error = null
                    try {
                        val options = GmsBarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        GmsBarcodeScanning.getClient(context, options).startScan()
                            .addOnSuccessListener { barcode ->
                                val payload = barcode.rawValue
                                if (payload == null) error = "The scanned QR code contains no text."
                                else acceptScanned(payload)
                            }
                            .addOnCanceledListener {
                                error = "QR scanning was cancelled."
                            }
                            .addOnFailureListener { failure ->
                                error = "QR scanning failed: ${failure.localizedMessage ?: "scanner unavailable"}"
                            }
                    } catch (failure: SecurityException) {
                        error = "Google Play services could not access the scanner."
                    } catch (failure: IllegalStateException) {
                        error = "QR scanner is unavailable on this device."
                    }
                }) { Text("Scan invitation QR") }
                scannedInvitation?.let { invite ->
                    Text("Invitation role: ${invite.role.name}")
                    Text("Expires: ${formatTimestamp(invite.expiresAtEpochMillis)}")
                    Text(validation?.let(::validationMessage) ?: "Invitation not validated.")
                    if (validation == PairingValidation.READY) {
                        Button(onClick = {
                            try {
                                val request = pairingCoordinator.createRequest(invite, localRole)
                                validation = PairingValidation.DUPLICATE
                                message = "Pairing request accepted for this session (${request.invitationId}). The devices are not paired; a backend must verify and authorize the request."
                            } catch (failure: IllegalArgumentException) {
                                validation = pairingCoordinator.validate(invite, localRole)
                                error = failure.message
                            }
                        }) { Text("Confirm pairing request") }
                    }
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

private fun validationMessage(validation: PairingValidation): String = when (validation) {
    PairingValidation.READY -> "Invitation format and expiry are valid. Server verification is still required."
    PairingValidation.MALFORMED -> "This is not a valid NEXUS invitation."
    PairingValidation.EXPIRED -> "This invitation has expired."
    PairingValidation.SAME_ROLE -> "Pairing requires one ADMIN and one USER role."
    PairingValidation.SELF_INVITATION -> "This invitation was created on this device. Scan the other device's invitation instead."
    PairingValidation.DUPLICATE -> "This invitation has already been used for a local request."
}

private fun createQrBitmap(payload: String): Bitmap? = try {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 480, 480)
    Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
} catch (_: com.google.zxing.WriterException) {
    null
}

private fun connectionLabel(status: DeviceConnectionStatus): String = when (status) {
    DeviceConnectionStatus.UNKNOWN -> "Not connected · status unavailable"
    DeviceConnectionStatus.ONLINE -> "Online"
    DeviceConnectionStatus.OFFLINE -> "Offline"
}

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epochMillis))
