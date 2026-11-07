package com.nexus.app.core

/** Whether a roadmap phase is implemented in this build. */
enum class PhaseState { AVAILABLE, PLANNED }

data class RoadmapPhase(
    val number: Int,
    val title: String,
    val summary: String,
    val state: PhaseState,
)

/**
 * The ten delivery phases from the NEXUS master prompt. The UI reads this list
 * so that what the app says about itself always matches what is built. A phase
 * is only switched to AVAILABLE in the same change that implements and tests it.
 */
object Roadmap {
    val phases: List<RoadmapPhase> = listOf(
        RoadmapPhase(
            1, "Foundation",
            "Project structure, branding, light/dark themes, navigation and CI.",
            PhaseState.AVAILABLE,
        ),
        RoadmapPhase(
            2, "Accounts and roles",
            "Authentication and the ADMIN and USER experiences.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            3, "Pairing",
            "QR and link invitations, App Links and enrollment.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            4, "Temporary ADMIN",
            "Passkey promotion with a server-enforced three-hour expiry.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            5, "Monitoring and battery",
            "Device status, presence and battery reporting with alerts.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            6, "Location",
            "Shared device locations on a map, live and last known.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            7, "Remote sessions",
            "Consent-based screen, audio and camera sessions.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            8, "File transfer",
            "Secure transfers between ADMIN and USER devices.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            9, "NEXUS Academy",
            "Interactive tutorials that match the implemented interface.",
            PhaseState.PLANNED,
        ),
        RoadmapPhase(
            10, "Hardening and release",
            "Security review, device testing and signed releases.",
            PhaseState.PLANNED,
        ),
    )

    fun phase(number: Int): RoadmapPhase =
        phases.first { it.number == number }
}
