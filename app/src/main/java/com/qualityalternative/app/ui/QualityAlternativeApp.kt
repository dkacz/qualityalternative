package com.qualityalternative.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.ui.theme.QualityAlternativeAppTheme
import com.qualityalternative.app.ui.theme.QualityAlternativeThemeTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun QualityAlternativeApp(
    viewModel: MainViewModel,
    onExitToTarget: () -> Unit = {},
) {
    val uiState = viewModel.uiState

    QualityAlternativeAppTheme(themeMode = uiState.themeMode) {
        val snackbarHostState = remember { SnackbarHostState() }

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
                when {
                    uiState.isLoadingSettings -> LoadingScreen()
                    !uiState.hasCompletedOnboarding -> OnboardingScreen(
                        selection = uiState.onboardingSelection,
                        supportedApps = uiState.allSupportedApps,
                        starterPacks = uiState.starterPacks,
                        onToggleApp = viewModel::toggleOnboardingApp,
                        onToggleTopic = viewModel::toggleOnboardingTopic,
                        onSelectDuration = viewModel::setOnboardingDuration,
                        onTogglePack = viewModel::toggleOnboardingPack,
                        onComplete = viewModel::completeOnboarding,
                    )

                    else -> when (uiState.screen) {
                        MainScreen.Onboarding -> OnboardingScreen(
                            selection = uiState.onboardingSelection,
                            supportedApps = uiState.allSupportedApps,
                            starterPacks = uiState.starterPacks,
                            onToggleApp = viewModel::toggleOnboardingApp,
                            onToggleTopic = viewModel::toggleOnboardingTopic,
                            onSelectDuration = viewModel::setOnboardingDuration,
                            onTogglePack = viewModel::toggleOnboardingPack,
                            onComplete = viewModel::completeOnboarding,
                        )

                        MainScreen.Home -> HomeScreen(
                            state = uiState,
                            onSelectTargetApp = viewModel::selectTargetApp,
                            onTriggerIntervention = viewModel::triggerDebugIntervention,
                            onRefreshReadiness = viewModel::refreshPermissionReadiness,
                            onOpenAddLink = viewModel::openAddLink,
                            onSelectTheme = viewModel::selectThemeMode,
                        )

                        MainScreen.AddLink -> AddLinkScreen(
                            form = uiState.addLinkForm,
                            onUrlChange = viewModel::updateAddLinkUrl,
                            onTitleChange = viewModel::updateAddLinkTitle,
                            onDurationChange = viewModel::updateAddLinkDuration,
                            onToggleTopic = viewModel::toggleAddLinkTopic,
                            onSave = viewModel::saveUserLink,
                            onCancel = viewModel::cancelAddLink,
                        )

                        MainScreen.Intervention -> InterventionScreen(
                            state = uiState,
                            onAcceptPrimary = viewModel::acceptPrimary,
                            onAcceptBackup = viewModel::acceptBackup,
                            onDelay = viewModel::delayFor15Minutes,
                            onOpenAnyway = {
                                if (viewModel.openAnyway()) {
                                    onExitToTarget()
                                }
                            },
                        )

                        MainScreen.Reader -> ReaderScreen(
                            state = uiState,
                            onFinishReading = viewModel::finishReading,
                            onSkipReading = viewModel::skipReading,
                        )

                        MainScreen.ExternalHandoff -> ExternalLinkHandoffScreen(
                            state = uiState,
                            onOpenLink = {
                                launchExternalLink(context = it, viewModel = viewModel)
                            },
                            onFinishSession = viewModel::finishReading,
                            onSkipSession = viewModel::skipReading,
                        )

                        MainScreen.Feedback -> FeedbackScreen(
                            onSubmit = viewModel::submitFeedback,
                            onSkip = viewModel::skipFeedback,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading local replacement state…",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(
    selection: OnboardingSelection,
    supportedApps: List<DistractingApp>,
    starterPacks: List<EditorialPack>,
    onToggleApp: (DistractingApp) -> Unit,
    onToggleTopic: (TopicTag) -> Unit,
    onSelectDuration: (DurationBucket) -> Unit,
    onTogglePack: (EditorialPack) -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Let’s set up your replacement loop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Choose at least 3 distracting apps, 3 topics, a preferred session length, and at least 1 starter pack.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SelectionSection(title = "Distracting apps") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                supportedApps.forEach { app ->
                    FilterChip(
                        selected = app.packageName in selection.selectedAppPackages,
                        onClick = { onToggleApp(app) },
                        label = { Text(app.displayName) },
                    )
                }
            }
            Text(
                text = "${selection.selectedAppPackages.size} selected",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SelectionSection(title = "Topics") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TopicTag.entries.forEach { topic ->
                    FilterChip(
                        selected = topic in selection.preferredTopics,
                        onClick = { onToggleTopic(topic) },
                        label = { Text(topic.displayName()) },
                    )
                }
            }
            Text(
                text = "${selection.preferredTopics.size} selected",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SelectionSection(title = "Preferred session length") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DurationBucket.entries.forEach { bucket ->
                    FilterChip(
                        selected = bucket == selection.preferredDurationBucket,
                        onClick = { onSelectDuration(bucket) },
                        label = { Text(bucket.displayName()) },
                    )
                }
            }
        }

        SelectionSection(title = "Starter packs") {
            starterPacks.forEach { pack ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FilterChip(
                            selected = pack.id in selection.selectedPackIds,
                            onClick = { onTogglePack(pack) },
                            label = { Text(if (pack.id in selection.selectedPackIds) "Selected" else "Select") },
                        )
                        Text(text = pack.title, fontWeight = FontWeight.SemiBold)
                        Text(text = pack.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${pack.items.size} items", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Button(
            onClick = onComplete,
            enabled = selection.isValid(),
        ) {
            Text("Complete setup")
        }
    }
}

@Composable
private fun SelectionSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: MainUiState,
    onSelectTargetApp: (DistractingApp) -> Unit,
    onTriggerIntervention: () -> Unit,
    onRefreshReadiness: () -> Unit,
    onOpenAddLink: () -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val selectedPacks = state.starterPacks.filter { pack ->
        pack.id in state.preferences?.selectedPackIds.orEmpty()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home-list"),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = "Quality Alternative",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.faintText,
                )
                Text(
                    text = "You're set up for quieter reading today.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "When an impulse opens a selected app, the Android alpha can pause it and offer one finite alternative.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.mutedText,
                )
                Button(
                    modifier = Modifier.testTag("home-add-link"),
                    onClick = onOpenAddLink,
                ) {
                    Text("Add link")
                }
            }
        }

        item {
            SetupSnapshotCard(
                state = state,
                onSelectTargetApp = onSelectTargetApp,
            )
        }

        item {
            PersonalLibraryCard(
                userLinks = state.userLinks,
                selectedStarterPacks = selectedPacks,
                onOpenAddLink = onOpenAddLink,
            )
        }

        item {
            PreviewInterventionCard(
                state = state,
                onSelectTargetApp = onSelectTargetApp,
                onTriggerIntervention = onTriggerIntervention,
            )
        }

        item {
            PermissionReadinessCard(
                readiness = state.permissionReadiness,
                onRefresh = onRefreshReadiness,
            )
        }

        item {
            ThemeSettingsCard(
                themeMode = state.themeMode,
                onSelectTheme = onSelectTheme,
            )
        }

        item {
            ReplacementHistoryCard(entries = state.historyEntries)
        }

        item {
            PreferenceSummary(state = state)
        }

        item {
            StarterPackSummary(starterPacks = selectedPacks)
        }

        item {
            AnalyticsLog(events = state.events)
        }
    }
}

