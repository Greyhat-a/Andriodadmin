package com.nexus.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceRepositoryTest {
    @Test
    fun registrationTrimsFieldsCreatesUniqueIdAndStartsWithoutInventedStatus() {
        val repository = LocalDeviceRepository(InMemoryDeviceDataStore())

        val first = repository.register("  Hall tablet ", " Pixel Tablet ", DeviceRole.ADMIN)
        val second = repository.register("Hall tablet", "Pixel Tablet", DeviceRole.USER)

        assertEquals("Hall tablet", first.name)
        assertEquals("Pixel Tablet", first.model)
        assertNotEquals(first.id, second.id)
        assertEquals(DeviceConnectionStatus.UNKNOWN, first.connectionStatus)
        assertNull(first.batteryPercent)
        assertNull(first.lastSeenEpochMillis)
    }

    @Test
    fun editAndDeleteUpdateRepositoryState() {
        val repository = LocalDeviceRepository(InMemoryDeviceDataStore())
        val registered = repository.register("Tablet", "Model A", DeviceRole.USER)

        repository.update(registered.copy(name = "Renamed", model = "Model B", role = DeviceRole.ADMIN))
        assertEquals("Renamed", repository.devices.value.single().name)
        assertEquals("Model B", repository.devices.value.single().model)
        assertEquals(DeviceRole.ADMIN, repository.devices.value.single().role)

        repository.remove(registered.id)
        assertTrue(repository.devices.value.isEmpty())
    }

    @Test
    fun repositoryReloadsSavedDevicesFromItsDataStore() {
        val store = InMemoryDeviceDataStore()
        val original = LocalDeviceRepository(store)
        val registered = original.register("Office", "Phone", DeviceRole.ADMIN)

        val restored = LocalDeviceRepository(store)

        assertEquals(listOf(registered), restored.devices.value)
    }

    @Test
    fun invalidDeviceDataDoesNotChangeStoredList() {
        val store = InMemoryDeviceDataStore()
        val repository = LocalDeviceRepository(store)
        repository.register("Valid", "Phone", DeviceRole.USER)

        assertThrows(IllegalArgumentException::class.java) {
            repository.register(" ", "Phone", DeviceRole.USER)
        }
        assertEquals(1, repository.devices.value.size)
        assertEquals(repository.devices.value, store.load())
    }
}

private class InMemoryDeviceDataStore : DeviceDataStore {
    private var data = emptyList<Device>()

    override fun load(): List<Device> = data.toList()

    override fun save(devices: List<Device>) {
        data = devices.toList()
    }
}
