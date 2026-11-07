package com.nexus.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DevicesScreen(modifier: Modifier = Modifier) {
    PlannedFeatureScreen(
        title = "Devices",
        summary = "Every device enrolled under your account will appear here.",
        phaseNumber = 5,
        plannedItems = listOf(
            "Name, model, role and online status for each device",
            "Battery level, charging state and last-seen time",
            "Search, sort, filter, rename and remove",
            "Add Device with QR and link invitations (Phase 3)",
        ),
        modifier = modifier,
    )
}

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    PlannedFeatureScreen(
        title = "Map",
        summary = "Locations that devices have chosen to share.",
        phaseNumber = 6,
        plannedItems = listOf(
            "Last known location and live location, clearly distinguished",
            "Update time and accuracy for every marker",
            "Sharing only after the device owner grants permission",
        ),
        modifier = modifier,
    )
}

@Composable
fun FilesScreen(modifier: Modifier = Modifier) {
    PlannedFeatureScreen(
        title = "Files",
        summary = "Send and receive files between your devices.",
        phaseNumber = 8,
        plannedItems = listOf(
            "ADMIN-to-USER and USER-to-ADMIN transfers",
            "Documents, images, video and audio via the system file picker",
            "Progress, history, failure handling and resume where supported",
        ),
        modifier = modifier,
    )
}

@Composable
fun AcademyScreen(modifier: Modifier = Modifier) {
    PlannedFeatureScreen(
        title = "NEXUS Academy",
        summary = "Step-by-step guides for installing, pairing and using NEXUS.",
        phaseNumber = 9,
        plannedItems = listOf(
            "Getting started and installing NEXUS",
            "Pairing devices and the ADMIN and USER guides",
            "Temporary ADMIN, remote management, battery and location",
            "File transfer, security, troubleshooting and FAQ",
            "Tutorials are written against the finished interface, with no mock screenshots",
        ),
        modifier = modifier,
    )
}