@Composable
private fun SetupSnapshotCard(
    state: MainUiState,
    onSelectTargetApp: (DistractingApp) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    AnalogCard {
        HomeSectionLabel(text = "Setup")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            StatCell(
                modifier = Modifier.weight(1f),
                label = "Intercepting",
                value = "${state.availableTargetApps.size} apps",
            )
            StatCell(
                modifier = Modifier.weight(1f),
                label = "Session length",
                value = state.preferences?.preferredDurationBucket?.displayName() ?: "Unset",
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            StatCell(
                modifier = Modifier.weight(1f),
                label = "Topics",
                value = "${state.preferences?.preferredTopics?.size ?: 0}",
            )
            StatCell(
                modifier = Modifier.weight(1f),
                label = "Status",
                value = if (state.permissionReadiness.interceptionReady) "Active" else "Paused",
                accent = if (state.permissionReadiness.interceptionReady) colors.success else colors.accent,
            )
        }
        if (state.availableTargetApps.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.availableTargetApps.forEach { app ->
                    FilterChip(
                        selected = state.selectedTargetApp == app,
                        onClick = { onSelectTargetApp(app) },
                        label = { Text(app.displayName) },
                    )
                }
            }
        }
        state.activeDelayWindow?.let { delayWindow ->
            ActiveDelayNotice(
                targetApp = state.selectedTargetApp,
                delayWindow = delayWindow,
            )
        }
    }
}

