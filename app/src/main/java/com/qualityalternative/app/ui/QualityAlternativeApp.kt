package com.qualityalternative.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.qualityalternative.app.data.ACCOUNT_LIGHT_PROFILE_FILE_NAME
import com.qualityalternative.app.data.accountLightTimestampedBackupFileName
import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.data.UserDocumentValidator
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.DEFAULT_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.DEFAULT_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.MAX_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MAX_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MIN_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MIN_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.OpenAnywayUnlockMinuteOptions
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.ReaderTocEntry
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.usesExternalHandoff
import com.qualityalternative.app.domain.model.usesMeditationTimer
import com.qualityalternative.app.domain.model.usesRepositoryBody
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import com.qualityalternative.app.domain.service.RecommendationExplainer
import com.qualityalternative.app.domain.service.RecommendationExplanation
import com.qualityalternative.app.ui.theme.QualityAlternativeAppTheme
import com.qualityalternative.app.ui.theme.QualityAlternativeThemeTokens
import com.qualityalternative.app.ui.theme.QualityDisplayFontFamily
import com.qualityalternative.app.ui.theme.QualityMonoFontFamily
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val LocalAppInterfaceTextScale = compositionLocalOf { DEFAULT_INTERFACE_TEXT_SCALE }

@Composable
fun QualityAlternativeApp(
    viewModel: MainViewModel,
    onExitToTarget: () -> Unit = {},
) {
    val uiState = viewModel.uiState

    ApplySystemBarsForTheme(themeMode = uiState.themeMode)

    QualityAlternativeAppTheme(themeMode = uiState.themeMode) {
        AppInterfaceTextScaleProvider(interfaceTextScale = uiState.interfaceTextScale) {
            DebugVisualParityDensityScale(enabled = uiState.screen != MainScreen.Reader) {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.latestMessage) {
                    val message = uiState.latestMessage
                    if (message == null) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        return@LaunchedEffect
                    }
                    snackbarHostState.showSnackbar(message)
                    viewModel.dismissMessage()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = uiState.screen.snackbarBottomPadding()),
                        )
                    },
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
}

@Composable
private fun AppInterfaceTextScaleProvider(
    interfaceTextScale: Double,
    content: @Composable () -> Unit,
) {
    val baseDensity = LocalDensity.current
    val safeScale = interfaceTextScale.coerceIn(MIN_INTERFACE_TEXT_SCALE, MAX_INTERFACE_TEXT_SCALE).toFloat()
    CompositionLocalProvider(
        LocalAppInterfaceTextScale provides safeScale.toDouble(),
        LocalDensity provides Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * safeScale,
        ),
    ) {
        content()
    }
}

@Composable
private fun ReaderTextDensity(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val interfaceTextScale = LocalAppInterfaceTextScale.current
        .coerceIn(MIN_INTERFACE_TEXT_SCALE, MAX_INTERFACE_TEXT_SCALE)
        .toFloat()
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density.density,
            fontScale = density.fontScale / interfaceTextScale,
        ),
    ) {
        content()
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

private enum class AnnotationDriveSyncMode {
    CONNECT,
    RETRY,
}

@Composable
private fun DebugVisualParityDensityScale(
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!BuildConfig.DEBUG || !enabled) {
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
    val driveAuthorizationContext = context.findActivity() ?: context
    val driveAuthorizationClient = remember(driveAuthorizationContext) {
        Identity.getAuthorizationClient(driveAuthorizationContext)
    }
    var driveSyncMode by remember { mutableStateOf(AnnotationDriveSyncMode.CONNECT) }
    fun handleDriveAuthorizationResult(result: AuthorizationResult) {
        val token = result.accessToken?.takeIf(String::isNotBlank)
        val hasDriveScope = result.grantedScopes.orEmpty().contains(ANNOTATION_DRIVE_SCOPE)
        if (token == null || !hasDriveScope) {
            viewModel.reportAnnotationDriveAuthorizationFailure("Google Drive permission was not granted.")
            return
        }
        if (driveSyncMode == AnnotationDriveSyncMode.RETRY || state.annotationDriveSyncEnabled) {
            viewModel.retryAnnotationDriveSync(token)
        } else {
            viewModel.connectAnnotationDriveSync(token)
        }
    }
    val annotationDriveFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            viewModel.reportAnnotationDriveAuthorizationFailure("Google Drive folder was not selected.")
            return@rememberLauncherForActivityResult
        }
        val metadata = context.documentMetadata(uri)
        viewModel.connectAnnotationDriveFolderProvider(
            uri = uri.toString(),
            displayName = metadata.displayName,
            persistWritePermission = { uriString ->
                persistAnnotationExportPermission(context = context, uri = Uri.parse(uriString))
            },
        )
    }
    fun startGoogleDriveFolderProvider() {
        viewModel.beginAnnotationDriveAuthorization()
        annotationDriveFolderPicker.launch(null)
    }
    val driveAuthorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED && result.data == null) {
            startGoogleDriveFolderProvider()
            return@rememberLauncherForActivityResult
        }
        googleDriveAuthorizationMissingResultMessage(
            resultCode = result.resultCode,
            hasResultIntent = result.data != null,
        )?.let { message ->
            viewModel.reportAnnotationDriveAuthorizationFailure(message)
            return@rememberLauncherForActivityResult
        }
        runCatching {
            driveAuthorizationClient.getAuthorizationResultFromIntent(result.data!!)
        }.onSuccess(::handleDriveAuthorizationResult).onFailure { error ->
            if (error.googleDriveAuthMessage() == "Google Drive authorization was cancelled.") {
                startGoogleDriveFolderProvider()
            } else {
                viewModel.reportAnnotationDriveAuthorizationFailure(error.googleDriveAuthMessage())
            }
        }
    }
    val startGoogleDriveSyncAuthorization: (AnnotationDriveSyncMode) -> Unit = { mode ->
        driveSyncMode = mode
        viewModel.beginAnnotationDriveAuthorization()
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(ANNOTATION_DRIVE_SCOPE)))
            .build()
        driveAuthorizationClient.authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    val pendingIntent = authorizationResult.pendingIntent
                    if (pendingIntent == null) {
                        viewModel.reportAnnotationDriveAuthorizationFailure(
                            "Google Drive authorization needs a missing consent screen.",
                        )
                    } else {
                        driveAuthorizationLauncher.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
                        )
                    }
                } else {
                    handleDriveAuthorizationResult(authorizationResult)
                }
            }
            .addOnFailureListener { error ->
                if (error.googleDriveAuthMessage() == "Google Drive authorization was cancelled.") {
                    startGoogleDriveFolderProvider()
                } else {
                    viewModel.reportAnnotationDriveAuthorizationFailure(error.googleDriveAuthMessage())
                }
            }
    }
    val disconnectGoogleDriveSync = {
        val revokeRequest = RevokeAccessRequest.builder()
            .setScopes(listOf(Scope(ANNOTATION_DRIVE_SCOPE)))
            .build()
        driveAuthorizationClient.revokeAccess(revokeRequest)
        viewModel.disconnectAnnotationDriveSync()
    }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }
        viewModel.prepareUserDocumentBatchImport(
            candidates = uris.map { uri -> context.documentImportCandidate(uri) },
        )
    }
    val annotationExportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        val metadata = context.documentMetadata(uri)
        viewModel.configureReadingAnnotationExport(
            uri = uri.toString(),
            displayName = metadata.displayName,
            persistWritePermission = { uriString ->
                persistAnnotationExportPermission(context = context, uri = Uri.parse(uriString))
            },
        )
    }
    val profileAutosavePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        val metadata = context.documentMetadata(uri)
        viewModel.configureAccountLightProfileAutosave(
            uri = uri.toString(),
            displayName = metadata.displayName,
            persistWritePermission = { uriString ->
                persistProfileAutosavePermission(context = context, uri = Uri.parse(uriString))
            },
        )
    }
    val accountLightExportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        viewModel.exportAccountLightProfile { json ->
            context.writeUtf8Text(uri = uri, text = json)
        }
    }
    val accountLightImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        runCatching {
            context.readUtf8Text(uri)
        }.onSuccess { rawJson ->
            viewModel.previewAccountLightImport(rawJson)
        }.onFailure {
            viewModel.reportAccountLightImportReadFailure()
        }
    }
    val onImportDocument = {
        documentPicker.launch(USER_DOCUMENT_PICKER_MIME_TYPES)
    }
    val onSelectAnnotationExport = {
        annotationExportPicker.launch(null)
    }
    val releasePersistedDocumentPermission: (String) -> Unit = { uri ->
        releaseDocumentPermission(context = context, uri = Uri.parse(uri))
    }
    val releasePersistedAnnotationExportPermission: (String) -> Unit = { uri ->
        releaseAnnotationExportPermission(context = context, uri = Uri.parse(uri))
    }
    val releasePersistedProfileAutosavePermission: (String) -> Unit = { uri ->
        releaseProfileAutosavePermission(context = context, uri = Uri.parse(uri))
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
                onOpenLibrary = viewModel::openLibrary,
                onOpenSettings = viewModel::openSettings,
                onStartDelayAlternative = viewModel::startActiveDelayAlternative,
                onContinueReading = { content -> viewModel.openLibraryItem(content, origin = "home") },
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
                onOpenAnnotations = viewModel::openAnnotationLibrary,
                onOpen = { content -> viewModel.openLibraryItem(content, origin = "library") },
                onTogglePriorityContent = viewModel::togglePriorityContent,
                onToggleCompletedActivation = viewModel::toggleCompletedContentActivation,
                onToggleManageMode = viewModel::toggleLibraryManageMode,
                onToggleSelection = viewModel::toggleLibraryContentSelection,
                onDeleteSelected = {
                    viewModel.deleteSelectedLibraryContent(releaseDocumentPermission = releasePersistedDocumentPermission)
                },
            )
        }

        MainScreen.Progress -> TabScaffold(
            active = MainScreen.Progress,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            ProgressTab(
                snapshot = progressSnapshot(state.historyEntries, state.events),
                annotationCount = state.readingAnnotations.size,
                onOpenAnnotations = viewModel::openAnnotationLibrary,
            )
        }

        MainScreen.Annotations -> TabScaffold(
            active = MainScreen.Progress,
            onHome = viewModel::openHome,
            onLibrary = viewModel::openLibrary,
            onProgress = viewModel::openProgress,
            onSettings = viewModel::openSettings,
        ) {
            AnnotationLibraryScreen(
                state = state,
                onOpenAnnotation = viewModel::openAnnotationTarget,
                onBack = viewModel::openProgress,
            )
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
                onSelectMeditationDuration = viewModel::setMeditationDurationMinutes,
                onSelectReaderFontScale = viewModel::setReaderFontScale,
                onSelectInterfaceTextScale = viewModel::setInterfaceTextScale,
                onSelectContentPriority = viewModel::setContentPriority,
                onSelectOpenAnywayUnlock = viewModel::setOpenAnywayUnlockMinutes,
                onSelectTheme = viewModel::selectThemeMode,
                onRefreshReadiness = viewModel::refreshPermissionReadiness,
                onSelectAnnotationExport = onSelectAnnotationExport,
                onClearAnnotationExport = {
                    viewModel.clearReadingAnnotationExport(releaseWritePermission = releasePersistedAnnotationExportPermission)
                },
                onRetryAnnotationExport = { viewModel.retryReadingAnnotationExport() },
                onConnectAnnotationDrive = { startGoogleDriveSyncAuthorization(AnnotationDriveSyncMode.CONNECT) },
                onRetryAnnotationDrive = { startGoogleDriveSyncAuthorization(AnnotationDriveSyncMode.RETRY) },
                onDisconnectAnnotationDrive = disconnectGoogleDriveSync,
                onExportAccountLightProfile = { accountLightExportPicker.launch(ACCOUNT_LIGHT_PROFILE_FILE_NAME) },
                onExportAccountLightBackup = { accountLightExportPicker.launch(accountLightTimestampedBackupFileName()) },
                onImportAccountLightProfile = { accountLightImportPicker.launch(arrayOf("application/json", "text/json", "*/*")) },
                onSelectAccountLightAutosave = { profileAutosavePicker.launch(null) },
                onRetryAccountLightAutosave = { viewModel.retryAccountLightProfileAutosave() },
                onClearAccountLightAutosave = {
                    viewModel.clearAccountLightProfileAutosave(releaseWritePermission = releasePersistedProfileAutosavePermission)
                },
                onMergeAccountLightProfile = viewModel::applyAccountLightMergeImport,
                onRequestAccountLightReplace = viewModel::requestAccountLightReplaceConfirmation,
                onConfirmAccountLightReplace = viewModel::confirmAccountLightReplaceImport,
                onCancelAccountLightImport = viewModel::cancelAccountLightImport,
            )
        }

        MainScreen.AddLink -> AddLinkScreen(
            form = state.addLinkForm,
            onUrlChange = viewModel::updateAddLinkUrl,
            onTitleChange = viewModel::updateAddLinkTitle,
            onDurationChange = viewModel::updateAddLinkDuration,
            onToggleTopic = viewModel::toggleAddLinkTopic,
            onTogglePriority = viewModel::toggleAddLinkPriority,
            onSave = viewModel::saveUserLink,
            onCancel = viewModel::cancelAddLink,
            onImportDocument = onImportDocument,
        )

        MainScreen.AddDocument -> AddDocumentScreen(
            form = state.addDocumentForm,
            onTitleChange = viewModel::updateAddDocumentTitle,
            onToggleTopic = viewModel::toggleAddDocumentTopic,
            onTogglePriority = viewModel::toggleAddDocumentPriority,
            onSave = {
                viewModel.saveUserDocument(
                    persistReadPermission = { uriString ->
                        persistDocumentPermission(context = context, uri = Uri.parse(uriString))
                    },
                )
            },
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
            onSelectMeditationDuration = viewModel::setMeditationDurationMinutes,
            onDelay = viewModel::delayFor15Minutes,
            onOpenAnyway = {
                if (viewModel.openAnyway()) {
                    onExitToTarget()
                }
            },
        )

        MainScreen.Reader -> ReaderScreen(
            state = state,
            onProgressChanged = { progressPercent, lastVisibleParagraphIndex, lastVisibleTextOffset, paragraphCount ->
                viewModel.saveCurrentReadingProgress(
                    progressPercent = progressPercent,
                    lastVisibleParagraphIndex = lastVisibleParagraphIndex,
                    lastVisibleTextOffset = lastVisibleTextOffset,
                    paragraphCount = paragraphCount,
                )
            },
            onSaveAnnotation = { paragraphIndex, quotedText, noteText, existingAnnotationId, selector ->
                viewModel.saveCurrentReadingAnnotation(
                    paragraphIndex = paragraphIndex,
                    quotedText = quotedText,
                    noteText = noteText,
                    existingAnnotationId = existingAnnotationId,
                    selector = selector,
                )
            },
            onDeleteAnnotation = viewModel::deleteReadingAnnotation,
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
            onSelectMeditationDuration = viewModel::setMeditationDurationMinutes,
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
    val density = LocalDensity.current
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
    Minus,
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
    Note,
    Dot,
}

