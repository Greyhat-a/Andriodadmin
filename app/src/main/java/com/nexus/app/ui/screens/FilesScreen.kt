package com.nexus.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.core.LocalFile
import com.nexus.app.core.LocalTransferRepository
import com.nexus.app.core.TransferRecord
import com.nexus.app.core.TransferStatus
import java.util.Locale

@Composable
fun FilesScreen(repository: LocalTransferRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val records by repository.records.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val failures = mutableListOf<String>()
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                repository.addSelection(context.resolveLocalFile(uri))
            } catch (failure: SecurityException) {
                failures += "NEXUS could not retain access to ${uri.lastPathSegment ?: "the selected file"}."
            } catch (failure: IllegalStateException) {
                failures += failure.message ?: "The selected file could not be saved."
            } catch (failure: IllegalArgumentException) {
                failures += failure.message ?: "The selected file could not be read."
            }
        }
        error = failures.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Files", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Select local documents, images, audio or video. Device-to-device transfer is unavailable until a secure transport is configured.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = { picker.launch(arrayOf("application/*", "image/*", "audio/*", "video/*")) }) {
            Text("Select files")
        }
        if (records.isEmpty()) {
            NexusCard {
                Text("No files selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Use the Android system picker to choose files. NEXUS will not upload them.")
            }
        } else {
            Text("Selection and transfer history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            records.asReversed().forEach { record ->
                TransferCard(
                    record = record,
                    onPreview = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(record.file.uri), record.file.mimeType ?: "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                            error = null
                        } catch (_: ActivityNotFoundException) {
                            error = "No installed app can preview this file type."
                        } catch (_: SecurityException) {
                            error = "NEXUS no longer has access to this file. Select it again."
                        }
                    },
                    onRequestTransfer = {
                        try {
                            repository.requestTransfer(record.id)
                            error = null
                        } catch (failure: IllegalArgumentException) {
                            error = failure.message
                        }
                    },
                    onCancel = {
                        try {
                            repository.cancel(record.id)
                            error = null
                        } catch (failure: IllegalArgumentException) {
                            error = failure.message
                        }
                    },
                )
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun TransferCard(
    record: TransferRecord,
    onPreview: () -> Unit,
    onRequestTransfer: () -> Unit,
    onCancel: () -> Unit,
) {
    NexusCard {
        Text(record.file.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("${record.file.mimeType ?: "Unknown type"} · ${record.file.sizeBytes?.let(::formatFileSize) ?: "Size unavailable"}")
        Text(transferStatusLabel(record.status), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (record.status == TransferStatus.WAITING_FOR_CONNECTION) {
            Text("No bytes have been sent. A backend connection is required.", style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPreview) { Text("Preview") }
            if (record.status == TransferStatus.SELECTED) {
                Button(onClick = onRequestTransfer) { Text("Request transfer") }
            } else if (record.status == TransferStatus.WAITING_FOR_CONNECTION) {
                TextButton(onClick = onCancel) { Text("Cancel request") }
            }
        }
    }
}

private fun android.content.Context.resolveLocalFile(uri: Uri): LocalFile {
    var displayName = uri.lastPathSegment ?: "Selected file"
    var size: Long? = null
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex) ?: displayName
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    return LocalFile(uri.toString(), displayName, size, contentResolver.getType(uri))
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
}

private fun transferStatusLabel(status: TransferStatus): String = when (status) {
    TransferStatus.SELECTED -> "Selected on this device"
    TransferStatus.WAITING_FOR_CONNECTION -> "Waiting for a secure device connection"
    TransferStatus.CANCELLED -> "Transfer request cancelled"
    TransferStatus.FAILED -> "Transfer failed"
}
