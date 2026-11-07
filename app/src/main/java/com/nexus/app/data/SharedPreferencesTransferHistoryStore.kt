package com.nexus.app.data

import android.content.SharedPreferences
import com.nexus.app.core.LocalFile
import com.nexus.app.core.TransferHistoryStore
import com.nexus.app.core.TransferRecord
import com.nexus.app.core.TransferStatus
import java.nio.charset.StandardCharsets
import java.util.Base64

class SharedPreferencesTransferHistoryStore(
    private val preferences: SharedPreferences,
) : TransferHistoryStore {
    override fun load(): List<TransferRecord> {
        val serialized = preferences.getString(KEY_TRANSFERS, "").orEmpty()
        if (serialized.isEmpty()) return emptyList()
        return serialized.split('\n').map { record ->
            val fields = record.split('|')
            check(fields.size == 8) { "Saved transfer data is malformed." }
            TransferRecord(
                id = decode(fields[0]),
                file = LocalFile(decode(fields[1]), decode(fields[2]), fields[3].toNullableLong(), decode(fields[4]).ifEmpty { null }),
                status = TransferStatus.valueOf(fields[5]),
                createdAtEpochMillis = fields[6].toLong(),
                progressPercent = fields[7].toInt(),
            )
        }
    }

    override fun save(records: List<TransferRecord>) {
        val serialized = records.joinToString("\n") { record ->
            listOf(
                encode(record.id),
                encode(record.file.uri),
                encode(record.file.name),
                record.file.sizeBytes?.toString() ?: "-",
                encode(record.file.mimeType.orEmpty()),
                record.status.name,
                record.createdAtEpochMillis.toString(),
                record.progressPercent.toString(),
            ).joinToString("|")
        }
        check(preferences.edit().putString(KEY_TRANSFERS, serialized).commit()) {
            "Could not save transfer history."
        }
    }

    private fun String.toNullableLong(): Long? = if (this == "-") null else toLong()

    private fun encode(value: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decode(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)

    private companion object {
        const val KEY_TRANSFERS = "transfer_history_v1"
    }
}
