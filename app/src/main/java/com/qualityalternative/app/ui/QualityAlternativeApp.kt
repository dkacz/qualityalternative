package com.qualityalternative.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.data.UserDocumentValidator
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
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
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.usesExternalHandoff
import com.qualityalternative.app.domain.model.usesMeditationTimer
import com.qualityalternative.app.ui.theme.QualityAlternativeAppTheme
import com.qualityalternative.app.ui.theme.QualityAlternativeThemeTokens
import com.qualityalternative.app.ui.theme.QualityDisplayFontFamily
import com.qualityalternative.app.ui.theme.QualityMonoFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min
import kotlinx.coroutines.delay

@Composable
fun QualityAlternativeApp(
    viewModel: MainViewModel,
    onExitToTarget: () -> Unit = {},
) {
    val uiState = viewModel.uiState

    ApplySystemBarsForTheme(themeMode = uiState.themeMode)

    QualityAlternativeAppTheme(themeMode = uiState.themeMode) {
        DebugVisualParityDensityScale {
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.latestMessage) {
                val message = uiState.latestMessage ?: return@LaunchedEffect
                snackbarHostState.showSnackbar(message)
                viewModel.dismissMessage()
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = QualityAlternativeThemeTokens.colors.background,
            ) { paddingValues ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    color = QualityAlternativeThemeTokens.colors.background,
                ) {
                    when {
                        uiState.isLoadingSettings -> LoadingScreen()
                        !uiState.hasCompletedOnboarding -> OnboardingFlow(
                            selection = uiState.onboardingSelection,
                            supportedApps = uiState.allSupportedApps,
                            onToggleApp = viewModel::toggleOnboardingApp,
                            onToggleTopic = viewModel::toggleOnboardingTopic,
                            onSelectDuration = viewModel::setOnboardingDuration,
                            onComplete = viewModel::completeOnboarding,
                        )

                        else -> MainRoute(
                            state = uiState,
                            viewModel = viewModel,
                            onExitToTarget = onExitToTarget,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplySystemBarsForTheme(themeMode: AppThemeMode) {
    val context = LocalContext.current
    val view = LocalView.current
    val useDarkIcons = themeMode == AppThemeMode.LIGHT

    SideEffect {
        val window = context.findActivity()?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
        controller.isAppearanceLightNavigationBars = useDarkIcons
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
private fun DebugVisualParityDensityScale(content: @Composable () -> Unit) {
    if (!BuildConfig.DEBUG) {
        content()
        return
    }

    // Debug-only bridge for fixed 340x740 handoff screenshots; release builds use platform density.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val baseDensity = LocalDensity.current
        val scale = min(
            maxWidth.value / DEBUG_PARITY_VIEWPORT_WIDTH_DP,
            maxHeight.value / DEBUG_PARITY_VIEWPORT_HEIGHT_DP,
        )
            .coerceIn(MIN_DEBUG_PARITY_SCALE, MAX_DEBUG_PARITY_SCALE)
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = baseDensity.density * scale,
                fontScale = baseDensity.fontScale,
            ),
        ) {
            content()
        }
    }
}

@Composable
private fun MainRoute(
    state: MainUiState,
    viewModel: MainViewModel,
    onExitToTarget: () -> Unit,
) {
    val context = LocalContext.current
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        persistDocumentPermission(context = context, uri = uri)
        val metadata = context.documentMetadata(uri)
        viewModel.prepareUserDocumentImport(
            uri = uri.toString(),
            displayName = metadata.displayName,
            mimeType = metadata.mimeType,
        )
    }
    val onImportDocument = {
        documentPicker.launch(USER_DOCUMENT_PICKER_MIME_TYPES)
    }

    when (state.screen) {
        MainScreen.Onboarding -> OnboardingFlow(
            selection = state.onboardingSelection,
            supportedApps = state.allSupportedApps,
            onToggleApp = viewModel::toggleOnboardingApp,
            onToggleTopic = viewModel::toggleOnboardingTopic,
            onSelectDuration = viewModel::setOnboardingDuration,
            onComplete = viewModel::completeOnboarding,
        )

        MainScreen.Home -> TabScaffold(
            active = MainScreen.Home,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            HomeTab(
                state = state,
                onSelectTargetApp = viewModel::selectTargetApp,
                onTriggerIntervention = viewModel::triggerDebugIntervention,
                onOpenAddLink = viewModel::openAddLink,
                onImportDocument = onImportDocument,
                onOpenSettings = viewModel::openSettings,
                onStartDelayAlternative = viewModel::startActiveDelayAlternative,
            )
        }

        MainScreen.Library -> TabScaffold(
            active = MainScreen.Library,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            LibraryTab(
                state = state,
                onAddLink = viewModel::openAddLink,
                onImportDocument = onImportDocument,
                onOpen = viewModel::openLibraryItem,
            )
        }

        MainScreen.Progress -> TabScaffold(
            active = MainScreen.Progress,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            ProgressTab(snapshot = progressSnapshot(state.historyEntries, state.events))
        }

        MainScreen.Settings -> TabScaffold(
            active = MainScreen.Settings,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            SettingsTab(
                state = state,
                onToggleApp = viewModel::toggleSettingsApp,
                onSelectDuration = viewModel::setPreferredDuration,
                onSelectTheme = viewModel::selectThemeMode,
                onRefreshReadiness = viewModel::refreshPermissionReadiness,
            )
        }

        MainScreen.AddLink -> AddLinkScreen(
            form = state.addLinkForm,
            onUrlChange = viewModel::updateAddLinkUrl,
            onTitleChange = viewModel::updateAddLinkTitle,
            onDurationChange = viewModel::updateAddLinkDuration,
            onToggleTopic = viewModel::toggleAddLinkTopic,
            onSave = viewModel::saveUserLink,
            onCancel = viewModel::cancelAddLink,
            onImportDocument = onImportDocument,
        )

        MainScreen.AddDocument -> AddDocumentScreen(
            form = state.addDocumentForm,
            onTitleChange = viewModel::updateAddDocumentTitle,
            onDurationChange = viewModel::updateAddDocumentDuration,
            onToggleTopic = viewModel::toggleAddDocumentTopic,
            onSave = viewModel::saveUserDocument,
            onCancel = viewModel::cancelAddLink,
            onPickAnother = onImportDocument,
        )

        MainScreen.AddLinkSuccess -> AddLinkSuccess(
            confirmation = state.savedLinkConfirmation,
            onDone = viewModel::finishAddLinkSuccess,
        )

        MainScreen.Intervention -> InterventionScreen(
            state = state,
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
            state = state,
            onDone = viewModel::finishReading,
            onBack = viewModel::skipReading,
        )

        MainScreen.ExternalHandoff -> ExternalLinkHandoffScreen(
            state = state,
            onOpenLink = { launchExternalLink(context = it, viewModel = viewModel) },
            onDone = viewModel::finishReading,
            onBack = viewModel::skipReading,
        )

        MainScreen.MeditationTimer -> MeditationTimerScreen(
            state = state,
            onComplete = viewModel::finishMeditationReset,
            onBack = viewModel::skipMeditationReset,
        )

        MainScreen.Feedback -> FeedbackScreen(
            state = state,
            onSubmit = viewModel::submitFeedback,
            onSkip = viewModel::skipFeedback,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = QualityAlternativeThemeTokens.colors.accent)
        Text(
            text = "Loading local replacement state...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = QualityAlternativeThemeTokens.colors.mutedText,
        )
    }
}

@Composable
private fun TabScaffold(
    active: MainScreen,
    onHome: () -> Unit,
    onLibrary: () -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content,
        )
        TabBar(
            active = active,
            onHome = onHome,
            onLibrary = onLibrary,
            onProgress = onProgress,
            onSettings = onSettings,
        )
    }
}

@Composable
private fun TabBar(
    active: MainScreen,
    onHome: () -> Unit,
    onLibrary: () -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.elevatedSurface),
    ) {
        HorizontalDivider(color = colors.line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabItem("Home", TabGlyph.Home, active == MainScreen.Home, onHome, Modifier.weight(1f), "tab-home")
            TabItem("Library", TabGlyph.Library, active == MainScreen.Library, onLibrary, Modifier.weight(1f), "tab-library")
            TabItem("Progress", TabGlyph.Progress, active == MainScreen.Progress, onProgress, Modifier.weight(1f), "tab-progress")
            TabItem("Settings", TabGlyph.Settings, active == MainScreen.Settings, onSettings, Modifier.weight(1f), "tab-settings")
        }
    }
}

private enum class TabGlyph { Home, Library, Progress, Settings }

private enum class QaIconKind {
    Plus,
    Check,
    ArrowRight,
    ArrowLeft,
    Close,
    Clock,
    Book,
    Link,
    Home,
    Library,
    History,
    Settings,
    Sparkle,
    Shield,
    Eye,
    Pause,
    External,
    Bell,
    ChevronRight,
    Dot,
}

@Composable
private fun TabItem(
    label: String,
    icon: TabGlyph,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val color = if (active) colors.primaryText else colors.faintText
    Surface(
        modifier = modifier.testTag(testTag),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        contentColor = color,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            TabGlyphIcon(icon = icon, color = color)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontSize = 10.5.sp,
            )
        }
    }
}

@Composable
private fun TabGlyphIcon(icon: TabGlyph, color: Color) {
    val kind = when (icon) {
        TabGlyph.Home -> QaIconKind.Home
        TabGlyph.Library -> QaIconKind.Library
        TabGlyph.Progress -> QaIconKind.History
        TabGlyph.Settings -> QaIconKind.Settings
    }
    QaIcon(kind = kind, color = color, size = 22.dp)
}

@Composable
private fun OnboardingFlow(
    selection: OnboardingSelection,
    supportedApps: List<DistractingApp>,
    onToggleApp: (DistractingApp) -> Unit,
    onToggleTopic: (TopicTag) -> Unit,
    onSelectDuration: (DurationBucket) -> Unit,
    onComplete: () -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    when (step) {
        0 -> OnboardingWelcome(onNext = { step = 1 })
        1 -> OnboardingApps(
            selection = selection,
            supportedApps = supportedApps,
            onToggleApp = onToggleApp,
            onBack = { step = 0 },
            onNext = { step = 2 },
        )
        2 -> OnboardingTopics(
            selection = selection,
            onToggleTopic = onToggleTopic,
            onBack = { step = 1 },
            onNext = { step = 3 },
        )
        3 -> OnboardingDuration(
            selection = selection,
            onSelectDuration = onSelectDuration,
            onBack = { step = 2 },
            onNext = { step = 4 },
        )
        else -> OnboardingPermissions(
            onBack = { step = 3 },
            onNext = onComplete,
        )
    }
}