@Composable
private fun StatCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color = QualityAlternativeThemeTokens.colors.primaryText,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = QualityAlternativeThemeTokens.colors.faintText,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PreviewInterventionCard(
    state: MainUiState,
    onSelectTargetApp: (DistractingApp) -> Unit,
    onTriggerIntervention: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    AnalogCard {
        HomeSectionLabel(text = "Preview intervention")
        Text(
            text = "Normally this appears automatically. Use this internal shortcut to rehearse the moment without opening a social app.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.mutedText,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.availableTargetApps.forEach { app ->
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
            Text("Preview intervention")
        }
    }
}

@Composable
private fun AnalogCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 1.dp, color = colors.line),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun HomeSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = QualityAlternativeThemeTokens.colors.faintText,
    )
}

@Composable
private fun PersonalLibraryCard(
    userLinks: List<ContentItem>,
    selectedStarterPacks: List<EditorialPack>,
    onOpenAddLink: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val editorialItems = selectedStarterPacks.sumOf { it.items.size }
    val editorialMinutes = selectedStarterPacks.flatMap { it.items }.sumOf(ContentItem::durationMinutes)

    AnalogCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HomeSectionLabel(text = "Your library")
                Text(
                    text = "A small shelf for impulse moments.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "${editorialItems + userLinks.size} items",
                style = MaterialTheme.typography.labelMedium,
                color = colors.faintText,
            )
        }

        LibrarySummaryRow(
            label = "Editorial picks",
            value = "$editorialItems curated • $editorialMinutes min",
            sourceType = ContentSourceType.EDITORIAL,
        )
        LibrarySummaryRow(
            label = "Your added links",
            value = "Personal library: ${userLinks.size} ${if (userLinks.size == 1) "link" else "links"} saved.",
            sourceType = ContentSourceType.USER_LINK,
        )
        userLinks.take(2).forEach { link ->
            SavedLinkRow(link = link)
        }
        if (userLinks.isEmpty()) {
            Text(
                text = "Add one piece you would genuinely read instead of scrolling.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedText,
            )
        }
        Button(
            onClick = onOpenAddLink,
        ) {
            Text("Add link")
        }
    }
}

@Composable
private fun LibrarySummaryRow(
    label: String,
    value: String,
    sourceType: ContentSourceType,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SourceBadge(sourceType = sourceType)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedText,
            )
        }
    }
}

