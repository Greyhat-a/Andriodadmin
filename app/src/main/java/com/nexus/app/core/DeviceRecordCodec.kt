package com.nexus.app.core

import java.nio.charset.StandardCharsets
import java.util.Base64

object DeviceRecordCodec {
    fun encode(devices: List<Device>): String = devices.joinToString("\n") { device ->
        listOf(
            encodeField(device.id),
            encodeField(device.name),
            encodeField(device.model),
            device.role.name,
            device.connectionStatus.name,
            device.batteryPercent?.toString() ?: "-",
            device.lastSeenEpochMillis?.toString() ?: "-",
        ).joinToString("|")
    }

    fun decode(serialized: String): List<Device> {
        if (serialized.isEmpty()) return emptyList()
        val devices = serialized.split('\n').map { record ->
            val fields = record.split('|')
            check(fields.size == 7) { "Saved device data is malformed." }
            Device(
                id = decodeField(fields[0]),
                name = decodeField(fields[1]),
                model = decodeField(fields[2]),
                role = DeviceRole.valueOf(fields[3]),
                connectionStatus = DeviceConnectionStatus.valueOf(fields[4]),
                batteryPercent = fields[5].toNullableLong()?.toInt(),
                lastSeenEpochMillis = fields[6].toNullableLong(),
            )
        }
        check(devices.map { it.id }.toSet().size == devices.size) { "Saved device identifiers are duplicated." }
        return devices
    }

    private fun String.toNullableLong(): Long? = if (this == "-") null else toLong()

    private fun encodeField(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeField(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
}
