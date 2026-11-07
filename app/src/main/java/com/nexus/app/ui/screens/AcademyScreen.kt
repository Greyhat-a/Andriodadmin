package com.nexus.app.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.core.AcademyContent
import com.nexus.app.core.Tutorial

private const val COMPLETED_TUTORIALS_KEY = "completed_tutorials_v1"

@Composable
fun AcademyScreen(preferences: SharedPreferences, modifier: Modifier = Modifier) {
    val completed = remember {
        mutableStateOf(preferences.getStringSet(COMPLETED_TUTORIALS_KEY, emptySet()).orEmpty().toSet())
    }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    var selected by remember { mutableStateOf<Tutorial?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val categories = listOf("All") + AcademyContent.tutorials.map { it.category }.distinct()
    val visible = AcademyContent.tutorials.filter { tutorial ->
        (category == "All" || tutorial.category == category) &&
            (tutorial.title.contains(query, true) ||
                tutorial.summary.contains(query, true) ||
                tutorial.steps.any { it.contains(query, true) })
    }
    val progress = completed.value.size.coerceAtMost(AcademyContent.tutorials.size)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("NEXUS Academy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Guides describe the features available in this build and clearly call out backend requirements.")
        NexusCard {
            Text("Learning progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("$progress of ${AcademyContent.tutorials.size} tutorials completed")
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress.toFloat() / AcademyContent.tutorials.size },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search tutorials") },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text(option) },
                )
            }
        }
        if (visible.isEmpty()) {
            NexusCard { Text("No tutorials match your search.") }
        } else {
            visible.forEach { tutorial ->
                androidx.compose.material3.Card(
                    onClick = { selected = tutorial },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tutorial.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(tutorial.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(tutorial.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (tutorial.id in completed.value) "Completed" else "Not completed",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }

    selected?.let { tutorial ->
        val isComplete = tutorial.id in completed.value
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(tutorial.title) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(tutorial.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    tutorial.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step")
                    }
                    saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val next = if (isComplete) completed.value - tutorial.id else completed.value + tutorial.id
                    val saved = preferences.edit().putStringSet(COMPLETED_TUTORIALS_KEY, next).commit()
                    if (saved) {
                        completed.value = next
                        saveError = null
                        selected = null
                    } else {
                        saveError = "Tutorial progress could not be saved."
                    }
                }) { Text(if (isComplete) "Mark incomplete" else "Mark complete") }
            },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("Close") } },
        )
    }
}