@Composable
private fun OnboardingWelcome(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 26.dp, top = 40.dp, end = 26.dp, bottom = 26.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
        ) {
            MonoText("Quality Alternative", modifier = Modifier.padding(bottom = 18.dp))
            DisplayText(
                text = "Turn an impulse\ninto a better\nchoice.",
                fontSize = 40.sp,
                lineHeight = 42.sp,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            BodyText(
                text = "When you reach for a distracting app, we offer something you actually wanted to read. No blocking. No guilt. Just a brief detour, if you'd like one.",
                fontSize = 16.sp,
                lineHeight = 25.sp,
                color = QualityAlternativeThemeTokens.colors.mutedText,
                modifier = Modifier.widthIn(max = 320.dp),
            )
        }
        FlowRow(
            modifier = Modifier.padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            WelcomeValue("Pause", "A single gentle prompt.")
            WelcomeValue("Choose", "Always your call.")
            WelcomeValue("Read", "One thing. Finish it.")
            WelcomeValue("Return", "Back to your day.")
        }
        QaButton(text = "Begin", onClick = onNext, variant = QaButtonVariant.Primary, trailingIcon = QaIconKind.ArrowRight)
        QaButton(text = "I have an account", onClick = {}, variant = QaButtonVariant.Ghost)
    }
}

@Composable
private fun WelcomeValue(label: String, value: String) {
    Column(modifier = Modifier.width(142.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MonoText(label)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            color = QualityAlternativeThemeTokens.colors.primaryText,
        )
    }
}

@Composable
private fun OnboardingApps(
    selection: OnboardingSelection,
    supportedApps: List<DistractingApp>,
    onToggleApp: (DistractingApp) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepScreen(
        step = 1,
        onBack = onBack,
        bottom = {
            QaButton(
                text = "Continue",
                onClick = onNext,
                enabled = selection.selectedAppPackages.size >= 3,
                variant = QaButtonVariant.Primary,
                trailingIcon = QaIconKind.ArrowRight,
            )
        },
    ) {
        DisplayText("Which apps pull at you?", fontSize = 28.sp, lineHeight = 31.sp)
        BodyText(
            text = "We'll gently intercept these. Pick at least three that feel honest.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            supportedApps.forEach { app ->
                AppSelectionRow(
                    app = app,
                    selected = app.packageName in selection.selectedAppPackages,
                    onClick = { onToggleApp(app) },
                )
            }
        }
    }
}

@Composable
private fun OnboardingTopics(
    selection: OnboardingSelection,
    onToggleTopic: (TopicTag) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepScreen(
        step = 2,
        onBack = onBack,
        bottom = {
            QaButton(
                text = "Continue",
                onClick = onNext,
                enabled = selection.preferredTopics.size >= 3,
                variant = QaButtonVariant.Primary,
                trailingIcon = QaIconKind.ArrowRight,
            )
        },
    ) {
        DisplayText("What would you rather read?", fontSize = 28.sp, lineHeight = 31.sp)
        BodyText(
            text = "We'll draw editorial picks from these. You can change this anytime.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prototypeTopics().forEach { topic ->
                QaChip(
                    text = topic.displayName(),
                    selected = topic in selection.preferredTopics,
                    onClick = { onToggleTopic(topic) },
                )
            }
        }
    }
}

@Composable
private fun OnboardingDuration(
    selection: OnboardingSelection,
    onSelectDuration: (DurationBucket) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepScreen(step = 3, onBack = onBack, bottom = {
        QaButton(text = "Continue", onClick = onNext, variant = QaButtonVariant.Primary, trailingIcon = QaIconKind.ArrowRight)
    }) {
        DisplayText("How long should a session feel?", fontSize = 28.sp, lineHeight = 31.sp)
        BodyText(
            text = "A default length for each intervention. You can always pick something longer in the moment.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DurationOption(DurationBucket.QUICK, "5 minutes", "A quick detour", selection, onSelectDuration)
            DurationOption(DurationBucket.FOCUS, "10 minutes", "One short piece", selection, onSelectDuration)
            DurationOption(DurationBucket.DEEP, "20 minutes", "A proper read", selection, onSelectDuration)
        }
    }
}

@Composable
private fun OnboardingPermissions(
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepScreen(
        step = 4,
        onBack = onBack,
        bottom = {
            QaButton(text = "Grant & finish", onClick = onNext, variant = QaButtonVariant.Primary, trailingIcon = QaIconKind.ArrowRight)
            QaButton(text = "Skip - I'll set this up later", onClick = onNext, variant = QaButtonVariant.Ghost)
        },
    ) {
        DisplayText("Two small permissions.", fontSize = 28.sp, lineHeight = 31.sp)
        BodyText(
            text = "Needed so we can notice when you open a distracting app. We do not collect screen text, messages, or browsing history.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
        )
        QaCard(modifier = Modifier.padding(bottom = 10.dp)) {
            PermissionIntroRow("Usage access", "To detect when a chosen app comes to the foreground.")
        }
        QaCard(modifier = Modifier.padding(bottom = 18.dp)) {
            PermissionIntroRow("Display over other apps", "To show a single prompt on top, then step out of the way.")
        }
        MonoText("Nothing leaves your phone.\nYou can revoke either one anytime.", lineHeight = 18.sp)
    }
}

@Composable
private fun StepScreen(
    step: Int,
    onBack: () -> Unit,
    bottom: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHead(onBack = onBack, step = step, total = 4)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            content = content,
        )
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = bottom,
        )
    }
}

@Composable
private fun ScreenHead(
    onBack: (() -> Unit)? = null,
    step: Int? = null,
    total: Int? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            QaIconButton(icon = QaIconKind.ArrowLeft, onClick = onBack)
        } else {
            Spacer(modifier = Modifier.width(34.dp))
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (step != null && total != null) {
                Dots(total = total, active = step - 1)
            }
        }
        Spacer(modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun HomeTab(
    state: MainUiState,
    onSelectTargetApp: (DistractingApp) -> Unit,
    onTriggerIntervention: () -> Unit,
    onOpenAddLink: () -> Unit,
    onImportDocument: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartDelayAlternative: () -> Unit,
) {
    val selectedPacks = state.starterPacks.filter { it.id in state.preferences?.selectedPackIds.orEmpty() }
    val editorialItems = selectedPacks.flatMap { it.items }
    val totalItems = editorialItems.size + state.userLinks.size + state.userDocuments.size
    val totalMins = editorialItems.sumOf(ContentItem::durationMinutes) +
        state.userLinks.sumOf(ContentItem::durationMinutes) +
        state.userDocuments.sumOf(ContentItem::durationMinutes)
    val permOk = state.permissionReadiness.interceptionReady
    val hero = homeHeroCopy(state.permissionReadiness)
    val todayLabel = remember {
        DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
            .format(Instant.now().atZone(ZoneId.systemDefault()))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)) {
                MonoText("$todayLabel · Good morning", modifier = Modifier.padding(bottom = 8.dp))
                DisplayText(hero.title, fontSize = 30.sp, lineHeight = 34.sp)
            }
        }
        if (!permOk) {
            item {
                PermissionWarningCard(onOpenSettings = onOpenSettings)
            }
        }
        state.activeDelayWindow?.let { delayWindow ->
            item {
                ActiveDelayCard(
                    targetApp = state.selectedTargetApp,
                    delayWindow = delayWindow,
                    suggestion = state.activeDelaySuggestion,
                    onReadAlternative = onStartDelayAlternative,
                )
            }
        }
        item {
            SectionLabel("Setup")
            QaCard {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCell("Intercepting", "${state.availableTargetApps.size} apps", Modifier.weight(1f))
                    StatCell("Session length", state.preferences?.preferredDurationBucket?.prototypeMinutesLabel() ?: "10 min", Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCell("Topics", "${state.preferences?.preferredTopics?.size ?: 0}", Modifier.weight(1f))
                    StatCell(
                        label = "Status",
                        value = if (permOk) "Active" else "Paused",
                        modifier = Modifier.weight(1f),
                        color = if (permOk) QualityAlternativeThemeTokens.colors.success else QualityAlternativeThemeTokens.colors.accent,
                    )
                }
                HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line, modifier = Modifier.padding(vertical = 16.dp))
                AppPills(apps = state.availableTargetApps, selectedApp = state.selectedTargetApp, onSelect = onSelectTargetApp)
            }
        }
        item {
            SectionLabel("Your library", right = "$totalItems items · $totalMins min")
            QaCard(padding = 0.dp) {
                LibrarySummaryRow("Editorial picks", "${editorialItems.size} curated", ContentSourceType.EDITORIAL)
                HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line)
                LibrarySummaryRow("Your added links", "${state.userLinks.size} saved", ContentSourceType.USER_LINK)
                HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line)
                LibrarySummaryRow("Your files", "${state.userDocuments.size} saved", ContentSourceType.USER_DOCUMENT)
            }
        }
        item {
            QaButton(
                text = "Add a link",
                onClick = onOpenAddLink,
                variant = QaButtonVariant.Outline,
                leadingIcon = QaIconKind.Plus,
                modifier = Modifier.testTag("home-add-link"),
            )
            QaButton(
                text = "Import PDF / MD / EPUB",
                onClick = onImportDocument,
                variant = QaButtonVariant.Ghost,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier.testTag("home-import-document"),
            )
        }
        item {
            SectionLabel("Try the intervention")
            QaCard {
                BodyText(
                    text = "Normally this appears automatically. Tap to preview what happens when you open a chosen app.",
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                QaButton(text = "Preview intervention", onClick = onTriggerIntervention, variant = QaButtonVariant.Accent)
            }
        }
    }
}

