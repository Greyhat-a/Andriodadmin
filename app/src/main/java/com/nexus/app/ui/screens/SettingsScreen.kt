package com.nexus.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.BuildConfig
import com.nexus.app.core.PhaseState
import com.nexus.app.core.Roadmap
import com.nexus.app.ui.theme.NexusExtras

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        NexusCard {
            SectionTitle("Appearance")
            InfoRow("Theme", "Follows system")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("Currently using", if (dark) "Dark" else "Light")
            Text(
                text = "Change Android's light or dark appearance and NEXUS follows it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        NexusCard {
            SectionTitle("Roles and security")
            InfoRow("ADMIN / USER", "Local profile labels only")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("Remote authorization", "Backend not configured")
            Text(
                "Local role labels do not grant access to another device. Temporary ADMIN elevation is unavailable until a backend can enforce authorization and the three-hour expiry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        NexusCard {
            SectionTitle("About NEXUS")
            InfoRow("Version", BuildConfig.VERSION_NAME)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("Package", BuildConfig.APPLICATION_ID)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("Build type", BuildConfig.BUILD_TYPE)
            Text(
                text = "Your devices. Your network. Your control.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        NexusCard {
            SectionTitle("Build progress")
            Roadmap.phases.forEachIndexed { index, phase ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val available = phase.state == PhaseState.AVAILABLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${phase.number}. ${phase.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (available) "Available" else "Planned",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (available) {
                            NexusExtras.colors.success
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
