package com.nexus.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingCoordinatorTest {
    @Test
    fun generatedInvitationIsRandomShortLivedAndCanBeParsed() {
        val coordinator = PairingCoordinator(now = { NOW })
        val invitation = coordinator.createInvitation(DeviceRole.ADMIN)

        assertNotNull(coordinator.parse(invitation.toQrPayload()))
        assertEquals(PAIRING_INVITATION_LIFETIME_MILLIS, invitation.expiresAtEpochMillis - NOW)
        assertEquals(PairingValidation.READY, coordinator.validate(invitation, DeviceRole.USER))
        assertEquals(PairingValidation.SAME_ROLE, coordinator.validate(invitation, DeviceRole.ADMIN))
    }

    @Test
    fun confirmationConsumesInvitationAndPreventsDuplicates() {
        val coordinator = PairingCoordinator(now = { NOW })
        val invitation = coordinator.createInvitation(DeviceRole.USER)

        val request = coordinator.createRequest(invitation, DeviceRole.ADMIN)

        assertEquals(DeviceRole.ADMIN, request.localRole)
        assertEquals(DeviceRole.USER, request.remoteRole)
        assertEquals(PairingValidation.DUPLICATE, coordinator.validate(invitation, DeviceRole.ADMIN))
    }

    @Test
    fun expiredInvitationCannotBeConfirmed() {
        var currentTime = NOW
        val coordinator = PairingCoordinator(now = { currentTime })
        val invitation = coordinator.createInvitation(DeviceRole.ADMIN)
        currentTime += PAIRING_INVITATION_LIFETIME_MILLIS + 1

        assertEquals(PairingValidation.EXPIRED, coordinator.validate(invitation, DeviceRole.USER))
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
            coordinator.validate(invitation.copy(expiresAtEpochMillis = NOW + PAIRING_INVITATION_LIFETIME_MILLIS + 1), DeviceRole.USER),
        )
    }

    private companion object {
        const val NOW = 1_000_000L
    }
}