@Composable
private fun LibraryTab(
    state: MainUiState,
    onAddLink: () -> Unit,
    onImportDocument: () -> Unit,
    onOpen: (ContentItem) -> Unit,
) {
    var filter by remember { mutableStateOf("all") }
    val editorial = state.starterPacks
        .filter { it.id in state.preferences?.selectedPackIds.orEmpty() }
        .flatMap { it.items }
    val list = when (filter) {
        "editorial" -> editorial
        "yours" -> state.userLinks
        "files" -> state.userDocuments
        else -> editorial + state.userLinks + state.userDocuments
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DisplayText("Library", modifier = Modifier.weight(1f), fontSize = 26.sp)
                QaButton(
                    text = "Add",
                    onClick = onAddLink,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    fullWidth = false,
                    leadingIcon = QaIconKind.Plus,
                )
            }
        }
        item {
            QaButton(
                text = "Import PDF / MD / EPUB",
                onClick = onImportDocument,
                variant = QaButtonVariant.Ghost,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier.testTag("library-import-document"),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                QaChip("All", selected = filter == "all", onClick = { filter = "all" })
                QaChip("Editorial", selected = filter == "editorial", onClick = { filter = "editorial" })
                QaChip("Your links", selected = filter == "yours", onClick = { filter = "yours" })
                QaChip("Files", selected = filter == "files", onClick = { filter = "files" })
            }
        }
        if (list.isEmpty()) {
            item {
                QaCard {
                    BodyText(
                        text = "Nothing here yet. Add one piece you'd actually read instead of scrolling.",
                        color = QualityAlternativeThemeTokens.colors.mutedText,
                    )
                }
            }
        } else {
            items(list, key = ContentItem::id) { item ->
                LibraryItemCard(item = item, onOpen = { onOpen(item) })
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
    onImportDocument: () -> Unit,
) {
    val host = form.url.hostLabel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add-link-screen"),
    ) {
        ScreenHead(onBack = onCancel)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
        ) {
            DisplayText("Add to your quality\nalternative.", fontSize = 30.sp, lineHeight = 33.sp)
            BodyText(
                text = "One piece you'd rather read than scroll. Not a bookmark graveyard — only things you'd actually open.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            QaButton(
                text = "Import PDF / MD / EPUB instead",
                onClick = onImportDocument,
                variant = QaButtonVariant.Outline,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .testTag("add-link-import-document"),
            )
            InputLabel("Link")
            QaTextField(
                value = form.url,
                onValueChange = onUrlChange,
                placeholder = "https://…",
                modifier = Modifier.testTag("add-link-url"),
                isError = form.validationErrors.any { it.isUrlError() },
            )
            AddLinkValidationLine(form = form, host = host)
            InputLabel("Title", Modifier.padding(top = 14.dp))
            QaTextField(
                value = form.title,
                onValueChange = onTitleChange,
                placeholder = "How you'd recognize it",
                modifier = Modifier.testTag("add-link-title"),
                isError = UserLinkValidationError.BLANK_TITLE in form.validationErrors,
            )
            InputLabel("Estimated read", Modifier.padding(top = 14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("3", "5", "8", "12", "20").forEach { mins ->
                    QaChip(
                        "$mins min",
                        selected = form.durationMinutes == mins,
                        onClick = { onDurationChange(mins) },
                        modifier = Modifier.weight(1f),
                        centered = true,
                        minHeight = 32.dp,
                        horizontalPadding = 0.dp,
                        verticalPadding = 6.dp,
                        fontSize = 11.sp,
                    )
                }
            }
            InputLabel("Topic", Modifier.padding(top = 14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prototypeTopics().forEach { topic ->
                    QaChip(
                        text = topic.displayName(),
                        selected = topic in form.selectedTopics,
                        accentSelected = true,
                        onClick = { onToggleTopic(topic) },
                        modifier = Modifier.testTag("add-link-topic-${topic.name}"),
                        minHeight = 32.dp,
                        horizontalPadding = 12.dp,
                        verticalPadding = 7.dp,
                    )
                }
            }
            if (UserLinkValidationError.NO_TOPICS in form.validationErrors) {
                BodyText(
                    text = UserLinkValidationError.NO_TOPICS.displayMessage(),
                    color = QualityAlternativeThemeTokens.colors.accent,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, QualityAlternativeThemeTokens.colors.line))
                .padding(horizontal = 28.dp, vertical = 10.dp),
        ) {
            QaButton(
                text = if (form.isSaving) "Saving..." else "Add to library",
                onClick = onSave,
                enabled = form.canSave && !form.isSaving,
                variant = QaButtonVariant.Primary,
                modifier = Modifier.testTag("add-link-save"),
            )
        }
    }
}

@Composable
private fun AddDocumentScreen(
    form: AddDocumentFormState,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onToggleTopic: (TopicTag) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onPickAnother: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add-document-screen"),
    ) {
        ScreenHead(onBack = onCancel)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
        ) {
            DisplayText("Add a private\nreading file.", fontSize = 30.sp, lineHeight = 33.sp)
            BodyText(
                text = "PDF and EPUB open through Android's document viewer. Markdown opens in the calm in-app reader. The file stays on this device.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            QaCard(modifier = Modifier.padding(bottom = 18.dp), padding = 16.dp) {
                MonoText(documentFormatLabel(form), modifier = Modifier.padding(bottom = 6.dp))
                Text(
                    text = form.displayName.ifBlank { "No file selected" },
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
                BodyText(
                    text = form.uri,
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            QaButton(
                text = "Choose a different file",
                onClick = onPickAnother,
                variant = QaButtonVariant.Outline,
                size = QaButtonSize.Small,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .testTag("add-document-pick-another"),
            )
            AddDocumentValidationLine(form)
            InputLabel("Title", Modifier.padding(top = 14.dp))
            QaTextField(
                value = form.title,
                onValueChange = onTitleChange,
                placeholder = "How you'd recognize it",
                modifier = Modifier.testTag("add-document-title"),
                isError = UserDocumentValidationError.BLANK_TITLE in form.validationErrors,
            )
            InputLabel("Estimated session", Modifier.padding(top = 14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("5", "10", "15", "20", "30").forEach { mins ->
                    QaChip(
                        "$mins min",
                        selected = form.durationMinutes == mins,
                        onClick = { onDurationChange(mins) },
                        modifier = Modifier.weight(1f),
                        centered = true,
                        minHeight = 32.dp,
                        horizontalPadding = 0.dp,
                        verticalPadding = 6.dp,
                        fontSize = 11.sp,
                    )
                }
            }
            InputLabel("Topic", Modifier.padding(top = 14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prototypeTopics().forEach { topic ->
                    QaChip(
                        text = topic.displayName(),
                        selected = topic in form.selectedTopics,
                        accentSelected = true,
                        onClick = { onToggleTopic(topic) },
                        modifier = Modifier.testTag("add-document-topic-${topic.name}"),
                        minHeight = 32.dp,
                        horizontalPadding = 12.dp,
                        verticalPadding = 7.dp,
                    )
                }
            }
            if (UserDocumentValidationError.NO_TOPICS in form.validationErrors) {
                BodyText(
                    text = UserDocumentValidationError.NO_TOPICS.displayMessage(),
                    color = QualityAlternativeThemeTokens.colors.accent,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, QualityAlternativeThemeTokens.colors.line))
                .padding(horizontal = 28.dp, vertical = 10.dp),
        ) {
            QaButton(
                text = if (form.isSaving) "Saving..." else "Add file to library",
                onClick = onSave,
                enabled = form.canSave && !form.isSaving,
                variant = QaButtonVariant.Primary,
                modifier = Modifier.testTag("add-document-save"),
            )
        }
    }
}

@Composable
private fun AddLinkSuccess(
    confirmation: AddLinkConfirmation?,
    onDone: () -> Unit,
) {
    val saved = confirmation ?: AddLinkConfirmation(
        title = "Saved link",
        host = "your link",
        durationMinutes = 8,
        topicLabel = "Reading",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add-link-success-screen")
            .padding(horizontal = 28.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(QualityAlternativeThemeTokens.colors.successSoft),
            contentAlignment = Alignment.Center,
        ) {
            QaIcon(kind = QaIconKind.Check, color = QualityAlternativeThemeTokens.colors.success, size = 28.dp)
        }
        DisplayText(
            text = "Ready when you are",
            fontSize = 28.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
            textAlign = TextAlign.Center,
        )
        BodyText(
            text = "We'll offer this next time you reach for one of your chosen apps.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        QaCard(modifier = Modifier.padding(top = 32.dp, bottom = 24.dp), padding = 16.dp) {
            MonoText("${saved.host} · ${saved.durationMinutes} min · ${saved.topicLabel}")
            Text(
                text = saved.title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        QaButton(
            text = "Done",
            onClick = onDone,
            variant = QaButtonVariant.Primary,
            modifier = Modifier.testTag("add-link-done"),
        )
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
    val primary = recommendationSet.primary
    val backups = recommendationSet.backups.take(MAX_BACKUP_RECOMMENDATIONS)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.accentSoft,
            colors.background,
            colors.background,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("intervention-screen")
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 26.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppDot(app = targetApp, size = 26.dp)
            BodyText(
                text = "You reached for ${targetApp.displayName}",
                color = colors.mutedText,
                modifier = Modifier.weight(1f),
                fontSize = 13.5.sp,
            )
            QaIconButton(icon = QaIconKind.Close, onClick = onOpenAnyway)
        }
        MonoText("A brief detour, if you'd like one", modifier = Modifier.padding(bottom = 14.dp))
        QaCard(
            borderColor = colors.lineStrong,
            padding = 25.dp,
            modifier = Modifier.padding(bottom = 18.dp),
        ) {
            ContentMetaRow(primary, stacked = true)
            DisplayText(
                text = primary.title,
                fontSize = 32.sp,
                lineHeight = 35.sp,
                modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
            )
            Text(
                text = "\"${primary.description}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = QualityDisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                ),
                color = colors.mutedText,
            )
        }
        QaButton(
            text = primaryActionLabel(primary),
            onClick = onAcceptPrimary,
            variant = QaButtonVariant.Accent,
            modifier = Modifier.padding(bottom = 16.dp),
            leadingIcon = primaryActionIcon(primary),
        )
        MonoText("Or", modifier = Modifier.padding(bottom = 6.dp))
        Column(modifier = Modifier.padding(bottom = 18.dp)) {
            backups.forEachIndexed { index, backup ->
                BackupRow(
                    item = backup,
                    onClick = { onAcceptBackup(backup) },
                    modifier = Modifier.testTag("intervention-backup-action-$index"),
                )
            }
            if (backups.isEmpty()) {
                BodyText("No extra choices are available right now.", color = colors.mutedText)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QaButton(
                text = "Pause 15 min",
                onClick = onDelay,
                variant = QaButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                fullWidth = false,
                size = QaButtonSize.Compact,
                leadingIcon = QaIconKind.Pause,
            )
            QaButton(
                text = "Open ${targetApp.displayName}",
                onClick = onOpenAnyway,
                variant = QaButtonVariant.Ghost,
                modifier = Modifier.weight(1f),
                fullWidth = false,
                size = QaButtonSize.Compact,
            )
        }
    }
}

@Composable
private fun ReaderScreen(
    state: MainUiState,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val content = state.currentContent ?: return
    val paragraphs = readerParagraphsForDisplay(
        body = state.currentContentBody,
        fallback = content.description,
    )
    var progress by remember(content.id, state.currentSessionStartedAtMillis) { mutableStateOf(0) }

    LaunchedEffect(content.id, state.currentSessionStartedAtMillis, content.durationMinutes) {
        val startedAtMillis = state.currentSessionStartedAtMillis ?: System.currentTimeMillis()
        val totalMillis = (content.durationMinutes * 60_000L).coerceAtLeast(1L)
        while (true) {
            val elapsedMillis = (System.currentTimeMillis() - startedAtMillis).coerceAtLeast(0L)
            progress = ((elapsedMillis * 100L) / totalMillis).toInt().coerceIn(0, 100)
            delay(1_000L)
        }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("reader-screen")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QaIconButton(icon = QaIconKind.Close, onClick = onBack)
            ProgressLine(progress = progress, modifier = Modifier.weight(1f))
            MonoText("${remainingMinutes(content.durationMinutes, progress)} min left")
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("reader-list"),
            contentPadding = PaddingValues(start = 28.dp, top = 18.dp, end = 28.dp, bottom = 24.dp),
        ) {
            item {
                MonoText("${content.sourceLabel()} · ${content.topicLine()}", modifier = Modifier.padding(bottom = 10.dp))
                DisplayText(content.title, fontSize = 27.sp, lineHeight = 31.sp, modifier = Modifier.padding(bottom = 18.dp))
            }
            items(paragraphs.withIndex().toList()) { indexedParagraph ->
                Text(
                    text = indexedParagraph.value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = QualityDisplayFontFamily,
                        fontSize = 17.sp,
                        lineHeight = 27.sp,
                    ),
                    color = QualityAlternativeThemeTokens.colors.primaryText,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            item {
                QaButton(
                    text = "I'm done reading",
                    onClick = onDone,
                    variant = QaButtonVariant.Primary,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun ExternalLinkHandoffScreen(
    state: MainUiState,
    onOpenLink: (android.content.Context) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val content = state.currentContent ?: return
    val context = LocalContext.current
    var hasOpenedLink by remember(content.id) { mutableStateOf(false) }
    val isFile = content.sourceType == ContentSourceType.USER_DOCUMENT

    Column(modifier = Modifier.fillMaxSize().testTag("external-handoff-screen")) {
        ScreenHead(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            MonoText(if (isFile) "Private file" else "External reading", modifier = Modifier.padding(bottom = 14.dp))
            DisplayText(content.title, fontSize = 26.sp, lineHeight = 30.sp, modifier = Modifier.padding(bottom = 12.dp))
            QaCard(modifier = Modifier.padding(bottom = 20.dp), padding = 16.dp) {
                BodyText(
                    if (isFile) "Opens with Android's document viewer" else "Opens in your browser",
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                BodyText(
                text = "We'll start a ${content.durationMinutes}-minute timer. When you come back, we'll ask one quick question.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                )
            }
            MonoText(if (isFile) "File" else "Link", modifier = Modifier.padding(bottom = 6.dp))
            BodyText(
                text = content.externalUrl ?: content.description,
                color = QualityAlternativeThemeTokens.colors.primaryText,
                modifier = Modifier.padding(bottom = 28.dp),
            )
            QaButton(
                text = if (isFile) {
                    "Open file & start ${content.durationMinutes}-min session"
                } else {
                    "Open & start ${content.durationMinutes}-min session"
                },
                onClick = {
                    hasOpenedLink = true
                    onOpenLink(context)
                },
                variant = QaButtonVariant.Accent,
                leadingIcon = QaIconKind.External,
                modifier = Modifier.testTag("external-link-open"),
            )
            QaButton(
                text = if (hasOpenedLink) "I'm back from reading" else if (isFile) "I've finished this file" else "I've finished this link",
                onClick = onDone,
                variant = QaButtonVariant.Outline,
                modifier = Modifier.testTag("external-link-done"),
            )
            QaButton(text = "Not now", onClick = onBack, variant = QaButtonVariant.Ghost)
        }
    }
}

@Composable
private fun MeditationTimerScreen(
    state: MainUiState,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val content = state.currentContent ?: return
    val startedAtMillis = state.currentSessionStartedAtMillis ?: System.currentTimeMillis()
    val totalMillis = (content.durationMinutes * 60_000L).coerceAtLeast(1L)
    var nowMillis by remember(content.id, startedAtMillis) { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(content.id, startedAtMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val remainingMillis = (totalMillis - (nowMillis - startedAtMillis)).coerceAtLeast(0L)
    val remainingSeconds = ((remainingMillis + 999L) / 1_000L).toInt()
    val isComplete = remainingMillis == 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("meditation-timer-screen")
            .padding(horizontal = 28.dp, vertical = 42.dp),
    ) {
        ScreenHead(onBack = onBack)
        Spacer(modifier = Modifier.height(28.dp))
        MonoText("Quiet reset", modifier = Modifier.padding(bottom = 14.dp))
        DisplayText(
            text = content.title,
            fontSize = 34.sp,
            lineHeight = 37.sp,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        BodyText(
            text = "Put the phone down if you can. Breathe out slowly. Let the urge pass before deciding what to do next.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
            fontSize = 16.sp,
            lineHeight = 25.sp,
            modifier = Modifier.padding(bottom = 34.dp),
        )
        QaCard(
            padding = 30.dp,
            background = QualityAlternativeThemeTokens.colors.accentSoft,
            borderColor = QualityAlternativeThemeTokens.colors.lineStrong,
            modifier = Modifier.testTag("meditation-timer-card"),
        ) {
            MonoText("Timer", modifier = Modifier.padding(bottom = 14.dp), color = QualityAlternativeThemeTokens.colors.accent)
            Text(
                text = meditationTimeLabel(remainingSeconds),
                style = TextStyle(
                    fontFamily = QualityDisplayFontFamily,
                    fontSize = 76.sp,
                    lineHeight = 78.sp,
                    color = QualityAlternativeThemeTokens.colors.primaryText,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meditation-countdown"),
                textAlign = TextAlign.Center,
            )
            BodyText(
                text = if (isComplete) "Reset complete. Log it if it helped." else "No feed. Just three minutes back.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        QaButton(
            text = if (isComplete) "Complete reset" else "Timer running",
            onClick = onComplete,
            enabled = isComplete,
            variant = QaButtonVariant.Primary,
            modifier = Modifier.testTag("meditation-complete"),
        )
        QaButton(
            text = "End early",
            onClick = onBack,
            variant = QaButtonVariant.Ghost,
            modifier = Modifier.testTag("meditation-skip"),
        )
    }
}

@Composable
private fun FeedbackScreen(
    state: MainUiState,
    onSubmit: (String, String) -> Unit,
    onSkip: () -> Unit,
) {
    val content = state.currentContent
    var fit by remember { mutableStateOf<String?>(null) }
    var helped by remember { mutableStateOf<String?>(null) }
    val done = fit != null && helped != null
    val minutes = content?.durationMinutes ?: state.preferences?.preferredDurationBucket?.prototypeMinutes() ?: 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feedback-screen")
            .padding(horizontal = 28.dp, vertical = 50.dp),
    ) {
        MonoText("$minutes minutes · well spent", modifier = Modifier.padding(bottom = 14.dp))
        DisplayText("Two quick questions.", fontSize = 28.sp, lineHeight = 31.sp, modifier = Modifier.padding(bottom = 26.dp))
        FeedbackQuestion(
            title = "Was this a good fit?",
            selected = fit,
            options = listOf("not" to "Not really", "ok" to "It was okay", "great" to "Great fit"),
            onSelect = { fit = it },
            modifier = Modifier.padding(bottom = 24.dp),
        )
        FeedbackQuestion(
            title = "Did it help you skip the scroll?",
            selected = helped,
            options = listOf("no" to "No", "maybe" to "Maybe", "yes" to "Yes"),
            onSelect = { helped = it },
            modifier = Modifier.padding(bottom = 30.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        QaButton(
            text = "Log session",
            onClick = { onSubmit(requireNotNull(fit), requireNotNull(helped)) },
            enabled = done,
            variant = QaButtonVariant.Primary,
            modifier = Modifier.testTag("feedback-log"),
        )
        QaButton(text = "Skip", onClick = onSkip, variant = QaButtonVariant.Ghost)
    }
}

@Composable
private fun ProgressTab(snapshot: ProgressSnapshot) {
    val colors = QualityAlternativeThemeTokens.colors
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("progress-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            DisplayText("Progress", fontSize = 26.sp, modifier = Modifier.padding(bottom = 4.dp))
            QaCard(
                modifier = Modifier.testTag("progress-card"),
                padding = 24.dp,
            ) {
                MonoText("Last 21 days", modifier = Modifier.padding(bottom = 12.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = snapshot.daysConverted.toString(),
                        style = TextStyle(
                            fontFamily = QualityDisplayFontFamily,
                            fontSize = 62.sp,
                            lineHeight = 62.sp,
                            color = colors.primaryText,
                        ),
                    )
                    BodyText("days converted", color = colors.mutedText, modifier = Modifier.padding(bottom = 10.dp))
                }
                BodyText(
                    text = "Days you replaced an impulse with a real read. Missed days aren't tracked - come back when you can.",
                    color = colors.mutedText,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
                )
                CalendarStrip(days = snapshot.dayBars)
            }
        }
        item {
            QaCard(padding = 0.dp) {
                SmallStatRow("Interventions shown", snapshot.interventionsShown.toString())
                HorizontalDivider(color = colors.line)
                SmallStatRow("Chose the alternative", "${snapshot.alternativesChosen}")
                HorizontalDivider(color = colors.line)
                SmallStatRow("Paused social", snapshot.delayedOpens.toString())
                HorizontalDivider(color = colors.line)
                SmallStatRow("Opened anyway", snapshot.consciousOverrides.toString(), subtle = true)
            }
        }
        item {
            SectionLabel("Recent replacements")
            QaCard(padding = 0.dp) {
                if (snapshot.recentReplacements.isEmpty()) {
                    BodyText(
                        text = "No converted impulses yet. Once you choose a replacement, it appears here as history.",
                        color = colors.mutedText,
                        modifier = Modifier.padding(18.dp),
                    )
                } else {
                    snapshot.recentReplacements.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(color = colors.line)
                        RecentReplacementRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: MainUiState,
    onToggleApp: (DistractingApp) -> Unit,
    onSelectDuration: (DurationBucket) -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
    onRefreshReadiness: () -> Unit,
) {
    val context = LocalContext.current
    val colors = QualityAlternativeThemeTokens.colors
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { DisplayText("Settings", fontSize = 26.sp) }
        item {
            SectionLabel("Permission readiness")
            QaCard(padding = 0.dp) {
                PermissionRow("Usage access", "Detect a chosen app opening", state.permissionReadiness.accessibilityStatus == PermissionStatus.READY) {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                HorizontalDivider(color = colors.line)
                PermissionRow("Display over apps", "Show the prompt on top", state.permissionReadiness.overlayStatus == PermissionStatus.READY) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
                HorizontalDivider(color = colors.line)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRefreshReadiness)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.permissionReadiness.interceptionReady) colors.success else colors.accent),
                    )
                    BodyText(
                        text = if (state.permissionReadiness.interceptionReady) "Interception is fully active" else "Interception is degraded",
                        color = if (state.permissionReadiness.interceptionReady) colors.success else colors.accent,
                    )
                }
            }
        }
        item {
            SectionLabel("Intercepting")
            QaCard {
                AppPills(
                    apps = state.allSupportedApps,
                    selectedApp = state.selectedTargetApp,
                    selectedPackages = state.availableTargetApps.mapTo(mutableSetOf(), DistractingApp::packageName),
                    onSelect = onToggleApp,
                    dimUnselected = true,
                )
            }
        }
        item {
            SectionLabel("Default session length")
            QaCard {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DurationBucket.entries.forEach { bucket ->
                        QaChip(
                            text = "${bucket.prototypeMinutes()} min",
                            selected = bucket == state.preferences?.preferredDurationBucket,
                            onClick = { onSelectDuration(bucket) },
                            modifier = Modifier.weight(1f),
                            centered = true,
                        )
                    }
                }
            }
        }
        item {
            SectionLabel("Theme")
            QaCard {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QaChip(
                        text = "Light",
                        selected = state.themeMode == AppThemeMode.LIGHT,
                        onClick = { onSelectTheme(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f).testTag("theme-LIGHT"),
                        centered = true,
                    )
                    QaChip(
                        text = "Dark",
                        selected = state.themeMode == AppThemeMode.DARK,
                        onClick = { onSelectTheme(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f).testTag("theme-DARK"),
                        centered = true,
                    )
                }
            }
        }
        item {
            SectionLabel("Mode")
            QaCard(padding = 0.dp) {
                ModeRow("Soft intervention", "Offer an alternative. You always have an override.", selected = true)
                HorizontalDivider(color = colors.line)
                ModeRow("Firm intervention", "Add a small friction step before opening anyway.", selected = false)
            }
        }
        item {
            MonoText(
                text = "Quality Alternative - v0.1 MVP",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AppSelectionRow(
    app: DistractingApp,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                BorderStroke(1.dp, if (selected) colors.primaryText else colors.line),
                RoundedCornerShape(10.dp),
            )
            .background(if (selected) colors.elevatedSurface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppDot(app = app)
        Text(
            text = app.displayName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.primaryText,
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(BorderStroke(1.5.dp, if (selected) colors.primaryText else colors.lineStrong), RoundedCornerShape(6.dp))
                .background(if (selected) colors.primaryText else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) QaIcon(kind = QaIconKind.Check, color = colors.background, size = 14.dp)
        }
    }
}

@Composable
private fun DurationOption(
    bucket: DurationBucket,
    label: String,
    description: String,
    selection: OnboardingSelection,
    onSelectDuration: (DurationBucket) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val selected = selection.preferredDurationBucket == bucket
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, if (selected) colors.primaryText else colors.line), RoundedCornerShape(10.dp))
            .background(if (selected) colors.elevatedSurface else Color.Transparent)
            .clickable { onSelectDuration(bucket) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = bucket.prototypeMinutes().toString(),
            style = TextStyle(
                fontFamily = QualityDisplayFontFamily,
                fontSize = 30.sp,
                lineHeight = 30.sp,
                color = if (selected) colors.accent else colors.primaryText,
            ),
            modifier = Modifier.width(56.dp),
        )
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            BodyText(description, color = colors.mutedText, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PermissionIntroRow(name: String, why: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        SourceBadge(
            sourceType = ContentSourceType.EDITORIAL,
            icon = if (name.startsWith("Usage")) QaIconKind.Eye else QaIconKind.Shield,
        )
        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
    BodyText(why, color = QualityAlternativeThemeTokens.colors.mutedText)
}

@Composable
private fun PermissionWarningCard(onOpenSettings: () -> Unit) {
    val colors = QualityAlternativeThemeTokens.colors
    QaCard(
        background = colors.accentSoft,
        borderColor = colors.accent,
        padding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 6.dp)) {
            QaIcon(kind = QaIconKind.Bell, color = colors.accent, size = 16.dp)
            Text("Interception is paused", style = MaterialTheme.typography.bodySmall, color = colors.accent, fontWeight = FontWeight.Medium)
        }
        BodyText(
            text = "A permission is missing. Without it, we can't offer you something better when you open a distracting app.",
            color = colors.primaryText,
            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        )
        QaButton("Fix in Settings", onClick = onOpenSettings, variant = QaButtonVariant.Outline, size = QaButtonSize.Small, fullWidth = false)
    }
}

@Composable
private fun ActiveDelayCard(
    targetApp: DistractingApp?,
    delayWindow: DelayWindow,
    suggestion: ContentItem?,
    onReadAlternative: () -> Unit,
) {
    QaCard(padding = 14.dp) {
        MonoText("${targetApp?.displayName ?: delayWindow.targetAppPackage} paused")
        BodyText(
            text = "We'll keep the social prompt quiet until ${formatTimestamp(delayWindow.endsAtMillis)}.",
            color = QualityAlternativeThemeTokens.colors.mutedText,
        )
        suggestion?.let { content ->
            QaButton(
                text = "Read a ${content.durationMinutes} min alternative",
                onClick = onReadAlternative,
                variant = QaButtonVariant.Outline,
                size = QaButtonSize.Small,
                fullWidth = false,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, right: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(text, modifier = Modifier.weight(1f))
        if (right != null) MonoText(right)
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = QualityAlternativeThemeTokens.colors.primaryText,
) {
    Column(modifier = modifier) {
        MonoText(label, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 22.sp,
            lineHeight = 24.sp,
            color = color,
        )
    }
}

@Composable
private fun AppPills(
    apps: List<DistractingApp>,
    selectedApp: DistractingApp?,
    onSelect: (DistractingApp) -> Unit,
    selectedPackages: Set<String> = apps.mapTo(mutableSetOf(), DistractingApp::packageName),
    dimUnselected: Boolean = false,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        apps.forEach { app ->
            val selected = app.packageName in selectedPackages
            val isActiveTarget = selectedApp?.packageName == app.packageName
            val borderColor = when {
                isActiveTarget && dimUnselected -> QualityAlternativeThemeTokens.colors.primaryText
                else -> QualityAlternativeThemeTokens.colors.line
            }
            Row(
                modifier = Modifier
                    .alpha(if (!selected && dimUnselected) 0.55f else 1f)
                    .clip(RoundedCornerShape(100.dp))
                    .border(
                        BorderStroke(1.dp, borderColor),
                        RoundedCornerShape(100.dp),
                    )
                    .clickable { onSelect(app) }
                    .padding(start = 6.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppDot(app = app, size = 20.dp)
                Text(app.displayName, style = MaterialTheme.typography.bodySmall, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
private fun LibrarySummaryRow(label: String, value: String, sourceType: ContentSourceType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 19.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SourceBadge(sourceType = sourceType)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            MonoText(value, modifier = Modifier.padding(top = 2.dp))
        }
        QaIcon(kind = QaIconKind.ChevronRight, color = QualityAlternativeThemeTokens.colors.faintText, size = 18.dp)
    }
}

@Composable
private fun LibraryItemCard(item: ContentItem, onOpen: () -> Unit) {
    QaCard(
        modifier = Modifier.clickable(onClick = onOpen),
        padding = 16.dp,
    ) {
        ContentMetaRow(item)
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 17.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        BodyText(item.sourceLabel(), color = QualityAlternativeThemeTokens.colors.mutedText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ContentMetaRow(item: ContentItem, stacked: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (stacked) {
            MonoText(item.sourceLabel(), modifier = Modifier.width(76.dp), lineHeight = 12.sp)
            MonoText("·")
            MonoText(item.topicLine(), modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            MonoText(item.sourceLabel(), modifier = Modifier.widthIn(max = 116.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            MonoText("·")
            MonoText(item.topicLine(), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            QaIcon(kind = QaIconKind.Clock, color = QualityAlternativeThemeTokens.colors.faintText, size = 14.dp)
            MonoText("${item.durationMinutes} min")
        }
    }
}

@Composable
private fun BackupRow(
    item: ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line)
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontSize = 16.sp, lineHeight = 19.sp, maxLines = 2)
                MonoText(
                    "${item.sourceLabel()} · ${item.durationMinutes} min · ${item.topicLine()}",
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            QaIcon(kind = QaIconKind.ChevronRight, color = QualityAlternativeThemeTokens.colors.faintText, size = 18.dp)
        }
    }
}

@Composable
private fun AddLinkValidationLine(form: AddLinkFormState, host: String) {
    val urlError = form.validationErrors.firstOrNull { it.isUrlError() }
    if (urlError != null) {
        BodyText(urlError.displayMessage(), color = QualityAlternativeThemeTokens.colors.accent, fontSize = 12.5.sp, modifier = Modifier.padding(top = 6.dp))
    } else if (host.isNotBlank()) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(QualityAlternativeThemeTokens.colors.successSoft)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QaIcon(kind = QaIconKind.Check, color = QualityAlternativeThemeTokens.colors.success, size = 14.dp)
            BodyText("Found · $host", color = QualityAlternativeThemeTokens.colors.success, fontSize = 12.5.sp)
        }
    }
}

@Composable
private fun FeedbackQuestion(
    title: String,
    selected: String?,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (id, label) ->
                QaChip(
                    text = label,
                    selected = selected == id,
                    onClick = { onSelect(id) },
                    modifier = Modifier.weight(1f),
                    centered = true,
                    minHeight = 34.dp,
                    horizontalPadding = 6.dp,
                    verticalPadding = 6.dp,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CalendarStrip(days: List<ProgressDayBar>) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (day.state) {
                            ProgressDayState.CONVERTED -> colors.accent
                            ProgressDayState.PARTIAL -> colors.accentSoft
                            ProgressDayState.EMPTY -> colors.line
                        },
                    )
                    .alpha(if (day.state == ProgressDayState.EMPTY) 0.5f else 1f)
                    .testTag("progress-day-${day.state.name.lowercase(Locale.US)}"),
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        MonoText("3 weeks ago")
        MonoText("today")
    }
}

@Composable
private fun SmallStatRow(label: String, value: String, subtle: Boolean = false) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BodyText(label, color = if (subtle) colors.mutedText else colors.primaryText, modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            color = if (subtle) colors.mutedText else colors.primaryText,
        )
    }
}

@Composable
private fun RecentReplacementRow(entry: ReplacementHistoryEntry) {
    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MonoText(formatRelativeDay(entry.acceptedAtMillis), modifier = Modifier.weight(1f))
            MonoText("${entry.contentMinutesGuess()} min")
        }
        Text(
            entry.contentTitle,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        BodyText("instead of ${entry.targetAppDisplayName}", color = QualityAlternativeThemeTokens.colors.mutedText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun PermissionRow(name: String, desc: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            BodyText(desc, color = QualityAlternativeThemeTokens.colors.mutedText, fontSize = 12.5.sp)
        }
        ToggleVisual(granted)
    }
}

@Composable
private fun ModeRow(title: String, desc: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RadioVisual(selected)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            BodyText(desc, color = QualityAlternativeThemeTokens.colors.mutedText, fontSize = 12.5.sp)
        }
    }
}

@Composable
private fun ToggleVisual(granted: Boolean) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (granted) colors.success else colors.lineStrong)
            .padding(2.dp),
        contentAlignment = if (granted) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun RadioVisual(selected: Boolean) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .border(BorderStroke(1.5.dp, if (selected) colors.accent else colors.lineStrong), CircleShape)
            .background(if (selected) colors.accent else Color.Transparent)
            .padding(4.dp),
    ) {
        if (selected) Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(colors.elevatedSurface))
    }
}

@Composable
private fun QaCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 21.dp,
    background: Color = QualityAlternativeThemeTokens.colors.elevatedSurface,
    borderColor: Color = QualityAlternativeThemeTokens.colors.line,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = background,
        contentColor = QualityAlternativeThemeTokens.colors.primaryText,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}

private enum class QaButtonVariant { Primary, Accent, Outline, Ghost }
private enum class QaButtonSize { Normal, Small, Compact }

@Composable
private fun QaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QaButtonVariant = QaButtonVariant.Primary,
    size: QaButtonSize = QaButtonSize.Normal,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    leadingIcon: QaIconKind? = null,
    trailingIcon: QaIconKind? = null,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val background = when (variant) {
        QaButtonVariant.Primary -> colors.primaryText
        QaButtonVariant.Accent -> colors.accent
        QaButtonVariant.Outline -> colors.elevatedSurface
        QaButtonVariant.Ghost -> Color.Transparent
    }
    val foreground = when (variant) {
        QaButtonVariant.Primary -> colors.background
        QaButtonVariant.Accent -> Color.White
        QaButtonVariant.Outline -> colors.primaryText
        QaButtonVariant.Ghost -> colors.mutedText
    }
    val border = when (variant) {
        QaButtonVariant.Outline -> BorderStroke(1.dp, colors.lineStrong)
        QaButtonVariant.Primary -> BorderStroke(1.dp, colors.primaryText)
        QaButtonVariant.Accent -> BorderStroke(1.dp, colors.accent)
        QaButtonVariant.Ghost -> null
    }
    val padding = when (size) {
        QaButtonSize.Normal -> PaddingValues(horizontal = 19.dp, vertical = 15.dp)
        QaButtonSize.Small -> PaddingValues(horizontal = 15.dp, vertical = 11.dp)
        QaButtonSize.Compact -> PaddingValues(horizontal = 13.dp, vertical = 13.dp)
    }
    Surface(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .alpha(if (enabled) 1f else 0.4f),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = background,
        contentColor = foreground,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                QaIcon(
                    kind = leadingIcon,
                    color = foreground,
                    size = if (size == QaButtonSize.Small || size == QaButtonSize.Compact) 14.dp else 16.dp,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontSize = if (size == QaButtonSize.Small || size == QaButtonSize.Compact) 14.sp else 16.sp,
            )
            if (trailingIcon != null) {
                QaIcon(
                    kind = trailingIcon,
                    color = foreground,
                    size = if (size == QaButtonSize.Small || size == QaButtonSize.Compact) 14.dp else 16.dp,
                    modifier = Modifier
                        .padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun QaIcon(
    kind: QaIconKind,
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 18.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        fun x(value: Float) = this.size.width * value / 24f
        fun y(value: Float) = this.size.height * value / 24f
        fun p(xValue: Float, yValue: Float) = Offset(x(xValue), y(yValue))
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, p(x1, y1), p(x2, y2), 1.5.dp.toPx(), StrokeCap.Round)
        }
        fun path(block: Path.() -> Unit) {
            val iconPath = Path().apply(block)
            drawPath(iconPath, color = color, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        }

        when (kind) {
            QaIconKind.Plus -> {
                line(12f, 5f, 12f, 19f)
                line(5f, 12f, 19f, 12f)
            }

            QaIconKind.Check -> path {
                moveTo(x(5f), y(12f))
                lineTo(x(9f), y(16f))
                lineTo(x(19f), y(7f))
            }

            QaIconKind.ArrowRight -> {
                line(5f, 12f, 19f, 12f)
                line(13f, 5f, 20f, 12f)
                line(13f, 19f, 20f, 12f)
            }

            QaIconKind.ArrowLeft -> {
                line(19f, 12f, 5f, 12f)
                line(11f, 5f, 4f, 12f)
                line(11f, 19f, 4f, 12f)
            }

            QaIconKind.Close -> {
                line(6f, 6f, 18f, 18f)
                line(6f, 18f, 18f, 6f)
            }

            QaIconKind.Clock -> {
                drawCircle(color = color, radius = x(9f), center = p(12f, 12f), style = Stroke(1.5.dp.toPx()))
                line(12f, 7f, 12f, 12f)
                line(12f, 12f, 15f, 14f)
            }

            QaIconKind.Book -> {
                path {
                    moveTo(x(4f), y(5.5f))
                    quadraticTo(x(4f), y(3.8f), x(6.5f), y(3f))
                    lineTo(x(20f), y(3f))
                    lineTo(x(20f), y(18f))
                    lineTo(x(6.5f), y(18f))
                    quadraticTo(x(4f), y(18f), x(4f), y(20.5f))
                    lineTo(x(4f), y(5.5f))
                }
                line(4f, 20.5f, 20f, 20.5f)
            }

            QaIconKind.Link -> {
                path {
                    moveTo(x(10f), y(14f))
                    quadraticTo(x(12f), y(16f), x(15f), y(14f))
                    lineTo(x(18f), y(11f))
                    quadraticTo(x(21f), y(8f), x(18f), y(5.5f))
                    quadraticTo(x(15.5f), y(3f), x(13f), y(6f))
                    lineTo(x(11.5f), y(7.5f))
                }
                path {
                    moveTo(x(14f), y(10f))
                    quadraticTo(x(12f), y(8f), x(9f), y(10f))
                    lineTo(x(6f), y(13f))
                    quadraticTo(x(3f), y(16f), x(6f), y(18.5f))
                    quadraticTo(x(8.5f), y(21f), x(11f), y(18f))
                    lineTo(x(12.5f), y(16.5f))
                }
            }

            QaIconKind.Home -> path {
                moveTo(x(3f), y(11f))
                lineTo(x(12f), y(4f))
                lineTo(x(21f), y(11f))
                lineTo(x(21f), y(20f))
                lineTo(x(15f), y(20f))
                lineTo(x(15f), y(14f))
                lineTo(x(9f), y(14f))
                lineTo(x(9f), y(20f))
                lineTo(x(3f), y(20f))
                close()
            }

            QaIconKind.Library -> {
                line(4f, 5f, 4f, 19f)
                line(9f, 5f, 9f, 19f)
                line(14f, 6f, 16f, 19f)
                line(19f, 6f, 21f, 19f)
            }

            QaIconKind.History -> {
                drawArc(
                    color = color,
                    startAngle = -210f,
                    sweepAngle = 285f,
                    useCenter = false,
                    style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(x(3f), y(3f)),
                    size = androidx.compose.ui.geometry.Size(x(18f), y(18f)),
                )
                line(3f, 3f, 3f, 8f)
                line(3f, 8f, 8f, 8f)
                line(12f, 7f, 12f, 12f)
                line(12f, 12f, 15f, 14f)
            }

            QaIconKind.Settings -> {
                drawCircle(color = color, radius = x(8.3f), center = p(12f, 12f), style = Stroke(1.5.dp.toPx()))
                drawCircle(color = color, radius = x(3f), center = p(12f, 12f), style = Stroke(1.5.dp.toPx()))
                repeat(8) { index ->
                    val radians = Math.toRadians((index * 45).toDouble())
                    val dx = kotlin.math.cos(radians).toFloat()
                    val dy = kotlin.math.sin(radians).toFloat()
                    drawLine(
                        color,
                        Offset(x(12f) + dx * x(7.4f), y(12f) + dy * y(7.4f)),
                        Offset(x(12f) + dx * x(10.2f), y(12f) + dy * y(10.2f)),
                        1.5.dp.toPx(),
                        StrokeCap.Round,
                    )
                }
            }

            QaIconKind.Sparkle -> path {
                moveTo(x(12f), y(3f))
                lineTo(x(14f), y(8f))
                lineTo(x(19f), y(10f))
                lineTo(x(14f), y(12f))
                lineTo(x(12f), y(17f))
                lineTo(x(10f), y(12f))
                lineTo(x(5f), y(10f))
                lineTo(x(10f), y(8f))
                close()
            }

            QaIconKind.Shield -> path {
                moveTo(x(12f), y(3f))
                lineTo(x(20f), y(6f))
                lineTo(x(20f), y(12f))
                quadraticTo(x(20f), y(18f), x(12f), y(21f))
                quadraticTo(x(4f), y(18f), x(4f), y(12f))
                lineTo(x(4f), y(6f))
                close()
            }

            QaIconKind.Eye -> {
                path {
                    moveTo(x(1f), y(12f))
                    quadraticTo(x(6f), y(5f), x(12f), y(5f))
                    quadraticTo(x(18f), y(5f), x(23f), y(12f))
                    quadraticTo(x(18f), y(19f), x(12f), y(19f))
                    quadraticTo(x(6f), y(19f), x(1f), y(12f))
                }
                drawCircle(color = color, radius = x(3f), center = p(12f, 12f), style = Stroke(1.5.dp.toPx()))
            }

            QaIconKind.Pause -> {
                line(8f, 5f, 8f, 19f)
                line(16f, 5f, 16f, 19f)
            }

            QaIconKind.External -> {
                path {
                    moveTo(x(18f), y(13f))
                    lineTo(x(18f), y(21f))
                    lineTo(x(3f), y(21f))
                    lineTo(x(3f), y(6f))
                    lineTo(x(11f), y(6f))
                }
                line(15f, 3f, 21f, 3f)
                line(21f, 3f, 21f, 9f)
                line(10f, 14f, 21f, 3f)
            }

            QaIconKind.Bell -> path {
                moveTo(x(6f), y(8f))
                quadraticTo(x(6f), y(4f), x(12f), y(4f))
                quadraticTo(x(18f), y(4f), x(18f), y(8f))
                quadraticTo(x(18f), y(15f), x(21f), y(17f))
                lineTo(x(3f), y(17f))
                quadraticTo(x(6f), y(15f), x(6f), y(8f))
                moveTo(x(10f), y(21f))
                quadraticTo(x(12f), y(22f), x(14f), y(21f))
            }

            QaIconKind.ChevronRight -> path {
                moveTo(x(9f), y(6f))
                lineTo(x(15f), y(12f))
                lineTo(x(9f), y(18f))
            }

            QaIconKind.Dot -> drawCircle(color = color, radius = x(2f), center = p(12f, 12f))
        }
    }
}

@Composable
private fun QaIconButton(icon: QaIconKind, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(34.dp),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        contentColor = QualityAlternativeThemeTokens.colors.mutedText,
    ) {
        Box(contentAlignment = Alignment.Center) {
            QaIcon(kind = icon, color = QualityAlternativeThemeTokens.colors.mutedText, size = 18.dp)
        }
    }
}

@Composable
private fun QaChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentSelected: Boolean = false,
    centered: Boolean = false,
    minHeight: androidx.compose.ui.unit.Dp = 0.dp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 15.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 9.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    maxLines: Int = 2,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val selectedColor = if (accentSelected) colors.accent else colors.primaryText
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) selectedColor else colors.elevatedSurface,
        contentColor = if (selected) colors.background else colors.mutedText,
        border = BorderStroke(1.dp, if (selected) selectedColor else colors.lineStrong),
    ) {
        Box(
            modifier = Modifier
                .then(if (minHeight.value > 0f) Modifier.heightIn(min = minHeight) else Modifier)
                .then(if (centered) Modifier.fillMaxWidth() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                fontSize = fontSize,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val colors = QualityAlternativeThemeTokens.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = colors.primaryText,
            fontSize = 15.sp,
            lineHeight = 19.sp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.elevatedSurface)
            .border(
                BorderStroke(1.dp, if (isError) colors.accent else colors.lineStrong),
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 15.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 19.sp),
                        color = colors.faintText,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun AppDot(app: DistractingApp, size: androidx.compose.ui.unit.Dp = 28.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(app.dotColor()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = app.displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SourceBadge(
    sourceType: ContentSourceType,
    icon: QaIconKind = when (sourceType) {
        ContentSourceType.USER_LINK -> QaIconKind.Link
        ContentSourceType.USER_DOCUMENT -> QaIconKind.Book
        ContentSourceType.MEDITATION -> QaIconKind.Pause
        ContentSourceType.EDITORIAL -> QaIconKind.Sparkle
    },
) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accentSoft),
        contentAlignment = Alignment.Center,
    ) {
        QaIcon(kind = icon, color = colors.accent, size = 17.dp)
    }
}

@Composable
private fun Dots(total: Int, active: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == active) 20.dp else 6.dp, height = 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == active) QualityAlternativeThemeTokens.colors.primaryText else QualityAlternativeThemeTokens.colors.lineStrong),
            )
        }
    }
}

@Composable
private fun ProgressLine(progress: Int, modifier: Modifier = Modifier) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = modifier
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(colors.line),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100f).coerceIn(0f, 1f))
                .background(colors.accent),
        )
    }
}

@Composable
private fun InputLabel(text: String, modifier: Modifier = Modifier) {
    LabelText(text, modifier = modifier.padding(bottom = 6.dp))
}

@Composable
private fun DisplayText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    lineHeight: androidx.compose.ui.unit.TextUnit = 31.sp,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = QualityDisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            lineHeight = lineHeight,
            letterSpacing = (-0.15).sp,
            color = QualityAlternativeThemeTokens.colors.primaryText,
        ),
        textAlign = textAlign,
    )
}

