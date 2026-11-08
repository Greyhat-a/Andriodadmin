package com.nexus.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class DeviceRole { ADMIN, USER }

/** UNKNOWN means that no server has supplied authoritative presence data. */
enum class DeviceConnectionStatus { UNKNOWN, ONLINE, OFFLINE }

data class Device(
    val id: String,
    val name: String,
    val model: String,
    val role: DeviceRole,
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.UNKNOWN,
    val batteryPercent: Int? = null,
    val lastSeenEpochMillis: Long? = null,
) {
    init {
        require(id.isNotBlank()) { "A device identifier is required." }
        require(name.isNotBlank()) { "A device name is required." }
        require(model.isNotBlank()) { "A device model is required." }
        require(batteryPercent == null || batteryPercent in 0..100) { "Battery level must be between 0 and 100." }
        require(lastSeenEpochMillis == null || lastSeenEpochMillis >= 0) { "Last-seen time is invalid." }
    }
}

interface DeviceDataStore {
    fun load(): List<Device>
    fun save(devices: List<Device>)
}

interface DeviceRepository {
    val devices: StateFlow<List<Device>>
    fun register(name: String, model: String, role: DeviceRole): Device
    fun update(device: Device)
    fun remove(id: String)
}

class LocalDeviceRepository(private val store: DeviceDataStore) : DeviceRepository {
    private val mutableDevices = MutableStateFlow(store.load())
    override val devices: StateFlow<List<Device>> = mutableDevices.asStateFlow()

    override fun register(name: String, model: String, role: DeviceRole): Device {
        val cleanName = name.trim()
        val cleanModel = model.trim()
        require(cleanName.isNotEmpty()) { "Enter a device name." }
        require(cleanModel.isNotEmpty()) { "Enter a device model." }

        var id: String
        do {
            id = UUID.randomUUID().toString()
        } while (mutableDevices.value.any { it.id == id })
        val device = Device(
            id = id,
            name = cleanName,
            model = cleanModel,
            role = role,
        )
        save(mutableDevices.value + device)
        return device
    }

    override fun update(device: Device) {
        require(device.name.isNotBlank()) { "Enter a device name." }
        require(device.model.isNotBlank()) { "Enter a device model." }
        require(mutableDevices.value.any { it.id == device.id }) { "This device no longer exists." }
        save(mutableDevices.value.map { if (it.id == device.id) device.copy(name = device.name.trim(), model = device.model.trim()) else it })
    }

    override fun remove(id: String) {
        require(mutableDevices.value.any { it.id == id }) { "This device no longer exists." }
        save(mutableDevices.value.filterNot { it.id == id })
    }

    private fun save(devices: List<Device>) {
        store.save(devices)
        mutableDevices.value = devices
    }
}
