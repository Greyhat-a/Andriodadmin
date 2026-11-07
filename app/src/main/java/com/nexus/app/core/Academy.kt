package com.nexus.app.core

data class Tutorial(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val steps: List<String>,
)

object AcademyContent {
    val tutorials = listOf(
        Tutorial("getting-started", "Getting started with NEXUS", "Basics", "Learn what the current NEXUS app can do.", listOf(
            "Open Devices to register a local device profile with a name, model and role.",
            "Use the pairing panel to create or scan a short-lived invitation. A backend is required to establish a remote pairing.",
            "Use Map to request this phone's location, or Files to pick local documents.",
            "Open Settings for privacy notes and the app build information.",
        )),
        Tutorial("installing", "Installing NEXUS", "Basics", "Install a trusted NEXUS APK on an Android device.", listOf(
            "Obtain the APK from a release source you trust.",
            "Open the APK on the Android device and review the system installer details.",
            "If Android asks, allow installs for the specific app you used to open the APK. Revoke that permission afterward if it is no longer needed.",
            "Launch NEXUS from the app list. Installation does not automatically pair this device with another one.",
        )),
        Tutorial("registering", "Registering a device", "Devices", "Create and maintain a local device profile.", listOf(
            "Open Devices and select Add device.",
            "Enter a device name and model, then choose ADMIN or USER.",
            "Save the form. NEXUS generates a stable unique identifier and stores the profile on this phone.",
            "Open a device card to view, edit, or remove it. Removal requires confirmation.",
        )),
        Tutorial("pairing", "Pairing two devices", "Devices", "Create an expiring invitation without implying a remote connection.", listOf(
            "Open Devices and choose Pair device.",
            "Choose the role for this device and generate an invitation. The QR contains a random, short-lived invitation token, not an administrator password.",
            "On the other device, scan the QR with the pairing scanner and review the roles and expiration.",
            "Confirm to record a local pairing request. This build has no backend, so the request is not a completed pairing and cannot authorize either device.",
        )),
        Tutorial("roles", "Understanding ADMIN and USER roles", "Security", "Understand the labels assigned to local device profiles.", listOf(
            "ADMIN and USER are labels on locally registered profiles in this build.",
            "A server is not present to authenticate accounts or enforce privileges.",
            "Do not treat the local ADMIN label as authorization to control another device.",
            "A real deployment must enforce every privileged action on its backend.",
        )),
        Tutorial("temporary-admin", "Temporary ADMIN access", "Security", "Understand why temporary elevation is not available locally.", listOf(
            "NEXUS does not provide a local passkey or temporary ADMIN elevation screen.",
            "The architecture requires server-enforced authorization that expires after three hours.",
            "Never share a permanent administrator credential in a pairing QR code.",
        )),
        Tutorial("location-permissions", "Location permissions", "Location", "Share only this phone's location when you request it.", listOf(
            "Open Map and choose Allow location access.",
            "Android may offer approximate or precise access; choose deliberately.",
            "Choose Refresh location to request a fresh fix. The map distinguishes a fresh fix from the last-known fix.",
            "Location is not sent to another device in this build. Revoke NEXUS access in Android Settings at any time.",
        )),
        Tutorial("battery", "Battery monitoring", "Devices", "Understand battery data in this build.", listOf(
            "The device list does not invent remote battery readings.",
            "Battery and last-seen values appear only when supplied by an authorized device connection.",
            "No backend connection is configured in this build, so locally registered profiles show no battery or last-seen value.",
        )),
        Tutorial("file-transfers", "File transfers", "Files", "Select local files and understand transfer status.", listOf(
            "Open Files and choose Select files.",
            "Choose documents, images, audio or video in Android's system picker.",
            "Review the selected name, size and type. Preview opens the file with an installed viewer when available.",
            "Request transfer records that a transfer is waiting for a connection; it does not send the file. Cancel removes that pending request.",
        )),
        Tutorial("security-privacy", "Security and privacy", "Security", "Keep local data and permissions under your control.", listOf(
            "Device profiles and file-selection history are stored in the app's private preferences.",
            "NEXUS does not request location or camera access until you use the relevant screen.",
            "Pairing tokens are random, short-lived, and contain no permanent administrator credentials.",
            "No backend, account login, or remote authorization is configured. Avoid relying on local role labels for access control.",
        )),
        Tutorial("troubleshooting", "Troubleshooting", "Help", "Resolve common local setup issues.", listOf(
            "If a device profile does not appear, return to Devices and check whether it was saved.",
            "If QR scanning is unavailable, update Google Play services or paste a valid NEXUS invitation when the scanner flow offers that option.",
            "If location is missing, check Android's location toggle and NEXUS permission, then refresh again outdoors.",
            "If a file cannot be previewed, install an app that supports its type. NEXUS does not copy files to another device.",
        )),
        Tutorial("faq", "Frequently asked questions", "Help", "Answers about what this build does and does not do.", listOf(
            "Are devices connected after registration? No. Registration only creates a local profile.",
            "Does generating a QR pair devices? No. The invitation expires and a backend must authenticate and complete pairing.",
            "Can NEXUS see another device's location or battery? No. Neither remote data nor a paired-device backend is configured.",
            "Can I transfer a file to another device? Not in this build. File selection and a pending transfer-history model are available locally.",
        )),
    )
}