@Composable
private fun SavedLinkRow(link: ContentItem) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = "Saved link • ${link.durationMinutes} min",
            style = MaterialTheme.typography.labelMedium,
            color = colors.faintText,
        )
        Text(
            text = link.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SourceBadge(sourceType: ContentSourceType) {
    val colors = QualityAlternativeThemeTokens.colors
    val label = when (sourceType) {
        ContentSourceType.EDITORIAL -> "E"
        ContentSourceType.USER_LINK -> "L"
    }
    val background = when (sourceType) {
        ContentSourceType.EDITORIAL -> colors.accentSoft
        ContentSourceType.USER_LINK -> colors.successSoft
    }
    val foreground = when (sourceType) {
        ContentSourceType.EDITORIAL -> colors.accent
        ContentSourceType.USER_LINK -> colors.success
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            text = label,
            color = foreground,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingsCard(
    themeMode: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Theme: ${themeMode.displayName()}",
                color = colors.mutedText,
            )
            Text(
                text = "Light keeps the warm paper feel. Dark is the calmer mode for night testing.",
                color = colors.mutedText,
                style = MaterialTheme.typography.bodySmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        modifier = Modifier.testTag("theme-${mode.name}"),
                        selected = themeMode == mode,
                        onClick = { onSelectTheme(mode) },
                        label = { Text(mode.displayName()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddLinkScreen(
    form: AddLinkFormState,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onToggleTopic: (TopicTag) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = "Add a replacement link",
                style = MaterialTheme.typography.labelMedium,
                color = colors.faintText,
            )
            Text(
                text = "Add to your quality alternative.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "One piece you would rather read than scroll. Keep the shelf small and intentional.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.mutedText,
            )
        }

        AnalogCard {
            HomeSectionLabel(text = "Link")
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add-link-url"),
                value = form.url,
                onValueChange = onUrlChange,
                label = { Text("https://...") },
                singleLine = true,
                isError = form.validationErrors.any { it.isUrlError() },
                supportingText = {
                    if (form.validationErrors.any { it.isUrlError() }) {
                        Text(form.validationErrors.first { it.isUrlError() }.displayMessage())
                    } else {
                        Text("Use a normal web link starting with http or https.")
                    }
                },
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add-link-title"),
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                isError = UserLinkValidationError.BLANK_TITLE in form.validationErrors,
                supportingText = {
                    if (UserLinkValidationError.BLANK_TITLE in form.validationErrors) {
                        Text(UserLinkValidationError.BLANK_TITLE.displayMessage())
                    } else {
                        Text("Give it the name you would recognize in an impulse moment.")
                    }
                },
            )
        }

        AnalogCard {
            HomeSectionLabel(text = "Estimated read")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("3", "5", "7", "12", "20").forEach { minutes ->
                    FilterChip(
                        selected = form.durationMinutes == minutes,
                        onClick = { onDurationChange(minutes) },
                        label = { Text("$minutes min") },
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add-link-duration"),
                value = form.durationMinutes,
                onValueChange = { value -> onDurationChange(value.filter(Char::isDigit).take(2)) },
                label = { Text("Custom minutes") },
                singleLine = true,
                isError = UserLinkValidationError.INVALID_DURATION in form.validationErrors,
                supportingText = {
                    if (UserLinkValidationError.INVALID_DURATION in form.validationErrors) {
                        Text(UserLinkValidationError.INVALID_DURATION.displayMessage())
                    } else {
                        Text("Use 1-60 minutes. The replacement stays finite.")
                    }
                },
            )
        }

        AnalogCard {
            HomeSectionLabel(text = "Topics")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TopicTag.entries.forEach { topic ->
                    FilterChip(
                        modifier = Modifier.testTag("add-link-topic-${topic.name}"),
                        selected = topic in form.selectedTopics,
                        onClick = { onToggleTopic(topic) },
                        label = { Text(topic.displayName()) },
                    )
                }
            }
            if (UserLinkValidationError.NO_TOPICS in form.validationErrors) {
                Text(
                    text = UserLinkValidationError.NO_TOPICS.displayMessage(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    text = "Topics help the local ranking choose this only when it fits.",
                    color = colors.mutedText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                modifier = Modifier.testTag("add-link-save"),
                onClick = onSave,
                enabled = form.canSave && !form.isSaving,
            ) {
                Text(if (form.isSaving) "Saving…" else "Add to library")
            }
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
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
                text = "Local preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Duration bucket: ${state.preferences?.preferredDurationBucket?.displayName() ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Preferred topics: ${state.preferences?.preferredTopics?.joinToString { it.displayName() }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Selected distracting apps: ${state.availableTargetApps.joinToString { it.displayName }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.completedContentIds.isNotEmpty()) {
                Text(
                    text = "Completed items excluded from future primary recommendations: ${state.completedContentIds.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (state.historyEntries.isNotEmpty()) {
                Text(
                    text = "Recent replacement sessions in the last 7 days: ${state.historyEntries.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ActiveDelayNotice(
    targetApp: DistractingApp?,
    delayWindow: DelayWindow,
) {
    Text(
        text = "${targetApp?.displayName ?: delayWindow.targetAppPackage} delayed until ${formatTimestamp(delayWindow.endsAtMillis)}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PermissionReadinessCard(
    readiness: PermissionReadiness,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Interception readiness",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = readiness.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Accessibility is the required path for the current Android alpha interception flow. Overlay permission is optional and reserved for future floating-surface experiments.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text("Overlay permission: ${readiness.overlayStatus.displayLabel()}")
            Text("Accessibility interception: ${readiness.accessibilityStatus.displayLabel()}")
            Text(
                text = if (readiness.interceptionReady) "System intervention ready" else "Setup still in progress",
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (readiness.accessibilityStatus != PermissionStatus.READY) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                    ) {
                        Text("Open Accessibility")
                    }
                }
                if (readiness.overlayStatus != PermissionStatus.READY) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}"),
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                },
                            )
                        },
                    ) {
                        Text("Optional Overlay")
                    }
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Refresh status")
                }
            }
        }
    }
}