@Composable
private fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = QualityAlternativeThemeTokens.colors.primaryText,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.5.sp,
    lineHeight: androidx.compose.ui.unit.TextUnit = 21.sp,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize, lineHeight = lineHeight, color = color),
        textAlign = textAlign,
    )
}

@Composable
private fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = QualityAlternativeThemeTokens.colors.faintText,
    lineHeight: androidx.compose.ui.unit.TextUnit = 14.sp,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text.uppercase(Locale.US),
        modifier = modifier,
        style = TextStyle(
            fontFamily = QualityMonoFontFamily,
            fontSize = 11.sp,
            lineHeight = lineHeight,
            letterSpacing = 0.4.sp,
            color = color,
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
private fun LabelText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(Locale.US),
        modifier = modifier,
        style = TextStyle(
            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = QualityAlternativeThemeTokens.colors.mutedText,
        ),
    )
}

internal data class ProgressSnapshot(
    val daysConverted: Int,
    val dayBars: List<ProgressDayBar>,
    val interventionsShown: Int,
    val alternativesChosen: Int,
    val delayedOpens: Int,
    val consciousOverrides: Int,
    val completedReads: Int,
    val recentReplacements: List<ReplacementHistoryEntry>,
)

internal data class ProgressDayBar(
    val date: LocalDate,
    val state: ProgressDayState,
)

