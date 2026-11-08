package com.nexus.app.data

import android.content.SharedPreferences
import com.nexus.app.core.Device
import com.nexus.app.core.DeviceRecordCodec
import com.nexus.app.core.DeviceDataStore

class SharedPreferencesDeviceDataStore(
    private val preferences: SharedPreferences,
) : DeviceDataStore {
    override fun load(): List<Device> {
        val serialized = preferences.getString(KEY_DEVICES, "").orEmpty()
        return DeviceRecordCodec.decode(serialized)
    }

    override fun save(devices: List<Device>) {
        val serialized = DeviceRecordCodec.encode(devices)
        check(preferences.edit().putString(KEY_DEVICES, serialized).commit()) {
            "Could not save the device list."
        }
    }

    private companion object {
        const val KEY_DEVICES = "registered_devices_v1"
    }
}
