package com.nexus.app.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceViewModel(private val repository: DeviceRepository) : ViewModel() {
    val devices: StateFlow<List<Device>> = repository.devices

    private val mutableError = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = mutableError.asStateFlow()

    fun register(name: String, model: String, role: DeviceRole): Boolean = perform {
        repository.register(name, model, role)
    }

    fun update(device: Device): Boolean = perform { repository.update(device) }

    fun remove(id: String): Boolean = perform { repository.remove(id) }

    fun dismissError() {
        mutableError.value = null
    }

    private inline fun perform(action: () -> Any?): Boolean {
        return try {
            action()
            mutableError.value = null
            true
        } catch (error: IllegalArgumentException) {
            mutableError.value = error.message ?: "The device details are invalid."
            false
        } catch (error: IllegalStateException) {
            mutableError.value = error.message ?: "The device could not be saved."
            false
        }
    }
}