@Composable
private fun ReplacementHistoryCard(entries: List<ReplacementHistoryEntry>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Recent replacement history",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (entries.isEmpty()) {
                Text(
                    text = "No replacement sessions yet. Finish one session to start building history.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                entries.forEach { entry ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = entry.contentTitle, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${entry.targetAppDisplayName} • ${formatTimestamp(entry.acceptedAtMillis)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = entry.statusSummary(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
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
                text = "Active starter packs",
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
    val colors = QualityAlternativeThemeTokens.colors
    val backups = recommendationSet.backups.take(MAX_BACKUP_RECOMMENDATIONS)
    val backupHeading = when (backups.size) {
        0 -> "No backup choices"
        1 -> "One backup choice"
        else -> "Two backup choices"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        InterventionHeaderCard(targetApp = targetApp)

        PrimaryRecommendationCard(
            content = recommendationSet.primary,
            actionLabel = if (recommendationSet.primary.isUserLink()) "Open link" else "Read now",
            onClick = onAcceptPrimary,
        )

        BackupRecommendationPanel(
            heading = backupHeading,
            backups = backups,
            onAcceptBackup = onAcceptBackup,
        )

        InterventionDecisionCard(
            onDelay = onDelay,
            onOpenAnyway = onOpenAnyway,
        )
    }
}

@Composable
private fun InterventionHeaderCard(targetApp: DistractingApp) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Pause before ${targetApp.displayName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "One finite replacement. Override stays available.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PrimaryRecommendationCard(
    content: ContentItem,
    actionLabel: String,
    onClick: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "One thoughtful alternative",
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedText,
            )
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = content.description,
                color = colors.mutedText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = content.metaLine(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedText,
            )
            Button(onClick = onClick) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun BackupRecommendationPanel(
    heading: String,
    backups: List<ContentItem>,
    onAcceptBackup: (ContentItem) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "No feed: just two lighter ways out.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        backups.forEachIndexed { index, backup ->
            BackupRecommendationRow(
                content = backup,
                actionLabel = if (backup.isUserLink()) "Open link" else "Choose backup",
                testTag = "intervention-backup-action-$index",
                onClick = { onAcceptBackup(backup) },
            )
        }
        if (backups.isEmpty()) {
            Text(
                text = "No extra choices are available right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedText,
            )
        }
    }
}

@Composable
private fun BackupRecommendationRow(
    content: ContentItem,
    actionLabel: String,
    testTag: String,
    onClick: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = content.metaLine(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.faintText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedButton(
            modifier = Modifier.testTag(testTag),
            onClick = onClick,
        ) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun InterventionDecisionCard(
    onDelay: () -> Unit,
    onOpenAnyway: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Your call, deliberately made",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Delay the impulse, or continue on purpose.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
private fun ExternalLinkHandoffScreen(
    state: MainUiState,
    onOpenLink: (android.content.Context) -> Unit,
    onFinishSession: () -> Unit,
    onSkipSession: () -> Unit,
) {
    val content = state.currentContent ?: return
    val context = LocalContext.current

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
            text = "Saved link • ${content.durationMinutes} min • ${content.topicTags.joinToString { it.displayName() }}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "This is one of your saved links. We'll hand you to the browser and keep the replacement session attached here for feedback.",
            style = MaterialTheme.typography.bodyLarge,
        )
        content.externalUrl?.let { url ->
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                modifier = Modifier.testTag("external-link-open"),
                onClick = { onOpenLink(context) },
            ) {
                Text("Open external link")
            }
            OutlinedButton(onClick = onSkipSession) {
                Text("Leave session")
            }
        }
        OutlinedButton(onClick = onFinishSession) {
            Text("I read it")
        }
    }
}