internal enum class ProgressDayState {
    EMPTY,
    PARTIAL,
    CONVERTED,
}

internal fun progressSnapshot(
    entries: List<ReplacementHistoryEntry>,
    events: List<AnalyticsEvent>,
    zoneId: ZoneId = ZoneId.systemDefault(),
    nowMillis: Long = System.currentTimeMillis(),
): ProgressSnapshot {
    val convertedDays = entries
        .map { entry ->
            Instant.ofEpochMilli(entry.acceptedAtMillis)
                .atZone(zoneId)
                .toLocalDate()
        }
        .toSet()
    val delayedDays = events
        .filter { it.type == AnalyticsEventType.DELAY_SELECTED }
        .map { event ->
            Instant.ofEpochMilli(event.timestampMillis)
                .atZone(zoneId)
                .toLocalDate()
        }
        .toSet()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val firstDay = today.minusDays((PROGRESS_STRIP_DAYS - 1).toLong())
    val dayBars = (0 until PROGRESS_STRIP_DAYS).map { offset ->
        val date = firstDay.plusDays(offset.toLong())
        ProgressDayBar(
            date = date,
            state = when {
                date in convertedDays -> ProgressDayState.CONVERTED
                date in delayedDays -> ProgressDayState.PARTIAL
                else -> ProgressDayState.EMPTY
            },
        )
    }

    return ProgressSnapshot(
        daysConverted = dayBars.count { it.state == ProgressDayState.CONVERTED },
        dayBars = dayBars,
        interventionsShown = events.distinctProgressEventCount(AnalyticsEventType.INTERVENTION_SHOWN),
        alternativesChosen = entries.size,
        delayedOpens = events.distinctProgressEventCount(AnalyticsEventType.DELAY_SELECTED),
        consciousOverrides = events.distinctProgressEventCount(AnalyticsEventType.OPEN_ANYWAY_SELECTED),
        completedReads = entries.count(ReplacementHistoryEntry::isCompleted),
        recentReplacements = entries
            .filter { entry ->
                val acceptedDate = Instant.ofEpochMilli(entry.acceptedAtMillis)
                    .atZone(zoneId)
                    .toLocalDate()
                acceptedDate in today.minusDays((RECENT_REPLACEMENTS_DAYS - 1).toLong())..today
            }
            .take(MAX_RECENT_PROGRESS_REPLACEMENTS),
    )
}