private fun MainScreen.snackbarBottomPadding() = when (this) {
    MainScreen.Home,
    MainScreen.Library,
    MainScreen.Annotations,
    MainScreen.Progress,
    MainScreen.Settings -> 88.dp

    else -> 0.dp
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
                    modifier = Modifier.testTag("onboarding-topic-${topic.name}"),
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
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartDelayAlternative: () -> Unit,
    onContinueReading: (ContentItem) -> Unit,
) {
    val selectedPacks = state.starterPacks.filter { it.id in state.preferences?.selectedPackIds.orEmpty() }
    val editorialItems = selectedPacks.flatMap { it.items }
    val allLibraryItems = editorialItems + state.userLinks + state.userDocuments
    val topContinue = allLibraryItems.unfinishedSortedByProgress(state.readingProgress).firstOrNull()
    val topContinueProgress = topContinue?.let { item -> state.readingProgress.unfinishedProgressFor(item.id) }
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
        if (topContinue != null && topContinueProgress != null) {
            item {
                ContinueReadingCard(
                    item = topContinue,
                    progress = topContinueProgress,
                    onContinue = { onContinueReading(topContinue) },
                )
            }
        }
        if (totalItems > 0) {
            item {
                ReadNowCard(
                    totalItems = totalItems,
                    totalMinutes = totalMins,
                    onOpenLibrary = onOpenLibrary,
                )
            }
        }
        item {
            SectionLabel("Setup")
            QaCard {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCell("Intercepting", quantityLabel(state.availableTargetApps.size, "app"), Modifier.weight(1f))
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
            SectionLabel("Your library", right = "${quantityLabel(totalItems, "item")} · $totalMins min")
            QaCard(padding = 0.dp) {
                LibrarySummaryRow("Editorial picks", quantityLabel(editorialItems.size, "pick"), ContentSourceType.EDITORIAL)
                HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line)
                LibrarySummaryRow("Your added links", quantityLabel(state.userLinks.size, "link"), ContentSourceType.USER_LINK)
                HorizontalDivider(color = QualityAlternativeThemeTokens.colors.line)
                LibrarySummaryRow("Your files", quantityLabel(state.userDocuments.size, "file"), ContentSourceType.USER_DOCUMENT)
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
    onOpenAnnotations: () -> Unit,
    onOpen: (ContentItem) -> Unit,
    onTogglePriorityContent: (ContentItem) -> Unit,
    onToggleCompletedActivation: (ContentItem) -> Unit,
    onToggleManageMode: () -> Unit,
    onToggleSelection: (ContentItem) -> Unit,
    onDeleteSelected: () -> Unit,
) {
    var filter by remember { mutableStateOf("all") }
    val editorial = state.starterPacks
        .filter { it.id in state.preferences?.selectedPackIds.orEmpty() }
        .flatMap { it.items }
    val allItems = editorial + state.userLinks + state.userDocuments
    val progressById = state.readingProgress.associateBy(ReadingProgress::contentId)
    val unfinished = allItems.unfinishedSortedByProgress(state.readingProgress)
    val priority = allItems
        .filter { it.id in state.priorityContentIds }
    val list = when (filter) {
        "priority" -> priority
        "unfinished" -> unfinished
        "editorial" -> editorial
        "yours" -> state.userLinks
        "files" -> state.userDocuments
        else -> allItems
    }
    val selectedCount = state.selectedLibraryContentIds.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 128.dp),
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
                Spacer(modifier = Modifier.width(8.dp))
                QaButton(
                    text = if (state.isManagingLibrary) "Done" else "Manage",
                    onClick = onToggleManageMode,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    fullWidth = false,
                    leadingIcon = if (state.isManagingLibrary) QaIconKind.Check else QaIconKind.Library,
                    modifier = Modifier.testTag("library-manage-toggle"),
                )
            }
        }
        if (state.isManagingLibrary) {
            item {
                QaCard(
                    padding = 14.dp,
                    modifier = Modifier.testTag("library-manage-panel"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            MonoText("$selectedCount selected")
                            BodyText(
                                text = "Only your saved links and files can be deleted.",
                                color = QualityAlternativeThemeTokens.colors.mutedText,
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        QaButton(
                            text = "Delete selected",
                            onClick = onDeleteSelected,
                            enabled = selectedCount > 0,
                            variant = QaButtonVariant.Accent,
                            size = QaButtonSize.Small,
                            fullWidth = false,
                            leadingIcon = QaIconKind.Close,
                            modifier = Modifier.testTag("library-delete-selected"),
                        )
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QaButton(
                    text = "Import PDF / MD / EPUB",
                    onClick = onImportDocument,
                    variant = QaButtonVariant.Ghost,
                    leadingIcon = QaIconKind.Book,
                    modifier = Modifier.testTag("library-import-document"),
                )
                QaButton(
                    text = "Annotations",
                    onClick = onOpenAnnotations,
                    variant = QaButtonVariant.Ghost,
                    leadingIcon = QaIconKind.Note,
                    modifier = Modifier.testTag("library-open-annotations"),
                )
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                QaChip("All", selected = filter == "all", onClick = { filter = "all" })
                QaChip("Unfinished", selected = filter == "unfinished", onClick = { filter = "unfinished" })
                QaChip("Priority", selected = filter == "priority", onClick = { filter = "priority" })
                QaChip("Editorial", selected = filter == "editorial", onClick = { filter = "editorial" })
                QaChip("Your links", selected = filter == "yours", onClick = { filter = "yours" })
                QaChip("Files", selected = filter == "files", onClick = { filter = "files" })
            }
        }
        if (list.isEmpty()) {
            item {
                QaCard {
                    BodyText(
                        text = if (filter == "priority") {
                            "No priority picks yet. Mark individual pieces in Library to make them more likely during an intervention."
                        } else if (filter == "unfinished") {
                            "No unfinished reading yet. Open a saved piece and it will stay easy to continue."
                        } else {
                            "Nothing here yet. Add one piece you'd actually read instead of scrolling."
                        },
                        color = QualityAlternativeThemeTokens.colors.mutedText,
                    )
                }
            }
        } else {
            items(list, key = ContentItem::id) { item ->
                LibraryItemCard(
                    item = item,
                    progress = progressById[item.id]?.takeIf(ReadingProgress::isUnfinished),
                    prioritized = item.id in state.priorityContentIds,
                    completed = item.id in state.completedContentIds,
                    reactivated = item.id in state.reactivatedCompletedContentIds,
                    isManaging = state.isManagingLibrary,
                    selected = item.id in state.selectedLibraryContentIds,
                    onOpen = { onOpen(item) },
                    onTogglePriority = { onTogglePriorityContent(item) },
                    onToggleCompletedActivation = { onToggleCompletedActivation(item) },
                    onToggleSelection = { onToggleSelection(item) },
                )
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
    onTogglePriority: () -> Unit,
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
            DisplayText("Add content.", fontSize = 30.sp, lineHeight = 33.sp)
            BodyText(
                text = "Save one piece you'd actually choose at the moment of impulse.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            QaButton(
                text = "Import PDF / MD / EPUB",
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
            InputLabel("Priority", Modifier.padding(top = 14.dp))
            QaChip(
                text = if (form.markPriority) "Priority on add" else "Mark as priority",
                selected = form.markPriority,
                accentSelected = true,
                onClick = onTogglePriority,
                modifier = Modifier.testTag("add-link-priority"),
                centered = true,
                minHeight = 34.dp,
            )
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
    onToggleTopic: (TopicTag) -> Unit,
    onTogglePriority: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onPickAnother: () -> Unit,
) {
    val saveLabel = when {
        form.isSaving -> "Saving..."
        form.supportedImportCount > 1 -> "Add files to library"
        else -> "Add file to library"
    }
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
            DisplayText("Import reading\nfiles.", fontSize = 30.sp, lineHeight = 33.sp)
            BodyText(
                text = "Choose one or several PDF, Markdown, or EPUB files. Reading time is calculated automatically; unsupported files are skipped.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            QaCard(modifier = Modifier.padding(bottom = 18.dp), padding = 16.dp) {
                MonoText("${form.supportedImportCount} ready · ${form.unsupportedImportCount} skipped", modifier = Modifier.padding(bottom = 8.dp))
                form.candidates.ifEmpty {
                    listOf(
                        DocumentImportCandidate(
                            uri = form.uri,
                            displayName = form.displayName,
                            mimeType = form.mimeType,
                            title = form.title,
                            durationMinutes = form.durationMinutes,
                            format = UserDocumentValidator.detectFormat(form.displayName, form.mimeType),
                        ),
                    )
                }.forEach { candidate ->
                    DocumentImportRow(candidate = candidate)
                }
            }
            QaButton(
                text = "Choose files",
                onClick = onPickAnother,
                variant = QaButtonVariant.Outline,
                size = QaButtonSize.Small,
                leadingIcon = QaIconKind.Book,
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .testTag("add-document-pick-another"),
            )
            AddDocumentValidationLine(form)
            if (form.importCount <= 1) {
                InputLabel("Title", Modifier.padding(top = 14.dp))
                QaTextField(
                    value = form.title,
                    onValueChange = onTitleChange,
                    placeholder = "How you'd recognize it",
                    modifier = Modifier.testTag("add-document-title"),
                    isError = UserDocumentValidationError.BLANK_TITLE in form.validationErrors,
                )
            } else {
                BodyText(
                    text = "Reading time is calculated per file. Topics and priority apply to every saved file in this batch.",
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
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
            InputLabel("Priority", Modifier.padding(top = 14.dp))
            QaChip(
                text = if (form.markPriority) "Priority on add" else "Mark saved files as priority",
                selected = form.markPriority,
                accentSelected = true,
                onClick = onTogglePriority,
                modifier = Modifier.testTag("add-document-priority"),
                centered = true,
                minHeight = 34.dp,
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, QualityAlternativeThemeTokens.colors.line))
                .padding(horizontal = 28.dp, vertical = 10.dp),
        ) {
            QaButton(
                text = saveLabel,
                onClick = onSave,
                enabled = form.canSave && !form.isSaving,
                variant = QaButtonVariant.Primary,
                modifier = Modifier.testTag("add-document-save"),
            )
        }
    }
}

@Composable
private fun DocumentImportRow(candidate: DocumentImportCandidate) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        MonoText(documentFormatLabel(candidate))
        Text(
            text = candidate.title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 3.dp),
        )
        Text(
            text = "${candidate.displayName} · ${candidate.durationMinutes} min · ${candidate.estimateSource.displayLabel()}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = QualityAlternativeThemeTokens.colors.mutedText,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
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
            text = if (saved.savedCount > 1) {
                "We'll offer these next time you reach for one of your chosen apps."
            } else {
                "We'll offer this next time you reach for one of your chosen apps."
            },
            color = QualityAlternativeThemeTokens.colors.mutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        QaCard(modifier = Modifier.padding(top = 32.dp, bottom = 24.dp), padding = 16.dp) {
            MonoText(addSuccessMeta(saved))
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
    onSelectMeditationDuration: (Int) -> Unit,
    onDelay: () -> Unit,
    onOpenAnyway: () -> Unit,
) {
    val recommendationSet = state.currentRecommendationSet ?: return
    val targetApp = state.selectedTargetApp ?: return
    val preferences = state.preferences ?: return
    val colors = QualityAlternativeThemeTokens.colors
    val primary = recommendationSet.primary
    val backups = recommendationSet.backups
    val primaryExplanation = RecommendationExplainer.explain(primary, preferences)
    val primaryContinueProgress = continueProgressMetaFor(item = primary, progress = state.readingProgress)
    val canAdjustMeditationBeforeStart = primary.usesMeditationTimer()
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
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
        QaCard(
            borderColor = colors.lineStrong,
            padding = 13.dp,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            ContentMetaRow(primary)
            DisplayText(
                text = primary.title,
                fontSize = 23.sp,
                lineHeight = 25.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp, bottom = 5.dp),
            )
            Text(
                text = "\"${primary.description}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = QualityDisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                ),
                color = colors.mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            RecommendationExplanationBlock(
                explanation = primaryExplanation,
                modifier = Modifier
                    .padding(top = 9.dp)
                    .testTag("intervention-primary-explanation"),
            )
            if (primaryContinueProgress != null) {
                ContinueProgressMetaLine(
                    progress = primaryContinueProgress,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("intervention-primary-progress"),
                )
            }
        }
        QaButton(
            text = primaryActionLabel(primary),
            onClick = onAcceptPrimary,
            variant = QaButtonVariant.Accent,
            modifier = Modifier.padding(bottom = 8.dp),
            size = QaButtonSize.Small,
            leadingIcon = primaryActionIcon(primary),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 6.dp),
        ) {
            if (canAdjustMeditationBeforeStart) {
                MeditationDurationChooser(
                    selectedMinutes = state.meditationDurationMinutes,
                    onSelect = onSelectMeditationDuration,
                    label = "Timer length",
                    modifier = Modifier.padding(bottom = 8.dp),
                    testTagPrefix = "intervention-meditation-duration",
                )
            }
            MonoText("Other options", modifier = Modifier.padding(bottom = 4.dp))
            if (backups.isEmpty()) {
                BodyText(
                    text = "No extra choices are available right now.",
                    color = colors.mutedText,
                    modifier = Modifier.testTag("intervention-empty-backups"),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("intervention-backup-list"),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(bottom = 2.dp),
                ) {
                    itemsIndexed(
                        items = backups,
                        key = { _, backup -> backup.id },
                    ) { index, backup ->
                        BackupRow(
                            item = backup,
                            continueProgress = continueProgressMetaFor(item = backup, progress = state.readingProgress),
                            progressTestTag = "intervention-backup-progress-$index",
                            onClick = { onAcceptBackup(backup) },
                            modifier = Modifier.testTag("intervention-backup-action-$index"),
                        )
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("intervention-bottom-actions"),
        ) {
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
private fun RecommendationExplanationBlock(
    explanation: RecommendationExplanation,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        explanation.chips.forEach { chip ->
            RecommendationReasonPill(chip)
        }
    }
}

@Composable
private fun RecommendationReasonPill(text: String) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .border(BorderStroke(1.dp, colors.lineStrong), RoundedCornerShape(100.dp))
            .background(colors.elevatedSurface)
            .padding(horizontal = 7.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedText,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReaderScreen(
    state: MainUiState,
    onProgressChanged: (Int, Int, Int, Int) -> Unit,
    onSaveAnnotation: (
        paragraphIndex: Int,
        quotedText: String,
        noteText: String,
        existingAnnotationId: String?,
        selector: ReadingAnnotationSelector,
    ) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val content = state.currentContent ?: return
    val readerDocument = state.currentReaderDocument
    val rawBlocks = remember(readerDocument, state.currentContentBody, content.description) {
        readerDocument?.blocks
            ?.takeIf { it.isNotEmpty() }
            ?.map { block ->
                readerMarkdownBlock(
                    rawBlock = block.text,
                    sourceHref = block.sourceHref,
                    sourceAnchor = block.anchor,
                    sourceBlockIndex = block.sourceBlockIndex,
                )
            }
            ?: readerBlocksForDisplay(
                body = state.currentContentBody,
                fallback = content.description,
            )
    }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val readerFontScale = state.readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE)
    var measuredReaderWidthDp by remember(content.id) { mutableStateOf(0f) }
    var measuredReaderHeightDp by remember(content.id) { mutableStateOf(0f) }
    val readerViewportWidthDp = measuredReaderWidthDp.takeIf { it > 0f } ?: configuration.screenWidthDp.toFloat()
    val readerViewportHeightDp = measuredReaderHeightDp.takeIf { it > 0f } ?: configuration.screenHeightDp.toFloat()
    val readerPageFit = remember(readerViewportWidthDp, readerViewportHeightDp, readerFontScale) {
        adaptiveReaderPageFit(
            viewportWidthDp = readerViewportWidthDp,
            viewportHeightDp = readerViewportHeightDp,
            readerFontScale = readerFontScale,
        )
    }
    val maxPageWeight = readerPageFit.maxPageWeight
    val charsPerLine = readerPageFit.charsPerLine
    val maxBlockWeight = readerPageFit.maxBlockWeight
    val blockGapLineCost = readerPageFit.blockGapLineCost
    val maxBlocksPerPage = readerPageFit.maxBlocksPerPage
    val effectiveReaderFontScale = readerPageFit.readerFontScale
    val readerBlockLayout = remember(rawBlocks, maxBlockWeight, charsPerLine) {
        splitOversizedReaderBlocks(
            blocks = rawBlocks,
            maxBlockWeight = maxBlockWeight,
            charsPerLine = charsPerLine,
        )
    }
    val blocks = readerBlockLayout.blocks
    val tableOfContents = remember(readerDocument, rawBlocks.size, readerBlockLayout) {
        readerDocument?.tableOfContents
            .orEmpty()
            .filter { entry -> entry.blockIndex in rawBlocks.indices }
            .map { entry ->
                entry.copy(blockIndex = readerBlockLayout.displayBlockIndexFor(entry.blockIndex))
            }
    }
    val readerStartParagraphIndex = state.currentReaderStartParagraphIndex
    val readerStartSelector = state.currentReaderStartSelector
    val pages = remember(content.id, blocks, maxPageWeight, charsPerLine, blockGapLineCost, maxBlocksPerPage, effectiveReaderFontScale) {
        readerPagesForBlocks(
            blocks = blocks,
            maxPageWeight = maxPageWeight,
            charsPerLine = charsPerLine,
            blockGapLineCost = blockGapLineCost,
            maxBlocksPerPage = maxBlocksPerPage,
            readerFontScale = effectiveReaderFontScale,
        )
    }
    val pageBoundarySignature = remember(pages) {
        readerPageBoundarySignature(pages)
    }
    val restoredProgress = remember(content.id) {
        state.currentReadingProgress?.takeIf { it.contentId == content.id && it.isUnfinished() }
    }
    val selectorStartParagraphIndex = remember(readerStartSelector, readerBlockLayout) {
        readerStartSelector?.let(readerBlockLayout::displayBlockIndexForSelector)
    }
    val restoredProgressParagraphIndex = remember(
        restoredProgress?.lastVisibleParagraphIndex,
        restoredProgress?.lastVisibleTextOffset,
        readerBlockLayout,
    ) {
        restoredProgress?.let { progress ->
            readerBlockLayout.displayBlockIndexForSourcePosition(
                sourceBlockIndex = progress.lastVisibleParagraphIndex,
                textOffset = progress.lastVisibleTextOffset,
            )
        }
    }
    val initialParagraphIndex = (selectorStartParagraphIndex ?: readerStartParagraphIndex ?: restoredProgressParagraphIndex ?: 0)
        .coerceIn(0, (blocks.size - 1).coerceAtLeast(0))
    val initialPageIndex = remember(
        content.id,
        selectorStartParagraphIndex,
        readerStartParagraphIndex,
        restoredProgressParagraphIndex,
        pageBoundarySignature,
    ) {
        readerPageIndexForParagraph(pages = pages, paragraphIndex = initialParagraphIndex)
    }
    var hasManualReaderNavigation by remember(
        content.id,
        selectorStartParagraphIndex,
        readerStartParagraphIndex,
        restoredProgressParagraphIndex,
    ) { mutableStateOf(false) }
    var currentPageIndex by remember(
        content.id,
        selectorStartParagraphIndex,
        readerStartParagraphIndex,
        restoredProgressParagraphIndex,
    ) { mutableStateOf(initialPageIndex) }
    val listState = rememberLazyListState()
    var annotationSelection by remember(content.id) { mutableStateOf<ReaderAnnotationSelection?>(null) }
    var annotationNoteDraft by remember(content.id) { mutableStateOf("") }
    var isTocOpen by remember(content.id) { mutableStateOf(false) }
    val annotationsByParagraph = remember(content.id, state.readingAnnotations) {
        state.readingAnnotations
            .filter { annotation -> annotation.contentId == content.id }
            .associateBy(ReadingAnnotation::paragraphIndex)
    }
    val annotationsForContent = remember(content.id, state.readingAnnotations) {
        state.readingAnnotations.filter { annotation -> annotation.contentId == content.id }
    }
    val safeCurrentPageIndex = currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    LaunchedEffect(safeCurrentPageIndex, pages.size) {
        if (currentPageIndex != safeCurrentPageIndex) {
            currentPageIndex = safeCurrentPageIndex
        }
    }
    LaunchedEffect(content.id, initialPageIndex, pageBoundarySignature) {
        if (!hasManualReaderNavigation && currentPageIndex != initialPageIndex) {
            currentPageIndex = initialPageIndex
        }
    }
    val currentPage = pages.getOrElse(safeCurrentPageIndex) { ReaderPage(0, (blocks.size - 1).coerceAtLeast(0)) }
    val displayedProgress = readerProgressPercentForPageIndex(
        pageIndex = safeCurrentPageIndex,
        pageCount = pages.size,
    )
    val firstVisibleParagraphIndex = currentPage.start
    val pageParagraphIndices = currentPage.start..currentPage.endInclusive
    LaunchedEffect(content.id, safeCurrentPageIndex, currentPage.start, currentPage.endInclusive) {
        listState.scrollToItem(0)
    }

    fun moveToPage(targetPageIndex: Int) {
        val nextPageIndex = targetPageIndex.coerceIn(0, pages.lastIndex)
        val nextPage = pages[nextPageIndex]
        if (nextPageIndex != safeCurrentPageIndex) {
            hasManualReaderNavigation = true
        }
        currentPageIndex = nextPageIndex
        annotationSelection = null
        annotationNoteDraft = ""
        isTocOpen = false
        if (nextPageIndex > safeCurrentPageIndex) {
            val sourcePosition = readerBlockLayout.sourcePositionForDisplayBlock(nextPage.endInclusive)
            onProgressChanged(
                readerProgressPercentForPageIndex(
                    pageIndex = nextPageIndex,
                    pageCount = pages.size,
                ),
                sourcePosition.sourceBlockIndex,
                sourcePosition.textOffset,
                rawBlocks.size,
            )
        }
    }

    fun updateAnnotationSelection(nextSelection: ReaderAnnotationSelection) {
        val previousSelection = annotationSelection
        annotationSelection = nextSelection
        isTocOpen = false
        val focus = nextSelection.focusChangedFrom(previousSelection)
        val nextPageIndex = readerPageIndexForAnnotationSelectionFocus(
            selection = nextSelection,
            focus = focus,
            layout = readerBlockLayout,
            pages = pages,
        )
        if (nextPageIndex != safeCurrentPageIndex) {
            hasManualReaderNavigation = true
            currentPageIndex = nextPageIndex
        }
    }
    val canHandlePageTap = annotationSelection == null && !isTocOpen

    BackHandler {
        when {
            isTocOpen -> isTocOpen = false
            annotationSelection != null -> {
                annotationSelection = null
                annotationNoteDraft = ""
            }
            else -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("reader-screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (BuildConfig.DEBUG) {
                Spacer(
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                        .semantics {
                            contentDescription = "reader-first-visible-paragraph-$firstVisibleParagraphIndex"
                        }
                        .testTag("reader-first-visible-paragraph-$firstVisibleParagraphIndex"),
                )
                Spacer(
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                        .semantics {
                            contentDescription = "reader-current-page-end-paragraph-${currentPage.endInclusive}"
                        }
                        .testTag("reader-current-page-end-paragraph-${currentPage.endInclusive}"),
                )
                Spacer(
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                        .semantics {
                            contentDescription = "reader-page-fit-${readerPageFit.viewportWidthDp.roundToInt()}x" +
                                "${readerPageFit.viewportHeightDp.roundToInt()}-" +
                                "font-${(readerPageFit.readerFontScale * 100).roundToInt()}-" +
                                "weight-${readerPageFit.maxPageWeight}-chars-${readerPageFit.charsPerLine}-" +
                                "gap-${(readerPageFit.blockGapLineCost * 100).roundToInt()}-" +
                                "cap-${readerPageFit.maxBlocksPerPage}-" +
                                "blocks-${currentPage.endInclusive - currentPage.start + 1}-pages-${pages.size}"
                        }
                        .testTag("reader-page-fit-summary"),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        with(density) {
                            measuredReaderWidthDp = size.width.toDp().value
                            measuredReaderHeightDp = size.height.toDp().value
                        }
                    }
                    .testTag("reader-list"),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(canHandlePageTap, safeCurrentPageIndex, pages.lastIndex) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var movedBeyondTap = false
                                val touchSlop = viewConfiguration.touchSlop
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { pointer -> pointer.id == down.id } ?: break
                                    if ((change.position - down.position).getDistance() > touchSlop) {
                                        movedBeyondTap = true
                                    }
                                    if (change.changedToUpIgnoreConsumed()) {
                                        val drag = change.position - down.position
                                        val isHorizontalPageSwipe =
                                            abs(drag.x) > touchSlop * 3 &&
                                                abs(drag.x) > abs(drag.y) * 1.2f
                                        val isShortTap = change.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis
                                        if (canHandlePageTap) {
                                            when {
                                                isHorizontalPageSwipe && drag.x < 0f && safeCurrentPageIndex < pages.lastIndex -> {
                                                    moveToPage(safeCurrentPageIndex + 1)
                                                }
                                                isHorizontalPageSwipe && drag.x < 0f -> {
                                                    onDone()
                                                }
                                                isHorizontalPageSwipe && drag.x > 0f && safeCurrentPageIndex > 0 -> {
                                                    moveToPage(safeCurrentPageIndex - 1)
                                                }
                                                !movedBeyondTap &&
                                                    isShortTap &&
                                                    down.position.x <= size.width * READER_PREVIOUS_TAP_EDGE_FRACTION &&
                                                    safeCurrentPageIndex > 0 -> {
                                                    moveToPage(safeCurrentPageIndex - 1)
                                                }
                                                !movedBeyondTap && isShortTap -> {
                                                    if (safeCurrentPageIndex < pages.lastIndex) {
                                                        moveToPage(safeCurrentPageIndex + 1)
                                                    } else {
                                                        onDone()
                                                    }
                                                }
                                            }
                                        }
                                        break
                                    }
                                    if (!change.pressed) {
                                        break
                                    }
                                }
                            }
                        }
                        .semantics {
                            if (canHandlePageTap) {
                                onClick(if (safeCurrentPageIndex < pages.lastIndex) "Advance" else "Complete") {
                                    if (safeCurrentPageIndex < pages.lastIndex) {
                                        moveToPage(safeCurrentPageIndex + 1)
                                    } else {
                                        onDone()
                                    }
                                    true
                                }
                            }
                        }
                        .testTag("reader-page-viewport"),
                ) {
                    LazyColumn(
                        state = listState,
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = READER_CONTENT_SIDE_PADDING_DP.dp,
                            top = READER_CONTENT_TOP_PADDING_DP.dp,
                            end = READER_CONTENT_SIDE_PADDING_DP.dp,
                            bottom = READER_CONTENT_BOTTOM_PADDING_DP.dp,
                        ),
                    ) {
                        items(pageParagraphIndices.toList(), key = { paragraphIndex -> "reader-paragraph-$paragraphIndex" }) { paragraphIndex ->
                            val block = blocks[paragraphIndex]
                            val annotation = readingAnnotationForBlock(
                                paragraphIndex = paragraphIndex,
                                block = block,
                                annotationsByParagraph = annotationsByParagraph,
                                annotationsForContent = annotationsForContent,
                            )
                            val activeSelection = annotationSelection?.takeIf { selection ->
                                selection.selector.overlapsReaderBlock(block)
                            }
                            val highlightRange = activeSelection
                                ?.selector
                                ?.readerBlockHighlightRange(block)
                                ?: annotation?.selector?.readerBlockHighlightRange(block)
                            ReaderAnnotatedBlock(
                                paragraphIndex = paragraphIndex,
                                block = block,
                                annotation = annotation,
                                highlightedText = activeSelection?.quotedText ?: annotation?.quotedText,
                                highlightRange = highlightRange,
                                readerFontScale = readerFontScale,
                                onStartEditing = { charOffset ->
                                    annotationSelection = initialReaderAnnotationSelection(
                                        paragraphIndex = paragraphIndex,
                                        block = block,
                                        charOffset = charOffset,
                                        annotation = annotation,
                                        selectionBlocks = blocks,
                                    )
                                    annotationNoteDraft = annotation?.noteText.orEmpty()
                                },
                            )
                        }
                    }
                }
            }
            ReaderMinimalFooter(
                content = content,
                pageIndex = safeCurrentPageIndex,
                pageCount = pages.size,
                progress = displayedProgress,
                hasTableOfContents = tableOfContents.isNotEmpty(),
                onOpenTableOfContents = { isTocOpen = true },
            )
        }
        if (isTocOpen) {
            ReaderTableOfContentsSheet(
                entries = tableOfContents,
                currentPageIndex = safeCurrentPageIndex,
                pages = pages,
                onSelectEntry = { entry ->
                    moveToPage(readerPageIndexForParagraph(pages = pages, paragraphIndex = entry.blockIndex))
                },
                onDismiss = { isTocOpen = false },
            )
        }
        annotationSelection?.let { selection ->
            ReaderAnnotationEditorOverlay(
                selection = selection,
                noteDraft = annotationNoteDraft,
                hasExistingAnnotation = selection.existingAnnotationId != null,
                onSelectionChanged = ::updateAnnotationSelection,
                onNoteDraftChanged = { annotationNoteDraft = it },
                onCancel = {
                    annotationSelection = null
                    annotationNoteDraft = ""
                },
                onDelete = selection.existingAnnotationId?.let { annotationId ->
                    {
                        onDeleteAnnotation(annotationId)
                        annotationSelection = null
                        annotationNoteDraft = ""
                    }
                },
                onSave = {
                    onSaveAnnotation(
                        selection.paragraphIndex,
                        selection.quotedText,
                        annotationNoteDraft,
                        selection.existingAnnotationId,
                        selection.selector,
                    )
                    annotationSelection = null
                    annotationNoteDraft = ""
                },
            )
        }
    }
}

@Composable
private fun BoxScope.ReaderTableOfContentsSheet(
    entries: List<ReaderTocEntry>,
    currentPageIndex: Int,
    pages: List<ReaderPage>,
    onSelectEntry: (ReaderTocEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .testTag("reader-toc-scrim")
            .clickable(onClick = onDismiss),
    )
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .heightIn(max = 430.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("reader-toc-sheet"),
        shape = RoundedCornerShape(18.dp),
        color = colors.elevatedSurface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DisplayText("Contents", fontSize = 20.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                QaIconButton(icon = QaIconKind.Close, onClick = onDismiss)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .testTag("reader-toc-list"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(entries) { index, entry ->
                    val entryPageIndex = readerPageIndexForParagraph(pages = pages, paragraphIndex = entry.blockIndex)
                    val isCurrent = entryPageIndex == currentPageIndex
                    ReaderTocEntryRow(
                        entry = entry,
                        pageIndex = entryPageIndex,
                        isCurrent = isCurrent,
                        onClick = { onSelectEntry(entry) },
                        modifier = Modifier.testTag("reader-toc-entry-$index"),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderTocEntryRow(
    entry: ReaderTocEntry,
    pageIndex: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val rowShape = RoundedCornerShape(10.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .clickable(onClick = onClick),
        shape = rowShape,
        color = if (isCurrent) colors.accentSoft else Color.Transparent,
        border = if (isCurrent) BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f)) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (entry.level.coerceIn(0, 4) * 16).dp + 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                color = colors.primaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            MonoText("p. ${pageIndex + 1}", color = if (isCurrent) colors.accent else colors.mutedText)
        }
    }
}

@Composable
private fun ReaderMinimalFooter(
    content: ContentItem,
    pageIndex: Int,
    pageCount: Int,
    progress: Int,
    hasTableOfContents: Boolean,
    onOpenTableOfContents: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 2.dp, end = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (hasTableOfContents) {
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Contents" }
                    .testTag("reader-toc-open"),
                onClick = onOpenTableOfContents,
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
                contentColor = colors.mutedText,
                border = BorderStroke(1.dp, colors.line.copy(alpha = 0.7f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    QaIcon(kind = QaIconKind.Library, color = colors.mutedText, size = 14.dp)
                }
            }
        }
        Text(
            text = content.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = colors.faintText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ProgressLine(
            progress = progress,
            modifier = Modifier
                .widthIn(min = 64.dp, max = 104.dp),
        )
        MonoText(
            "${pageIndex + 1}/$pageCount · $progress%",
            color = colors.faintText,
            modifier = Modifier.testTag("reader-page-label"),
        )
    }
}

@Composable
private fun ReaderAnnotatedBlock(
    paragraphIndex: Int,
    block: ReaderMarkdownBlock,
    annotation: ReadingAnnotation?,
    highlightedText: String?,
    highlightRange: IntRange?,
    readerFontScale: Double,
    onStartEditing: (Int) -> Unit,
) {
    var textLayoutResult by remember(block.text) { mutableStateOf<TextLayoutResult?>(null) }
    val hasHighlight = highlightRange != null || !highlightedText.isNullOrBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = READER_BLOCK_OUTER_BOTTOM_PADDING_DP.dp)
            .pointerInput(paragraphIndex, annotation?.id) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val charOffset = textLayoutResult
                            ?.getOffsetForPosition(offset)
                            ?.coerceIn(0, block.text.text.length)
                            ?: 0
                        onStartEditing(charOffset)
                    },
                )
            }
            .semantics {
                val actionLabel = if (annotation != null) {
                    "Edit note for paragraph $paragraphIndex"
                } else {
                    "Add note for paragraph $paragraphIndex"
                }
                onLongClick(actionLabel) {
                    onStartEditing(0)
                    true
                }
            }
            .testTag("reader-annotation-block-$paragraphIndex"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reader-annotation-rendered-block-$paragraphIndex"),
        ) {
            ReaderMarkdownBlockText(
                block = block,
                highlightedText = highlightedText,
                highlightRange = highlightRange,
                readerFontScale = readerFontScale,
                modifier = if (hasHighlight) Modifier.testTag("reader-annotation-highlight-$paragraphIndex") else Modifier,
                onTextLayout = { layout -> textLayoutResult = layout },
            )
        }
    }
}