@Composable
private fun ReaderScreen(
    state: MainUiState,
    onFinishReading: () -> Unit,
    onSkipReading: () -> Unit,
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
            text = "${content.durationMinutes} min • ${content.topicTags.joinToString { it.displayName() }}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = state.currentContentBody,
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onFinishReading) {
                Text("Finish session")
            }
            OutlinedButton(onClick = onSkipReading) {
                Text("Leave session")
            }
        }
    }
}

@Composable
private fun FeedbackScreen(
    onSubmit: (Boolean, Boolean) -> Unit,
    onSkip: () -> Unit,
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onSubmit(wasGoodFit, helped) }) {
                Text("Submit feedback")
            }
            OutlinedButton(onClick = onSkip) {
                Text("Skip feedback")
            }
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

private fun TopicTag.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

private fun ContentItem.isUserLink(): Boolean = sourceType == ContentSourceType.USER_LINK

private fun ContentItem.metaLine(): String {
    val sourceLabel = if (isUserLink()) "Saved link" else "Editorial"
    val topicLabel = topicTags.joinToString { it.displayName() }
    return "$durationMinutes min • $sourceLabel • $topicLabel"
}

private fun AppThemeMode.displayName(): String = when (this) {
    AppThemeMode.LIGHT -> "Light"
    AppThemeMode.DARK -> "Dark"
}

private const val MAX_BACKUP_RECOMMENDATIONS = 2

private fun launchExternalLink(
    context: android.content.Context,
    viewModel: MainViewModel,
) {
    val url = viewModel.currentExternalLinkUrl()
    if (url == null) {
        viewModel.recordExternalLinkHandoffFailed(reason = "missing_url")
        return
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching {
        context.startActivity(intent)
    }.onSuccess {
        viewModel.recordExternalLinkOpened()
    }.onFailure { error ->
        val reason = if (error is ActivityNotFoundException) "no_handler" else "start_activity_failed"
        viewModel.recordExternalLinkHandoffFailed(reason = reason)
    }
}

private fun DurationBucket.displayName(): String = when (this) {
    DurationBucket.QUICK -> "3-5 minutes"
    DurationBucket.FOCUS -> "5-10 minutes"
    DurationBucket.DEEP -> "10-20 minutes"
}

private fun PermissionStatus.displayLabel(): String = when (this) {
    PermissionStatus.READY -> "Ready"
    PermissionStatus.MISSING -> "Missing"
    PermissionStatus.UNAVAILABLE_IN_BUILD -> "Not available in this build"
}

private fun ReplacementHistoryEntry.statusSummary(): String {
    val labels = mutableListOf<String>()
    if (isCompleted()) {
        labels += "Completed"
    }
    if (isSkipped()) {
        labels += "Skipped"
    }
    if (returnedToTarget()) {
        labels += "Returned to app"
    }
    if (labels.isEmpty()) {
        labels += "Accepted"
    }
    if (feedbackGoodFit != null && feedbackHelpedAvoidScrolling != null) {
        labels += if (feedbackHelpedAvoidScrolling == true) "Helped" else "Did not help"
    }
    return labels.joinToString(" • ")
}

private fun UserLinkValidationError.isUrlError(): Boolean {
    return this == UserLinkValidationError.EMPTY_URL ||
        this == UserLinkValidationError.UNSUPPORTED_SCHEME ||
        this == UserLinkValidationError.MISSING_HOST
}

private fun UserLinkValidationError.displayMessage(): String {
    return when (this) {
        UserLinkValidationError.EMPTY_URL -> "Add the link you want to save."
        UserLinkValidationError.UNSUPPORTED_SCHEME -> "Use a normal web link starting with http or https."
        UserLinkValidationError.MISSING_HOST -> "This link is missing a website address."
        UserLinkValidationError.BLANK_TITLE -> "Add a title so the recommendation is easy to recognize."
        UserLinkValidationError.INVALID_DURATION -> "Choose an estimated reading time from 1 to 60 minutes."
        UserLinkValidationError.NO_TOPICS -> "Choose at least one topic so the app can rank this link."
    }
}
