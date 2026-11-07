package com.nexus.app.data

import android.content.SharedPreferences
import com.nexus.app.core.Device
import com.nexus.app.core.DeviceConnectionStatus
import com.nexus.app.core.DeviceDataStore
import com.nexus.app.core.DeviceRole
import java.nio.charset.StandardCharsets
import java.util.Base64

class SharedPreferencesDeviceDataStore(
    private val preferences: SharedPreferences,
) : DeviceDataStore {
    override fun load(): List<Device> {
        val serialized = preferences.getString(KEY_DEVICES, "").orEmpty()
        if (serialized.isEmpty()) return emptyList()
        return serialized.split('\n').map { record ->
            val fields = record.split('|')
            check(fields.size == 7) { "Saved device data is malformed." }
            Device(
                id = decode(fields[0]),
                name = decode(fields[1]),
                model = decode(fields[2]),
                role = DeviceRole.valueOf(fields[3]),
                connectionStatus = DeviceConnectionStatus.valueOf(fields[4]),
                batteryPercent = fields[5].toNullableLong()?.toInt(),
                lastSeenEpochMillis = fields[6].toNullableLong(),
            )
        }
    }

    override fun save(devices: List<Device>) {
        val serialized = devices.joinToString("\n") { device ->
            listOf(
                encode(device.id),
                encode(device.name),
                encode(device.model),
                device.role.name,
                device.connectionStatus.name,
                device.batteryPercent?.toString() ?: "-",
                device.lastSeenEpochMillis?.toString() ?: "-",
            ).joinToString("|")
        }
        check(preferences.edit().putString(KEY_DEVICES, serialized).commit()) {
            "Could not save the device list."
        }
    }

    private fun String.toNullableLong(): Long? = if (this == "-") null else toLong()

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decode(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)

    private companion object {
        const val KEY_DEVICES = "registered_devices_v1"
    }
}