@Composable
private fun BoxScope.ReaderAnnotationEditorOverlay(
    selection: ReaderAnnotationSelection,
    noteDraft: String,
    hasExistingAnnotation: Boolean,
    onSelectionChanged: (ReaderAnnotationSelection) -> Unit,
    onNoteDraftChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.30f))
            .clickable(onClick = onCancel)
            .testTag("reader-annotation-overlay"),
    )
    BoxWithConstraints(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxSize(),
    ) {
        val horizontalPadding = 12.dp
        val verticalPadding = 12.dp
        val reportedImeBottomPadding = with(density) { WindowInsets.ime.getBottom(density).toDp() }
        val isKeyboardVisible = reportedImeBottomPadding > 0.dp
        val rootAlreadyAvoidsIme = isKeyboardVisible && maxHeight < 620.dp
        val effectiveImeBottomPadding = if (rootAlreadyAvoidsIme) 0.dp else reportedImeBottomPadding
        val rawUsableHeight = (maxHeight - effectiveImeBottomPadding - (verticalPadding * 2))
            .coerceAtLeast(260.dp)
        val sheetMaxHeight = if (isKeyboardVisible) {
            rawUsableHeight.coerceAtMost(360.dp)
        } else {
            maxHeight * 0.94f
        }
        val isKeyboardConstrained = isKeyboardVisible || sheetMaxHeight < 620.dp
        val sheetContentPadding = if (isKeyboardConstrained) 10.dp else 12.dp
        val sheetGap = if (isKeyboardConstrained) 6.dp else 8.dp
        val fixedRowBudget = (sheetContentPadding * 2) +
            if (isKeyboardConstrained) 136.dp else 178.dp
        val scrollableBudget = (sheetMaxHeight - fixedRowBudget)
            .coerceAtLeast(if (isKeyboardConstrained) 112.dp else 200.dp)
        val noteMaxHeight = if (isKeyboardConstrained) {
            (scrollableBudget * 0.56f).coerceIn(64.dp, 112.dp)
        } else {
            (sheetMaxHeight * 0.25f).coerceAtLeast(104.dp)
        }
        val quoteMaxHeight = if (isKeyboardConstrained) {
            (scrollableBudget - noteMaxHeight).coerceIn(48.dp, 132.dp)
        } else {
            (sheetMaxHeight * 0.56f).coerceAtLeast(96.dp)
        }
        val quoteScrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = verticalPadding,
                    bottom = verticalPadding + effectiveImeBottomPadding,
                ),
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .then(
                        if (isKeyboardVisible) {
                            Modifier.height(sheetMaxHeight)
                        } else {
                            Modifier.heightIn(max = sheetMaxHeight)
                        },
                    )
                    .testTag("reader-annotation-editor-${selection.paragraphIndex}"),
                shape = RoundedCornerShape(18.dp),
                color = colors.elevatedSurface,
                border = BorderStroke(1.dp, colors.lineStrong),
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = sheetMaxHeight)
                        .padding(sheetContentPadding),
                    verticalArrangement = Arrangement.spacedBy(sheetGap),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (isKeyboardConstrained) 34.dp else 44.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DisplayText(
                            "Note",
                            fontSize = 19.sp,
                            lineHeight = 23.sp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader-annotation-title-${selection.paragraphIndex}"),
                        )
                        ReaderAnnotationRangeControls(
                            selection = selection,
                            onSelectionChanged = onSelectionChanged,
                        )
                        QaIconButton(
                            icon = QaIconKind.Close,
                            onClick = onCancel,
                            modifier = Modifier.testTag("reader-annotation-close-${selection.paragraphIndex}"),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = quoteMaxHeight)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.background.copy(alpha = 0.56f))
                            .verticalScroll(quoteScrollState)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("reader-annotation-selected-quote-scroll-${selection.paragraphIndex}"),
                    ) {
                        Text(
                            text = selection.quotedText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = colors.primaryText,
                            modifier = Modifier.testTag("reader-annotation-selected-quote-${selection.paragraphIndex}"),
                        )
                    }
                    QaMultilineTextField(
                        value = noteDraft,
                        onValueChange = onNoteDraftChanged,
                        placeholder = "Write a note",
                        maxHeight = noteMaxHeight,
                        modifier = Modifier.testTag("reader-annotation-note-input-${selection.paragraphIndex}"),
                    )
                    Row(
                        modifier = Modifier.heightIn(min = if (isKeyboardConstrained) 46.dp else 52.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (onDelete != null) {
                            QaButton(
                                text = "Delete note",
                                onClick = onDelete,
                                variant = QaButtonVariant.Ghost,
                                size = QaButtonSize.Small,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reader-annotation-delete-${selection.paragraphIndex}"),
                            )
                        }
                        QaButton(
                            text = "Cancel",
                            onClick = onCancel,
                            variant = QaButtonVariant.Ghost,
                            size = QaButtonSize.Small,
                            modifier = Modifier.weight(1f),
                        )
                        QaButton(
                            text = if (hasExistingAnnotation) "Update" else "Save",
                            onClick = onSave,
                            variant = QaButtonVariant.Primary,
                            size = QaButtonSize.Small,
                            enabled = noteDraft.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader-annotation-save-${selection.paragraphIndex}"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderAnnotationRangeControls(
    selection: ReaderAnnotationSelection,
    onSelectionChanged: (ReaderAnnotationSelection) -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = Modifier
            .semantics { contentDescription = "Annotation range controls" }
            .testTag("reader-annotation-range-controls"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReaderRangeIconButton(
            icon = QaIconKind.ArrowLeft,
            label = "Move start earlier",
            enabled = selection.canExpandStart,
            onClick = { onSelectionChanged(selection.expandStart()) },
            modifier = Modifier.testTag("reader-annotation-start-earlier"),
        )
        ReaderRangeIconButton(
            icon = QaIconKind.ArrowRight,
            label = "Move start later",
            enabled = selection.canShrinkStart,
            onClick = { onSelectionChanged(selection.shrinkStart()) },
            modifier = Modifier.testTag("reader-annotation-start-later"),
        )
        Box(
            modifier = Modifier
                .height(12.dp)
                .width(1.dp)
                .background(colors.line),
        )
        ReaderRangeIconButton(
            icon = QaIconKind.ArrowLeft,
            label = "Move end earlier",
            enabled = selection.canShrinkEnd,
            onClick = { onSelectionChanged(selection.shrinkEnd()) },
            modifier = Modifier.testTag("reader-annotation-end-earlier"),
        )
        ReaderRangeIconButton(
            icon = QaIconKind.ArrowRight,
            label = "Move end later",
            enabled = selection.canExpandEnd,
            onClick = { onSelectionChanged(selection.expandEnd()) },
            modifier = Modifier.testTag("reader-annotation-end-later"),
        )
    }
}

@Composable
private fun ReaderRangeIconButton(
    icon: QaIconKind,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val contentColor = if (enabled) colors.primaryText else colors.faintText
    Box(
        modifier = modifier
            .size(36.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .semantics { contentDescription = label }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = RoundedCornerShape(6.dp),
            color = colors.background.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, colors.line),
            contentColor = contentColor,
        ) {
            QaIcon(kind = icon, color = contentColor, size = 11.dp)
        }
    }
}

@Composable
private fun ReaderMarkdownBlockText(
    block: ReaderMarkdownBlock,
    highlightedText: String? = null,
    highlightRange: IntRange? = null,
    readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val colors = QualityAlternativeThemeTokens.colors
    val scale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    val displayText = remember(block.text, highlightedText, highlightRange) {
        block.text.withReaderHighlight(
            highlightedText = highlightedText,
            highlightRange = highlightRange,
            highlightColor = colors.accent.copy(alpha = 0.22f),
        )
    }
    val baseStyle = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = QualityDisplayFontFamily,
        fontSize = (17f * scale).sp,
        lineHeight = (27f * scale).sp,
    )
    val style = when (block.kind) {
        ReaderMarkdownBlockKind.HEADING -> baseStyle.copy(
            fontSize = (22f * scale).sp,
            lineHeight = (29f * scale).sp,
            fontWeight = FontWeight.SemiBold,
        )

        ReaderMarkdownBlockKind.CODE -> baseStyle.copy(
            fontFamily = QualityMonoFontFamily,
            fontSize = (14f * scale).sp,
            lineHeight = (22f * scale).sp,
        )

        ReaderMarkdownBlockKind.QUOTE -> baseStyle.copy(fontStyle = FontStyle.Italic)
        ReaderMarkdownBlockKind.BODY,
        ReaderMarkdownBlockKind.LIST,
        -> baseStyle
    }
    val textColor = when (block.kind) {
        ReaderMarkdownBlockKind.QUOTE -> colors.mutedText
        else -> colors.primaryText
    }
    val blockModifier = when (block.kind) {
        ReaderMarkdownBlockKind.CODE -> Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.elevatedSurface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 3.dp)

        else -> Modifier.padding(
            bottom = if (block.kind == ReaderMarkdownBlockKind.HEADING) {
                READER_HEADING_TEXT_BOTTOM_PADDING_DP.dp
            } else {
                READER_BODY_TEXT_BOTTOM_PADDING_DP.dp
            },
        )
    }

    ReaderTextDensity {
        Text(
            text = displayText,
            style = style,
            color = textColor,
            modifier = blockModifier.then(modifier),
            onTextLayout = onTextLayout,
        )
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
    onSelectMeditationDuration: (Int) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val content = state.currentContent ?: return
    val startedAtMillis = state.currentSessionStartedAtMillis ?: System.currentTimeMillis()
    val totalMillis = (content.durationMinutes * 60_000L).coerceAtLeast(1L)
    var nowMillis by remember(content.id, startedAtMillis) { mutableStateOf(System.currentTimeMillis()) }
    var hasPlayedGong by remember(content.id, startedAtMillis) { mutableStateOf(false) }
    val toneGenerator = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 85) }.getOrNull() }

    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator?.release() }
    }

    LaunchedEffect(content.id, startedAtMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val remainingMillis = (totalMillis - (nowMillis - startedAtMillis)).coerceAtLeast(0L)
    val remainingSeconds = ((remainingMillis + 999L) / 1_000L).toInt()
    val isComplete = remainingMillis == 0L

    LaunchedEffect(isComplete) {
        if (isComplete && !hasPlayedGong) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 900)
            hasPlayedGong = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("meditation-timer-screen")
            .padding(horizontal = 28.dp, vertical = 28.dp),
    ) {
        ScreenHead(onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 14.dp, bottom = 10.dp),
        ) {
            MonoText("Quiet reset", modifier = Modifier.padding(bottom = 10.dp))
            DisplayText(
                text = content.title,
                fontSize = 30.sp,
                lineHeight = 32.sp,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            BodyText(
                text = "Put the phone down if you can. Breathe out slowly. Let the urge pass before deciding what to do next.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            MeditationDurationChooser(
                selectedMinutes = content.durationMinutes,
                onSelect = onSelectMeditationDuration,
                label = "Length for this reset",
                helper = "Restarts the countdown and saves the default.",
                modifier = Modifier.padding(bottom = 12.dp),
                testTagPrefix = "timer-meditation-duration",
            )
            BodyText(
                text = "No feed. Just ${content.durationMinutes} minutes back.",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            QaCard(
                padding = 18.dp,
                background = QualityAlternativeThemeTokens.colors.accentSoft,
                borderColor = QualityAlternativeThemeTokens.colors.lineStrong,
                modifier = Modifier.testTag("meditation-timer-card"),
            ) {
                MonoText("Timer", modifier = Modifier.padding(bottom = 8.dp), color = QualityAlternativeThemeTokens.colors.accent)
                Text(
                    text = meditationTimeLabel(remainingSeconds),
                    style = TextStyle(
                        fontFamily = QualityDisplayFontFamily,
                        fontSize = 62.sp,
                        lineHeight = 64.sp,
                        color = QualityAlternativeThemeTokens.colors.primaryText,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meditation-countdown"),
                    textAlign = TextAlign.Center,
                )
                BodyText(
                    text = if (isComplete) {
                        "Reset complete. The gong marks the end - log it if it helped."
                    } else {
                        "A gong will sound when the timer ends."
                    },
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        }
        QaButton(
            text = if (isComplete) "Complete reset" else "Timer running",
            onClick = onComplete,
            enabled = isComplete,
            variant = QaButtonVariant.Primary,
            modifier = Modifier.testTag("meditation-complete"),
            size = QaButtonSize.Small,
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
private fun ProgressTab(
    snapshot: ProgressSnapshot,
    annotationCount: Int,
    onOpenAnnotations: () -> Unit,
) {
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
                    BodyText(
                        convertedDayNounLabel(snapshot.daysConverted),
                        color = colors.mutedText,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
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
                SmallStatRow("Current reading streak", dayCountLabel(snapshot.currentStreakDays, "day"))
                HorizontalDivider(color = colors.line)
                SmallStatRow("Completed reads", snapshot.completedReads.toString())
                HorizontalDivider(color = colors.line)
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
            QaCard(
                padding = 18.dp,
                modifier = Modifier.testTag("progress-annotations-card"),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SourceBadge(sourceType = ContentSourceType.EDITORIAL, icon = QaIconKind.Note)
                    Column(modifier = Modifier.weight(1f)) {
                        MonoText("ANNOTATIONS")
                        BodyText(
                            text = annotationCountLabel(annotationCount),
                            color = colors.primaryText,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    QaButton(
                        text = "Open",
                        onClick = onOpenAnnotations,
                        variant = QaButtonVariant.Outline,
                        size = QaButtonSize.Small,
                        fullWidth = false,
                        leadingIcon = QaIconKind.Note,
                        modifier = Modifier.testTag("progress-open-annotations"),
                    )
                }
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
private fun AnnotationLibraryScreen(
    state: MainUiState,
    onOpenAnnotation: (String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val contentById = remember(state.starterPacks, state.userLinks, state.userDocuments) {
        annotationContentIndex(state)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("annotation-library-list"),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DisplayText("Annotations", modifier = Modifier.weight(1f), fontSize = 26.sp)
                QaButton(
                    text = "Progress",
                    onClick = onBack,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    fullWidth = false,
                    leadingIcon = QaIconKind.History,
                    modifier = Modifier.testTag("annotation-library-back"),
                )
            }
        }
        if (state.readingAnnotations.isEmpty()) {
            item {
                QaCard(modifier = Modifier.testTag("annotation-library-empty")) {
                    MonoText("NO NOTES YET")
                    BodyText(
                        text = "Annotations saved in the reader appear here with their source fragment.",
                        color = colors.mutedText,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        } else {
            items(state.readingAnnotations, key = ReadingAnnotation::id) { annotation ->
                val content = contentById[annotation.contentId]
                AnnotationLibraryRow(
                    annotation = annotation,
                    content = content,
                    onOpen = { onOpenAnnotation(annotation.id) },
                )
            }
        }
    }
}

@Composable
private fun AnnotationLibraryRow(
    annotation: ReadingAnnotation,
    content: ContentItem?,
    onOpen: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val canOpen = content != null && content.usesRepositoryBody()
    val sourceTitle = content?.title
        ?: annotation.sourceTitle.takeIf(String::isNotBlank)
        ?: "Source no longer in Library"
    QaCard(
        padding = 16.dp,
        modifier = Modifier
            .clickable(enabled = canOpen, onClick = onOpen)
            .testTag("annotation-row-${annotation.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SourceBadge(
                sourceType = content?.sourceType ?: ContentSourceType.EDITORIAL,
                icon = content?.sourceType?.annotationSourceIcon() ?: QaIconKind.Close,
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = sourceTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 17.sp,
                        lineHeight = 21.sp,
                        color = colors.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MonoText(
                        text = "P${annotation.paragraphIndex + 1}",
                        color = colors.mutedText,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                MonoText(
                    text = buildString {
                        append((content?.sourceType ?: annotation.sourceType)?.annotationSourceTypeLabel() ?: "Missing source")
                        (content?.sourceLabel() ?: annotation.sourceLabel)?.let { sourceLabel ->
                            append(" · ")
                            append(sourceLabel)
                        }
                        append(" · ")
                        append(annotationUpdatedLabel(annotation.updatedAtMillis))
                    },
                    color = colors.mutedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    text = "\"${annotation.quotedText.trim()}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 19.sp),
                    color = colors.primaryText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag("annotation-quote-${annotation.id}"),
                )
                Text(
                    text = annotation.noteText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        color = colors.mutedText,
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("annotation-note-${annotation.id}"),
                )
                QaButton(
                    text = if (canOpen) "Open fragment" else "Source missing",
                    onClick = onOpen,
                    enabled = canOpen,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    fullWidth = false,
                    leadingIcon = if (canOpen) QaIconKind.Book else QaIconKind.Close,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag("annotation-open-${annotation.id}"),
                )
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: MainUiState,
    onToggleApp: (DistractingApp) -> Unit,
    onSelectDuration: (DurationBucket) -> Unit,
    onSelectMeditationDuration: (Int) -> Unit,
    onSelectReaderFontScale: (Double) -> Unit,
    onSelectInterfaceTextScale: (Double) -> Unit,
    onSelectContentPriority: (ContentPriority) -> Unit,
    onSelectOpenAnywayUnlock: (Int) -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
    onRefreshReadiness: () -> Unit,
    onSelectAnnotationExport: () -> Unit,
    onClearAnnotationExport: () -> Unit,
    onRetryAnnotationExport: () -> Unit,
    onConnectAnnotationDrive: () -> Unit,
    onRetryAnnotationDrive: () -> Unit,
    onDisconnectAnnotationDrive: () -> Unit,
    onExportAccountLightProfile: () -> Unit,
    onExportAccountLightBackup: () -> Unit,
    onImportAccountLightProfile: () -> Unit,
    onSelectAccountLightAutosave: () -> Unit,
    onRetryAccountLightAutosave: () -> Unit,
    onClearAccountLightAutosave: () -> Unit,
    onMergeAccountLightProfile: () -> Unit,
    onRequestAccountLightReplace: () -> Unit,
    onConfirmAccountLightReplace: () -> Unit,
    onCancelAccountLightImport: () -> Unit,
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
            Column(modifier = Modifier.testTag("settings-app-selection-section")) {
                SectionLabel("Apps to interrupt", right = "${quantityLabel(state.availableTargetApps.size, "app")} active")
                QaCard {
                    BodyText(
                        text = "Checked apps trigger the replacement prompt. The Home screen only chooses which checked app to preview.",
                        color = colors.mutedText,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.allSupportedApps.forEach { app ->
                            AppSelectionRow(
                                app = app,
                                selected = state.availableTargetApps.any { it.packageName == app.packageName },
                                onClick = { onToggleApp(app) },
                            )
                        }
                    }
                    BodyText(
                        text = "Keep at least 3 selected for the alpha.",
                        color = colors.mutedText,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
        item {
            SectionLabel("Content priority")
            QaCard {
                BodyText(
                    text = "Keep recommendations balanced, or gently prioritize one type.",
                    color = colors.mutedText,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentPriority.entries.forEach { priority ->
                        QaChip(
                            text = priority.displayLabel(),
                            selected = state.contentPriority == priority,
                            onClick = { onSelectContentPriority(priority) },
                            modifier = Modifier.testTag("content-priority-${priority.name}"),
                            minHeight = 32.dp,
                            horizontalPadding = 12.dp,
                            verticalPadding = 7.dp,
                            fontSize = 11.5.sp,
                        )
                    }
                }
                BodyText(
                    text = state.contentPriority.displayDescription(),
                    color = colors.mutedText,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
                BodyText(
                    text = "${state.priorityContentIds.size} individual priority ${if (state.priorityContentIds.size == 1) "pick" else "picks"} selected in Library.",
                    color = colors.mutedText,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        item {
            SectionLabel("Text size")
            QaCard {
                TextScaleStepper(
                    title = "Reading text",
                    value = state.readerFontScale,
                    min = MIN_READER_FONT_SCALE,
                    max = MAX_READER_FONT_SCALE,
                    onChange = onSelectReaderFontScale,
                    containerTag = "reader-font-scale-control",
                    decreaseTag = "reader-font-scale-decrease",
                    increaseTag = "reader-font-scale-increase",
                    valueTag = "reader-font-scale-value",
                ) {
                    ReaderTextPreview(readerFontScale = state.readerFontScale)
                }
                HorizontalDivider(
                    color = colors.line,
                    modifier = Modifier.padding(vertical = 14.dp),
                )
                TextScaleStepper(
                    title = "Interface text",
                    value = state.interfaceTextScale,
                    min = MIN_INTERFACE_TEXT_SCALE,
                    max = MAX_INTERFACE_TEXT_SCALE,
                    onChange = onSelectInterfaceTextScale,
                    containerTag = "interface-text-scale-control",
                    decreaseTag = "interface-text-scale-decrease",
                    increaseTag = "interface-text-scale-increase",
                    valueTag = "interface-text-scale-value",
                ) {
                    InterfaceTextPreview()
                }
            }
        }
        item {
            AnnotationAutosaveSettingsSection(
                state = state,
                onSelectAnnotationExport = onSelectAnnotationExport,
                onClearAnnotationExport = onClearAnnotationExport,
                onRetryAnnotationExport = onRetryAnnotationExport,
                onConnectAnnotationDrive = onConnectAnnotationDrive,
                onRetryAnnotationDrive = onRetryAnnotationDrive,
                onDisconnectAnnotationDrive = onDisconnectAnnotationDrive,
            )
        }
        item {
            AccountLightSettingsSection(
                state = state,
                onExport = onExportAccountLightProfile,
                onExportBackup = onExportAccountLightBackup,
                onImport = onImportAccountLightProfile,
                onSelectAutosave = onSelectAccountLightAutosave,
                onRetryAutosave = onRetryAccountLightAutosave,
                onClearAutosave = onClearAccountLightAutosave,
                onMerge = onMergeAccountLightProfile,
                onRequestReplace = onRequestAccountLightReplace,
                onConfirmReplace = onConfirmAccountLightReplace,
                onCancel = onCancelAccountLightImport,
            )
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
            SectionLabel("Open anyway unlock")
            QaCard {
                BodyText(
                    text = "After opening the original app, repeated opens stay quiet for the selected time.",
                    color = colors.mutedText,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpenAnywayUnlockMinuteOptions.forEach { minutes ->
                        QaChip(
                            text = if (minutes >= 60) "${minutes / 60} hr" else "$minutes min",
                            selected = state.openAnywayUnlockMinutes == minutes,
                            onClick = { onSelectOpenAnywayUnlock(minutes) },
                            modifier = Modifier.testTag("open-anyway-unlock-$minutes"),
                            centered = true,
                            minHeight = 34.dp,
                            horizontalPadding = 12.dp,
                            verticalPadding = 7.dp,
                        )
                    }
                }
            }
        }
        item {
            SectionLabel("Meditation reset")
            QaCard {
                BodyText(
                    text = "Set the default length for the breathing timer. You can still change it right before starting.",
                    color = colors.mutedText,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                MeditationDurationChooser(
                    selectedMinutes = state.meditationDurationMinutes,
                    onSelect = onSelectMeditationDuration,
                    testTagPrefix = "meditation-duration",
                )
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
                text = "Quality Alternative - v${BuildConfig.VERSION_NAME}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
                preserveCase = true,
            )
        }
    }
}

@Composable
private fun AccountLightSettingsSection(
    state: MainUiState,
    onExport: () -> Unit,
    onExportBackup: () -> Unit,
    onImport: () -> Unit,
    onSelectAutosave: () -> Unit,
    onRetryAutosave: () -> Unit,
    onClearAutosave: () -> Unit,
    onMerge: () -> Unit,
    onRequestReplace: () -> Unit,
    onConfirmReplace: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val preview = state.accountLightImportPreview
    Column(modifier = Modifier.testTag("settings-account-light-section")) {
        SectionLabel("Portable profile", right = if (preview != null) "Import ready" else "Local")
        QaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SourceBadge(sourceType = ContentSourceType.USER_DOCUMENT, icon = QaIconKind.Shield)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Export / import",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = colors.primaryText,
                        fontWeight = FontWeight.SemiBold,
                    )
                    MonoText(
                        text = state.accountLightStatus
                            ?: state.accountLightImportError
                            ?: "Preferences, saved links, document titles, reading progress, and safe sync settings.",
                        color = if (state.accountLightImportError == null) colors.mutedText else colors.accent,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .testTag("settings-account-light-status"),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QaButton(
                    text = if (state.isAccountLightExporting) "Exporting" else "Export",
                    onClick = onExport,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    enabled = !state.isAccountLightExporting,
                    leadingIcon = QaIconKind.External,
                    modifier = Modifier.weight(1f).testTag("settings-account-light-export"),
                )
                QaButton(
                    text = "Import",
                    onClick = onImport,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    leadingIcon = QaIconKind.External,
                    modifier = Modifier.weight(1f).testTag("settings-account-light-import"),
                )
            }
            HorizontalDivider(
                color = colors.line,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            AccountLightAutosaveControls(
                state = state,
                onSelectAutosave = onSelectAutosave,
                onRetryAutosave = onRetryAutosave,
                onClearAutosave = onClearAutosave,
            )
            if (preview != null) {
                AccountLightImportPreviewCard(
                    state = state,
                    onMerge = onMerge,
                    onRequestReplace = onRequestReplace,
                    onExportBackup = onExportBackup,
                    onConfirmReplace = onConfirmReplace,
                    onCancel = onCancel,
                )
            }
        }
    }
}

@Composable
private fun AccountLightAutosaveControls(
    state: MainUiState,
    onSelectAutosave: () -> Unit,
    onRetryAutosave: () -> Unit,
    onClearAutosave: () -> Unit,
) {
    val configured = !state.profileAutosaveUri.isNullOrBlank()
    val usesLocalDefault = state.profileAutosaveUsesLocalDefault
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BodyText(
                    text = state.profileAutosaveDisplayName ?: "No profile backup destination selected",
                    color = QualityAlternativeThemeTokens.colors.primaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                MonoText(
                    text = profileAutosaveStatusText(state),
                    color = if (state.profileAutosaveLastError == null) {
                        QualityAlternativeThemeTokens.colors.mutedText
                    } else {
                        QualityAlternativeThemeTokens.colors.accent
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("settings-account-light-autosave-status"),
                )
            }
            if (state.isProfileAutosaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp).testTag("settings-account-light-autosave-progress"),
                    strokeWidth = 2.dp,
                    color = QualityAlternativeThemeTokens.colors.accent,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QaButton(
                text = if (configured) "Change destination" else "Choose destination",
                onClick = onSelectAutosave,
                variant = QaButtonVariant.Outline,
                size = QaButtonSize.Small,
                enabled = !state.isProfileAutosaving,
                leadingIcon = QaIconKind.External,
                modifier = Modifier.weight(1f).testTag("settings-account-light-autosave-pick"),
            )
            QaButton(
                text = if (state.profileAutosaveLastError == null) "Save now" else "Retry",
                onClick = onRetryAutosave,
                variant = QaButtonVariant.Ghost,
                size = QaButtonSize.Small,
                enabled = configured && !state.isProfileAutosaving,
                fullWidth = false,
                modifier = Modifier.testTag(
                    if (state.profileAutosaveLastError == null) {
                        "settings-account-light-autosave-save-now"
                    } else {
                        "settings-account-light-autosave-retry"
                    },
                ),
            )
        }
        if (configured && !usesLocalDefault) {
            QaButton(
                text = "Use app storage",
                onClick = onClearAutosave,
                variant = QaButtonVariant.Ghost,
                size = QaButtonSize.Small,
                enabled = !state.isProfileAutosaving,
                modifier = Modifier.testTag("settings-account-light-autosave-clear"),
            )
        }
    }
}

@Composable
private fun AccountLightImportPreviewCard(
    state: MainUiState,
    onMerge: () -> Unit,
    onRequestReplace: () -> Unit,
    onExportBackup: () -> Unit,
    onConfirmReplace: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val preview = state.accountLightImportPreview ?: return
    Column(
        modifier = Modifier
            .padding(top = 14.dp)
            .border(BorderStroke(1.dp, colors.lineStrong), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("settings-account-light-import-preview"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BodyText(
            text = "Profile ${preview.profileId.takeLast(8)} · ${preview.importedLinkCount} links · ${preview.importedDocumentCount} documents · ${preview.importedProgressCount} progress",
            color = colors.primaryText,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        if (preview.importedAppCount > 0 || preview.importedTopicCount > 0 || preview.importedPackCount > 0) {
            MonoText(
                text = "${preview.importedAppCount} apps · ${preview.importedTopicCount} topics · ${preview.importedPackCount} packs",
                color = colors.mutedText,
            )
        }
        if (preview.unsupportedAppCount > 0 || preview.missingDocumentCount > 0 || preview.warningCount > 0) {
            MonoText(
                text = "${preview.unsupportedAppCount} unsupported apps · ${preview.missingDocumentCount} documents need reattach · ${preview.warningCount} warnings",
                color = colors.mutedText,
                modifier = Modifier.testTag("settings-account-light-import-warning-summary"),
            )
            preview.warningSummaries.take(3).forEach { summary ->
                BodyText(
                    text = summary,
                    color = colors.mutedText,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                )
            }
        }
        if (state.isAccountLightReplaceConfirming) {
            QaCard(
                modifier = Modifier.testTag("settings-account-light-replace-confirm"),
                padding = 12.dp,
                background = colors.background,
            ) {
                BodyText(
                    text = "Export a backup first if needed. Replace local portable data; device permissions stay local.",
                    color = colors.mutedText,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                QaButton(
                    text = "Export backup",
                    onClick = onExportBackup,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    leadingIcon = QaIconKind.External,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("settings-account-light-replace-backup"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QaButton(
                        text = "Cancel",
                        onClick = onCancel,
                        variant = QaButtonVariant.Ghost,
                        size = QaButtonSize.Small,
                        modifier = Modifier.weight(1f),
                    )
                    QaButton(
                        text = if (state.isAccountLightImporting) "Replacing" else "Replace",
                        onClick = onConfirmReplace,
                        variant = QaButtonVariant.Primary,
                        size = QaButtonSize.Small,
                        enabled = !state.isAccountLightImporting,
                        modifier = Modifier.weight(1f).testTag("settings-account-light-replace-confirm-action"),
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QaButton(
                    text = "Merge",
                    onClick = onMerge,
                    variant = QaButtonVariant.Primary,
                    size = QaButtonSize.Small,
                    enabled = !state.isAccountLightImporting,
                    modifier = Modifier.weight(1f).testTag("settings-account-light-import-merge"),
                )
                QaButton(
                    text = "Replace",
                    onClick = onRequestReplace,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    enabled = !state.isAccountLightImporting,
                    modifier = Modifier.weight(1f).testTag("settings-account-light-import-replace"),
                )
            }
        }
    }
}

private const val TEXT_SCALE_STEP = 0.05

@Composable
private fun TextScaleStepper(
    title: String,
    value: Double,
    min: Double,
    max: Double,
    onChange: (Double) -> Unit,
    containerTag: String,
    decreaseTag: String,
    increaseTag: String,
    valueTag: String,
    preview: @Composable () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val safeValue = value.coerceIn(min, max).toPortableTextScale()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(containerTag),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BodyText(text = title, fontSize = 15.sp, lineHeight = 20.sp)
                MonoText(
                    text = safeValue.toPercentLabel(),
                    color = colors.mutedText,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag(valueTag),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextScaleIconButton(
                    icon = QaIconKind.Minus,
                    label = "Decrease $title",
                    enabled = safeValue > min,
                    onClick = { onChange((safeValue - TEXT_SCALE_STEP).coerceIn(min, max).toPortableTextScale()) },
                    modifier = Modifier.testTag(decreaseTag),
                )
                TextScaleIconButton(
                    icon = QaIconKind.Plus,
                    label = "Increase $title",
                    enabled = safeValue < max,
                    onClick = { onChange((safeValue + TEXT_SCALE_STEP).coerceIn(min, max).toPortableTextScale()) },
                    modifier = Modifier.testTag(increaseTag),
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(10.dp),
            color = colors.background,
            border = BorderStroke(1.dp, colors.line),
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                preview()
            }
        }
    }
}

@Composable
private fun TextScaleIconButton(
    icon: QaIconKind,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val contentColor = if (enabled) colors.primaryText else colors.faintText
    Surface(
        modifier = modifier
            .size(36.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .semantics { contentDescription = label },
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = colors.elevatedSurface,
        contentColor = contentColor,
        border = BorderStroke(1.dp, colors.lineStrong),
    ) {
        Box(contentAlignment = Alignment.Center) {
            QaIcon(kind = icon, color = contentColor, size = 16.dp)
        }
    }
}

@Composable
private fun ReaderTextPreview(readerFontScale: Double) {
    val colors = QualityAlternativeThemeTokens.colors
    val scale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    ReaderTextDensity {
        Text(
            text = "Reader preview text",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = QualityDisplayFontFamily,
                fontSize = (17f * scale).sp,
                lineHeight = (24f * scale).sp,
            ),
            color = colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("reader-font-scale-preview"),
        )
    }
}

@Composable
private fun InterfaceTextPreview() {
    BodyText(
        text = "Interface preview text",
        color = QualityAlternativeThemeTokens.colors.mutedText,
        modifier = Modifier.testTag("interface-text-scale-preview"),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun Double.toPortableTextScale(): Double {
    return (this * 100.0).roundToInt() / 100.0
}

private fun Double.toPercentLabel(): String {
    return "${(this * 100.0).roundToInt()}%"
}

@Composable
private fun AnnotationAutosaveSettingsSection(
    state: MainUiState,
    onSelectAnnotationExport: () -> Unit,
    onClearAnnotationExport: () -> Unit,
    onRetryAnnotationExport: () -> Unit,
    onConnectAnnotationDrive: () -> Unit,
    onRetryAnnotationDrive: () -> Unit,
    onDisconnectAnnotationDrive: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val configured = !state.annotationExportUri.isNullOrBlank()
    val usesLocalDefault = state.annotationExportUsesLocalDefault
    val exportActionLabel = if (state.annotationExportLastError == null) "Save now" else "Retry"
    val driveProviderConfigured = annotationExportUsesGoogleDriveProvider(state.annotationExportUri)
    val driveConfigured = state.annotationDriveSyncEnabled || driveProviderConfigured
    val driveActionLabel = when {
        state.isAnnotationDriveSyncing -> "Syncing"
        !driveConfigured -> "Connect"
        state.annotationDriveLastError == null -> "Save now"
        else -> "Retry"
    }
    Column(modifier = Modifier.testTag("settings-annotation-export-section")) {
        SectionLabel(
            "Annotation sync",
            right = when {
                driveConfigured -> "Drive"
                usesLocalDefault -> "Local"
                configured -> "Folder"
                else -> "Off"
            },
        )
        QaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SourceBadge(sourceType = ContentSourceType.USER_DOCUMENT, icon = QaIconKind.Note)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.annotationExportDisplayName ?: "No annotation sync destination selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = colors.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MonoText(
                        text = annotationExportStatusText(state),
                        color = if (state.annotationExportLastError == null) colors.mutedText else colors.accent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .testTag("settings-annotation-export-status"),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QaButton(
                    text = if (configured) "Change destination" else "Choose destination",
                    onClick = onSelectAnnotationExport,
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    leadingIcon = QaIconKind.External,
                    modifier = Modifier.weight(1f).testTag("settings-annotation-export-pick"),
                )
                QaButton(
                    text = exportActionLabel,
                    onClick = onRetryAnnotationExport,
                    variant = QaButtonVariant.Ghost,
                    size = QaButtonSize.Small,
                    enabled = configured,
                    fullWidth = false,
                    modifier = Modifier.testTag(
                        if (state.annotationExportLastError == null) {
                            "settings-annotation-export-save-now"
                        } else {
                            "settings-annotation-export-retry"
                        },
                    ),
                )
            }
            if (configured && !usesLocalDefault) {
                QaButton(
                    text = "Use app storage",
                    onClick = onClearAnnotationExport,
                    variant = QaButtonVariant.Ghost,
                    size = QaButtonSize.Small,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("settings-annotation-export-clear"),
                )
            }
        }
        QaCard(modifier = Modifier.padding(top = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SourceBadge(sourceType = ContentSourceType.USER_LINK, icon = QaIconKind.External)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            state.annotationDriveSyncEnabled -> "Google Drive connected"
                            driveProviderConfigured -> "Google Drive folder connected"
                            else -> "Google Drive not connected"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = colors.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MonoText(
                        text = annotationDriveStatusText(state),
                        color = if (state.annotationDriveLastError == null) colors.mutedText else colors.accent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .testTag("settings-annotation-drive-status"),
                    )
                }
                if (state.isAnnotationDriveSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp).testTag("settings-annotation-drive-progress"),
                        strokeWidth = 2.dp,
                        color = colors.accent,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QaButton(
                    text = driveActionLabel,
                    onClick = when {
                        state.annotationDriveSyncEnabled -> onRetryAnnotationDrive
                        driveProviderConfigured -> onRetryAnnotationExport
                        else -> onConnectAnnotationDrive
                    },
                    variant = QaButtonVariant.Outline,
                    size = QaButtonSize.Small,
                    enabled = !state.isAnnotationDriveSyncing,
                    leadingIcon = QaIconKind.External,
                    modifier = Modifier.weight(1f).testTag(
                        if (driveConfigured) {
                            "settings-annotation-drive-sync-now"
                        } else {
                            "settings-annotation-drive-connect"
                        },
                    ),
                )
                QaButton(
                    text = "Disconnect",
                    onClick = if (driveProviderConfigured && !state.annotationDriveSyncEnabled) {
                        onClearAnnotationExport
                    } else {
                        onDisconnectAnnotationDrive
                    },
                    variant = QaButtonVariant.Ghost,
                    size = QaButtonSize.Small,
                    enabled = driveConfigured && !state.isAnnotationDriveSyncing,
                    fullWidth = false,
                    modifier = Modifier.testTag("settings-annotation-drive-disconnect"),
                )
            }
        }
    }
}

@Composable
private fun MeditationDurationChooser(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    helper: String? = null,
    testTagPrefix: String = "meditation-duration",
) {
    Column(modifier = modifier) {
        label?.let {
            MonoText(text = it, modifier = Modifier.padding(bottom = 6.dp))
        }
        helper?.let {
            BodyText(
                text = it,
                color = QualityAlternativeThemeTokens.colors.mutedText,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 5, 10).forEach { minutes ->
                QaChip(
                    text = "${minutes}m",
                    selected = selectedMinutes == minutes,
                    onClick = { onSelect(minutes) },
                    modifier = Modifier.weight(1f).testTag("$testTagPrefix-$minutes"),
                    centered = true,
                )
            }
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
            .testTag("settings-app-${app.packageName}")
            .semantics { this.selected = selected }
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
private fun ContinueReadingCard(
    item: ContentItem,
    progress: ReadingProgress,
    onContinue: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    QaCard(
        background = colors.accentSoft,
        borderColor = colors.accent,
        padding = 16.dp,
        modifier = Modifier.testTag("home-continue-card"),
    ) {
        MonoText("Continue reading", color = colors.accent, modifier = Modifier.padding(bottom = 6.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            lineHeight = 21.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        ProgressLine(progress = progress.progressPercent)
        BodyText(
            text = "${progress.progressPercent}% read · ${remainingMinutes(item.durationMinutes, progress.progressPercent)} min left",
            color = colors.mutedText,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        QaButton(
            text = "Continue",
            onClick = onContinue,
            variant = QaButtonVariant.Accent,
            size = QaButtonSize.Small,
            fullWidth = false,
            leadingIcon = QaIconKind.Book,
            modifier = Modifier.testTag("home-continue-action"),
        )
    }
}

@Composable
private fun ReadNowCard(
    totalItems: Int,
    totalMinutes: Int,
    onOpenLibrary: () -> Unit,
) {
    val colors = QualityAlternativeThemeTokens.colors
    QaCard(
        padding = 16.dp,
        modifier = Modifier.testTag("home-read-now-card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SourceBadge(sourceType = ContentSourceType.EDITORIAL, icon = QaIconKind.Book)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Read now",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                )
                MonoText("${quantityLabel(totalItems, "item")} · $totalMinutes min", modifier = Modifier.padding(top = 4.dp))
            }
            QaButton(
                text = "Library",
                onClick = onOpenLibrary,
                variant = QaButtonVariant.Outline,
                size = QaButtonSize.Small,
                fullWidth = false,
                leadingIcon = QaIconKind.Library,
                modifier = Modifier.testTag("home-read-now-action"),
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
private fun LibraryItemCard(
    item: ContentItem,
    progress: ReadingProgress?,
    prioritized: Boolean,
    completed: Boolean,
    reactivated: Boolean,
    isManaging: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onTogglePriority: () -> Unit,
    onToggleCompletedActivation: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    val canDelete = item.isUserContent()
    val canOpen = item.availability != ContentAvailability.UNAVAILABLE
    QaCard(
        padding = 16.dp,
        modifier = Modifier.testTag("library-item-${item.id}"),
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
        if (progress != null) {
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag("library-unfinished-${item.id}"),
            ) {
                ProgressLine(progress = progress.progressPercent)
                BodyText(
                    text = "${progress.progressPercent}% read · ${remainingMinutes(item.durationMinutes, progress.progressPercent)} min left",
                    color = QualityAlternativeThemeTokens.colors.mutedText,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        if (completed) {
            BodyText(
                text = if (reactivated) {
                    "Completed · active in suggestions"
                } else {
                    "Completed · hidden from suggestions"
                },
                color = if (reactivated) {
                    QualityAlternativeThemeTokens.colors.success
                } else {
                    QualityAlternativeThemeTokens.colors.mutedText
                },
                fontSize = 12.5.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag("library-completed-status-${item.id}"),
            )
        }
        if (isManaging && !canDelete) {
            BodyText(
                text = "Starter pack · not deletable",
                color = QualityAlternativeThemeTokens.colors.mutedText,
                fontSize = 12.5.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .testTag("library-editorial-note-${item.id}"),
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isManaging) {
                if (canDelete) {
                    QaChip(
                        text = if (selected) "Selected" else "Select",
                        selected = selected,
                        accentSelected = true,
                        onClick = onToggleSelection,
                        modifier = Modifier
                            .testTag("library-select-${item.id}"),
                        centered = true,
                        minHeight = 34.dp,
                        horizontalPadding = 8.dp,
                        verticalPadding = 7.dp,
                    )
                } else {
                    ReadOnlyPill(
                        text = "Locked",
                        modifier = Modifier
                            .testTag("library-editorial-locked-${item.id}"),
                    )
                }
            }
            if (completed) {
                QaChip(
                    text = if (reactivated) "Deactivate" else "Reactivate",
                    selected = reactivated,
                    accentSelected = true,
                    onClick = onToggleCompletedActivation,
                    modifier = Modifier.testTag("completed-activation-${item.id}"),
                    centered = true,
                    minHeight = 34.dp,
                    horizontalPadding = 10.dp,
                    verticalPadding = 7.dp,
                )
            }
            QaChip(
                text = if (prioritized) "Priority" else "Prioritize",
                selected = prioritized,
                accentSelected = true,
                onClick = onTogglePriority,
                modifier = Modifier.testTag("priority-content-${item.id}"),
                centered = true,
                minHeight = 34.dp,
                horizontalPadding = 8.dp,
                verticalPadding = 7.dp,
            )
            if (canOpen) {
                QaChip(
                    text = if (progress != null) "Continue" else "Open",
                    selected = false,
                    onClick = onOpen,
                    modifier = Modifier.testTag("library-open-${item.id}"),
                    centered = true,
                    minHeight = 34.dp,
                    horizontalPadding = 8.dp,
                    verticalPadding = 7.dp,
                )
            } else {
                ReadOnlyPill(
                    text = "File missing",
                    modifier = Modifier.testTag("library-unavailable-${item.id}"),
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyPill(text: String, modifier: Modifier = Modifier) {
    val colors = QualityAlternativeThemeTokens.colors
    Box(
        modifier = modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(999.dp))
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(999.dp))
            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 14.sp),
            color = colors.mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
    continueProgress: ContinueProgressMeta? = null,
    progressTestTag: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val accessibilityLabel = if (continueProgress != null) {
        "${item.title}, ${continueProgress.label}"
    } else {
        "${item.title}, ${item.durationMinutes} min"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(14.dp))
            .background(colors.elevatedSurface)
            .semantics { contentDescription = accessibilityLabel }
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            QaIcon(kind = primaryActionIcon(item), color = colors.accent, size = 13.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            if (continueProgress != null) {
                MonoText(
                    text = "Continue",
                    color = colors.accent,
                    modifier = Modifier.padding(bottom = 1.dp),
                    maxLines = 1,
                    preserveCase = true,
                )
                MonoText(
                    text = continueProgress.label,
                    color = colors.success,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .then(progressTestTag?.let { Modifier.testTag(it) } ?: Modifier),
                    maxLines = 2,
                    preserveCase = true,
                )
            } else {
                MonoText(
                    text = "Try instead · ${item.durationMinutes} min",
                    color = colors.accent,
                    modifier = Modifier.padding(bottom = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MonoText(
                "${item.sourceLabel()} · ${item.topicLine()}",
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        QaIcon(kind = QaIconKind.ChevronRight, color = colors.faintText, size = 16.dp)
    }
}

private data class ContinueProgressMeta(
    val progressPercent: Int,
    val remainingMinutes: Int,
) {
    val label: String = "$progressPercent% read · $remainingMinutes min left"
}

@Composable
private fun ContinueProgressMetaLine(
    progress: ContinueProgressMeta,
    modifier: Modifier = Modifier,
) {
    val colors = QualityAlternativeThemeTokens.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.successSoft)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        QaIcon(kind = QaIconKind.Clock, color = colors.success, size = 14.dp)
        MonoText(
            text = progress.label,
            color = colors.success,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            preserveCase = true,
        )
    }
}

private fun continueProgressMetaFor(
    item: ContentItem,
    progress: List<ReadingProgress>,
): ContinueProgressMeta? {
    val unfinishedProgress = progress.firstOrNull { candidate ->
        candidate.contentId == item.id && candidate.isUnfinished()
    } ?: return null
    return ContinueProgressMeta(
        progressPercent = unfinishedProgress.progressPercent,
        remainingMinutes = item.remainingMinutesAfter(unfinishedProgress.progressPercent),
    )
}

private fun ContentItem.remainingMinutesAfter(progressPercent: Int): Int {
    val remainingPercent = (100 - progressPercent.coerceIn(1, 99)).coerceAtLeast(1)
    return ((durationMinutes * remainingPercent) + 99) / 100
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
            MonoText(recentReplacementDurationLabel(entry))
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

            QaIconKind.Minus -> line(5f, 12f, 19f, 12f)

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

            QaIconKind.Note -> {
                path {
                    moveTo(x(5f), y(4f))
                    lineTo(x(17f), y(4f))
                    lineTo(x(20f), y(7f))
                    lineTo(x(20f), y(20f))
                    lineTo(x(5f), y(20f))
                    close()
                }
                line(8f, 9f, 16f, 9f)
                line(8f, 13f, 16f, 13f)
                line(8f, 17f, 13f, 17f)
            }

            QaIconKind.Dot -> drawCircle(color = color, radius = x(2f), center = p(12f, 12f))
        }
    }
}

@Composable
private fun QaIconButton(icon: QaIconKind, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(34.dp),
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
private fun QaMultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    maxHeight: Dp? = null,
) {
    val colors = QualityAlternativeThemeTokens.colors
    val defaultMinHeight = 92.dp
    val horizontalPadding = 13.dp
    val verticalPadding = 12.dp
    val fieldMinHeight = maxHeight
        ?.let { height -> minOf(defaultMinHeight, height) }
        ?: defaultMinHeight
    val innerMinHeight = (fieldMinHeight - (verticalPadding * 2))
        .coerceAtLeast(48.dp)
    val fieldHeightModifier = maxHeight
        ?.let { height -> Modifier.heightIn(min = fieldMinHeight, max = height) }
        ?: Modifier.heightIn(min = defaultMinHeight)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = colors.primaryText,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(fieldHeightModifier)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.background)
            .border(
                BorderStroke(1.dp, if (isError) colors.accent else colors.lineStrong),
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = innerMinHeight),
                contentAlignment = Alignment.TopStart,
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
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
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
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
        maxLines = maxLines,
        overflow = overflow,
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
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize, lineHeight = lineHeight, color = color),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
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
    preserveCase: Boolean = false,
) {
    Text(
        text = if (preserveCase) text else text.uppercase(Locale.US),
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
    val currentStreakDays: Int,
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
    val completedReadingEntries = entries.filter(ReplacementHistoryEntry::isCompletedReadingReplacement)
    val completedDays = completedReadingEntries
        .map { entry ->
            Instant.ofEpochMilli(entry.completedAtMillis ?: entry.acceptedAtMillis)
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
        currentStreakDays = currentReadingStreakDays(completedDays = completedDays, today = today),
        dayBars = dayBars,
        interventionsShown = events.distinctProgressEventCount(AnalyticsEventType.INTERVENTION_SHOWN),
        alternativesChosen = entries.size,
        delayedOpens = events.distinctProgressEventCount(AnalyticsEventType.DELAY_SELECTED),
        consciousOverrides = events.distinctProgressEventCount(AnalyticsEventType.OPEN_ANYWAY_SELECTED),
        completedReads = completedReadingEntries.size,
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

private fun ReplacementHistoryEntry.isCompletedReadingReplacement(): Boolean {
    return isCompleted() && contentId != MEDITATION_TIMER_CONTENT_ID
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
            title = "Finish setup to intercept distracting apps.",
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

internal enum class ReaderMarkdownBlockKind {
    BODY,
    HEADING,
    QUOTE,
    LIST,
    CODE,
}

internal data class ReaderMarkdownBlock(
    val text: AnnotatedString,
    val kind: ReaderMarkdownBlockKind,
    val sourceHref: String? = null,
    val sourceAnchor: String? = null,
    val sourceBlockIndex: Int = 0,
    val sourceTextStartOffset: Int = 0,
    val sourceFullText: String? = null,
)

internal data class ReaderBlockLayout(
    val blocks: List<ReaderMarkdownBlock>,
    val originalToDisplayStart: List<Int>,
) {
    fun displayBlockIndexFor(originalBlockIndex: Int): Int {
        return originalToDisplayStart.getOrElse(originalBlockIndex) {
            blocks.lastIndex.coerceAtLeast(0)
        }
    }

    fun displayBlockIndexForSelector(selector: ReadingAnnotationSelector): Int {
        if (selector.endSourceBlockIndex <= selector.sourceBlockIndex && selector.textEndOffset <= selector.textStartOffset) {
            return displayBlockIndexFor(selector.sourceBlockIndex)
        }
        return blocks.indexOfFirst { block -> selector.overlapsReaderBlock(block) }
            .takeIf { index -> index >= 0 }
            ?: displayBlockIndexFor(selector.sourceBlockIndex)
    }

    fun sourceBlockIndexForDisplayBlock(displayBlockIndex: Int): Int {
        return blocks
            .getOrNull(displayBlockIndex.coerceIn(0, blocks.lastIndex.coerceAtLeast(0)))
            ?.sourceBlockIndex
            ?: 0
    }

    fun sourcePositionForDisplayBlock(displayBlockIndex: Int): ReaderSourcePosition {
        val block = blocks.getOrNull(displayBlockIndex.coerceIn(0, blocks.lastIndex.coerceAtLeast(0)))
            ?: return ReaderSourcePosition(sourceBlockIndex = 0, textOffset = 0)
        return ReaderSourcePosition(
            sourceBlockIndex = block.sourceBlockIndex,
            textOffset = block.sourceTextEndOffset(),
        )
    }

    fun displayBlockIndexForSourcePosition(sourceBlockIndex: Int, textOffset: Int): Int {
        val safeOffset = textOffset.coerceAtLeast(0)
        return blocks.indexOfFirst { block ->
            block.sourceBlockIndex == sourceBlockIndex &&
                safeOffset >= block.sourceTextStartOffset &&
                safeOffset <= block.sourceTextEndOffset()
        }.takeIf { index -> index >= 0 }
            ?: displayBlockIndexFor(sourceBlockIndex)
    }
}

internal data class ReaderSourcePosition(
    val sourceBlockIndex: Int,
    val textOffset: Int,
)

private data class ReaderAnnotatedChunk(
    val text: AnnotatedString,
    val sourceStartOffset: Int,
)

internal data class ReaderSentenceRange(
    val start: Int,
    val endExclusive: Int,
    val sourceBlockIndex: Int = 0,
    val sourceHref: String? = null,
    val sourceAnchor: String? = null,
    val sourceTextStartOffset: Int = start,
    val sourceTextEndOffset: Int = endExclusive,
)

internal enum class ReaderAnnotationSelectionFocus {
    START,
    END,
}

internal data class ReaderAnnotationSelection(
    val paragraphIndex: Int,
    val sourceText: String,
    val sourceHref: String?,
    val sourceAnchor: String?,
    val sourceBlockIndex: Int,
    val sourceTextStartOffset: Int,
    val sentenceRanges: List<ReaderSentenceRange>,
    val startSentenceIndex: Int,
    val endSentenceIndex: Int,
    val existingAnnotationId: String?,
) {
    val quotedText: String
        get() {
            val start = sentenceRanges[startSentenceIndex].start
            val end = sentenceRanges[endSentenceIndex].endExclusive
            return sourceText.substring(start, end).trim()
        }

    val selector: ReadingAnnotationSelector
        get() {
            val startRange = sentenceRanges[startSentenceIndex]
            val endRange = sentenceRanges[endSentenceIndex]
            val start = startRange.start
            val end = endRange.endExclusive
            return ReadingAnnotationSelector(
                sourceHref = startRange.sourceHref ?: sourceHref,
                sourceAnchor = startRange.sourceAnchor ?: sourceAnchor,
                sourceBlockIndex = startRange.sourceBlockIndex,
                endSourceBlockIndex = endRange.sourceBlockIndex,
                textStartOffset = startRange.sourceTextStartOffset,
                textEndOffset = endRange.sourceTextEndOffset,
                prefixText = sourceText.substring(0, start).takeLast(120).trim(),
                suffixText = sourceText.substring(end).take(120).trim(),
            )
        }

    val canExpandStart: Boolean
        get() = startSentenceIndex > 0

    val canShrinkStart: Boolean
        get() = startSentenceIndex < endSentenceIndex

    val canShrinkEnd: Boolean
        get() = endSentenceIndex > startSentenceIndex

    val canExpandEnd: Boolean
        get() = endSentenceIndex < sentenceRanges.lastIndex

    fun expandStart(): ReaderAnnotationSelection {
        return if (canExpandStart) copy(startSentenceIndex = startSentenceIndex - 1) else this
    }

    fun shrinkStart(): ReaderAnnotationSelection {
        return if (canShrinkStart) copy(startSentenceIndex = startSentenceIndex + 1) else this
    }

    fun shrinkEnd(): ReaderAnnotationSelection {
        return if (canShrinkEnd) copy(endSentenceIndex = endSentenceIndex - 1) else this
    }

    fun expandEnd(): ReaderAnnotationSelection {
        return if (canExpandEnd) copy(endSentenceIndex = endSentenceIndex + 1) else this
    }

    fun focusChangedFrom(previous: ReaderAnnotationSelection?): ReaderAnnotationSelectionFocus {
        if (previous == null) {
            return ReaderAnnotationSelectionFocus.END
        }
        return when {
            selector.sourceBlockIndex != previous.selector.sourceBlockIndex -> ReaderAnnotationSelectionFocus.START
            selector.textStartOffset != previous.selector.textStartOffset -> ReaderAnnotationSelectionFocus.START
            else -> ReaderAnnotationSelectionFocus.END
        }
    }
}

internal fun readerPageIndexForAnnotationSelectionFocus(
    selection: ReaderAnnotationSelection,
    focus: ReaderAnnotationSelectionFocus,
    layout: ReaderBlockLayout,
    pages: List<ReaderPage>,
): Int {
    val selector = selection.selector
    val focusSourceBlockIndex = when (focus) {
        ReaderAnnotationSelectionFocus.START -> selector.sourceBlockIndex
        ReaderAnnotationSelectionFocus.END -> selector.endSourceBlockIndex
    }
    val focusOffset = when (focus) {
        ReaderAnnotationSelectionFocus.START -> selector.textStartOffset
        ReaderAnnotationSelectionFocus.END -> selector.textEndOffset
    }
    val focusDisplayBlockIndex = layout.displayBlockIndexForSourcePosition(
        sourceBlockIndex = focusSourceBlockIndex,
        textOffset = focusOffset,
    )
    return readerPageIndexForParagraph(
        pages = pages,
        paragraphIndex = focusDisplayBlockIndex,
    )
}

internal data class ReaderPage(
    val start: Int,
    val endInclusive: Int,
)

internal fun readerBlocksForDisplay(body: String, fallback: String): List<ReaderMarkdownBlock> {
    return readerParagraphsForDisplay(body = body, fallback = fallback)
        .mapIndexed { index, paragraph ->
            readerMarkdownBlock(rawBlock = paragraph, sourceBlockIndex = index)
        }
}

internal fun splitOversizedReaderBlocks(
    blocks: List<ReaderMarkdownBlock>,
    maxBlockWeight: Int = READER_DEFAULT_PAGE_WEIGHT,
    charsPerLine: Int = READER_BODY_CHARS_PER_LINE,
): ReaderBlockLayout {
    if (blocks.isEmpty()) {
        return ReaderBlockLayout(blocks = emptyList(), originalToDisplayStart = emptyList())
    }
    val expanded = mutableListOf<ReaderMarkdownBlock>()
    val originalToDisplayStart = mutableListOf<Int>()
    blocks.forEach { block ->
        originalToDisplayStart += expanded.size
        val chunks = block.splitForReaderPage(maxBlockWeight = maxBlockWeight, charsPerLine = charsPerLine)
        expanded += chunks
    }
    return ReaderBlockLayout(blocks = expanded, originalToDisplayStart = originalToDisplayStart)
}

internal fun initialReaderAnnotationSelection(
    paragraphIndex: Int,
    block: ReaderMarkdownBlock,
    charOffset: Int,
    annotation: ReadingAnnotation?,
    selectionBlocks: List<ReaderMarkdownBlock> = listOf(block),
): ReaderAnnotationSelection {
    val sourceBlocks = readerAnnotationSourceBlocks(selectionBlocks.ifEmpty { listOf(block) })
        .ifEmpty { readerAnnotationSourceBlocks(listOf(block)) }
    val sourceText = sourceBlocks.joinToString(separator = READER_ANNOTATION_SOURCE_BLOCK_SEPARATOR) { sourceBlock ->
        sourceBlock.text
    }
    val currentSourceBlock = sourceBlocks.firstOrNull { sourceBlock -> sourceBlock.matches(block) }
        ?: sourceBlocks.first()
    val visibleTargetOffset = (
        currentSourceBlock.combinedStart +
            block.sourceTextStartOffset +
            charOffset
        )
        .coerceIn(0, sourceText.length.coerceAtLeast(1) - 1)
    val selectionRanges = sourceBlocks.flatMap { sourceBlock ->
        readerSelectionRanges(sourceBlock.text).map { range ->
            ReaderSentenceRange(
                start = sourceBlock.combinedStart + range.start,
                endExclusive = sourceBlock.combinedStart + range.endExclusive,
                sourceBlockIndex = sourceBlock.sourceBlockIndex,
                sourceHref = sourceBlock.sourceHref,
                sourceAnchor = sourceBlock.sourceAnchor,
                sourceTextStartOffset = range.start,
                sourceTextEndOffset = range.endExclusive,
            )
        }
    }.ifEmpty {
        listOf(ReaderSentenceRange(start = 0, endExclusive = sourceText.length))
    }
    val existingQuote = annotation
        ?.quotedText
        ?.trim()
        ?.takeIf(String::isNotBlank)
    val selector = annotation?.selector?.takeIf { selector ->
        selector.hasUsableReaderRange() &&
            sourceBlocks.any { sourceBlock -> sourceBlock.sourceBlockIndex == selector.sourceBlockIndex } &&
            sourceBlocks.any { sourceBlock -> sourceBlock.sourceBlockIndex == selector.endSourceBlockIndex }
    }
    val selectorStart = selector?.let { currentSelector ->
        sourceBlocks.firstOrNull { sourceBlock -> sourceBlock.sourceBlockIndex == currentSelector.sourceBlockIndex }
            ?.let { sourceBlock -> sourceBlock.combinedStart + currentSelector.textStartOffset }
            ?.coerceIn(0, sourceText.length)
    }
    val selectorEndExclusive = selector?.let { currentSelector ->
        sourceBlocks.firstOrNull { sourceBlock -> sourceBlock.sourceBlockIndex == currentSelector.endSourceBlockIndex }
            ?.let { sourceBlock -> sourceBlock.combinedStart + currentSelector.textEndOffset }
            ?.coerceIn(selectorStart ?: 0, sourceText.length)
    }
    val annotationStart = selectorStart ?: existingQuote
        ?.let { quote -> sourceText.indexOf(quote).takeIf { index -> index >= 0 } }
    val annotationEndExclusive = selectorEndExclusive ?: annotationStart
        ?.let { start -> (start + existingQuote.orEmpty().length).coerceIn(0, sourceText.length) }
    val targetOffset = (annotationStart ?: visibleTargetOffset).coerceIn(0, sourceText.length.coerceAtLeast(1) - 1)
    val sentenceIndex = selectionRanges
        .indexOfFirst { range -> targetOffset in range.start until range.endExclusive }
        .takeIf { index -> index >= 0 }
        ?: selectionRanges.indices.minByOrNull { index ->
            val range = selectionRanges[index]
            minOf(kotlin.math.abs(targetOffset - range.start), kotlin.math.abs(targetOffset - range.endExclusive))
        }
        ?: 0
    val startSelectionIndex = if (annotationStart != null && annotationEndExclusive != null) {
        selectionRanges
            .indexOfFirst { range -> range.start < annotationEndExclusive && range.endExclusive > annotationStart }
            .takeIf { index -> index >= 0 }
            ?: sentenceIndex
    } else {
        sentenceIndex
    }
    val endSelectionIndex = if (annotationStart != null && annotationEndExclusive != null) {
        selectionRanges
            .indexOfLast { range -> range.start < annotationEndExclusive && range.endExclusive > annotationStart }
            .takeIf { index -> index >= startSelectionIndex }
            ?: startSelectionIndex
    } else {
        startSelectionIndex
    }
    return ReaderAnnotationSelection(
        paragraphIndex = paragraphIndex,
        sourceText = sourceText,
        sourceHref = block.sourceHref,
        sourceAnchor = block.sourceAnchor,
        sourceBlockIndex = block.sourceBlockIndex,
        sourceTextStartOffset = 0,
        sentenceRanges = selectionRanges,
        startSentenceIndex = startSelectionIndex,
        endSentenceIndex = endSelectionIndex,
        existingAnnotationId = annotation?.id,
    )
}

private const val READER_ANNOTATION_SOURCE_BLOCK_SEPARATOR = "\n\n"

private data class ReaderAnnotationSourceBlock(
    val sourceBlockIndex: Int,
    val sourceHref: String?,
    val sourceAnchor: String?,
    val text: String,
    val combinedStart: Int,
) {
    fun matches(block: ReaderMarkdownBlock): Boolean {
        return sourceBlockIndex == block.sourceBlockIndex &&
            sourceHref == block.sourceHref &&
            sourceAnchor == block.sourceAnchor
    }
}

private fun readerAnnotationSourceBlocks(blocks: List<ReaderMarkdownBlock>): List<ReaderAnnotationSourceBlock> {
    val uniqueBlocks = linkedMapOf<String, ReaderMarkdownBlock>()
    blocks.forEach { block ->
        val key = listOf(block.sourceBlockIndex.toString(), block.sourceHref.orEmpty(), block.sourceAnchor.orEmpty())
            .joinToString(separator = "\u0000")
        uniqueBlocks.putIfAbsent(key, block)
    }
    var combinedStart = 0
    return uniqueBlocks.values.map { block ->
        val sourceText = block.annotationSourceText()
        val sourceBlock = ReaderAnnotationSourceBlock(
            sourceBlockIndex = block.sourceBlockIndex,
            sourceHref = block.sourceHref,
            sourceAnchor = block.sourceAnchor,
            text = sourceText,
            combinedStart = combinedStart,
        )
        combinedStart += sourceText.length + READER_ANNOTATION_SOURCE_BLOCK_SEPARATOR.length
        sourceBlock
    }
}

internal fun readerSentenceRanges(text: String): List<ReaderSentenceRange> {
    val ranges = mutableListOf<ReaderSentenceRange>()
    var start = 0
    while (start < text.length && text[start].isWhitespace()) {
        start += 1
    }
    var index = start
    while (index < text.length) {
        val char = text[index]
        val endsSentence = (char == '.' || char == '!' || char == '?' || char == ';') &&
            (index == text.lastIndex || text[index + 1].isWhitespace())
        if (endsSentence) {
            val end = index + 1
            if (start < end) {
                ranges += ReaderSentenceRange(start = start, endExclusive = end)
            }
            start = end
            while (start < text.length && text[start].isWhitespace()) {
                start += 1
            }
            index = start
        } else {
            index += 1
        }
    }
    if (start < text.length) {
        ranges += ReaderSentenceRange(start = start, endExclusive = text.length)
    }
    return ranges
}

internal fun readerSelectionRanges(text: String): List<ReaderSentenceRange> {
    val sentences = readerSentenceRanges(text).ifEmpty {
        listOf(ReaderSentenceRange(start = 0, endExclusive = text.length))
    }
    val wordRegex = Regex("\\S+")
    return sentences.flatMap { sentence ->
        val sentenceText = text.substring(sentence.start, sentence.endExclusive)
        val ranges = mutableListOf<ReaderSentenceRange>()
        var groupStart: Int? = null
        var groupWordCount = 0
        wordRegex.findAll(sentenceText).forEach { match ->
            val absoluteStart = sentence.start + match.range.first
            val absoluteEnd = sentence.start + match.range.last + 1
            if (groupStart == null) {
                groupStart = absoluteStart
            }
            groupWordCount += 1
            val lastChar = match.value.lastOrNull()
            val punctuationBreak = lastChar == ',' || lastChar == ':' || lastChar == ';'
            if (punctuationBreak || groupWordCount >= READER_ANNOTATION_RANGE_WORD_STEP) {
                ranges += ReaderSentenceRange(start = groupStart ?: absoluteStart, endExclusive = absoluteEnd)
                groupStart = null
                groupWordCount = 0
            }
        }
        groupStart?.let { start ->
            ranges += ReaderSentenceRange(start = start, endExclusive = sentence.endExclusive)
        }
        ranges.ifEmpty { listOf(sentence) }
    }
}

internal fun readerPagesForBlocks(
    blocks: List<ReaderMarkdownBlock>,
    maxPageWeight: Int = READER_DEFAULT_PAGE_WEIGHT,
    charsPerLine: Int = READER_BODY_CHARS_PER_LINE,
    blockGapLineCost: Double = READER_DEFAULT_BLOCK_GAP_LINE_COST,
    maxBlocksPerPage: Int = Int.MAX_VALUE,
    readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
): List<ReaderPage> {
    if (blocks.isEmpty()) {
        return listOf(ReaderPage(start = 0, endInclusive = 0))
    }
    val safeMaxWeight = maxPageWeight.coerceAtLeast(1).toDouble()
    val safeBlockGapLineCost = blockGapLineCost.coerceIn(0.0, 1.5)
    val safeMaxBlocksPerPage = maxBlocksPerPage.coerceAtLeast(1)
    val pages = mutableListOf<ReaderPage>()
    var pageStart = 0
    var pageWeight = 0.0
    var pageCappedBlockCount = 0
    var pageHasBodyBlock = false
    blocks.forEachIndexed { index, block ->
        val blockWeight = block.readerPageCost(
            charsPerLine = charsPerLine,
            blockGapLineCost = safeBlockGapLineCost,
            readerFontScale = readerFontScale,
        )
        val appliesRenderedBlockCap = block.kind != ReaderMarkdownBlockKind.CODE
        val startsReaderSection = block.kind == ReaderMarkdownBlockKind.HEADING && pageHasBodyBlock
        val exceedsRenderedBlockCap = appliesRenderedBlockCap && pageCappedBlockCount >= safeMaxBlocksPerPage
        if (index > pageStart && (startsReaderSection || exceedsRenderedBlockCap || pageWeight + blockWeight > safeMaxWeight)) {
            pages += ReaderPage(start = pageStart, endInclusive = index - 1)
            pageStart = index
            pageWeight = 0.0
            pageCappedBlockCount = 0
            pageHasBodyBlock = false
        }
        pageWeight += blockWeight
        if (appliesRenderedBlockCap) {
            pageCappedBlockCount += 1
        }
        if (block.kind != ReaderMarkdownBlockKind.HEADING) {
            pageHasBodyBlock = true
        }
    }
    pages += ReaderPage(start = pageStart, endInclusive = blocks.lastIndex)
    return pages
}

internal fun adaptiveReaderPageWeight(
    viewportWidthDp: Float,
    viewportHeightDp: Float,
    readerFontScale: Double,
): Int {
    val safeFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    val usableHeightDp = (viewportHeightDp - READER_VERTICAL_PADDING_DP).coerceAtLeast(180f)
    val lineHeightDp = READER_BODY_LINE_HEIGHT_DP * safeFontScale
    val visibleLineCapacity = (usableHeightDp / lineHeightDp).coerceAtLeast(6f)
    val baseReserve = if (viewportHeightDp < READER_COMPACT_VIEWPORT_HEIGHT_DP) {
        READER_COMPACT_PAGE_SAFETY_RESERVE_LINES
    } else {
        READER_PAGE_SAFETY_RESERVE_LINES
    }
    val fixedViewportReserveLines = (
        baseReserve +
            ((safeFontScale - 1f).coerceAtLeast(0f) * READER_LARGE_FONT_SAFETY_LINES)
        ).coerceAtMost(visibleLineCapacity * READER_MAX_RESERVE_SHARE)
    val fillAllowanceLines = when {
        viewportHeightDp < READER_COMPACT_VIEWPORT_HEIGHT_DP -> 0f
        safeFontScale <= 1.05f -> READER_DEFAULT_TEXT_FILL_ALLOWANCE_LINES
        else -> 0f
    }
    val safeLineCapacity = (visibleLineCapacity - fixedViewportReserveLines + fillAllowanceLines).coerceAtLeast(6f)
    return floor(safeLineCapacity)
        .toInt()
        .coerceIn(READER_MIN_PAGE_WEIGHT, READER_MAX_PAGE_WEIGHT)
}

internal data class AdaptiveReaderPageFit(
    val viewportWidthDp: Float,
    val viewportHeightDp: Float,
    val readerFontScale: Double,
    val maxPageWeight: Int,
    val charsPerLine: Int,
    val maxBlockWeight: Int,
    val blockGapLineCost: Double,
    val maxBlocksPerPage: Int,
)

internal fun adaptiveReaderPageFit(
    viewportWidthDp: Float,
    viewportHeightDp: Float,
    readerFontScale: Double,
): AdaptiveReaderPageFit {
    val maxPageWeight = adaptiveReaderPageWeight(
        viewportWidthDp = viewportWidthDp,
        viewportHeightDp = viewportHeightDp,
        readerFontScale = readerFontScale,
    )
    val charsPerLine = adaptiveReaderCharsPerLine(
        viewportWidthDp = viewportWidthDp,
        readerFontScale = readerFontScale,
    )
    return AdaptiveReaderPageFit(
        viewportWidthDp = viewportWidthDp,
        viewportHeightDp = viewportHeightDp,
        readerFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE),
        maxPageWeight = maxPageWeight,
        charsPerLine = charsPerLine,
        maxBlockWeight = adaptiveReaderBlockChunkWeight(maxPageWeight),
        blockGapLineCost = adaptiveReaderBlockGapLineCost(
            viewportHeightDp = viewportHeightDp,
            readerFontScale = readerFontScale,
        ),
        maxBlocksPerPage = adaptiveReaderMaxBlocksPerPage(
            viewportHeightDp = viewportHeightDp,
            readerFontScale = readerFontScale,
        ),
    )
}

internal fun adaptiveReaderMaxBlocksPerPage(
    viewportHeightDp: Float,
    readerFontScale: Double,
): Int {
    val safeFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    val usableHeightDp = (viewportHeightDp - READER_VERTICAL_PADDING_DP).coerceAtLeast(180f)
    val shortestRenderedBodyBlockDp =
        (READER_BODY_LINE_HEIGHT_DP * safeFontScale) +
            READER_BODY_TEXT_BOTTOM_PADDING_DP +
            READER_BLOCK_OUTER_BOTTOM_PADDING_DP
    return floor(usableHeightDp / shortestRenderedBodyBlockDp)
        .toInt()
        .coerceAtLeast(1)
}

internal fun adaptiveReaderBlockGapLineCost(
    viewportHeightDp: Float,
    readerFontScale: Double,
): Double {
    val safeFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE)
    return when {
        viewportHeightDp < READER_COMPACT_VIEWPORT_HEIGHT_DP -> READER_COMPACT_BLOCK_GAP_LINE_COST
        safeFontScale >= READER_LARGE_TEXT_GAP_THRESHOLD -> READER_LARGE_TEXT_BLOCK_GAP_LINE_COST
        else -> READER_TALL_BLOCK_GAP_LINE_COST
    }
}

internal fun adaptiveReaderCharsPerLine(
    viewportWidthDp: Float,
    readerFontScale: Double,
): Int {
    val safeFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    val usableWidthDp = (viewportWidthDp - READER_HORIZONTAL_PADDING_DP).coerceAtLeast(220f)
    return (READER_BODY_CHARS_PER_LINE * (usableWidthDp / READER_REFERENCE_TEXT_WIDTH_DP) / safeFontScale)
        .roundToInt()
        .coerceIn(READER_MIN_CHARS_PER_LINE, READER_MAX_CHARS_PER_LINE)
}

internal fun adaptiveReaderBlockChunkWeight(maxPageWeight: Int): Int {
    return (maxPageWeight * 0.42f)
        .roundToInt()
        .coerceIn(6, 10)
}

private fun ReaderMarkdownBlock.annotationSourceText(): String {
    return sourceFullText
        ?.takeIf { sourceText -> sourceText.length >= sourceTextEndOffset() }
        ?: text.text
}

internal fun ReaderMarkdownBlock.sourceTextEndOffset(): Int {
    return sourceTextStartOffset + text.text.length
}

private fun ReadingAnnotationSelector.matchesReaderSource(block: ReaderMarkdownBlock): Boolean {
    return sourceBlockIndex == block.sourceBlockIndex &&
        (sourceHref.isNullOrBlank() || sourceHref == block.sourceHref) &&
        (sourceAnchor.isNullOrBlank() || sourceAnchor == block.sourceAnchor)
}

private fun ReadingAnnotationSelector.overlapsReaderBlock(block: ReaderMarkdownBlock): Boolean {
    val startBlock = sourceBlockIndex
    val endBlock = endSourceBlockIndex.coerceAtLeast(startBlock)
    if (startBlock == endBlock && textEndOffset <= textStartOffset) {
        return false
    }
    if (endBlock > startBlock && textEndOffset <= 0) {
        return false
    }
    if (block.sourceBlockIndex !in startBlock..endBlock) {
        return false
    }
    if (startBlock == endBlock) {
        return matchesReaderSource(block) &&
            textStartOffset < block.sourceTextEndOffset() &&
            textEndOffset > block.sourceTextStartOffset
    }
    return when (block.sourceBlockIndex) {
        startBlock -> textStartOffset < block.sourceTextEndOffset()
        endBlock -> textEndOffset > block.sourceTextStartOffset
        else -> true
    }
}

private fun ReadingAnnotationSelector.readerBlockHighlightRange(block: ReaderMarkdownBlock): IntRange? {
    if (!overlapsReaderBlock(block)) {
        return null
    }
    val startBlock = sourceBlockIndex
    val endBlock = endSourceBlockIndex.coerceAtLeast(startBlock)
    val start = if (block.sourceBlockIndex == startBlock) {
        (textStartOffset - block.sourceTextStartOffset).coerceIn(0, block.text.text.length)
    } else {
        0
    }
    val endExclusive = if (block.sourceBlockIndex == endBlock) {
        (textEndOffset - block.sourceTextStartOffset).coerceIn(start, block.text.text.length)
    } else {
        block.text.text.length
    }
    if (start >= endExclusive) {
        return null
    }
    return start until endExclusive
}

private fun ReadingAnnotationSelector.hasUsableReaderRange(): Boolean {
    return if (endSourceBlockIndex > sourceBlockIndex) {
        textEndOffset > 0
    } else {
        textEndOffset > textStartOffset
    }
}

internal fun readingAnnotationForBlock(
    paragraphIndex: Int,
    block: ReaderMarkdownBlock,
    annotationsByParagraph: Map<Int, ReadingAnnotation>,
    annotationsForContent: List<ReadingAnnotation>,
): ReadingAnnotation? {
    annotationsForContent.firstOrNull { annotation ->
        annotation.selector.overlapsReaderBlock(block)
    }?.let { annotation -> return annotation }

    val legacyParagraphAnnotation = annotationsByParagraph[paragraphIndex] ?: return null
    return legacyParagraphAnnotation.takeIf { annotation ->
        !annotation.selector.hasUsableReaderRange() || annotation.selector.overlapsReaderBlock(block)
    }
}

internal fun readerPageIndexForParagraph(pages: List<ReaderPage>, paragraphIndex: Int): Int {
    if (pages.isEmpty()) {
        return 0
    }
    val safeParagraphIndex = paragraphIndex.coerceAtLeast(0)
    return pages.indexOfFirst { page -> safeParagraphIndex in page.start..page.endInclusive }
        .takeIf { index -> index >= 0 }
        ?: pages.lastIndex
}

internal fun readerPageBoundarySignature(pages: List<ReaderPage>): String {
    return pages.joinToString(separator = "|") { page -> "${page.start}-${page.endInclusive}" }
}

internal fun readerProgressPercentForParagraphIndex(paragraphIndex: Int, paragraphCount: Int): Int {
    if (paragraphCount <= 0) {
        return 100
    }
    val visibleParagraphs = (paragraphIndex + 1).coerceIn(0, paragraphCount)
    return ((visibleParagraphs * 100) / paragraphCount).coerceIn(1, 100)
}

internal fun readerProgressPercentForPageIndex(pageIndex: Int, pageCount: Int): Int {
    if (pageCount <= 0) {
        return 100
    }
    val visiblePages = (pageIndex + 1).coerceIn(0, pageCount)
    return ((visiblePages * 100) / pageCount).coerceIn(1, 100)
}

private fun ReaderMarkdownBlock.readerPageWeight(charsPerLine: Int = READER_BODY_CHARS_PER_LINE): Int {
    val effectiveCharsPerLine = readerCharsPerLineForKind(kind = kind, bodyCharsPerLine = charsPerLine)
    val lineWeight = text.text
        .lines()
        .sumOf { line ->
            val visibleChars = line.length.coerceAtLeast(1)
            ((visibleChars + effectiveCharsPerLine - 1) / effectiveCharsPerLine).coerceAtLeast(1)
        }
    return when (kind) {
        ReaderMarkdownBlockKind.HEADING -> lineWeight + 2
        ReaderMarkdownBlockKind.CODE,
        ReaderMarkdownBlockKind.LIST,
        ReaderMarkdownBlockKind.QUOTE,
        ReaderMarkdownBlockKind.BODY,
        -> lineWeight + 1
    }
}

private fun ReaderMarkdownBlock.readerPageCost(
    charsPerLine: Int = READER_BODY_CHARS_PER_LINE,
    blockGapLineCost: Double = READER_DEFAULT_BLOCK_GAP_LINE_COST,
    readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
): Double {
    val safeFontScale = readerFontScale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE).toFloat()
    val effectiveCharsPerLine = readerCharsPerLineForKind(kind = kind, bodyCharsPerLine = charsPerLine)
    val lineWeight = text.text
        .lines()
        .sumOf { line ->
            val visibleChars = line.length.coerceAtLeast(1)
            ((visibleChars + effectiveCharsPerLine - 1) / effectiveCharsPerLine).coerceAtLeast(1)
        }
        .toDouble()
    return when (kind) {
        ReaderMarkdownBlockKind.HEADING -> lineWeight + 2.0
        ReaderMarkdownBlockKind.CODE -> {
            val bodyLineHeightDp = READER_BODY_LINE_HEIGHT_DP * safeFontScale
            val codeLineHeightDp = READER_CODE_LINE_HEIGHT_DP * safeFontScale
            val codeLineCostFactor = when {
                lineWeight <= 1.0 -> READER_CODE_ONE_LINE_VISUAL_LINE_COST_FACTOR
                safeFontScale >= READER_LARGE_TEXT_GAP_THRESHOLD.toFloat() -> {
                    READER_CODE_LARGE_TEXT_MULTI_LINE_VISUAL_LINE_COST_FACTOR
                }
                lineWeight <= 2.0 -> READER_CODE_TWO_LINE_VISUAL_LINE_COST_FACTOR
                lineWeight >= READER_CODE_LONG_MULTI_LINE_COST_THRESHOLD -> READER_CODE_LONG_MULTI_LINE_VISUAL_LINE_COST_FACTOR
                else -> readerShortMultiLineCodeVisualLineCostFactor(lineWeight)
            }
            val codeFixedPaddingDp = if (safeFontScale <= 1.05f) {
                READER_CODE_DEFAULT_TEXT_FIXED_VERTICAL_PADDING_DP
            } else {
                READER_CODE_LARGE_TEXT_FIXED_VERTICAL_PADDING_DP
            }
            ((lineWeight * codeLineHeightDp * codeLineCostFactor) + codeFixedPaddingDp + READER_BLOCK_OUTER_BOTTOM_PADDING_DP) / bodyLineHeightDp
        }

        ReaderMarkdownBlockKind.LIST,
        ReaderMarkdownBlockKind.QUOTE,
        ReaderMarkdownBlockKind.BODY,
        -> {
            val bodyLineHeightDp = READER_BODY_LINE_HEIGHT_DP * safeFontScale
            val fixedBodyPaddingCost =
                (READER_BODY_TEXT_BOTTOM_PADDING_DP + READER_BLOCK_OUTER_BOTTOM_PADDING_DP) / bodyLineHeightDp
            lineWeight + maxOf(blockGapLineCost, fixedBodyPaddingCost.toDouble())
        }
    }
}

private fun readerShortMultiLineCodeVisualLineCostFactor(lineWeight: Double): Float {
    return when {
        lineWeight <= 4.0 -> READER_CODE_THREE_TO_FOUR_LINE_VISUAL_LINE_COST_FACTOR
        lineWeight <= 6.0 -> READER_CODE_FIVE_TO_SIX_LINE_VISUAL_LINE_COST_FACTOR
        lineWeight <= 7.0 -> READER_CODE_SEVEN_LINE_VISUAL_LINE_COST_FACTOR
        lineWeight <= 8.0 -> READER_CODE_EIGHT_LINE_VISUAL_LINE_COST_FACTOR
        lineWeight <= 9.0 -> READER_CODE_NINE_LINE_VISUAL_LINE_COST_FACTOR
        else -> READER_CODE_TEN_TO_ELEVEN_LINE_VISUAL_LINE_COST_FACTOR
    }
}

private fun ReaderMarkdownBlock.splitForReaderPage(
    maxBlockWeight: Int,
    charsPerLine: Int = READER_BODY_CHARS_PER_LINE,
): List<ReaderMarkdownBlock> {
    val safeMaxWeight = maxBlockWeight.coerceAtLeast(6)
    val blockWeight = readerPageWeight(charsPerLine = charsPerLine)
    val wholeBlockWeightAllowance = if (kind == ReaderMarkdownBlockKind.CODE) {
        (safeMaxWeight * READER_CODE_WHOLE_BLOCK_SPLIT_ALLOWANCE_FACTOR).roundToInt()
    } else {
        safeMaxWeight
    }
    if (kind == ReaderMarkdownBlockKind.CODE) {
        val maxRenderedLineWeight = wholeBlockWeightAllowance - READER_CODE_SPLIT_SAFETY_LINES
        val shouldSplitCodeBlock = blockWeight > wholeBlockWeightAllowance ||
            hasUnsafeShortLineCodeTail(
                maxRenderedLineWeight = maxRenderedLineWeight,
                charsPerLine = charsPerLine,
            )
        if (shouldSplitCodeBlock) {
            val fullSourceText = sourceFullText ?: text.text
            return text.splitCodeAnnotatedByRenderedWeight(
                maxRenderedLineWeight = maxRenderedLineWeight,
                charsPerLine = charsPerLine,
            ).map { chunk ->
                copy(
                    text = chunk.text,
                    sourceTextStartOffset = sourceTextStartOffset + chunk.sourceStartOffset,
                    sourceFullText = fullSourceText,
                )
            }
        }
        return listOf(this)
    }
    if (kind == ReaderMarkdownBlockKind.HEADING || blockWeight <= wholeBlockWeightAllowance) {
        return listOf(this)
    }
    val targetChars = readerApproxCharsForWeight(
        kind = kind,
        weight = safeMaxWeight - 1,
        charsPerLine = charsPerLine,
    )
    val chunks = text.splitAnnotatedAtSentenceBoundaries(targetChars = targetChars)
    val fullSourceText = sourceFullText ?: text.text
    var searchStart = 0
    return chunks.map { chunk ->
        val localStart = text.text.indexOf(chunk.text, startIndex = searchStart)
            .takeIf { index -> index >= 0 }
            ?: searchStart
        searchStart = localStart + chunk.text.length
        copy(
            text = chunk,
            sourceTextStartOffset = sourceTextStartOffset + localStart,
            sourceFullText = fullSourceText,
        )
    }
}

private fun ReaderMarkdownBlock.hasUnsafeShortLineCodeTail(
    maxRenderedLineWeight: Int,
    charsPerLine: Int,
): Boolean {
    if (kind != ReaderMarkdownBlockKind.CODE) {
        return false
    }
    val effectiveCharsPerLine = readerCharsPerLineForKind(
        kind = ReaderMarkdownBlockKind.CODE,
        bodyCharsPerLine = charsPerLine,
    )
    var sourceLineCount = 0
    var renderedLineWeight = 0
    text.text.forEachLineRange { lineStart, lineEnd ->
        sourceLineCount += 1
        val visibleChars = (lineEnd - lineStart).coerceAtLeast(1)
        renderedLineWeight += ((visibleChars + effectiveCharsPerLine - 1) / effectiveCharsPerLine)
            .coerceAtLeast(1)
    }
    return sourceLineCount > maxRenderedLineWeight &&
        renderedLineWeight <= sourceLineCount + READER_CODE_SHORT_LINE_WRAP_ALLOWANCE
}

private fun readerApproxCharsForWeight(
    kind: ReaderMarkdownBlockKind,
    weight: Int,
    charsPerLine: Int = READER_BODY_CHARS_PER_LINE,
): Int {
    val effectiveCharsPerLine = readerCharsPerLineForKind(kind = kind, bodyCharsPerLine = charsPerLine)
    return (effectiveCharsPerLine * weight.coerceAtLeast(3)).coerceAtLeast(120)
}

private fun readerCharsPerLineForKind(
    kind: ReaderMarkdownBlockKind,
    bodyCharsPerLine: Int,
): Int {
    return when (kind) {
        ReaderMarkdownBlockKind.HEADING -> (bodyCharsPerLine * 0.62f).roundToInt()
        ReaderMarkdownBlockKind.CODE -> (bodyCharsPerLine * 0.60f).roundToInt()
        ReaderMarkdownBlockKind.LIST -> (bodyCharsPerLine * 0.90f).roundToInt()
        ReaderMarkdownBlockKind.QUOTE,
        ReaderMarkdownBlockKind.BODY,
        -> bodyCharsPerLine
    }.coerceIn(READER_MIN_CHARS_PER_LINE, READER_MAX_CHARS_PER_LINE)
}

private fun AnnotatedString.splitCodeAnnotatedByRenderedWeight(
    maxRenderedLineWeight: Int,
    charsPerLine: Int,
): List<ReaderAnnotatedChunk> {
    val raw = text
    if (raw.isBlank()) {
        return listOf(ReaderAnnotatedChunk(text = this, sourceStartOffset = 0))
    }
    val effectiveCharsPerLine = readerCharsPerLineForKind(
        kind = ReaderMarkdownBlockKind.CODE,
        bodyCharsPerLine = charsPerLine,
    )
    val targetLineWeight = maxRenderedLineWeight.coerceAtLeast(1)
    val chunks = mutableListOf<ReaderAnnotatedChunk>()
    var chunkStart = -1
    var chunkEnd = 0
    var chunkWeight = 0

    fun appendChunk(start: Int, end: Int) {
        if (end > start) {
            chunks += ReaderAnnotatedChunk(
                text = subAnnotatedString(start = start, end = end),
                sourceStartOffset = start,
            )
        }
    }

    fun flushChunk() {
        if (chunkStart >= 0) {
            appendChunk(start = chunkStart, end = chunkEnd)
            chunkStart = -1
            chunkEnd = 0
            chunkWeight = 0
        }
    }

    raw.forEachLineRange { lineStart, lineEnd ->
        val visibleChars = (lineEnd - lineStart).coerceAtLeast(1)
        val lineWeight = ((visibleChars + effectiveCharsPerLine - 1) / effectiveCharsPerLine).coerceAtLeast(1)
        if (lineWeight > targetLineWeight) {
            flushChunk()
            val targetChars = effectiveCharsPerLine * targetLineWeight
            var segmentStart = lineStart
            while (segmentStart < lineEnd) {
                val segmentEnd = (segmentStart + targetChars).coerceAtMost(lineEnd)
                appendChunk(start = segmentStart, end = segmentEnd)
                segmentStart = segmentEnd
            }
            return@forEachLineRange
        }
        if (chunkStart >= 0 && chunkWeight + lineWeight > targetLineWeight) {
            flushChunk()
        }
        if (chunkStart < 0) {
            chunkStart = lineStart
        }
        chunkEnd = lineEnd
        chunkWeight += lineWeight
    }
    flushChunk()

    return chunks.filter { chunk -> chunk.text.text.isNotBlank() }
        .ifEmpty { listOf(ReaderAnnotatedChunk(text = this, sourceStartOffset = 0)) }
}

private inline fun String.forEachLineRange(action: (start: Int, endExclusive: Int) -> Unit) {
    var lineStart = 0
    while (lineStart <= length) {
        val newline = indexOf('\n', startIndex = lineStart)
        val lineEnd = if (newline < 0) length else newline
        action(lineStart, lineEnd)
        if (newline < 0) {
            return
        }
        lineStart = newline + 1
    }
}

private fun AnnotatedString.splitAnnotatedAtSentenceBoundaries(targetChars: Int): List<AnnotatedString> {
    val raw = text.trim()
    if (raw.length <= targetChars) {
        return listOf(this)
    }
    val chunks = mutableListOf<AnnotatedString>()
    var start = text.indexOf(raw.first()).coerceAtLeast(0)
    val hardEnd = start + raw.length
    while (start < hardEnd) {
        val desiredEnd = (start + targetChars).coerceAtMost(hardEnd)
        val end = if (desiredEnd >= hardEnd) {
            hardEnd
        } else {
            sentenceBoundaryBefore(desiredEnd = desiredEnd, minEnd = start + (targetChars / 2))
                ?: wordBoundaryBefore(desiredEnd = desiredEnd, minEnd = start + (targetChars / 2))
                ?: desiredEnd
        }
        chunks += subAnnotatedString(start = start, end = end).trimAnnotated()
        start = end
        while (start < hardEnd && text[start].isWhitespace()) {
            start += 1
        }
    }
    return chunks.filter { chunk -> chunk.text.isNotBlank() }.ifEmpty { listOf(this) }
}

private fun AnnotatedString.wordBoundaryBefore(desiredEnd: Int, minEnd: Int): Int? {
    for (index in desiredEnd.coerceAtMost(text.lastIndex) downTo minEnd.coerceAtLeast(0)) {
        if (text[index].isWhitespace()) {
            return index
        }
    }
    return null
}

private fun AnnotatedString.sentenceBoundaryBefore(desiredEnd: Int, minEnd: Int): Int? {
    for (index in desiredEnd.coerceAtMost(text.lastIndex) downTo minEnd.coerceAtLeast(0)) {
        val char = text[index]
        if ((char == '.' || char == '!' || char == '?' || char == ';') && index + 1 < text.length && text[index + 1].isWhitespace()) {
            return index + 1
        }
    }
    return null
}

private fun AnnotatedString.trimAnnotated(): AnnotatedString {
    val startOffset = text.indexOfFirst { char -> !char.isWhitespace() }
    if (startOffset < 0) {
        return AnnotatedString("")
    }
    val endOffset = text.indexOfLast { char -> !char.isWhitespace() } + 1
    return subAnnotatedString(start = startOffset, end = endOffset)
}

private fun AnnotatedString.subAnnotatedString(start: Int, end: Int): AnnotatedString {
    val safeStart = start.coerceIn(0, text.length)
    val safeEnd = end.coerceIn(safeStart, text.length)
    val adjustedSpans = spanStyles.mapNotNull { range ->
        val overlapStart = maxOf(range.start, safeStart)
        val overlapEnd = minOf(range.end, safeEnd)
        if (overlapStart < overlapEnd) {
            AnnotatedString.Range(
                item = range.item,
                start = overlapStart - safeStart,
                end = overlapEnd - safeStart,
                tag = range.tag,
            )
        } else {
            null
        }
    }
    return AnnotatedString(
        text = text.substring(safeStart, safeEnd),
        spanStyles = adjustedSpans,
    )
}

private fun AnnotatedString.withReaderHighlight(
    highlightedText: String?,
    highlightRange: IntRange?,
    highlightColor: Color,
): AnnotatedString {
    val rangeStart = highlightRange?.first?.coerceIn(0, text.length)
    val rangeEnd = highlightRange?.last?.plus(1)?.coerceIn(rangeStart ?: 0, text.length)
    if (rangeStart != null && rangeEnd != null && rangeStart < rangeEnd) {
        return buildAnnotatedString {
            append(this@withReaderHighlight)
            addStyle(
                style = SpanStyle(background = highlightColor),
                start = rangeStart,
                end = rangeEnd,
            )
        }
    }
    val quote = highlightedText?.trim().orEmpty()
    if (quote.isBlank()) {
        return this
    }
    val start = text.indexOf(quote)
    if (start < 0) {
        return this
    }
    return buildAnnotatedString {
        append(this@withReaderHighlight)
        addStyle(
            style = SpanStyle(background = highlightColor),
            start = start,
            end = start + quote.length,
        )
    }
}

internal fun readerMarkdownBlock(
    rawBlock: String,
    sourceHref: String? = null,
    sourceAnchor: String? = null,
    sourceBlockIndex: Int = 0,
): ReaderMarkdownBlock {
    val block = rawBlock.trim()
    val fencedCode = Regex("""^```[A-Za-z0-9_-]*\n([\s\S]*?)\n?```$""")
        .matchEntire(block)
    if (fencedCode != null) {
        return ReaderMarkdownBlock(
            text = AnnotatedString(fencedCode.groupValues[1].trim('\n')),
            kind = ReaderMarkdownBlockKind.CODE,
            sourceHref = sourceHref,
            sourceAnchor = sourceAnchor,
            sourceBlockIndex = sourceBlockIndex,
        )
    }

    Regex("""^(#{1,6})\s+(.+)$""")
        .matchEntire(block)
        ?.let { match ->
            return ReaderMarkdownBlock(
                text = parseInlineMarkdown(match.groupValues[2].trim()),
                kind = ReaderMarkdownBlockKind.HEADING,
                sourceHref = sourceHref,
                sourceAnchor = sourceAnchor,
                sourceBlockIndex = sourceBlockIndex,
            )
        }

    val lines = block.lines().map(String::trimEnd).filter(String::isNotBlank)
    if (lines.isNotEmpty() && lines.all { line -> line.trimStart().startsWith(">") }) {
        return ReaderMarkdownBlock(
            text = parseInlineMarkdown(
                lines.joinToString("\n") { line ->
                    line.trimStart().removePrefix(">").trimStart()
                },
            ),
            kind = ReaderMarkdownBlockKind.QUOTE,
            sourceHref = sourceHref,
            sourceAnchor = sourceAnchor,
            sourceBlockIndex = sourceBlockIndex,
        )
    }

    val unorderedListMarker = Regex("""^[-*+]\s+(.+)$""")
    val orderedListMarker = Regex("""^(\d+[.)])\s+(.+)$""")
    val continuationLine = Regex("""^\s{2,}\S.*$""")
    if (lines.isNotEmpty() && lines.first().isReaderListItem(unorderedListMarker, orderedListMarker) &&
        lines.all { line ->
            line.isReaderListItem(unorderedListMarker, orderedListMarker) || continuationLine.matches(line)
        }
    ) {
        return ReaderMarkdownBlock(
            text = parseInlineMarkdown(
                lines.joinToString("\n") { line ->
                    val ordered = orderedListMarker.matchEntire(line)
                    val unordered = unorderedListMarker.matchEntire(line)
                    when {
                        ordered != null -> "${ordered.groupValues[1]} ${ordered.groupValues[2].trim()}"
                        unordered != null -> "• ${unordered.groupValues[1].trim()}"
                        else -> line.asReaderContinuationLine()
                    }
                },
            ),
            kind = ReaderMarkdownBlockKind.LIST,
            sourceHref = sourceHref,
            sourceAnchor = sourceAnchor,
            sourceBlockIndex = sourceBlockIndex,
        )
    }

    return ReaderMarkdownBlock(
        text = parseInlineMarkdown(block),
        kind = ReaderMarkdownBlockKind.BODY,
        sourceHref = sourceHref,
        sourceAnchor = sourceAnchor,
        sourceBlockIndex = sourceBlockIndex,
    )
}

private fun String.isReaderListItem(unordered: Regex, ordered: Regex): Boolean {
    return unordered.matches(this) || ordered.matches(this)
}

internal fun parseInlineMarkdown(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < rawText.length) {
            when {
                rawText[index] == '\\' && index + 1 < rawText.length -> {
                    append(rawText[index + 1])
                    index += 2
                }

                rawText.startsWith("**", index) || rawText.startsWith("__", index) -> {
                    val marker = rawText.substring(index, index + 2)
                    val close = rawText.indexOf(marker, startIndex = index + 2)
                    if (close > index + 2) {
                        appendStyledMarkdown(
                            text = rawText.substring(index + 2, close),
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        )
                        index = close + 2
                    } else {
                        append(rawText[index])
                        index += 1
                    }
                }

                rawText[index] == '`' -> {
                    val close = rawText.indexOf('`', startIndex = index + 1)
                    if (close > index + 1) {
                        appendStyledMarkdown(
                            text = rawText.substring(index + 1, close),
                            style = SpanStyle(fontFamily = QualityMonoFontFamily),
                        )
                        index = close + 1
                    } else {
                        append(rawText[index])
                        index += 1
                    }
                }

                rawText[index] == '*' || rawText[index] == '_' -> {
                    val marker = rawText[index]
                    val close = if (canOpenInlineEmphasis(rawText, index)) {
                        findClosingInlineEmphasis(rawText = rawText, marker = marker, startIndex = index + 1)
                    } else {
                        -1
                    }
                    if (close > index + 1 && rawText.substring(index + 1, close).isNotBlank()) {
                        appendStyledMarkdown(
                            text = rawText.substring(index + 1, close),
                            style = SpanStyle(fontStyle = FontStyle.Italic),
                        )
                        index = close + 1
                    } else {
                        append(rawText[index])
                        index += 1
                    }
                }

                else -> {
                    append(rawText[index])
                    index += 1
                }
            }
        }
    }
}

private fun canOpenInlineEmphasis(rawText: String, index: Int): Boolean {
    val marker = rawText[index]
    val previous = rawText.getOrNull(index - 1)
    val next = rawText.getOrNull(index + 1)
    if (next == null || next.isWhitespace()) {
        return false
    }
    return marker != '_' || previous?.isLetterOrDigit() != true
}

private fun findClosingInlineEmphasis(rawText: String, marker: Char, startIndex: Int): Int {
    var close = rawText.indexOf(marker, startIndex = startIndex)
    while (close != -1) {
        if (canCloseInlineEmphasis(rawText = rawText, marker = marker, index = close)) {
            return close
        }
        close = rawText.indexOf(marker, startIndex = close + 1)
    }
    return -1
}

private fun canCloseInlineEmphasis(rawText: String, marker: Char, index: Int): Boolean {
    val previous = rawText.getOrNull(index - 1)
    val next = rawText.getOrNull(index + 1)
    if (previous == null || previous.isWhitespace()) {
        return false
    }
    return marker != '_' || next?.isLetterOrDigit() != true
}

private fun AnnotatedString.Builder.appendStyledMarkdown(text: String, style: SpanStyle) {
    val start = length
    append(parseInlineMarkdown(text))
    addStyle(style, start, length)
}

internal fun readerProgressPercent(lastVisibleItemIndex: Int, paragraphCount: Int): Int {
    if (paragraphCount <= 0) {
        return 100
    }
    val visibleParagraphs = lastVisibleItemIndex.coerceIn(0, paragraphCount)
    return ((visibleParagraphs * 100) / paragraphCount).coerceIn(0, 100)
}

internal fun readerProgressPercentForReaderList(lastVisibleItemIndex: Int, paragraphCount: Int): Int {
    val lastVisibleParagraphIndex = (lastVisibleItemIndex - READER_HEADER_ITEM_COUNT).coerceAtLeast(0)
    return readerProgressPercent(lastVisibleItemIndex = lastVisibleParagraphIndex, paragraphCount = paragraphCount)
}

internal fun dayCountLabel(count: Int, singular: String): String {
    return quantityLabel(count = count, singular = singular)
}

internal fun quantityLabel(count: Int, singular: String): String {
    return "$count ${dayNounLabel(count = count, singular = singular)}"
}

internal fun dayNounLabel(count: Int, singular: String): String {
    return if (count == 1) singular else "${singular}s"
}

internal fun convertedDayNounLabel(count: Int): String {
    return if (count == 1) "day converted" else "days converted"
}

private fun String.asReaderContinuationLine(): String {
    val indent = takeWhile(Char::isWhitespace)
        .sumOf { char -> if (char == '\t') 2 else 1 }
        .coerceAtLeast(2)
    return " ".repeat(indent) + trim()
}

internal fun currentReadingStreakDays(completedDays: Set<LocalDate>, today: LocalDate): Int {
    val anchor = when {
        today in completedDays -> today
        today.minusDays(1) in completedDays -> today.minusDays(1)
        else -> return 0
    }
    var streak = 0
    var cursor = anchor
    while (cursor in completedDays) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
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
        TopicTag.ATTENTION -> "Attention"
        TopicTag.PRACTICAL -> "Practical"
        TopicTag.BODY -> "Body"
        TopicTag.NATURE -> "Nature"
        TopicTag.HISTORY_CULTURE -> "History & culture"
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
        TopicTag.CREATIVITY -> "Creativity"
        TopicTag.PSYCHOLOGY -> "Psychology"
        TopicTag.OTHER -> "Other"
    }
}

private fun ContentPriority.displayLabel(): String {
    return when (this) {
        ContentPriority.BALANCED -> "Balanced"
        ContentPriority.READINGS -> "Built-in readings"
        ContentPriority.MY_FILES -> "My files"
        ContentPriority.SAVED_LINKS -> "Saved links"
        ContentPriority.MEDITATION -> "Meditation"
    }
}

private fun ContentPriority.displayDescription(): String {
    return when (this) {
        ContentPriority.BALANCED -> "Let the app choose from all eligible replacements."
        ContentPriority.READINGS -> "Prefer curated reader pieces when they fit."
        ContentPriority.MY_FILES -> "Prefer your imported Markdown, EPUB, or PDF handoffs."
        ContentPriority.SAVED_LINKS -> "Prefer your saved external links."
        ContentPriority.MEDITATION -> "Prefer the breathing timer when it is a good fit."
    }
}

internal fun prototypeTopics(): List<TopicTag> = listOf(
    TopicTag.ATTENTION,
    TopicTag.PRACTICAL,
    TopicTag.BODY,
    TopicTag.NATURE,
    TopicTag.HISTORY_CULTURE,
    TopicTag.ESSAYS,
    TopicTag.PHILOSOPHY,
    TopicTag.SCIENCE,
    TopicTag.PSYCHOLOGY,
    TopicTag.HISTORY,
    TopicTag.TECH,
    TopicTag.ECONOMICS,
    TopicTag.CLIMATE,
    TopicTag.CREATIVITY,
    TopicTag.DESIGN,
    TopicTag.FICTION,
    TopicTag.POETRY,
    TopicTag.FOOD,
    TopicTag.ARCHITECTURE,
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

private fun ContentItem.isUserContent(): Boolean = isUserLink() || isUserDocument()

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

        isUserDocument() -> {
            val label = sourceLabel?.removeSuffix(" (missing)")?.ifBlank { null }
            when {
                availability == com.qualityalternative.app.domain.model.ContentAvailability.UNAVAILABLE ->
                    label?.let { "Your file · $it (missing)" } ?: "Your file · missing"
                label != null -> "Your file · $label"
                else -> "Your file"
            }
        }
        usesMeditationTimer() -> sourceLabel ?: "Quality Alternative"
        else -> sourceLabel?.ifBlank { null }
            ?: packId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }
}

private fun annotationContentIndex(state: MainUiState): Map<String, ContentItem> {
    return (state.starterPacks.flatMap(EditorialPack::items) + state.userLinks + state.userDocuments)
        .distinctBy(ContentItem::id)
        .associateBy(ContentItem::id)
}

private fun ContentSourceType.annotationSourceIcon(): QaIconKind {
    return when (this) {
        ContentSourceType.USER_LINK -> QaIconKind.Link
        ContentSourceType.USER_DOCUMENT -> QaIconKind.Book
        ContentSourceType.MEDITATION -> QaIconKind.Pause
        ContentSourceType.EDITORIAL -> QaIconKind.Sparkle
    }
}

private fun ContentSourceType.annotationSourceTypeLabel(): String {
    return when (this) {
        ContentSourceType.USER_LINK -> "User link"
        ContentSourceType.USER_DOCUMENT -> "User file"
        ContentSourceType.MEDITATION -> "Meditation"
        ContentSourceType.EDITORIAL -> "Editorial"
    }
}

private fun annotationCountLabel(count: Int): String {
    return if (count == 1) {
        "1 saved note"
    } else {
        "$count saved notes"
    }
}

private fun annotationUpdatedLabel(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.US)
    return formatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
}

private fun annotationExportStatusText(state: MainUiState): String {
    state.annotationExportLastError?.takeIf(String::isNotBlank)?.let { error ->
        return "Sync failed. ${error.removeSuffix(".")}."
    }
    return state.annotationExportLastSuccessfulAtMillis?.let { timestampMillis ->
        if (state.annotationExportUsesLocalDefault) {
            "Local files saved ${annotationUpdatedLabel(timestampMillis)}"
        } else {
            "Last saved ${annotationUpdatedLabel(timestampMillis)}"
        }
    } ?: when {
        state.annotationExportUsesLocalDefault -> "Stores annotation files in app storage"
        !state.annotationExportUri.isNullOrBlank() -> "Ready to save annotation files"
        else -> "No annotation sync destination selected"
    }
}

private fun annotationDriveStatusText(state: MainUiState): String {
    if (state.isAnnotationDriveSyncing) {
        return "Syncing annotations"
    }
    if (annotationExportUsesGoogleDriveProvider(state.annotationExportUri)) {
        return state.annotationExportLastSuccessfulAtMillis?.let { timestampMillis ->
            "Drive folder saved ${annotationUpdatedLabel(timestampMillis)}"
        } ?: "Uses the Android Google Drive folder picker"
    }
    state.annotationDriveLastError?.takeIf(String::isNotBlank)?.let { error ->
        return error.removeSuffix(".")
    }
    return state.annotationDriveLastSuccessfulAtMillis?.let { timestampMillis ->
        "Last synced ${annotationUpdatedLabel(timestampMillis)}"
    } ?: if (state.annotationDriveSyncEnabled) {
        "Ready to sync"
    } else {
        "Uses Google Drive file access only"
    }
}

internal fun annotationExportUsesGoogleDriveProvider(uri: String?): Boolean {
    return uri
        ?.takeIf(String::isNotBlank)
        ?.let { rawUri ->
            runCatching { Uri.parse(rawUri).authority.orEmpty() }.getOrDefault(rawUri)
        }
        ?.contains("com.google.android.apps.docs", ignoreCase = true) == true
}

private fun profileAutosaveStatusText(state: MainUiState): String {
    if (state.isProfileAutosaving) {
        return "Saving profile backup"
    }
    state.profileAutosaveLastError?.takeIf(String::isNotBlank)?.let { error ->
        return "Backup failed. ${error.removeSuffix(".")}."
    }
    return state.profileAutosaveLastSuccessfulAtMillis?.let { timestampMillis ->
        if (state.profileAutosaveUsesLocalDefault) {
            "Local backup saved ${annotationUpdatedLabel(timestampMillis)}"
        } else {
            "Last saved ${annotationUpdatedLabel(timestampMillis)}"
        }
    } ?: when {
        state.profileAutosaveUsesLocalDefault -> "Stores the profile backup in app storage"
        !state.profileAutosaveUri.isNullOrBlank() -> "Ready to save profile backups"
        else -> "No profile backup destination selected"
    }
}

internal fun googleDriveAuthorizationMissingResultMessage(resultCode: Int, hasResultIntent: Boolean): String? {
    if (hasResultIntent) {
        return null
    }
    return if (resultCode == Activity.RESULT_CANCELED) {
        "Google Drive authorization was cancelled."
    } else {
        "Google Drive authorization returned no result. Retry Google Drive connection."
    }
}

internal fun Throwable.googleDriveAuthMessage(): String {
    val apiException = this as? ApiException
    if (apiException != null) {
        return when (apiException.statusCode) {
            CommonStatusCodes.CANCELED -> "Google Drive authorization was cancelled."
            CommonStatusCodes.NETWORK_ERROR,
            CommonStatusCodes.TIMEOUT -> "Google Drive authorization could not reach Google services. Check connection and retry."
            CommonStatusCodes.SIGN_IN_REQUIRED -> "Choose a Google account to connect Google Drive."
            CommonStatusCodes.API_NOT_CONNECTED -> "Google Play services must be available and updated to connect Google Drive."
            CommonStatusCodes.DEVELOPER_ERROR -> "Google Drive authorization is not configured for this app build."
            CommonStatusCodes.INTERNAL_ERROR -> "Google Drive authorization hit a Google Play services error. Retry Google Drive connection."
            else -> apiException.message?.takeIf(String::isNotBlank)
                ?: "Google Drive authorization failed with ${CommonStatusCodes.getStatusCodeString(apiException.statusCode)}."
        }
    }
    return message?.takeIf(String::isNotBlank) ?: "Google Drive authorization failed."
}

private fun remainingMinutes(totalMinutes: Int, progress: Int): Int {
    return kotlin.math.ceil(totalMinutes * (1 - progress.coerceIn(0, 100) / 100f).toDouble())
        .toInt()
        .coerceAtLeast(0)
}

private fun List<ContentItem>.unfinishedSortedByProgress(progress: List<ReadingProgress>): List<ContentItem> {
    val progressById = progress
        .filter(ReadingProgress::isUnfinished)
        .associateBy(ReadingProgress::contentId)
    return filter { item ->
        item.id in progressById &&
            item.availability != com.qualityalternative.app.domain.model.ContentAvailability.UNAVAILABLE
    }.sortedWith(
        compareByDescending<ContentItem> { item -> progressById.getValue(item.id).updatedAtMillis }
            .thenBy(ContentItem::title),
    )
}

private fun List<ReadingProgress>.unfinishedProgressFor(contentId: String): ReadingProgress? {
    return firstOrNull { progress -> progress.contentId == contentId && progress.isUnfinished() }
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

internal fun recentReplacementDurationLabel(entry: ReplacementHistoryEntry): String {
    return "${entry.contentDurationMinutes} min"
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
        UserDocumentValidationError.NO_TOPICS -> "Choose at least one topic so the app can rank this file."
    }
}

private fun documentFormatLabel(form: AddDocumentFormState): String {
    return when (UserDocumentValidator.detectFormat(displayName = form.displayName, mimeType = form.mimeType)) {
        ContentFormat.MARKDOWN -> "Markdown · private reader"
        ContentFormat.PDF -> "PDF · external viewer"
        ContentFormat.EPUB -> "EPUB · private reader"
        ContentFormat.HTML -> "Document"
        null -> "Unsupported file"
    }
}

private fun documentFormatLabel(candidate: DocumentImportCandidate): String {
    return when (candidate.format) {
        ContentFormat.MARKDOWN -> "Markdown · private reader"
        ContentFormat.PDF -> "PDF · external viewer"
        ContentFormat.EPUB -> "EPUB · private reader"
        ContentFormat.HTML -> "Document"
        null -> "Unsupported file · skipped"
    }
}

private fun ReadingTimeEstimateSource.displayLabel(): String {
    return when (this) {
        ReadingTimeEstimateSource.EXTRACTED_TEXT -> "auto"
        ReadingTimeEstimateSource.PDF_DEFAULT -> "PDF"
        ReadingTimeEstimateSource.FALLBACK_DEFAULT -> "default"
    }
}

private fun addSuccessMeta(saved: AddLinkConfirmation): String {
    val savedLabel = if (saved.savedCount > 1) "${saved.savedCount} files" else saved.host
    val skipped = if (saved.skippedCount > 0) " · ${saved.skippedCount} skipped" else ""
    val priority = if (saved.priorityMarked) " · priority" else ""
    return "$savedLabel · ${saved.durationMinutes} min · ${saved.topicLabel}$priority$skipped"
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

private fun Context.documentImportCandidate(uri: Uri): DocumentImportCandidate {
    val metadata = documentMetadata(uri)
    return DocumentImportCandidateFactory.fromPickedDocument(
        uri = uri.toString(),
        displayName = metadata.displayName,
        mimeType = metadata.mimeType,
    ) { contentResolver.openInputStream(uri) }
}

private fun Context.readUtf8Text(uri: Uri): String {
    return contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
        reader.readText()
    } ?: error("Unable to read profile.")
}

private fun Context.writeUtf8Text(uri: Uri, text: String) {
    val bytes = text.toByteArray(Charsets.UTF_8)
    contentResolver.openOutputStream(uri)?.use { output ->
        output.write(bytes)
    } ?: error("Unable to write profile.")
}

private fun persistDocumentPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private fun persistAnnotationExportPermission(context: Context, uri: Uri) {
    context.contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
    )
}

private fun persistProfileAutosavePermission(context: Context, uri: Uri) {
    context.contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
    )
}

private fun releaseAnnotationExportPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.releasePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
    }
}

private fun releaseProfileAutosavePermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.releasePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
    }
}

private fun releaseDocumentPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private const val READER_HEADER_ITEM_COUNT = 1
private const val READER_DEFAULT_PAGE_WEIGHT = 18
private const val READER_MIN_PAGE_WEIGHT = 8
private const val READER_MAX_PAGE_WEIGHT = 56
private const val READER_BODY_CHARS_PER_LINE = 62
private const val READER_MIN_CHARS_PER_LINE = 24
private const val READER_MAX_CHARS_PER_LINE = 74
private const val READER_CONTENT_SIDE_PADDING_DP = 28f
private const val READER_CONTENT_TOP_PADDING_DP = 18f
private const val READER_CONTENT_BOTTOM_PADDING_DP = 24f
private const val READER_HORIZONTAL_PADDING_DP = READER_CONTENT_SIDE_PADDING_DP * 2f
private const val READER_VERTICAL_PADDING_DP = READER_CONTENT_TOP_PADDING_DP + READER_CONTENT_BOTTOM_PADDING_DP
private const val READER_REFERENCE_TEXT_WIDTH_DP = 340f
private const val READER_BODY_FONT_SIZE_DP = 17f
private const val READER_BODY_LINE_HEIGHT_DP = 27f
private const val READER_CODE_LINE_HEIGHT_DP = 22f
private const val READER_BODY_TEXT_BOTTOM_PADDING_DP = 7f
private const val READER_HEADING_TEXT_BOTTOM_PADDING_DP = 10f
private const val READER_CODE_DEFAULT_TEXT_FIXED_VERTICAL_PADDING_DP = 10f
private const val READER_CODE_LARGE_TEXT_FIXED_VERTICAL_PADDING_DP = 5f
private const val READER_CODE_ONE_LINE_VISUAL_LINE_COST_FACTOR = 0.75f
private const val READER_CODE_TWO_LINE_VISUAL_LINE_COST_FACTOR = 0.94f
private const val READER_CODE_THREE_TO_FOUR_LINE_VISUAL_LINE_COST_FACTOR = 0.95f
private const val READER_CODE_FIVE_TO_SIX_LINE_VISUAL_LINE_COST_FACTOR = 0.98f
private const val READER_CODE_SEVEN_LINE_VISUAL_LINE_COST_FACTOR = 1.02f
private const val READER_CODE_EIGHT_LINE_VISUAL_LINE_COST_FACTOR = 0.95f
private const val READER_CODE_NINE_LINE_VISUAL_LINE_COST_FACTOR = 1.02f
private const val READER_CODE_TEN_TO_ELEVEN_LINE_VISUAL_LINE_COST_FACTOR = 0.95f
private const val READER_CODE_LONG_MULTI_LINE_VISUAL_LINE_COST_FACTOR = 1.06f
private const val READER_CODE_LARGE_TEXT_MULTI_LINE_VISUAL_LINE_COST_FACTOR = 0.99f
private const val READER_CODE_LONG_MULTI_LINE_COST_THRESHOLD = 12.0
private const val READER_CODE_WHOLE_BLOCK_SPLIT_ALLOWANCE_FACTOR = 2f
private const val READER_CODE_SPLIT_SAFETY_LINES = 3
private const val READER_CODE_SHORT_LINE_WRAP_ALLOWANCE = 1
private const val READER_DEFAULT_TEXT_FILL_ALLOWANCE_LINES = 4.2f
private const val READER_BLOCK_OUTER_BOTTOM_PADDING_DP = 6f
private const val READER_PAGE_SAFETY_RESERVE_LINES = 0.35f
private const val READER_COMPACT_PAGE_SAFETY_RESERVE_LINES = 1.1f
private const val READER_LARGE_FONT_SAFETY_LINES = 0.4f
private const val READER_MAX_RESERVE_SHARE = 0.22f
private const val READER_COMPACT_VIEWPORT_HEIGHT_DP = 620f
private const val READER_LARGE_TEXT_GAP_THRESHOLD = 1.25
private const val READER_DEFAULT_BLOCK_GAP_LINE_COST = 1.0
private const val READER_COMPACT_BLOCK_GAP_LINE_COST = 1.0
private const val READER_TALL_BLOCK_GAP_LINE_COST = 0.35
private const val READER_LARGE_TEXT_BLOCK_GAP_LINE_COST = 0.7
private const val READER_PREVIOUS_TAP_EDGE_FRACTION = 0.12f
private const val READER_ANNOTATION_RANGE_WORD_STEP = 6
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
