package com.nexus.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TransferStatus { SELECTED, WAITING_FOR_CONNECTION, CANCELLED, FAILED }

data class LocalFile(
    val uri: String,
    val name: String,
    val sizeBytes: Long?,
    val mimeType: String?,
)

data class TransferRecord(
    val id: String = UUID.randomUUID().toString(),
    val file: LocalFile,
    val status: TransferStatus,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val progressPercent: Int = 0,
)

/** A future authenticated transport must report real progress and outcomes. */
interface DeviceTransferTransport {
    suspend fun send(file: LocalFile, destinationDeviceId: String, onProgress: (Int) -> Unit)
}

interface TransferHistoryStore {
    fun load(): List<TransferRecord>
    fun save(records: List<TransferRecord>)
}

class LocalTransferRepository(private val store: TransferHistoryStore) {
    private val mutableRecords = MutableStateFlow(store.load())
    val records: StateFlow<List<TransferRecord>> = mutableRecords.asStateFlow()

    fun addSelection(file: LocalFile) = append(TransferRecord(file = file, status = TransferStatus.SELECTED))

    fun requestTransfer(id: String) = update(id) {
        require(it.status == TransferStatus.SELECTED) { "This file is not available to transfer." }
        it.copy(status = TransferStatus.WAITING_FOR_CONNECTION)
    }

    fun cancel(id: String) = update(id) {
        require(it.status == TransferStatus.SELECTED || it.status == TransferStatus.WAITING_FOR_CONNECTION) {
            "This transfer can no longer be cancelled."
        }
        it.copy(status = TransferStatus.CANCELLED)
    }

    private fun append(record: TransferRecord) {
        val records = mutableRecords.value + record
        store.save(records)
        mutableRecords.value = records
    }

    private fun update(id: String, transform: (TransferRecord) -> TransferRecord) {
        require(mutableRecords.value.any { it.id == id }) { "This transfer no longer exists." }
        val records = mutableRecords.value.map { if (it.id == id) transform(it) else it }
        store.save(records)
        mutableRecords.value = records
    }
}