internal data class HomeHeroCopy(
    val title: String,
    val body: String,
    val showAddLinkAction: Boolean,
)

internal fun homeHeroCopy(readiness: PermissionReadiness): HomeHeroCopy {
    return if (readiness.interceptionReady) {
        HomeHeroCopy(
            title = "You're set up for quieter reading today.",
            body = "When an impulse opens a selected app, the Android alpha can pause it and offer one finite alternative.",
            showAddLinkAction = true,
        )
    } else {
        HomeHeroCopy(
            title = "You're set up for quieter reading today.",
            body = "Finish the Android setup first, so the app can meet the impulse before a social feed opens.",
            showAddLinkAction = false,
        )
    }
}

internal fun finiteReaderParagraphs(body: String): List<String> {
    return body
        .split(Regex("\\n\\s*\\n"))
        .map(String::trim)
        .filter(String::isNotEmpty)
}

internal fun readerParagraphsForDisplay(body: String, fallback: String): List<String> {
    return finiteReaderParagraphs(body).ifEmpty { listOf(fallback) }
}

private fun formatTimestamp(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return formatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
}

private fun formatRelativeDay(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, h:mm a")
    return formatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
}

private fun TopicTag.displayName(): String {
    return when (this) {
        TopicTag.ESSAYS -> "Essays"
        TopicTag.PHILOSOPHY -> "Philosophy"
        TopicTag.SCIENCE -> "Science"
        TopicTag.DESIGN -> "Design"
        TopicTag.POETRY -> "Poetry"
        TopicTag.HISTORY -> "History"
        TopicTag.TECH -> "Tech"
        TopicTag.FICTION -> "Fiction"
        TopicTag.CLIMATE -> "Climate"
        TopicTag.ECONOMICS -> "Economics"
        TopicTag.FOOD -> "Food"
        TopicTag.ARCHITECTURE -> "Architecture"
        TopicTag.CREATIVITY -> "Design"
        TopicTag.PSYCHOLOGY -> "Psychology"
    }
}

