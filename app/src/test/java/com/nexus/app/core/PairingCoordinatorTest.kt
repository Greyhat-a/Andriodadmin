package com.nexus.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingCoordinatorTest {
    @Test
    fun generatedInvitationIsRandomShortLivedAndCanBeParsed() {
        val issuer = PairingCoordinator(now = { NOW })
        val invitation = issuer.createInvitation(DeviceRole.ADMIN)
        val receiver = PairingCoordinator(now = { NOW })

        assertNotNull(receiver.parse(invitation.toQrPayload()))
        assertEquals(PAIRING_INVITATION_LIFETIME_MILLIS, invitation.expiresAtEpochMillis - NOW)
        assertEquals(PairingValidation.READY, receiver.validate(invitation, DeviceRole.USER))
        assertEquals(PairingValidation.SAME_ROLE, receiver.validate(invitation, DeviceRole.ADMIN))
        assertEquals(PairingValidation.SELF_INVITATION, issuer.validate(invitation, DeviceRole.USER))
    }

    @Test
    fun confirmationConsumesInvitationAndPreventsDuplicates() {
        val issuer = PairingCoordinator(now = { NOW })
        val invitation = issuer.createInvitation(DeviceRole.USER)
        val store = InMemoryPairingRequestStore()
        val coordinator = PairingCoordinator(now = { NOW }, requestStore = store)
        val scanned = coordinator.parse(invitation.toQrPayload())!!

        val request = coordinator.createRequest(scanned, DeviceRole.ADMIN)

        assertEquals(DeviceRole.ADMIN, request.localRole)
        assertEquals(DeviceRole.USER, request.remoteRole)
        assertEquals(PairingValidation.DUPLICATE, coordinator.validate(scanned, DeviceRole.ADMIN))
        assertEquals(
            PairingValidation.DUPLICATE,
            PairingCoordinator(now = { NOW }, requestStore = store).validate(scanned, DeviceRole.ADMIN),
        )
    }

    @Test
    fun expiredInvitationCannotBeConfirmed() {
        var currentTime = NOW
        val issuer = PairingCoordinator(now = { currentTime })
        val invitation = issuer.createInvitation(DeviceRole.ADMIN)
        val coordinator = PairingCoordinator(now = { currentTime })
        val scanned = coordinator.parse(invitation.toQrPayload())!!
        currentTime += PAIRING_INVITATION_LIFETIME_MILLIS + 1

        assertEquals(PairingValidation.EXPIRED, coordinator.validate(scanned, DeviceRole.USER))
    }

    @Test
    fun malformedAndTamperedInvitationsAreRejected() {
        val coordinator = PairingCoordinator(now = { NOW })
        val invitation = coordinator.createInvitation(DeviceRole.ADMIN)
        val tampered = invitation.copy(token = "A".repeat(43))

        assertNull(coordinator.parse("https://example.com/not-a-pairing-invitation"))
        assertTrue(coordinator.parse("nexus://pair?id=not-a-uuid&token=bad&role=ADMIN&expires=1") == null)
        assertEquals(PairingValidation.MALFORMED, coordinator.validate(tampered, DeviceRole.USER))
        assertEquals(
            PairingValidation.MALFORMED,
            coordinator.validate(invitation.copy(role = DeviceRole.USER), DeviceRole.ADMIN),
        )
        assertEquals(
            PairingValidation.MALFORMED,
            coordinator.validate(invitation.copy(expiresAtEpochMillis = NOW + PAIRING_INVITATION_LIFETIME_MILLIS + 1), DeviceRole.USER),
        )
    }

    private companion object {
        const val NOW = 1_000_000L
    }
}

private class InMemoryPairingRequestStore : PairingRequestStore {
    private var consumed = emptyMap<String, Long>()

    override fun loadConsumedInvitations(): Map<String, Long> = consumed.toMap()

    override fun saveConsumedInvitations(expirationsById: Map<String, Long>) {
        consumed = expirationsById.toMap()
    }
}
