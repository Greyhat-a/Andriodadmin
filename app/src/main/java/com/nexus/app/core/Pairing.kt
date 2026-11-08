package com.nexus.app.core

import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

const val PAIRING_INVITATION_LIFETIME_MILLIS = 10 * 60 * 1000L

data class PairingInvitation(
    val id: String,
    val token: String,
    val role: DeviceRole,
    val expiresAtEpochMillis: Long,
) {
    fun toQrPayload(): String =
        "nexus://pair?id=$id&token=$token&role=${role.name}&expires=$expiresAtEpochMillis"
}

data class PairingRequest(
    val invitationId: String,
    val localRole: DeviceRole,
    val remoteRole: DeviceRole,
    val createdAtEpochMillis: Long,
)

interface PairingRequestStore {
    fun loadConsumedInvitations(): Map<String, Long>
    fun saveConsumedInvitations(expirationsById: Map<String, Long>)
}

enum class PairingValidation {
    READY,
    MALFORMED,
    EXPIRED,
    SAME_ROLE,
    SELF_INVITATION,
    DUPLICATE,
}

/** Client-side protocol checks only; a server must authenticate and authorize pairing. */
class PairingCoordinator(
    private val now: () -> Long = System::currentTimeMillis,
    private val random: SecureRandom = SecureRandom(),
    private val requestStore: PairingRequestStore = InMemoryPairingRequestStore(),
) {
    private val consumedInvitationExpirations = requestStore.loadConsumedInvitations().toMutableMap()
    private val locallyIssuedInvitations = mutableMapOf<String, IssuedInvitation>()

    fun createInvitation(role: DeviceRole): PairingInvitation {
        val issuedAt = now()
        locallyIssuedInvitations.entries.removeAll { it.value.expiresAtEpochMillis <= issuedAt }
        val tokenBytes = ByteArray(32).also(random::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
        val id = UUID.randomUUID().toString()
        val invitation = PairingInvitation(
            id = id,
            token = token,
            role = role,
            expiresAtEpochMillis = issuedAt + PAIRING_INVITATION_LIFETIME_MILLIS,
        )
        locallyIssuedInvitations[id] = IssuedInvitation(sha256(token), role, invitation.expiresAtEpochMillis)
        return invitation
    }

    fun parse(payload: String): PairingInvitation? {
        val uri = try {
            URI(payload)
        } catch (_: URISyntaxException) {
            return null
        }
        if (uri.scheme != "nexus" || uri.host != "pair") return null
        val query = uri.rawQuery?.split('&')
            ?.mapNotNull { item ->
                val parts = item.split('=', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }?.toMap() ?: return null
        return try {
            val id = query.getValue("id")
            UUID.fromString(id)
            val token = query.getValue("token")
            if (Base64.getUrlDecoder().decode(token).size != 32) return null
            PairingInvitation(
                id = id,
                token = token,
                role = DeviceRole.valueOf(query.getValue("role")),
                expiresAtEpochMillis = query.getValue("expires").toLong(),
            )
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: NoSuchElementException) {
            null
        }
    }

    fun validate(invitation: PairingInvitation, localRole: DeviceRole): PairingValidation {
        val currentTime = now()
        if (invitation.expiresAtEpochMillis <= currentTime) return PairingValidation.EXPIRED
        if (invitation.expiresAtEpochMillis - currentTime > PAIRING_INVITATION_LIFETIME_MILLIS) {
            return PairingValidation.MALFORMED
        }
        if (consumedInvitationExpirations[invitation.id]?.let { it > currentTime } == true) {
            return PairingValidation.DUPLICATE
        }
        val tokenSize = try {
            Base64.getUrlDecoder().decode(invitation.token).size
        } catch (_: IllegalArgumentException) {
            return PairingValidation.MALFORMED
        }
        if (tokenSize != 32) {
            return PairingValidation.MALFORMED
        }
        val issued = locallyIssuedInvitations[invitation.id]
        if (issued != null) {
            if (invitation.role != issued.role ||
                invitation.expiresAtEpochMillis != issued.expiresAtEpochMillis ||
                !MessageDigest.isEqual(issued.tokenHash, sha256(invitation.token))
            ) {
                return PairingValidation.MALFORMED
            }
            return PairingValidation.SELF_INVITATION
        }
        if (invitation.role == localRole) return PairingValidation.SAME_ROLE
        return PairingValidation.READY
    }

    /** Records a local request only; it does not mark either device as paired. */
    fun createRequest(invitation: PairingInvitation, localRole: DeviceRole): PairingRequest {
        require(validate(invitation, localRole) == PairingValidation.READY) {
            "This invitation is invalid, expired, duplicated, or uses the same role."
        }
        val currentTime = now()
        val activeInvitations = consumedInvitationExpirations
            .filterValues { it > currentTime }
            .toMutableMap()
            .apply { put(invitation.id, invitation.expiresAtEpochMillis) }
        requestStore.saveConsumedInvitations(activeInvitations)
        consumedInvitationExpirations.clear()
        consumedInvitationExpirations.putAll(activeInvitations)
        return PairingRequest(invitation.id, localRole, invitation.role, currentTime)
    }

    private fun sha256(token: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))

    private data class IssuedInvitation(
        val tokenHash: ByteArray,
        val role: DeviceRole,
        val expiresAtEpochMillis: Long,
    )
}

private class InMemoryPairingRequestStore : PairingRequestStore {
    private var expirationsById = emptyMap<String, Long>()

    override fun loadConsumedInvitations(): Map<String, Long> = expirationsById.toMap()

    override fun saveConsumedInvitations(expirationsById: Map<String, Long>) {
        this.expirationsById = expirationsById.toMap()
    }
}