private fun prototypeTopics(): List<TopicTag> = listOf(
    TopicTag.ESSAYS,
    TopicTag.SCIENCE,
    TopicTag.DESIGN,
    TopicTag.PHILOSOPHY,
    TopicTag.POETRY,
    TopicTag.HISTORY,
    TopicTag.TECH,
    TopicTag.FICTION,
)

private fun DurationBucket.prototypeMinutes(): Int {
    return when (this) {
        DurationBucket.QUICK -> 5
        DurationBucket.FOCUS -> 10
        DurationBucket.DEEP -> 20
    }
}

private fun DurationBucket.prototypeMinutesLabel(): String = "${prototypeMinutes()} min"

private fun ContentItem.isUserLink(): Boolean = sourceType == ContentSourceType.USER_LINK

private fun ContentItem.isUserDocument(): Boolean = sourceType == ContentSourceType.USER_DOCUMENT

private fun primaryActionLabel(item: ContentItem): String {
    return when {
        item.usesMeditationTimer() -> "Start timer · ${item.durationMinutes} min"
        item.isUserDocument() && item.usesExternalHandoff() -> "Open file · ${item.durationMinutes} min"
        item.usesExternalHandoff() -> "Open link · ${item.durationMinutes} min"
        else -> "Read this · ${item.durationMinutes} min"
    }
}

