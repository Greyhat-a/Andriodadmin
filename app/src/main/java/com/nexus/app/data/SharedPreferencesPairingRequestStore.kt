package com.nexus.app.data

import android.content.SharedPreferences
import com.nexus.app.core.PairingRequestStore
import java.nio.charset.StandardCharsets
import java.util.Base64

class SharedPreferencesPairingRequestStore(
    private val preferences: SharedPreferences,
) : PairingRequestStore {
    override fun loadConsumedInvitations(): Map<String, Long> {
        val serialized = preferences.getString(KEY_CONSUMED_INVITATIONS, "").orEmpty()
        if (serialized.isEmpty()) return emptyMap()
        val entries = serialized.split('\n').map { record ->
            val fields = record.split('|')
            check(fields.size == 2) { "Saved pairing data is malformed." }
            decode(fields[0]) to fields[1].toLong()
        }
        check(entries.map { it.first }.toSet().size == entries.size) {
            "Saved pairing invitations are duplicated."
        }
        return entries.toMap()
    }

    override fun saveConsumedInvitations(expirationsById: Map<String, Long>) {
        val serialized = expirationsById.entries.joinToString("\n") { (id, expiration) ->
            "${encode(id)}|$expiration"
        }
        check(preferences.edit().putString(KEY_CONSUMED_INVITATIONS, serialized).commit()) {
            "Could not save pairing request state."
        }
    }

    private fun encode(value: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decode(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)

    private companion object {
        const val KEY_CONSUMED_INVITATIONS = "consumed_pairing_invitations_v1"
    }
}
