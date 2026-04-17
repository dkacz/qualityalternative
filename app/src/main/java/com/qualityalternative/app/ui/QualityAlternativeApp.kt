package com.qualityalternative.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.compose.rememberLifecycleOwner
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.ui.theme.QualityAlternativeTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun QualityAlternativeApp(
    viewModel: MainViewModel,
) {
    QualityAlternativeTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val uiState = viewModel.uiState

        LaunchedEffect(uiState.latestMessage) {
            val message = uiState.latestMessage ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(message)
            viewModel.dismissMessage()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .safeDrawingPadding(),
                color = MaterialTheme.colorScheme.background,
            ) {
                when (uiState.screen) {
                    MainScreen.Home -> HomeScreen(
                        state = uiState,
                        onSelectTargetApp = viewModel::selectTargetApp,
                        onTriggerIntervention = viewModel::triggerDebugIntervention,
                    )

                    MainScreen.Intervention -> InterventionScreen(
                        state = uiState,
                        onAcceptPrimary = viewModel::acceptPrimary,
                        onAcceptBackup = viewModel::acceptBackup,
                        onDelay = viewModel::delayFor15Minutes,
                        onOpenAnyway = viewModel::openAnyway,
                    )

                    MainScreen.Reader -> ReaderScreen(
                        state = uiState,
                        onFinishReading = viewModel::finishReading,
                    )

                    MainScreen.Feedback -> FeedbackScreen(
                        onSubmit = viewModel::submitFeedback,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: MainUiState,
    onSelectTargetApp: (DistractingApp) -> Unit,
    onTriggerIntervention: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quality Alternative",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Sprint 0-1 prototype: a manual replacement loop with starter packs, recommendation ranking, and local analytics.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Debug trigger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.supportedApps.forEach { app ->
                            FilterChip(
                                selected = state.selectedTargetApp == app,
                                onClick = { onSelectTargetApp(app) },
                                label = { Text(app.displayName) },
                            )
                        }
                    }
                    Button(
                        onClick = onTriggerIntervention,
                        enabled = state.selectedTargetApp != null,
                    ) {
                        Text("Trigger debug intervention")
                    }
                }
            }
        }

        item {
            PreferenceSummary(state = state)
        }

        item {
            StarterPackSummary(starterPacks = state.starterPacks)
        }

        item {
            AnalyticsLog(events = state.events)
        }
    }
}

@Composable
private fun PreferenceSummary(state: MainUiState) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Prototype preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Duration bucket: ${state.preferences?.preferredDurationBucket?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Preferred topics: ${state.preferences?.preferredTopics?.joinToString()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.completedContentIds.isNotEmpty()) {
                Text(
                    text = "Completed items excluded from future primary recommendations: ${state.completedContentIds.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StarterPackSummary(starterPacks: List<EditorialPack>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Editorial starter packs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            starterPacks.forEach { pack ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = pack.title, fontWeight = FontWeight.Medium)
                    Text(text = pack.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${pack.items.size} items", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsLog(events: List<AnalyticsEvent>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Local analytics ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (events.isEmpty()) {
                Text(
                    text = "No events yet. Trigger an intervention to record the first session.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                events.take(8).forEach { event ->
                    Text(
                        text = "${event.type.name} • ${event.targetAppPackage ?: "prototype"} • ${formatTimestamp(event.timestampMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun InterventionScreen(
    state: MainUiState,
    onAcceptPrimary: () -> Unit,
    onAcceptBackup: (ContentItem) -> Unit,
    onDelay: () -> Unit,
    onOpenAnyway: () -> Unit,
) {
    val recommendationSet = state.currentRecommendationSet ?: return
    val targetApp = state.selectedTargetApp ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Pause before ${targetApp.displayName}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "One strong replacement, two lighter backups, and an explicit override path.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RecommendationCard(
            title = recommendationSet.primary.title,
            subtitle = recommendationSet.primary.description,
            durationLabel = "${recommendationSet.primary.durationMinutes} min",
            actionLabel = "Read now",
            highlighted = true,
            onClick = onAcceptPrimary,
        )

        recommendationSet.backups.forEach { backup ->
            RecommendationCard(
                title = backup.title,
                subtitle = backup.description,
                durationLabel = "${backup.durationMinutes} min",
                actionLabel = "Choose backup",
                highlighted = false,
                onClick = { onAcceptBackup(backup) },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDelay) {
                Text("Delay for 15 minutes")
            }
            Button(onClick = onOpenAnyway) {
                Text("Open anyway")
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    title: String,
    subtitle: String,
    durationLabel: String,
    actionLabel: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AssistChip(onClick = {}, label = { Text(durationLabel) })
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ReaderScreen(
    state: MainUiState,
    onFinishReading: () -> Unit,
) {
    val content = state.currentContent ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = content.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${content.durationMinutes} min • ${content.topicTags.joinToString()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = state.currentContentBody,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onFinishReading) {
            Text("Finish session")
        }
    }
}

@Composable
private fun FeedbackScreen(
    onSubmit: (Boolean, Boolean) -> Unit,
) {
    var wasGoodFit by remember { mutableStateOf(true) }
    var helped by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Feedback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        BinaryQuestion(
            title = "Was this a good fit?",
            selected = wasGoodFit,
            onSelect = { wasGoodFit = it },
        )
        BinaryQuestion(
            title = "Did it help you avoid mindless scrolling?",
            selected = helped,
            onSelect = { helped = it },
        )
        Button(onClick = { onSubmit(wasGoodFit, helped) }) {
            Text("Submit feedback")
        }
    }
}

@Composable
private fun BinaryQuestion(
    title: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(true) },
                    label = { Text("Yes") },
                )
                FilterChip(
                    selected = !selected,
                    onClick = { onSelect(false) },
                    label = { Text("No") },
                )
            }
        }
    }
}

private fun formatTimestamp(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return formatter.format(
        Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()),
    )
}