private fun primaryActionIcon(item: ContentItem): QaIconKind {
    return when {
        item.usesMeditationTimer() -> QaIconKind.Pause
        item.usesExternalHandoff() -> QaIconKind.External
        else -> QaIconKind.Book
    }
}

private fun meditationTimeLabel(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(Locale.US, minutes, seconds)
}

private fun ContentItem.topicLine(): String {
    return topicTags.joinToString { it.displayName() }.ifBlank { "Essays" }
}

private fun ContentItem.sourceLabel(): String {
    return when {
        isUserLink() -> {
            val host = sourceLabel?.ifBlank { null } ?: externalUrl?.hostLabel()?.ifBlank { null }
            host?.let { "Your link · $it" } ?: "Your link"
        }

        isUserDocument() -> sourceLabel?.let { "Your file · $it" } ?: "Your file"
        usesMeditationTimer() -> sourceLabel ?: "Quality Alternative"
        else -> sourceLabel?.ifBlank { null }
            ?: packId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }
}

private fun remainingMinutes(totalMinutes: Int, progress: Int): Int {
    return kotlin.math.ceil(totalMinutes * (1 - progress.coerceIn(0, 100) / 100f).toDouble())
        .toInt()
        .coerceAtLeast(0)
}

private fun String.hostLabel(): String {
    return runCatching {
        val normalized = if (startsWith("http://") || startsWith("https://")) this else "https://$this"
        Uri.parse(normalized).host.orEmpty().removePrefix("www.")
    }.getOrDefault("")
}

private fun DistractingApp.dotColor(): Color {
    return when (packageName) {
        "com.instagram.android" -> Color(0xFFE1306C)
        "com.zhiliaoapp.musically" -> Color(0xFF010101)
        "com.twitter.android" -> Color(0xFF222222)
        "com.reddit.frontpage" -> Color(0xFFFF4500)
        "com.facebook.katana" -> Color(0xFF1877F2)
        "com.google.android.youtube" -> Color(0xFFFF0033)
        else -> Color(0xFF965630)
    }
}

private fun ReplacementHistoryEntry.contentMinutesGuess(): Int {
    return when {
        contentDescription.contains("seven", ignoreCase = true) -> 7
        contentDescription.contains("five", ignoreCase = true) -> 5
        contentDescription.contains("six", ignoreCase = true) -> 6
        contentDescription.contains("eight", ignoreCase = true) -> 8
        else -> 10
    }
}

private fun List<AnalyticsEvent>.distinctProgressEventCount(type: AnalyticsEventType): Int {
    return filter { it.type == type }
        .map { event ->
            event.metadata["delayId"]
                ?: event.semanticKey
                ?: event.interventionId
                ?: "${event.type}:${event.targetAppPackage}:${event.timestampMillis}"
        }
        .toSet()
        .size
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

@Composable
private fun AddDocumentValidationLine(form: AddDocumentFormState) {
    val blockingError = form.validationErrors.firstOrNull { error ->
        error == UserDocumentValidationError.EMPTY_URI || error == UserDocumentValidationError.UNSUPPORTED_FORMAT
    }
    if (blockingError != null) {
        BodyText(
            text = blockingError.displayMessage(),
            color = QualityAlternativeThemeTokens.colors.accent,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun UserDocumentValidationError.displayMessage(): String {
    return when (this) {
        UserDocumentValidationError.EMPTY_URI -> "Choose a local PDF, Markdown, or EPUB file first."
        UserDocumentValidationError.UNSUPPORTED_FORMAT -> "Use a PDF, .md/.markdown, or EPUB file."
        UserDocumentValidationError.BLANK_TITLE -> "Add a title so the recommendation is easy to recognize."
        UserDocumentValidationError.INVALID_DURATION -> "Choose an estimated session time from 1 to 120 minutes."
        UserDocumentValidationError.NO_TOPICS -> "Choose at least one topic so the app can rank this file."
    }
}

private fun documentFormatLabel(form: AddDocumentFormState): String {
    return when (UserDocumentValidator.detectFormat(displayName = form.displayName, mimeType = form.mimeType)) {
        ContentFormat.MARKDOWN -> "Markdown · private reader"
        ContentFormat.PDF -> "PDF · external viewer"
        ContentFormat.EPUB -> "EPUB · external viewer"
        ContentFormat.HTML -> "Document"
        null -> "Unsupported file"
    }
}

private fun launchExternalLink(
    context: android.content.Context,
    viewModel: MainViewModel,
) {
    val url = viewModel.currentExternalLinkUrl()
    if (url == null) {
        viewModel.recordExternalLinkHandoffFailed(reason = "missing_url")
        return
    }
    val uri = Uri.parse(url)
    val mimeType = viewModel.currentExternalContentMimeType()
    val intent = Intent(Intent.ACTION_VIEW).apply {
        if (mimeType != null) {
            setDataAndType(uri, mimeType)
        } else {
            data = uri
        }
        if (uri.scheme == "content") {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    runCatching {
        context.startActivity(intent)
    }.onSuccess {
        viewModel.recordExternalLinkOpened()
    }.onFailure { error ->
        val reason = if (error is ActivityNotFoundException) "no_handler" else "start_activity_failed"
        viewModel.recordExternalLinkHandoffFailed(reason = reason)
    }
}

private data class PickedDocumentMetadata(
    val displayName: String,
    val mimeType: String?,
)

private fun Context.documentMetadata(uri: Uri): PickedDocumentMetadata {
    val queriedName = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }.getOrNull()
    val fallbackName = uri.lastPathSegment?.substringAfterLast('/').orEmpty()
    return PickedDocumentMetadata(
        displayName = queriedName?.takeIf(String::isNotBlank) ?: fallbackName.ifBlank { "Untitled document" },
        mimeType = contentResolver.getType(uri),
    )
}

private fun persistDocumentPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private const val MAX_BACKUP_RECOMMENDATIONS = 2
private const val MAX_RECENT_PROGRESS_REPLACEMENTS = 3
private const val RECENT_REPLACEMENTS_DAYS = 7
private const val PROGRESS_STRIP_DAYS = 21
private const val DEBUG_PARITY_VIEWPORT_WIDTH_DP = 340f
private const val DEBUG_PARITY_VIEWPORT_HEIGHT_DP = 740f
private const val MIN_DEBUG_PARITY_SCALE = 0.94f
private const val MAX_DEBUG_PARITY_SCALE = 1.22f
private val USER_DOCUMENT_PICKER_MIME_TYPES = arrayOf(
    "application/pdf",
    "application/epub+zip",
    "text/markdown",
    "text/x-markdown",
    "text/plain",
    "application/octet-stream",
)
