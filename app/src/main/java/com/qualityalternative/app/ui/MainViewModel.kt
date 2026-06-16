package com.qualityalternative.app.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.data.AccountLightImportException
import com.qualityalternative.app.data.AccountLightImportPlan
import com.qualityalternative.app.data.AccountLightImportPreview
import com.qualityalternative.app.data.AccountLightProfileExporter
import com.qualityalternative.app.data.AccountLightProfileImporter
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.data.ACCOUNT_LIGHT_PROFILE_FILE_NAME
import com.qualityalternative.app.data.AgentInboxDocumentStore
import com.qualityalternative.app.data.AgentInboxManifestValidator
import com.qualityalternative.app.data.AgentInboxImportResult
import com.qualityalternative.app.data.AgentInboxImportStatus
import com.qualityalternative.app.data.AgentInboxPackageValidationError
import com.qualityalternative.app.data.AgentInboxPackageImporter
import com.qualityalternative.app.data.AgentInboxReviewCandidate
import com.qualityalternative.app.data.AgentInboxReviewCandidateFactory
import com.qualityalternative.app.data.AgentInboxReviewStatus
import com.qualityalternative.app.data.isGoogleDriveDocumentTreeUri
import com.qualityalternative.app.data.toAgentInboxImportFailureDetail
import com.qualityalternative.app.data.toAgentInboxImportPackageError
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.data.ReadingTimeEstimator
import com.qualityalternative.app.data.StoredAgentInboxDocument
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.data.UserDocumentValidator
import com.qualityalternative.app.data.UserLinkValidator
import com.qualityalternative.app.data.WebsiteRuleDraftResult
import com.qualityalternative.app.data.WebsiteRuleNormalizer
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsSemanticKeys
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.CustomTargetAppCandidate
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_ENABLED
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_END_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_START_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.DEFAULT_INTERVENTION_MODE
import com.qualityalternative.app.domain.model.DEFAULT_MEDITATION_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.DEFAULT_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.InterventionMode
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.MAX_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MAX_BEDTIME_MINUTES
import com.qualityalternative.app.domain.model.MAX_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MAX_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MIN_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MIN_BEDTIME_MINUTES
import com.qualityalternative.app.domain.model.MIN_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MIN_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.ReaderDocument
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import com.qualityalternative.app.domain.model.bedtimeWindowIsActive
import com.qualityalternative.app.domain.model.meditationTimerContentItem
import com.qualityalternative.app.domain.model.usesExternalHandoff
import com.qualityalternative.app.domain.model.usesMeditationTimer
import com.qualityalternative.app.domain.model.usesRepositoryBody
import com.qualityalternative.app.domain.service.AccountLightProfileAutosaveWriter
import com.qualityalternative.app.domain.service.AccountLightProfileBackupReader
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER
import com.qualityalternative.app.domain.service.AgentInboxDriveAccessLostException
import com.qualityalternative.app.domain.service.AgentInboxDriveClient
import com.qualityalternative.app.domain.service.AgentInboxDriveDownloadTooLargeException
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderListRequest
import com.qualityalternative.app.domain.service.AgentInboxDriveHttpException
import com.qualityalternative.app.domain.service.AgentInboxDriveScanRequest
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_MANIFEST_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.ReadingAnnotationRepository
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncClient
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncRequest
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFormatter
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import com.qualityalternative.app.interception.WebsiteInterceptionResolver
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val MAIN_VIEW_MODEL_LOG_TAG = "MainViewModel"

data class MainUiState(
    val isLoadingSettings: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val allSupportedApps: List<DistractingApp> = emptyList(),
    val customTargetAppCandidates: List<CustomTargetAppCandidate> = emptyList(),
    val availableTargetApps: List<DistractingApp> = emptyList(),
    val onboardingSelection: OnboardingSelection = OnboardingSelection(
        selectedAppPackages = emptySet(),
        preferredTopics = emptySet(),
        preferredDurationBucket = DurationBucket.FOCUS,
        selectedPackIds = emptySet(),
    ),
    val selectedTargetApp: DistractingApp? = null,
    val preferences: UserPreferences? = null,
    val starterPacks: List<EditorialPack> = emptyList(),
    val currentInterventionId: String? = null,
    val currentInterventionShownAtMillis: Long? = null,
    val currentOpenAnywayUnlockAvailableAtMillis: Long? = null,
    val currentRecommendationSet: RecommendationSet? = null,
    val currentInterventionOrigin: InterventionOrigin? = null,
    val currentInterventionMetadata: Map<String, String> = emptyMap(),
    val currentInterventionSuppressionKey: String? = null,
    val currentContent: ContentItem? = null,
    val currentReaderDocument: ReaderDocument? = null,
    val currentContentBody: String = "",
    val isReaderOpening: Boolean = false,
    val currentReadingProgress: ReadingProgress? = null,
    val currentReaderStartParagraphIndex: Int? = null,
    val currentReaderStartSelector: ReadingAnnotationSelector? = null,
    val currentSessionId: String? = null,
    val currentSessionStartedAtMillis: Long? = null,
    val activeDelayWindow: DelayWindow? = null,
    val activeDelaySuggestion: ContentItem? = null,
    val permissionReadiness: PermissionReadiness = unavailablePermissionReadiness(),
    val historyEntries: List<ReplacementHistoryEntry> = emptyList(),
    val completedContentIds: Set<String> = emptySet(),
    val readingProgress: List<ReadingProgress> = emptyList(),
    val readingAnnotations: List<ReadingAnnotation> = emptyList(),
    val userLinks: List<ContentItem> = emptyList(),
    val userDocuments: List<ContentItem> = emptyList(),
    val addLinkForm: AddLinkFormState = AddLinkFormState(),
    val addDocumentForm: AddDocumentFormState = AddDocumentFormState(),
    val savedLinkConfirmation: AddLinkConfirmation? = null,
    val interventionMode: InterventionMode = DEFAULT_INTERVENTION_MODE,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val meditationDurationMinutes: Int = DEFAULT_MEDITATION_MINUTES,
    val readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
    val interfaceTextScale: Double = DEFAULT_INTERFACE_TEXT_SCALE,
    val contentPriority: ContentPriority = ContentPriority.BALANCED,
    val priorityContentIds: Set<String> = emptySet(),
    val reactivatedCompletedContentIds: Set<String> = emptySet(),
    val openAnywayUnlockMinutes: Int = DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES,
    val bedtimeEnabled: Boolean = DEFAULT_BEDTIME_ENABLED,
    val bedtimeStartMinutes: Int = DEFAULT_BEDTIME_START_MINUTES,
    val bedtimeEndMinutes: Int = DEFAULT_BEDTIME_END_MINUTES,
    val isBedtimeActive: Boolean = false,
    val currentInterventionBedtimeEnforced: Boolean = false,
    val annotationExportUri: String? = null,
    val annotationExportDisplayName: String? = null,
    val annotationExportUsesLocalDefault: Boolean = false,
    val annotationExportLastSuccessfulAtMillis: Long? = null,
    val annotationExportLastError: String? = null,
    val annotationDriveSyncEnabled: Boolean = false,
    val annotationDriveFolderId: String? = null,
    val annotationDriveLastSuccessfulAtMillis: Long? = null,
    val annotationDriveLastError: String? = null,
    val agentInboxDriveEnabled: Boolean = false,
    val agentInboxDriveFolderId: String? = null,
    val agentInboxDriveGrantMode: String? = null,
    val agentInboxDriveLastSuccessfulAtMillis: Long? = null,
    val agentInboxDriveLastError: String? = null,
    val agentInboxDriveFolderDraft: String = "",
    val agentInboxDriveFolderDraftError: String? = null,
    val isAgentInboxDriveFolderBrowserOpen: Boolean = false,
    val isAgentInboxDriveFolderBrowserLoading: Boolean = false,
    val agentInboxDriveFolderBrowserLocation: AgentInboxDriveFolderBrowserLocation =
        AgentInboxDriveFolderBrowserLocation.Root,
    val agentInboxDriveFolderBrowserBackStack: List<AgentInboxDriveFolderBrowserLocation> = emptyList(),
    val agentInboxDriveFolderOptions: List<AgentInboxDriveFolderOption> = emptyList(),
    val agentInboxDriveFolderBrowserHasMore: Boolean = false,
    val agentInboxDriveFolderBrowserError: String? = null,
    val isAgentInboxScanning: Boolean = false,
    val isAgentInboxImporting: Boolean = false,
    val agentInboxCandidates: List<AgentInboxReviewCandidate> = emptyList(),
    val agentInboxPriorityAcceptedPackageIds: Set<String> = emptySet(),
    val agentInboxScanTruncated: Boolean = false,
    val profileAutosaveUri: String? = null,
    val profileAutosaveDisplayName: String? = null,
    val profileAutosaveUsesLocalDefault: Boolean = false,
    val profileAutosaveLastSuccessfulAtMillis: Long? = null,
    val profileAutosaveLastError: String? = null,
    val websiteRules: List<WebsiteRule> = emptyList(),
    val websiteRuleDraftText: String = "",
    val websiteRuleDraftWildcard: Boolean = false,
    val websiteRuleDraftIncludeApex: Boolean = false,
    val websiteRuleDraftEditingId: String? = null,
    val websiteRuleDraftError: String? = null,
    val isProfileAutosaving: Boolean = false,
    val isAnnotationDriveSyncing: Boolean = false,
    val isAccountLightExporting: Boolean = false,
    val isAccountLightImporting: Boolean = false,
    val accountLightImportPreview: AccountLightImportPreview? = null,
    val accountLightImportError: String? = null,
    val accountLightStatus: String? = null,
    val isAccountLightReplaceConfirming: Boolean = false,
    val isManagingLibrary: Boolean = false,
    val selectedLibraryContentIds: Set<String> = emptySet(),
    val latestMessage: String? = null,
    // Bumped by the ViewModel whenever a message is freshly set, so the snackbar effect re-fires even
    // when the same text is shown twice in a row (keying on the text alone would swallow the repeat).
    val latestMessageId: Long = 0L,
    val events: List<AnalyticsEvent> = emptyList(),
    val screen: MainScreen = MainScreen.Onboarding,
    val lastFeedback: SessionFeedback? = null,
) {
    val hasAgentInboxPickerFolderGrant: Boolean
        get() = agentInboxDriveEnabled &&
            !agentInboxDriveFolderId.isNullOrBlank() &&
            agentInboxDriveGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER

    val hasAgentInboxReadonlyFolderGrant: Boolean
        get() = agentInboxDriveEnabled &&
            !agentInboxDriveFolderId.isNullOrBlank() &&
            agentInboxDriveGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER

    val hasAgentInboxDocumentTreeFolderGrant: Boolean
        get() = agentInboxDriveEnabled &&
            !agentInboxDriveFolderId.isNullOrBlank() &&
            agentInboxDriveGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER

    val hasAgentInboxDriveFolderGrant: Boolean
        get() = hasAgentInboxReadonlyFolderGrant ||
            hasAgentInboxDocumentTreeFolderGrant
}

data class AgentInboxDriveFolderBrowserLocation(
    val folderId: String?,
    val name: String,
) {
    companion object {
        val Root = AgentInboxDriveFolderBrowserLocation(folderId = null, name = "My Drive")
    }
}

data class AgentInboxDriveFolderOption(
    val id: String,
    val name: String,
)

internal fun parseAgentInboxDriveFolderId(value: String): String? {
    val trimmed = value.trim().trim('"', '\'')
    if (trimmed.isBlank()) return null
    val fromFolderPath = Regex("""/folders/([^/?#]+)""").find(trimmed)?.groupValues?.getOrNull(1)
    val fromQuery = Regex("""[?&]id=([^&#]+)""").find(trimmed)?.groupValues?.getOrNull(1)
    val candidate = (fromFolderPath ?: fromQuery ?: trimmed).trim()
    if (candidate.startsWith("http://") || candidate.startsWith("https://")) return null
    return candidate.takeIf { it.matches(Regex("""[A-Za-z0-9_-]{8,}""")) }
}

data class AddLinkConfirmation(
    val title: String,
    val host: String,
    val durationMinutes: Int,
    val topicLabel: String,
    val savedCount: Int = 1,
    val skippedCount: Int = 0,
    val priorityMarked: Boolean = false,
)

data class AddLinkFormState(
    val url: String = "",
    val title: String = "",
    val durationMinutes: String = ReadingTimeEstimator.DEFAULT_LINK_MINUTES.toString(),
    val selectedTopics: Set<TopicTag> = emptySet(),
    val markPriority: Boolean = false,
    val validationErrors: Set<UserLinkValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
)

data class DocumentImportCandidate(
    val uri: String,
    val displayName: String,
    val mimeType: String? = null,
    val title: String,
    val durationMinutes: String,
    val format: ContentFormat? = null,
    val estimateSource: ReadingTimeEstimateSource = ReadingTimeEstimateSource.FALLBACK_DEFAULT,
    val estimatedWordCount: Int? = null,
    val imageAttachmentUris: Map<String, String> = emptyMap(),
)

data class AddDocumentFormState(
    val uri: String = "",
    val displayName: String = "",
    val mimeType: String? = null,
    val title: String = "",
    val durationMinutes: String = ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString(),
    val candidates: List<DocumentImportCandidate> = emptyList(),
    val selectedTopics: Set<TopicTag> = emptySet(),
    val markPriority: Boolean = false,
    val validationErrors: Set<UserDocumentValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isPreparing: Boolean = false,
    val isSaving: Boolean = false,
) {
    val importCount: Int
        get() = candidates.size.coerceAtLeast(if (uri.isBlank()) 0 else 1)

    val supportedImportCount: Int
        get() = documentCandidates().count { candidate -> candidate.format != null }

    val unsupportedImportCount: Int
        get() = documentCandidates().count { candidate -> candidate.format == null }
}

enum class MainScreen {
    Onboarding,
    Home,
    Library,
    Annotations,
    Progress,
    Settings,
    AddLink,
    AddDocument,
    AddLinkSuccess,
    Intervention,
    Reader,
    ExternalHandoff,
    MeditationTimer,
    Feedback,
}

enum class InterventionOrigin {
    DEBUG,
    SYSTEM,
}

class MainViewModel(
    private val contentRepository: ContentRepository,
    private val userLinkRepository: UserLinkRepository = EmptyUserLinkRepository,
    private val userDocumentRepository: UserDocumentRepository = EmptyUserDocumentRepository,
    private val settingsRepository: SettingsRepository,
    private val recommendationEngine: RecommendationEngine,
    private val delayGate: DelayGate,
    private val analyticsTracker: AnalyticsTracker,
    private val historyRepository: HistoryRepository,
    private val readingProgressRepository: ReadingProgressRepository = EmptyReadingProgressRepository,
    private val readingAnnotationRepository: ReadingAnnotationRepository = EmptyReadingAnnotationRepository,
    private val readingAnnotationExportWriter: ReadingAnnotationExportWriter = NoOpReadingAnnotationExportWriter,
    private val readingAnnotationDriveSyncClient: ReadingAnnotationDriveSyncClient = NoOpReadingAnnotationDriveSyncClient,
    private val readingAnnotationDriveTokenProvider: ReadingAnnotationDriveTokenProvider = NoOpReadingAnnotationDriveTokenProvider,
    private var agentInboxDriveClient: AgentInboxDriveClient = NoOpAgentInboxDriveClient,
    private val agentInboxPackageImporter: AgentInboxPackageImporter = AgentInboxPackageImporter(
        userDocumentRepository = userDocumentRepository,
        documentStore = NoOpAgentInboxDocumentStore,
    ),
    private val accountLightProfileExporter: AccountLightProfileExporter = AccountLightProfileExporter(
        settingsRepository = settingsRepository,
        appVersionName = "test",
        appVersionCode = 1,
        userLinkRepository = userLinkRepository,
        userDocumentRepository = userDocumentRepository,
        readingProgressRepository = readingProgressRepository,
    ),
    private val accountLightProfileImporter: AccountLightProfileImporter = AccountLightProfileImporter(
        settingsRepository = settingsRepository,
        userLinkRepository = userLinkRepository,
        userDocumentRepository = userDocumentRepository,
        readingProgressRepository = readingProgressRepository,
        knownContentIdsProvider = { contentRepository.inventory().mapTo(mutableSetOf(), ContentItem::id) },
    ),
    private val accountLightProfileAutosaveWriter: AccountLightProfileAutosaveWriter = NoOpAccountLightProfileAutosaveWriter,
    private val accountLightProfileBackupReader: AccountLightProfileBackupReader = NoOpAccountLightProfileBackupReader,
    private val defaultAnnotationExportUri: String? = null,
    private val defaultAnnotationExportDisplayName: String = LOCAL_ANNOTATION_EXPORT_DISPLAY_NAME,
    private val defaultProfileAutosaveUri: String? = null,
    private val defaultProfileAutosaveDisplayName: String = LOCAL_PROFILE_BACKUP_DISPLAY_NAME,
    private val interceptionMonitor: InterceptionMonitor,
    private val enableDelayRefreshTicker: Boolean = true,
    private val progressPersistenceScope: CoroutineScope? = null,
    private val documentWorkDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nowProvider: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val supportedApps = settingsRepository.supportedDistractingApps()
    private val customTargetAppCandidates = settingsRepository.customTargetAppCandidates()
    private val starterPacks = contentRepository.starterPacks()
    private val defaultSelectedPackIds = defaultStarterPackIds(starterPacks)
    private var settingsLoaded = false
    private var contentReady = contentRepository.isReady()
    private var userDocumentReady = userDocumentRepository.isReady()
    private var analyticsReady = analyticsTracker.isReady()
    private var historyReady = historyRepository.isReady()
    private var readingProgressReady = readingProgressRepository.isReady()
    private var readingAnnotationReady = readingAnnotationRepository.isReady()
    private var delayReady = delayGate.isReady()
    private var historyCompletedContentIds: Set<String> = emptySet()
    private var readingProgressCompletedContentIds: Set<String> = emptySet()
    private var lastReadingProgressUpdatedAtMillis = 0L
    private var pendingSystemInterception: PendingSystemInterception? = null
    private var pendingAccountLightImportPlan: AccountLightImportPlan? = null
    private var annotationDriveAccessToken: String? = null
    private var documentImportPreparationRequestId = 0
    private var pendingDocumentImportBase: PendingDocumentImportBase? = null
    private var readerOpenRequestId = 0
    // Single-flight for interventions: a newer trigger supersedes an older in-flight one so two rapid
    // triggers cannot interleave their writes to the shared intervention state (latest wins).
    private var interventionJob: Job? = null
    private val durationRepairAttemptedContentIds = mutableSetOf<String>()
    private val durationRepairInFlightContentIds = mutableSetOf<String>()
    private val durationRepairEventRecordedContentIds = mutableSetOf<String>()
    private var legacyReadingTimeBackgroundRepairCycleStarted = false
    private val readingAnnotationExportFormatter = ReadingAnnotationExportFormatter()
    // Coalesces high-frequency profile autosave triggers (one per reading-progress save while
    // scrolling). CONFLATED keeps only the latest pending request, so a burst settles into a single
    // full-profile export+write instead of one per scroll. A write already in flight is never
    // cancelled (the collector consumes one request at a time), so the backup file is never left
    // half-written. The mutex serialises this path with the direct restore/merge autosave path.
    private val profileAutosaveRequests = Channel<Long>(Channel.CONFLATED)
    private val profileAutosaveMutex = Mutex()
    // Serialises Drive syncs so a first sync persists the folder id before a second one reads it,
    // preventing concurrent first-time syncs from each creating a duplicate Drive folder.
    private val annotationDriveSyncMutex = Mutex()

    private var latestMessageIdCounter = 0L
    private val uiStateHolder = mutableStateOf(
        MainUiState(
            allSupportedApps = supportedApps,
            customTargetAppCandidates = customTargetAppCandidates,
            starterPacks = starterPacks,
            onboardingSelection = defaultOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            events = analyticsTracker.allEvents(),
            historyEntries = historyRepository.recentHistory(),
            readingProgress = readingProgressRepository.readingProgress(),
            readingAnnotations = readingAnnotationRepository.readingAnnotations(),
            permissionReadiness = interceptionMonitor.currentReadiness(),
        ),
    )
    var uiState: MainUiState
        get() = uiStateHolder.value
        private set(value) {
            // Centralised so the ~100 call sites that copy(latestMessage = ...) need no change: when a
            // message transitions onto a non-null text, stamp it with a fresh id so the snackbar shows
            // even repeated identical text.
            val previous = uiStateHolder.value
            uiStateHolder.value = if (value.latestMessage != null && value.latestMessage != previous.latestMessage) {
                value.copy(latestMessageId = ++latestMessageIdCounter)
            } else {
                value
            }
        }

    init {
        viewModelScope.launch {
            for (requestedAtMillis in profileAutosaveRequests) {
                autosaveAccountLightProfileIfConfigured(nowMillis = requestedAtMillis)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeAppSettings().collect { settings ->
                settingsLoaded = true
                applySettings(settings)
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            contentRepository.observeReady().collect { ready ->
                contentReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            userLinkRepository.observeUserLinks().collect { links ->
                uiState = uiState.copy(userLinks = links)
            }
        }
        viewModelScope.launch {
            userDocumentRepository.observeUserDocuments().collect { documents ->
                uiState = uiState.copy(userDocuments = documents)
            }
        }
        viewModelScope.launch {
            userDocumentRepository.observeReady().collect { ready ->
                userDocumentReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            combine(
                userDocumentRepository.observeUserDocuments(),
                readingProgressRepository.observeReadingProgress(),
            ) { documents, progress -> documents to progress }
                .collect { (documents, progress) ->
                    scheduleLegacyReadingTimeEstimateRepairs(
                        documents = documents,
                        progress = progress,
                    )
                }
        }
        viewModelScope.launch {
            analyticsTracker.observeEvents().collect { events ->
                uiState = uiState.copy(events = events)
            }
        }
        viewModelScope.launch {
            analyticsTracker.observeReady().collect { ready ->
                analyticsReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            historyRepository.observeRecentHistory(windowDays = PROGRESS_HISTORY_WINDOW_DAYS).collect { history ->
                uiState = uiState.copy(
                    historyEntries = history,
                )
            }
        }
        viewModelScope.launch {
            historyRepository.observeCompletedContentIds().collect { completedIds ->
                historyCompletedContentIds = completedIds.trackedCompletedContentIds()
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            historyRepository.observeReady().collect { ready ->
                historyReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeReadingProgress().collect { progress ->
                val effectiveProgress = progress.withActiveProgressOverride(
                    activeProgress = uiState.currentReadingProgress?.takeIf { currentProgress ->
                        currentProgress.contentId == uiState.currentContent?.id &&
                            progress.none { storedProgress ->
                                storedProgress.contentId == currentProgress.contentId &&
                                    storedProgress.updatedAtMillis >= currentProgress.updatedAtMillis
                            }
                    },
                )
                readingProgressCompletedContentIds = effectiveProgress.filter(ReadingProgress::isCompleted)
                    .mapTo(mutableSetOf(), ReadingProgress::contentId)
                    .trackedCompletedContentIds()
                val unfinishedIds = effectiveProgress.unfinishedContentIds()
                uiState = uiState.copy(
                    readingProgress = effectiveProgress,
                    preferences = uiState.preferences?.copy(unfinishedContentIds = unfinishedIds),
                    currentReadingProgress = uiState.currentContent?.id?.let { contentId ->
                        effectiveProgress.firstOrNull { candidate -> candidate.contentId == contentId && candidate.isUnfinished() }
                    },
                )
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeCompletedContentIds().collect { completedIds ->
                readingProgressCompletedContentIds = completedIds.trackedCompletedContentIds()
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeReady().collect { ready ->
                readingProgressReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            readingAnnotationRepository.observeReadingAnnotations().collect { annotations ->
                uiState = uiState.copy(readingAnnotations = annotations)
            }
        }
        viewModelScope.launch {
            readingAnnotationRepository.observeReady().collect { ready ->
                readingAnnotationReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            delayGate.observeReady().collect { ready ->
                delayReady = ready
                refreshActiveDelayWindow()
                if (ready) {
                    uiState.selectedTargetApp?.let { targetApp ->
                        reconcilePersistedDelayAnalytics(targetApp = targetApp)
                    }
                }
                updateHydrationState()
            }
        }
        if (enableDelayRefreshTicker) {
            viewModelScope.launch {
                while (true) {
                    delay(ACTIVE_DELAY_REFRESH_INTERVAL_MILLIS)
                    refreshActiveDelayWindow()
                    refreshBedtimeInterventionBoundary()
                }
            }
        }
    }

    fun selectTargetApp(app: DistractingApp) {
        val activeDelayWindow = if (delayReady) delayGate.activeDelay(app) else null
        uiState = uiState.copy(
            selectedTargetApp = app,
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            latestMessage = null,
        )
        if (delayReady) {
            viewModelScope.launch {
                reconcilePersistedDelayAnalytics(targetApp = app)
            }
        }
    }

    fun refreshPermissionReadiness() {
        uiState = uiState.copy(permissionReadiness = interceptionMonitor.currentReadiness())
    }

    fun toggleOnboardingApp(app: DistractingApp) {
        val selected = uiState.onboardingSelection.selectedAppPackages.toMutableSet()
        if (!selected.add(app.packageName)) {
            selected.remove(app.packageName)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(selectedAppPackages = selected),
        )
    }

    fun toggleOnboardingTopic(topic: TopicTag) {
        val selected = uiState.onboardingSelection.preferredTopics.toMutableSet()
        if (!selected.add(topic)) {
            selected.remove(topic)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(preferredTopics = selected),
        )
    }

    fun setOnboardingDuration(durationBucket: DurationBucket) {
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(preferredDurationBucket = durationBucket),
        )
    }

    fun toggleSettingsApp(app: DistractingApp) {
        val preferences = uiState.preferences ?: return
        val selectedPackages = preferences.selectedApps.mapTo(mutableSetOf(), DistractingApp::packageName)
        val isRemoving = app.packageName in selectedPackages
        if (isRemoving) {
            selectedPackages.remove(app.packageName)
        } else {
            selectedPackages.add(app.packageName)
        }
        if (isRemoving && selectedPackages.size < MIN_SELECTED_DISTRACTING_APPS) {
            uiState = uiState.copy(latestMessage = "Keep at least 3 apps selected for the alpha.")
            return
        }

        val selectedApps = supportedApps.filter { supportedApp ->
            supportedApp.packageName in selectedPackages
        }
        val selectedTargetApp = uiState.selectedTargetApp
            ?.takeIf { it.packageName in selectedPackages }
            ?: selectedApps.firstOrNull()
        val activeDelayWindow = selectedTargetApp?.takeIf { delayReady }?.let { delayGate.activeDelay(it) }

        uiState = uiState.copy(
            availableTargetApps = selectedApps,
            selectedTargetApp = selectedTargetApp,
            preferences = preferences.copy(selectedApps = selectedApps),
            onboardingSelection = uiState.onboardingSelection.copy(selectedAppPackages = selectedPackages),
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveSelectedAppPackages(selectedPackages)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun updateWebsiteRuleDraftText(value: String) {
        val explicitWildcard = value.trim().startsWith("*.")
        uiState = uiState.copy(
            websiteRuleDraftText = value,
            websiteRuleDraftWildcard = uiState.websiteRuleDraftWildcard || explicitWildcard,
            websiteRuleDraftError = null,
            latestMessage = null,
        )
    }

    fun setWebsiteRuleDraftWildcard(enabled: Boolean) {
        uiState = uiState.copy(
            websiteRuleDraftWildcard = enabled,
            websiteRuleDraftIncludeApex = if (enabled) uiState.websiteRuleDraftIncludeApex else false,
            websiteRuleDraftError = null,
            latestMessage = null,
        )
    }

    fun setWebsiteRuleDraftIncludeApex(enabled: Boolean) {
        uiState = uiState.copy(
            websiteRuleDraftIncludeApex = enabled,
            websiteRuleDraftError = null,
            latestMessage = null,
        )
    }

    fun beginEditWebsiteRule(rule: WebsiteRule) {
        uiState = uiState.copy(
            websiteRuleDraftText = rule.host,
            websiteRuleDraftWildcard = rule.type == WebsiteRuleType.WILDCARD_SUBDOMAINS,
            websiteRuleDraftIncludeApex = rule.includeApex,
            websiteRuleDraftEditingId = rule.id,
            websiteRuleDraftError = null,
            latestMessage = null,
        )
    }

    fun cancelWebsiteRuleEdit() {
        uiState = uiState.copy(
            websiteRuleDraftText = "",
            websiteRuleDraftWildcard = false,
            websiteRuleDraftIncludeApex = false,
            websiteRuleDraftEditingId = null,
            websiteRuleDraftError = null,
            latestMessage = null,
        )
    }

    fun saveWebsiteRuleDraft(nowMillis: Long = nowProvider()) {
        val result = WebsiteRuleNormalizer.normalize(
            input = uiState.websiteRuleDraftText,
            wildcard = uiState.websiteRuleDraftWildcard,
        )
        if (result is WebsiteRuleDraftResult.Invalid) {
            uiState = uiState.copy(websiteRuleDraftError = result.message, latestMessage = null)
            return
        }
        val valid = result as WebsiteRuleDraftResult.Valid
        val editingId = uiState.websiteRuleDraftEditingId
        val includeApex = valid.type == WebsiteRuleType.WILDCARD_SUBDOMAINS && uiState.websiteRuleDraftIncludeApex
        val duplicate = uiState.websiteRules.any { rule ->
            rule.id != editingId &&
                rule.type == valid.type &&
                rule.host == valid.host &&
                rule.includeApex == includeApex
        }
        if (duplicate) {
            uiState = uiState.copy(websiteRuleDraftError = "This website rule already exists.", latestMessage = null)
            return
        }

        val updatedRule = uiState.websiteRules.firstOrNull { it.id == editingId }?.copy(
            type = valid.type,
            host = valid.host,
            includeApex = includeApex,
            updatedAtMillis = nowMillis.coerceAtLeast(0L),
        ) ?: WebsiteRule(
            id = "website-rule-${UUID.randomUUID()}",
            type = valid.type,
            host = valid.host,
            includeApex = includeApex,
            enabled = true,
            createdAtMillis = nowMillis.coerceAtLeast(0L),
            updatedAtMillis = nowMillis.coerceAtLeast(0L),
        )
        val updatedRules = if (editingId == null) {
            uiState.websiteRules + updatedRule
        } else {
            uiState.websiteRules.map { rule -> if (rule.id == editingId) updatedRule else rule }
        }.sortedWith(compareBy<WebsiteRule> { it.host }.thenBy { it.type.name }.thenBy { it.id })

        persistWebsiteRules(
            rules = updatedRules,
            message = if (editingId == null) "Website rule saved." else "Website rule updated.",
            nowMillis = nowMillis,
        )
        clearWebsiteRuleDraft(clearMessage = false)
    }

    fun toggleWebsiteRuleEnabled(ruleId: String, nowMillis: Long = nowProvider()) {
        val updatedRules = uiState.websiteRules.map { rule ->
            if (rule.id == ruleId) {
                rule.copy(enabled = !rule.enabled, updatedAtMillis = nowMillis.coerceAtLeast(0L))
            } else {
                rule
            }
        }
        persistWebsiteRules(rules = updatedRules, message = "Website rule updated.", nowMillis = nowMillis)
    }

    fun deleteWebsiteRule(ruleId: String, nowMillis: Long = nowProvider()) {
        val wasEditing = uiState.websiteRuleDraftEditingId == ruleId
        val updatedRules = uiState.websiteRules.filterNot { it.id == ruleId }
        persistWebsiteRules(rules = updatedRules, message = "Website rule deleted.", nowMillis = nowMillis)
        if (wasEditing) clearWebsiteRuleDraft(clearMessage = false)
    }

    private fun clearWebsiteRuleDraft(clearMessage: Boolean) {
        uiState = uiState.copy(
            websiteRuleDraftText = "",
            websiteRuleDraftWildcard = false,
            websiteRuleDraftIncludeApex = false,
            websiteRuleDraftEditingId = null,
            websiteRuleDraftError = null,
            latestMessage = if (clearMessage) null else uiState.latestMessage,
        )
    }

    private fun persistWebsiteRules(rules: List<WebsiteRule>, message: String, nowMillis: Long) {
        uiState = uiState.copy(websiteRules = rules, latestMessage = message)
        viewModelScope.launch {
            settingsRepository.saveWebsiteRules(rules)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
        }
    }

    fun setPreferredDuration(durationBucket: DurationBucket) {
        val preferences = uiState.preferences
        uiState = uiState.copy(
            preferences = preferences?.copy(preferredDurationBucket = durationBucket),
            onboardingSelection = uiState.onboardingSelection.copy(preferredDurationBucket = durationBucket),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.savePreferredDurationBucket(durationBucket)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setMeditationDurationMinutes(minutes: Int) {
        val preferences = uiState.preferences
        val meditation = meditationTimerContentItem(minutes)
        val isCurrentMeditation = uiState.currentContent?.usesMeditationTimer() == true
        val activeSessionId = uiState.currentSessionId
        uiState = uiState.copy(
            meditationDurationMinutes = meditation.durationMinutes,
            preferences = preferences?.copy(meditationDurationMinutes = meditation.durationMinutes),
            currentRecommendationSet = uiState.currentRecommendationSet?.withMeditationDuration(meditation),
            currentContent = uiState.currentContent?.replaceIfMeditation(meditation),
            currentSessionStartedAtMillis = if (isCurrentMeditation) {
                nowProvider()
            } else {
                uiState.currentSessionStartedAtMillis
            },
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveMeditationDurationMinutes(meditation.durationMinutes)
            if (isCurrentMeditation && activeSessionId != null) {
                historyRepository.updateAcceptedSessionContent(sessionId = activeSessionId, content = meditation)
            }
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setReaderFontScale(scale: Double) {
        val normalizedScale = scale.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE)
        if (uiState.readerFontScale == normalizedScale) return
        uiState = uiState.copy(readerFontScale = normalizedScale, latestMessage = null)
        viewModelScope.launch {
            settingsRepository.saveReaderFontScale(normalizedScale)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setInterfaceTextScale(scale: Double) {
        val normalizedScale = scale.coerceIn(MIN_INTERFACE_TEXT_SCALE, MAX_INTERFACE_TEXT_SCALE)
        if (uiState.interfaceTextScale == normalizedScale) return
        uiState = uiState.copy(interfaceTextScale = normalizedScale, latestMessage = null)
        viewModelScope.launch {
            settingsRepository.saveInterfaceTextScale(normalizedScale)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setContentPriority(priority: ContentPriority) {
        val preferences = uiState.preferences
        uiState = uiState.copy(
            contentPriority = priority,
            preferences = preferences?.copy(contentPriority = priority),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveContentPriority(priority)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun togglePriorityContent(item: ContentItem) {
        val selectedIds = uiState.priorityContentIds.toMutableSet()
        val isPrioritized = if (selectedIds.add(item.id)) {
            true
        } else {
            selectedIds.remove(item.id)
            false
        }
        val updatedIds = selectedIds.toSet()
        val preferences = uiState.preferences
        uiState = uiState.copy(
            priorityContentIds = updatedIds,
            preferences = preferences?.copy(priorityContentIds = updatedIds),
            latestMessage = if (isPrioritized) {
                "Prioritizing ${item.title}."
            } else {
                "Removed ${item.title} from priority picks."
            },
        )
        viewModelScope.launch {
            settingsRepository.savePriorityContentIds(updatedIds)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.PRIORITY_CONTENT_TOGGLED,
                    timestampMillis = nowProvider(),
                    contentId = item.id,
                    metadata = item.analyticsMetadata() + mapOf(
                        "priorityEnabled" to isPrioritized.toString(),
                        "priorityContentCount" to updatedIds.size.toString(),
                    ),
                ),
            )
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun toggleCompletedContentActivation(item: ContentItem) {
        if (item.id !in uiState.completedContentIds) {
            uiState = uiState.copy(latestMessage = "${item.title} is not completed yet.")
            return
        }
        val activeIds = uiState.reactivatedCompletedContentIds.toMutableSet()
        val isReactivated = if (activeIds.add(item.id)) {
            true
        } else {
            activeIds.remove(item.id)
            false
        }
        val updatedIds = activeIds.toSet()
        val activeDelaySuggestion = activeDelaySuggestionFor(
            delayWindow = uiState.activeDelayWindow,
            excludedContentIds = uiState.completedContentIds - updatedIds,
        )
        uiState = uiState.copy(
            reactivatedCompletedContentIds = updatedIds,
            activeDelaySuggestion = activeDelaySuggestion,
            latestMessage = if (isReactivated) {
                "${item.title} can appear in suggestions again."
            } else {
                "${item.title} is hidden from suggestions again."
            },
        )
        viewModelScope.launch {
            if (isReactivated) {
                readingProgressRepository.deleteProgress(item.id)
            }
            settingsRepository.saveReactivatedCompletedContentIds(updatedIds)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.COMPLETED_CONTENT_ACTIVATION_TOGGLED,
                    timestampMillis = nowProvider(),
                    contentId = item.id,
                    metadata = item.analyticsMetadata() + mapOf(
                        "reactivated" to isReactivated.toString(),
                        "reactivatedCompletedContentCount" to updatedIds.size.toString(),
                    ),
                ),
            )
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setOpenAnywayUnlockMinutes(minutes: Int) {
        val safeMinutes = minutes.coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
        uiState = uiState.copy(
            openAnywayUnlockMinutes = safeMinutes,
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveOpenAnywayUnlockMinutes(safeMinutes)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun setBedtimeSettings(
        enabled: Boolean = uiState.bedtimeEnabled,
        startMinutes: Int = uiState.bedtimeStartMinutes,
        endMinutes: Int = uiState.bedtimeEndMinutes,
    ) {
        val safeStartMinutes = startMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES)
        val safeEndMinutes = endMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES)
        val activeNow = bedtimeWindowIsActive(
            enabled = enabled,
            startMinutes = safeStartMinutes,
            endMinutes = safeEndMinutes,
            nowMillis = nowProvider(),
        )
        uiState = uiState.copy(
            bedtimeEnabled = enabled,
            bedtimeStartMinutes = safeStartMinutes,
            bedtimeEndMinutes = safeEndMinutes,
            isBedtimeActive = activeNow,
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveBedtimeSettings(
                enabled = enabled,
                startMinutes = safeStartMinutes,
                endMinutes = safeEndMinutes,
            )
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun configureReadingAnnotationExport(
        uri: String,
        displayName: String,
        persistWritePermission: (String) -> Unit = {},
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedUri = uri.trim()
        if (normalizedUri.isBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a destination for annotation sync.")
            return
        }
        val normalizedDisplayName = displayName.trim().ifBlank { "Annotation sync folder" }
        viewModelScope.launch {
            val permissionError = runCatching { persistWritePermission(normalizedUri) }.exceptionOrNull()
            settingsRepository.saveAnnotationExportDestination(
                uri = normalizedUri,
                displayName = normalizedDisplayName,
            )
            if (permissionError != null) {
                val message = permissionError.annotationExportErrorMessage()
                settingsRepository.saveAnnotationExportFailure(message)
                uiState = uiState.copy(
                    annotationExportUri = normalizedUri,
                    annotationExportDisplayName = normalizedDisplayName,
                    annotationExportUsesLocalDefault = false,
                    annotationExportLastSuccessfulAtMillis = null,
                    annotationExportLastError = message,
                    latestMessage = "Annotation sync needs folder permission.",
                )
                return@launch
            }
            val exported = exportReadingAnnotationsTo(
                uri = normalizedUri,
                nowMillis = nowMillis,
            )
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                annotationExportUri = normalizedUri,
                annotationExportDisplayName = normalizedDisplayName,
                annotationExportUsesLocalDefault = false,
                annotationExportLastSuccessfulAtMillis = if (exported) nowMillis else null,
                latestMessage = if (exported) {
                    "Annotation sync destination changed."
                } else {
                    "Annotation sync destination changed, but the first write failed."
                },
            )
        }
    }

    fun connectAnnotationDriveFolderProvider(
        uri: String,
        displayName: String,
        persistWritePermission: (String) -> Unit = {},
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedUri = uri.trim()
        if (normalizedUri.isBlank()) {
            reportAnnotationDriveAuthorizationFailure("Google Drive folder was not selected.")
            return
        }
        val normalizedDisplayName = displayName.trim().ifBlank { "Google Drive annotation folder" }
        viewModelScope.launch {
            val permissionError = runCatching { persistWritePermission(normalizedUri) }.exceptionOrNull()
            settingsRepository.saveAnnotationExportDestination(
                uri = normalizedUri,
                displayName = normalizedDisplayName,
            )
            if (permissionError != null) {
                val message = permissionError.annotationExportErrorMessage()
                settingsRepository.saveAnnotationExportFailure(message)
                uiState = uiState.copy(
                    annotationExportUri = normalizedUri,
                    annotationExportDisplayName = normalizedDisplayName,
                    annotationExportUsesLocalDefault = false,
                    annotationExportLastSuccessfulAtMillis = null,
                    annotationExportLastError = message,
                    annotationDriveLastError = message,
                    isAnnotationDriveSyncing = false,
                    latestMessage = "Google Drive folder needs permission.",
                )
                return@launch
            }
            settingsRepository.clearAnnotationDriveSyncConnection()
            val exported = exportReadingAnnotationsTo(
                uri = normalizedUri,
                nowMillis = nowMillis,
            )
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                annotationExportUri = normalizedUri,
                annotationExportDisplayName = normalizedDisplayName,
                annotationExportUsesLocalDefault = false,
                annotationExportLastSuccessfulAtMillis = if (exported) nowMillis else null,
                annotationDriveLastError = null,
                annotationDriveSyncEnabled = false,
                annotationDriveFolderId = null,
                annotationDriveLastSuccessfulAtMillis = null,
                isAnnotationDriveSyncing = false,
                latestMessage = if (exported) {
                    "Google Drive folder connected."
                } else {
                    "Google Drive folder connected, but the first write failed."
                },
            )
        }
    }

    fun clearReadingAnnotationExport(releaseWritePermission: (String) -> Unit = {}) {
        val uri = uiState.annotationExportUri
        val wasUsingLocalDefault = uiState.annotationExportUsesLocalDefault
        viewModelScope.launch {
            if (!wasUsingLocalDefault) {
                uri?.takeIf(String::isNotBlank)?.let { configuredUri ->
                    runCatching { releaseWritePermission(configuredUri) }
                }
            }
            settingsRepository.clearAnnotationExportDestination()
            autosaveAccountLightProfileAfterPortableMutation()
            val defaultUri = normalizedDefaultAnnotationExportUri()
            val hasDefault = defaultUri != null
            uiState = uiState.copy(
                annotationExportUri = defaultUri,
                annotationExportDisplayName = defaultAnnotationExportDisplayName.takeIf { hasDefault },
                annotationExportUsesLocalDefault = hasDefault,
                annotationExportLastSuccessfulAtMillis = null,
                annotationExportLastError = null,
                latestMessage = if (hasDefault) {
                    "Annotation sync returned to app storage."
                } else {
                    "Annotation autosave disabled."
                },
            )
        }
    }

    fun retryReadingAnnotationExport(nowMillis: Long = nowProvider()) {
        val uri = uiState.annotationExportUri
        if (uri.isNullOrBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a folder for annotation sync.")
            return
        }
        viewModelScope.launch {
            val exported = exportReadingAnnotationsTo(uri = uri, nowMillis = nowMillis)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                latestMessage = if (exported) {
                    "Annotations synced locally."
                } else {
                    "Annotation sync failed."
                },
            )
        }
    }

    fun beginAnnotationDriveAuthorization() {
        uiState = uiState.copy(
            isAnnotationDriveSyncing = true,
            annotationDriveLastError = null,
            latestMessage = null,
        )
    }

    fun connectAnnotationDriveSync(accessToken: String, nowMillis: Long = nowProvider()) {
        val normalizedToken = accessToken.trim()
        if (normalizedToken.isBlank()) {
            reportAnnotationDriveAuthorizationFailure("Google Drive did not return an access token.")
            return
        }
        annotationDriveAccessToken = normalizedToken
        uiState = uiState.copy(isAnnotationDriveSyncing = true, latestMessage = null)
        viewModelScope.launch {
            settingsRepository.saveAnnotationDriveSyncConnection(folderId = uiState.annotationDriveFolderId)
            val synced = syncReadingAnnotationsToDrive(accessToken = normalizedToken, nowMillis = nowMillis)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                isAnnotationDriveSyncing = false,
                annotationDriveSyncEnabled = true,
                latestMessage = if (synced) {
                    "Google Drive sync enabled."
                } else {
                    "Google Drive connected, but sync failed."
                },
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_CONNECTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("synced" to synced.toString()),
                ),
            )
        }
    }

    fun retryAnnotationDriveSync(accessToken: String, nowMillis: Long = nowProvider()) {
        val normalizedToken = accessToken.trim()
        if (normalizedToken.isBlank()) {
            reportAnnotationDriveAuthorizationFailure("Google Drive did not return an access token.")
            return
        }
        annotationDriveAccessToken = normalizedToken
        uiState = uiState.copy(isAnnotationDriveSyncing = true, latestMessage = null)
        viewModelScope.launch {
            val synced = syncReadingAnnotationsToDrive(accessToken = normalizedToken, nowMillis = nowMillis)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                isAnnotationDriveSyncing = false,
                latestMessage = if (synced) {
                    "Annotations synced to Google Drive."
                } else {
                    "Google Drive sync failed."
                },
            )
        }
    }

    fun reportAnnotationDriveAuthorizationFailure(errorMessage: String) {
        val message = errorMessage.trim().ifBlank { "Google Drive authorization failed." }
        uiState = uiState.copy(
            isAnnotationDriveSyncing = false,
            annotationDriveLastError = message,
            latestMessage = "Google Drive connection failed.",
        )
        viewModelScope.launch {
            settingsRepository.saveAnnotationDriveSyncFailure(message)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun disconnectAnnotationDriveSync(nowMillis: Long = nowProvider()) {
        annotationDriveAccessToken = null
        uiState = uiState.copy(
            annotationDriveSyncEnabled = false,
            annotationDriveFolderId = null,
            annotationDriveLastSuccessfulAtMillis = null,
            annotationDriveLastError = null,
            isAnnotationDriveSyncing = false,
            latestMessage = "Google Drive sync disconnected.",
        )
        viewModelScope.launch {
            settingsRepository.clearAnnotationDriveSyncConnection()
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_DISCONNECTED,
                    timestampMillis = nowMillis,
                ),
            )
        }
    }

    fun scanAgentInboxDrive(accessToken: String, nowMillis: Long = nowProvider()) {
        if (!userDocumentReady) {
            uiState = uiState.copy(latestMessage = "Agent Inbox is waiting for your library to finish loading.")
            return
        }
        val selectedFolderId = uiState.agentInboxDriveFolderId?.takeIf(String::isNotBlank)
        val selectedGrantMode = uiState.agentInboxDriveGrantMode
        if (selectedGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER) {
            uiState = uiState.copy(
                agentInboxDriveFolderDraft = selectedFolderId.orEmpty(),
                agentInboxDriveFolderDraftError = null,
            )
            reportAgentInboxDriveFailure("Drive file Picker access is not enough for Agent Inbox. Use Drive link access.")
            return
        }
        if (!uiState.hasAgentInboxDriveFolderGrant || selectedFolderId == null || selectedGrantMode == null) {
            reportAgentInboxDriveFailure("Connect an Agent Inbox folder before scanning.")
            return
        }
        val requiresAccessToken = selectedGrantMode != AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER ||
            selectedFolderId.isGoogleDriveDocumentTreeUri()
        val normalizedToken = accessToken.trim()
        if (requiresAccessToken && normalizedToken.isBlank()) {
            reportAgentInboxDriveFailure("Google Drive did not return an access token.")
            return
        }
        uiState = uiState.copy(
            isAgentInboxScanning = true,
            agentInboxDriveLastError = null,
            latestMessage = null,
        )
        viewModelScope.launch {
            try {
                settingsRepository.saveAgentInboxDriveConnection(
                    folderId = selectedFolderId,
                    grantMode = selectedGrantMode,
                )
                val scan = agentInboxDriveClient.scanPackages(
                    AgentInboxDriveScanRequest(
                        accessToken = normalizedToken,
                        folderId = selectedFolderId,
                    ),
                )
                val existingDocumentIdsBySha = userDocumentRepository.userDocuments()
                    .mapNotNull { item ->
                        item.documentFingerprintSha256?.takeIf(String::isNotBlank)?.let { sha -> sha to item.id }
                    }
                    .toMap()
                val candidates = scan.packages.map { drivePackage ->
                    val manifestFile = drivePackage.manifestFile
                    val manifestJson = when {
                        manifestFile == null -> null
                        (manifestFile.sizeBytes ?: 0L) > AGENT_INBOX_MAX_MANIFEST_BYTES -> {
                            return@map AgentInboxReviewCandidateFactory.invalidPackage(
                                drivePackage = drivePackage,
                                packageErrors = setOf(AgentInboxPackageValidationError.MANIFEST_FILE_TOO_LARGE),
                            )
                        }
                        else -> {
                            val manifestBytes = try {
                                agentInboxDriveClient.downloadFile(
                                    accessToken = normalizedToken,
                                    fileId = manifestFile.id,
                                    maxBytes = AGENT_INBOX_MAX_MANIFEST_BYTES,
                                    expectedBytes = manifestFile.sizeBytes,
                                )
                            } catch (tooLarge: AgentInboxDriveDownloadTooLargeException) {
                                return@map AgentInboxReviewCandidateFactory.invalidPackage(
                                    drivePackage = drivePackage,
                                    packageErrors = setOf(AgentInboxPackageValidationError.MANIFEST_FILE_TOO_LARGE),
                                )
                            } catch (error: Throwable) {
                                if (error is CancellationException) throw error
                                if (error is AgentInboxDriveAccessLostException) throw error
                                return@map AgentInboxReviewCandidateFactory.invalidPackage(
                                    drivePackage = drivePackage,
                                    packageErrors = setOf(AgentInboxPackageValidationError.DOWNLOAD_UNAVAILABLE),
                                )
                            }
                            if (manifestBytes.size.toLong() > AGENT_INBOX_MAX_MANIFEST_BYTES) {
                                return@map AgentInboxReviewCandidateFactory.invalidPackage(
                                    drivePackage = drivePackage,
                                    packageErrors = setOf(AgentInboxPackageValidationError.MANIFEST_FILE_TOO_LARGE),
                                )
                            }
                            String(manifestBytes, Charsets.UTF_8)
                        }
                    }
                    val initialCandidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
                        drivePackage = drivePackage,
                        manifestJson = manifestJson,
                        existingDocumentIdsBySha256 = existingDocumentIdsBySha,
                    )
                    val contentFileId = initialCandidate.contentFileId
                    if (!initialCandidate.canImport || contentFileId == null) {
                        initialCandidate
                    } else {
                        val contentFile = drivePackage.allFiles.firstOrNull { file -> file.id == contentFileId }
                        if ((contentFile?.sizeBytes ?: 0L) > AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES) {
                            initialCandidate.copy(
                                status = AgentInboxReviewStatus.INVALID,
                                packageErrors = initialCandidate.packageErrors + AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE,
                            )
                        } else {
                            val contentBytes = try {
                                agentInboxDriveClient.downloadFile(
                                    accessToken = normalizedToken,
                                    fileId = contentFileId,
                                    maxBytes = AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES,
                                    expectedBytes = contentFile?.sizeBytes,
                                )
                            } catch (tooLarge: AgentInboxDriveDownloadTooLargeException) {
                                return@map initialCandidate.copy(
                                    status = AgentInboxReviewStatus.INVALID,
                                    packageErrors = initialCandidate.packageErrors +
                                        AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE,
                                )
                            } catch (error: Throwable) {
                                if (error is CancellationException) throw error
                                if (error is AgentInboxDriveAccessLostException) throw error
                                return@map initialCandidate.copy(
                                    status = AgentInboxReviewStatus.INVALID,
                                    packageErrors = initialCandidate.packageErrors +
                                        AgentInboxPackageValidationError.DOWNLOAD_UNAVAILABLE,
                                )
                            }
                            if (contentBytes.size.toLong() > AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES) {
                                initialCandidate.copy(
                                    status = AgentInboxReviewStatus.INVALID,
                                    packageErrors = initialCandidate.packageErrors + AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE,
                                )
                            } else {
                                AgentInboxReviewCandidateFactory.fromDrivePackage(
                                    drivePackage = drivePackage,
                                    manifestJson = manifestJson,
                                    existingDocumentIdsBySha256 = existingDocumentIdsBySha,
                                    actualContentSha256 = AgentInboxManifestValidator.sha256(contentBytes),
                                    actualContentSizeBytes = contentBytes.size.toLong(),
                                )
                            }
                        }
                    }
                }
                val scanTruncated = scan.hasMorePackages || scan.packages.any { drivePackage -> drivePackage.hasMoreFiles }
                settingsRepository.saveAgentInboxDriveScanSuccess(
                    timestampMillis = nowMillis,
                    folderId = scan.folderId,
                )
                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.AGENT_INBOX_SCAN_SUCCEEDED,
                        timestampMillis = nowMillis,
                        metadata = mapOf(
                            "inboxCandidateCount" to candidates.size.toString(),
                            "inboxReadyCount" to candidates.count { it.status == AgentInboxReviewStatus.READY }.toString(),
                            "inboxInvalidCount" to candidates.count { it.status == AgentInboxReviewStatus.INVALID }.toString(),
                            "inboxDuplicateCount" to candidates.count { it.status == AgentInboxReviewStatus.DUPLICATE }.toString(),
                        ),
                    ),
                )
                candidates.forEach { candidate ->
                    recordEventDurably(
                        AnalyticsEvent(
                            type = AnalyticsEventType.AGENT_INBOX_CANDIDATE_DETECTED,
                            timestampMillis = nowMillis,
                            metadata = candidate.agentInboxAnalyticsMetadata(),
                        ),
                    )
                }
                uiState = uiState.copy(
                    agentInboxDriveEnabled = true,
                    agentInboxDriveFolderId = scan.folderId,
                    agentInboxDriveLastSuccessfulAtMillis = nowMillis,
                    agentInboxDriveLastError = null,
                    agentInboxDriveFolderDraft = if (selectedGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER) {
                        uiState.agentInboxDriveFolderDraft
                    } else {
                        uiState.agentInboxDriveFolderDraft.ifBlank { scan.folderId }
                    },
                    agentInboxDriveFolderDraftError = null,
                    isAgentInboxScanning = false,
                    agentInboxCandidates = candidates,
                    agentInboxPriorityAcceptedPackageIds = emptySet(),
                    agentInboxScanTruncated = scanTruncated,
                    latestMessage = if (candidates.isEmpty()) {
                        "Agent Inbox is connected. No packages found."
                    } else if (scanTruncated) {
                        "Agent Inbox found ${candidates.size} package${if (candidates.size == 1) "" else "s"}; more items were left unshown."
                    } else {
                        "Agent Inbox found ${candidates.size} package${if (candidates.size == 1) "" else "s"}."
                    },
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                if (error is AgentInboxDriveHttpException && error.requiresAgentInboxReconnect()) {
                    reportAgentInboxDriveAccessLost(
                        "Agent Inbox folder access was lost. Connect the folder again.",
                    )
                } else if (error is AgentInboxDriveAccessLostException) {
                    reportAgentInboxDriveAccessLost(
                        "Agent Inbox folder access was lost. Choose the folder again.",
                    )
                } else {
                    reportAgentInboxDriveFailure("Agent Inbox scan failed. Retry from Settings.")
                }
            }
        }
    }

    fun beginAgentInboxDriveScan() {
        uiState = uiState.copy(
            isAgentInboxScanning = true,
            agentInboxDriveLastError = null,
            agentInboxScanTruncated = false,
            latestMessage = null,
        )
    }

    fun beginAgentInboxFolderSelection() {
        uiState = uiState.copy(
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraftError = null,
            agentInboxScanTruncated = false,
            latestMessage = null,
        )
    }

    fun beginAgentInboxDriveFolderBrowser() {
        uiState = uiState.copy(
            isAgentInboxDriveFolderBrowserOpen = true,
            isAgentInboxDriveFolderBrowserLoading = true,
            agentInboxDriveFolderBrowserLocation = AgentInboxDriveFolderBrowserLocation.Root,
            agentInboxDriveFolderBrowserBackStack = emptyList(),
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraftError = null,
            agentInboxScanTruncated = false,
            latestMessage = null,
        )
    }

    fun loadAgentInboxDriveFolderBrowserRoot(accessToken: String) {
        loadAgentInboxDriveFolderBrowser(
            accessToken = accessToken,
            location = AgentInboxDriveFolderBrowserLocation.Root,
            backStack = emptyList(),
        )
    }

    fun openAgentInboxDriveFolderBrowserFolder(
        accessToken: String,
        folderId: String,
        folderName: String,
    ) {
        val normalizedFolderId = parseAgentInboxDriveFolderId(folderId)
        val safeFolderName = folderName.trim().takeIf(String::isNotBlank) ?: "Drive folder"
        if (normalizedFolderId == null) {
            reportAgentInboxDriveFailure("Agent Inbox folder could not be opened.")
            return
        }
        loadAgentInboxDriveFolderBrowser(
            accessToken = accessToken,
            location = AgentInboxDriveFolderBrowserLocation(
                folderId = normalizedFolderId,
                name = safeFolderName,
            ),
            backStack = uiState.agentInboxDriveFolderBrowserBackStack +
                uiState.agentInboxDriveFolderBrowserLocation,
        )
    }

    fun backAgentInboxDriveFolderBrowser(accessToken: String) {
        val previous = uiState.agentInboxDriveFolderBrowserBackStack.lastOrNull() ?: return
        loadAgentInboxDriveFolderBrowser(
            accessToken = accessToken,
            location = previous,
            backStack = uiState.agentInboxDriveFolderBrowserBackStack.dropLast(1),
        )
    }

    fun selectAgentInboxDriveFolderFromBrowser(
        accessToken: String,
        folderId: String,
        folderName: String,
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedFolderId = parseAgentInboxDriveFolderId(folderId)
        val safeFolderName = folderName.trim().takeIf(String::isNotBlank) ?: "Drive folder"
        if (normalizedFolderId == null) {
            reportAgentInboxDriveFailure("Agent Inbox folder could not be selected.")
            return
        }
        uiState = uiState.copy(
            agentInboxDriveEnabled = true,
            agentInboxDriveFolderId = normalizedFolderId,
            agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraft = safeFolderName,
            agentInboxDriveFolderDraftError = null,
            isAgentInboxDriveFolderBrowserOpen = false,
            isAgentInboxDriveFolderBrowserLoading = false,
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox folder selected.",
        )
        viewModelScope.launch {
            settingsRepository.saveAgentInboxDriveConnection(
                folderId = normalizedFolderId,
                grantMode = AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_CONNECTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("grantMode" to "driveFolderBrowser"),
                ),
            )
        }
        scanAgentInboxDrive(accessToken = accessToken, nowMillis = nowMillis)
    }

    fun cancelAgentInboxDriveFolderBrowser() {
        uiState = uiState.copy(
            isAgentInboxDriveFolderBrowserOpen = false,
            isAgentInboxDriveFolderBrowserLoading = false,
            agentInboxDriveFolderBrowserLocation = AgentInboxDriveFolderBrowserLocation.Root,
            agentInboxDriveFolderBrowserBackStack = emptyList(),
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
        )
    }

    private fun loadAgentInboxDriveFolderBrowser(
        accessToken: String,
        location: AgentInboxDriveFolderBrowserLocation,
        backStack: List<AgentInboxDriveFolderBrowserLocation>,
    ) {
        val normalizedToken = accessToken.trim()
        if (normalizedToken.isBlank()) {
            uiState = uiState.copy(
                isAgentInboxDriveFolderBrowserOpen = true,
                isAgentInboxDriveFolderBrowserLoading = false,
                agentInboxDriveFolderBrowserError = "Google Drive did not return an access token.",
            )
            return
        }
        uiState = uiState.copy(
            isAgentInboxDriveFolderBrowserOpen = true,
            isAgentInboxDriveFolderBrowserLoading = true,
            agentInboxDriveFolderBrowserLocation = location,
            agentInboxDriveFolderBrowserBackStack = backStack,
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
            latestMessage = null,
        )
        viewModelScope.launch {
            try {
                val listedFolders = agentInboxDriveClient.listFolders(
                    AgentInboxDriveFolderListRequest(
                        accessToken = normalizedToken,
                        parentFolderId = location.folderId,
                    ),
                )
                uiState = uiState.copy(
                    isAgentInboxDriveFolderBrowserOpen = true,
                    isAgentInboxDriveFolderBrowserLoading = false,
                    agentInboxDriveFolderBrowserLocation = location,
                    agentInboxDriveFolderBrowserBackStack = backStack,
                    agentInboxDriveFolderOptions = listedFolders.folders.map { folder ->
                        AgentInboxDriveFolderOption(
                            id = folder.id,
                            name = folder.name,
                        )
                    },
                    agentInboxDriveFolderBrowserHasMore = listedFolders.hasMoreFolders,
                    agentInboxDriveFolderBrowserError = null,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(
                    isAgentInboxDriveFolderBrowserOpen = true,
                    isAgentInboxDriveFolderBrowserLoading = false,
                    agentInboxDriveFolderBrowserLocation = location,
                    agentInboxDriveFolderBrowserBackStack = backStack,
                    agentInboxDriveFolderOptions = emptyList(),
                    agentInboxDriveFolderBrowserHasMore = false,
                    agentInboxDriveFolderBrowserError = "Could not load Google Drive folders. Retry folder selection.",
                    latestMessage = "Agent Inbox folder picker failed.",
                )
            }
        }
    }

    fun updateAgentInboxDriveFolderDraft(value: String) {
        uiState = uiState.copy(
            agentInboxDriveFolderDraft = value,
            agentInboxDriveFolderDraftError = null,
            agentInboxDriveLastError = null,
        )
    }

    fun beginAgentInboxReadonlyFolderConnection(): Boolean {
        val parsedFolderId = parseAgentInboxDriveFolderId(uiState.agentInboxDriveFolderDraft)
        if (parsedFolderId == null) {
            uiState = uiState.copy(
                agentInboxDriveFolderDraftError = "Paste a Google Drive folder link or folder id.",
                latestMessage = "Paste an Agent Inbox Drive folder link first.",
            )
            return false
        }
        uiState = uiState.copy(
            agentInboxDriveFolderDraft = parsedFolderId,
            agentInboxDriveFolderDraftError = null,
            agentInboxDriveLastError = null,
            agentInboxScanTruncated = false,
            latestMessage = null,
        )
        return true
    }

    fun connectAgentInboxReadonlyDriveFolder(folderId: String, nowMillis: Long = nowProvider()) {
        val normalizedFolderId = parseAgentInboxDriveFolderId(folderId)
        if (normalizedFolderId == null) {
            reportAgentInboxDriveFailure("Paste a Google Drive folder link or folder id.")
            uiState = uiState.copy(agentInboxDriveFolderDraftError = "Paste a Google Drive folder link or folder id.")
            return
        }
        uiState = uiState.copy(
            agentInboxDriveEnabled = true,
            agentInboxDriveFolderId = normalizedFolderId,
            agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraft = normalizedFolderId,
            agentInboxDriveFolderDraftError = null,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox folder connected with Drive read access.",
        )
        viewModelScope.launch {
            settingsRepository.saveAgentInboxDriveConnection(
                folderId = normalizedFolderId,
                grantMode = AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_CONNECTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("grantMode" to "readOnlyFolder"),
                ),
            )
        }
    }

    fun connectAgentInboxDocumentTreeFolder(
        uri: String,
        displayName: String,
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedUri = uri.trim()
        if (normalizedUri.isBlank()) {
            reportAgentInboxDriveFailure("No Agent Inbox folder was selected.")
            return
        }
        val safeDisplayName = displayName.trim().takeIf(String::isNotBlank) ?: "selected folder"
        uiState = uiState.copy(
            agentInboxDriveEnabled = true,
            agentInboxDriveFolderId = normalizedUri,
            agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER,
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraft = safeDisplayName,
            agentInboxDriveFolderDraftError = null,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox folder selected.",
        )
        viewModelScope.launch {
            settingsRepository.saveAgentInboxDriveConnection(
                folderId = normalizedUri,
                grantMode = AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_CONNECTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("grantMode" to "documentTreeFolder"),
                ),
            )
        }
    }

    fun reportAgentInboxDriveAuthorizationFailure(errorMessage: String) {
        val message = errorMessage.trim().ifBlank { "Google Drive authorization failed." }
        reportAgentInboxDriveFailure(message)
    }

    fun disconnectAgentInboxDrive(nowMillis: Long = nowProvider()) {
        uiState = uiState.copy(
            agentInboxDriveEnabled = false,
            agentInboxDriveFolderId = null,
            agentInboxDriveGrantMode = null,
            agentInboxDriveLastSuccessfulAtMillis = null,
            agentInboxDriveLastError = null,
            agentInboxDriveFolderDraft = "",
            agentInboxDriveFolderDraftError = null,
            isAgentInboxDriveFolderBrowserOpen = false,
            isAgentInboxDriveFolderBrowserLoading = false,
            agentInboxDriveFolderBrowserLocation = AgentInboxDriveFolderBrowserLocation.Root,
            agentInboxDriveFolderBrowserBackStack = emptyList(),
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
            isAgentInboxScanning = false,
            isAgentInboxImporting = false,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox disconnected.",
        )
        viewModelScope.launch {
            settingsRepository.clearAgentInboxDriveConnection()
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_DISCONNECTED,
                    timestampMillis = nowMillis,
                ),
            )
        }
    }

    fun toggleAgentInboxPriority(packageFolderId: String) {
        val selected = uiState.agentInboxPriorityAcceptedPackageIds.toMutableSet()
        val accepted = if (selected.add(packageFolderId)) {
            true
        } else {
            selected.remove(packageFolderId)
            false
        }
        uiState = uiState.copy(
            agentInboxPriorityAcceptedPackageIds = selected,
            latestMessage = null,
        )
        viewModelScope.launch {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_PRIORITY_TOGGLED,
                    timestampMillis = nowProvider(),
                    metadata = mapOf("acceptedPriority" to accepted.toString()),
                ),
            )
        }
    }

    fun rejectAgentInboxCandidate(packageFolderId: String, nowMillis: Long = nowProvider()) {
        val candidate = uiState.agentInboxCandidates.firstOrNull { it.packageFolderId == packageFolderId }
        if (candidate == null) {
            uiState = uiState.copy(latestMessage = "Agent Inbox package is no longer available.")
            return
        }
        uiState = uiState.copy(
            agentInboxCandidates = uiState.agentInboxCandidates.filterNot { it.packageFolderId == packageFolderId },
            agentInboxPriorityAcceptedPackageIds = uiState.agentInboxPriorityAcceptedPackageIds - packageFolderId,
            latestMessage = "Removed Agent Inbox package from review.",
        )
        viewModelScope.launch {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_CANDIDATE_REJECTED,
                    timestampMillis = nowMillis,
                    metadata = candidate.agentInboxAnalyticsMetadata() + mapOf("reviewAction" to "rejected"),
                ),
            )
        }
    }

    fun importAgentInboxCandidate(
        packageFolderId: String,
        accessToken: String,
        nowMillis: Long = nowProvider(),
    ) {
        if (!userDocumentReady) {
            uiState = uiState.copy(latestMessage = "Agent Inbox is waiting for your library to finish loading.")
            return
        }
        if (uiState.isAgentInboxImporting) {
            uiState = uiState.copy(latestMessage = "Agent Inbox import is already running.")
            return
        }
        val candidate = uiState.agentInboxCandidates.firstOrNull { it.packageFolderId == packageFolderId }
        if (candidate == null) {
            uiState = uiState.copy(latestMessage = "Agent Inbox package is no longer available.")
            return
        }
        if (uiState.agentInboxDriveGrantMode == AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER) {
            uiState = uiState.copy(
                agentInboxDriveFolderDraft = uiState.agentInboxDriveFolderId.orEmpty(),
                agentInboxDriveFolderDraftError = null,
            )
            reportAgentInboxDriveFailure("Drive file Picker access is not enough for Agent Inbox import. Use Drive link access.")
            return
        }
        val requiresAccessToken = uiState.agentInboxDriveGrantMode != AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER ||
            uiState.agentInboxDriveFolderId.isGoogleDriveDocumentTreeUri()
        val normalizedToken = accessToken.trim()
        if (requiresAccessToken && normalizedToken.isBlank()) {
            reportAgentInboxDriveFailure("Google Drive did not return an access token.")
            return
        }
        if (!candidate.canImport) {
            uiState = uiState.copy(latestMessage = "Agent Inbox package needs review before import.")
            return
        }
        uiState = uiState.copy(isAgentInboxImporting = true, latestMessage = null)
        viewModelScope.launch {
            try {
                val contentBytes = try {
                    agentInboxDriveClient.downloadFile(
                        accessToken = normalizedToken,
                        fileId = requireNotNull(candidate.contentFileId),
                        maxBytes = AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES,
                        expectedBytes = candidate.reviewedContentSizeBytes,
                    )
                } catch (tooLarge: AgentInboxDriveDownloadTooLargeException) {
                    applyAgentInboxImportResult(
                        packageFolderId = packageFolderId,
                        result = AgentInboxImportResult(
                            status = AgentInboxImportStatus.INVALID,
                            requestedHighPriority = candidate.requestsHighPriority,
                            packageErrors = setOf(AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE),
                        ),
                        nowMillis = nowMillis,
                    )
                    return@launch
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    if (error is AgentInboxDriveAccessLostException) {
                        reportAgentInboxDriveAccessLost(
                            "Agent Inbox folder access was lost. Choose the folder again.",
                        )
                        return@launch
                    }
                    logAgentInboxImportFailure(error)
                    applyAgentInboxImportResult(
                        packageFolderId = packageFolderId,
                        result = AgentInboxImportResult(
                            status = AgentInboxImportStatus.INVALID,
                            requestedHighPriority = candidate.requestsHighPriority,
                            packageErrors = setOf(AgentInboxPackageValidationError.DOWNLOAD_UNAVAILABLE),
                            failureDetail = error.toAgentInboxImportFailureDetail(),
                        ),
                        nowMillis = nowMillis,
                    )
                    return@launch
                }
                val imageAttachmentBytes = mutableMapOf<String, ByteArray>()
                var totalImageAttachmentBytes = 0L
                candidate.imageAttachmentFiles.forEach { attachment ->
                    if ((attachment.sizeBytes ?: 0L) > AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES) {
                        applyAgentInboxImportResult(
                            packageFolderId = packageFolderId,
                            result = AgentInboxImportResult(
                                status = AgentInboxImportStatus.INVALID,
                                requestedHighPriority = candidate.requestsHighPriority,
                                packageErrors = setOf(AgentInboxPackageValidationError.IMAGE_ATTACHMENT_TOO_LARGE),
                            ),
                            nowMillis = nowMillis,
                        )
                        return@launch
                    }
                    val attachmentBytes = try {
                        agentInboxDriveClient.downloadFile(
                            accessToken = normalizedToken,
                            fileId = attachment.fileId,
                            maxBytes = AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES,
                            expectedBytes = attachment.sizeBytes,
                        )
                    } catch (tooLarge: AgentInboxDriveDownloadTooLargeException) {
                        applyAgentInboxImportResult(
                            packageFolderId = packageFolderId,
                            result = AgentInboxImportResult(
                                status = AgentInboxImportStatus.INVALID,
                                requestedHighPriority = candidate.requestsHighPriority,
                                packageErrors = setOf(AgentInboxPackageValidationError.IMAGE_ATTACHMENT_TOO_LARGE),
                            ),
                            nowMillis = nowMillis,
                        )
                        return@launch
                    } catch (error: Throwable) {
                        if (error is CancellationException) throw error
                        if (error is AgentInboxDriveAccessLostException) {
                            reportAgentInboxDriveAccessLost(
                                "Agent Inbox folder access was lost. Choose the folder again.",
                            )
                            return@launch
                        }
                        logAgentInboxImportFailure(error)
                        applyAgentInboxImportResult(
                            packageFolderId = packageFolderId,
                            result = AgentInboxImportResult(
                                status = AgentInboxImportStatus.INVALID,
                                requestedHighPriority = candidate.requestsHighPriority,
                                packageErrors = setOf(AgentInboxPackageValidationError.DOWNLOAD_UNAVAILABLE),
                                failureDetail = error.toAgentInboxImportFailureDetail(),
                            ),
                            nowMillis = nowMillis,
                        )
                        return@launch
                    }
                    totalImageAttachmentBytes += attachmentBytes.size.toLong()
                    if (totalImageAttachmentBytes > AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES) {
                        applyAgentInboxImportResult(
                            packageFolderId = packageFolderId,
                            result = AgentInboxImportResult(
                                status = AgentInboxImportStatus.INVALID,
                                requestedHighPriority = candidate.requestsHighPriority,
                                packageErrors = setOf(AgentInboxPackageValidationError.IMAGE_ATTACHMENT_TOO_LARGE),
                            ),
                            nowMillis = nowMillis,
                        )
                        return@launch
                    }
                    imageAttachmentBytes[attachment.fileName] = attachmentBytes
                }
                val result = agentInboxPackageImporter.importCandidate(
                    candidate = candidate,
                    contentBytes = contentBytes,
                    imageAttachmentBytes = imageAttachmentBytes,
                    nowMillis = nowMillis,
                )
                applyAgentInboxImportResult(
                    packageFolderId = packageFolderId,
                    result = result,
                    nowMillis = nowMillis,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                logAgentInboxImportFailure(error)
                applyAgentInboxImportResult(
                    packageFolderId = packageFolderId,
                    result = AgentInboxImportResult(
                        status = AgentInboxImportStatus.REJECTED,
                        requestedHighPriority = candidate.requestsHighPriority,
                        packageErrors = setOf(error.toAgentInboxImportPackageError()),
                        failureDetail = error.toAgentInboxImportFailureDetail(),
                    ),
                    nowMillis = nowMillis,
                )
            }
        }
    }

    private suspend fun applyAgentInboxImportResult(
        packageFolderId: String,
        result: AgentInboxImportResult,
        nowMillis: Long,
    ) {
        when (result.status) {
            AgentInboxImportStatus.IMPORTED -> {
                val item = result.item
                val importedCandidate = uiState.agentInboxCandidates.firstOrNull {
                    it.packageFolderId == packageFolderId
                }
                val duplicatePackageIds = if (item != null && importedCandidate?.reviewedContentSha256 != null) {
                    uiState.agentInboxCandidates
                        .filter { candidate ->
                            candidate.packageFolderId != packageFolderId &&
                                candidate.reviewedContentSha256 == importedCandidate.reviewedContentSha256
                        }
                        .mapTo(mutableSetOf(), AgentInboxReviewCandidate::packageFolderId)
                } else {
                    emptySet()
                }
                val updatedCandidates = uiState.agentInboxCandidates.mapNotNull { candidate ->
                    when {
                        candidate.packageFolderId == packageFolderId -> null
                        item != null &&
                            importedCandidate?.reviewedContentSha256 != null &&
                            candidate.reviewedContentSha256 == importedCandidate.reviewedContentSha256 -> {
                            candidate.copy(
                                status = AgentInboxReviewStatus.DUPLICATE,
                                duplicateContentId = item.id,
                            )
                        }
                        else -> candidate
                    }
                }
                val priorityAccepted = packageFolderId in uiState.agentInboxPriorityAcceptedPackageIds
                val updatedPriorityIds = if (item != null && priorityAccepted) {
                    val ids = uiState.priorityContentIds + item.id
                    settingsRepository.savePriorityContentIds(ids)
                    recordPrioritySetDuringAdd(
                        item = item,
                        nowMillis = nowMillis,
                        priorityContentIds = ids,
                    )
                    ids
                } else {
                    uiState.priorityContentIds
                }
                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.AGENT_INBOX_CANDIDATE_IMPORTED,
                        timestampMillis = nowMillis,
                        contentId = item?.id,
                        metadata = (item?.analyticsMetadata() ?: emptyMap()) + mapOf(
                            "acceptedPriority" to priorityAccepted.toString(),
                            "priorityRequested" to result.requestedHighPriority.toString(),
                        ),
                    ),
                )
                autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
                uiState = uiState.copy(
                    isAgentInboxImporting = false,
                    agentInboxCandidates = updatedCandidates,
                    agentInboxPriorityAcceptedPackageIds = uiState.agentInboxPriorityAcceptedPackageIds -
                        packageFolderId -
                        duplicatePackageIds,
                    priorityContentIds = updatedPriorityIds,
                    preferences = uiState.preferences?.copy(priorityContentIds = updatedPriorityIds),
                    latestMessage = if (item != null) {
                        "Imported ${item.title} from Agent Inbox."
                    } else {
                        "Imported Agent Inbox package."
                    },
                )
            }

            AgentInboxImportStatus.DUPLICATE -> {
                val duplicateCandidate = uiState.agentInboxCandidates.firstOrNull {
                    it.packageFolderId == packageFolderId
                }
                val duplicatePackageIds = uiState.agentInboxCandidates
                    .filter { candidate ->
                        candidate.packageFolderId == packageFolderId ||
                            (
                                duplicateCandidate?.reviewedContentSha256 != null &&
                                    candidate.reviewedContentSha256 == duplicateCandidate.reviewedContentSha256
                                )
                    }
                    .mapTo(mutableSetOf(), AgentInboxReviewCandidate::packageFolderId)
                val updatedCandidates = uiState.agentInboxCandidates.map { candidate ->
                    if (candidate.packageFolderId in duplicatePackageIds) {
                        candidate.copy(
                            status = AgentInboxReviewStatus.DUPLICATE,
                            duplicateContentId = result.duplicateContentId ?: candidate.duplicateContentId,
                        )
                    } else {
                        candidate
                    }
                }
                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.AGENT_INBOX_CANDIDATE_DUPLICATE,
                        timestampMillis = nowMillis,
                        metadata = mapOf(
                            "importStatus" to result.status.name,
                            "priorityRequested" to result.requestedHighPriority.toString(),
                        ),
                    ),
                )
                uiState = uiState.copy(
                    isAgentInboxImporting = false,
                    agentInboxCandidates = updatedCandidates,
                    agentInboxPriorityAcceptedPackageIds = uiState.agentInboxPriorityAcceptedPackageIds -
                        duplicatePackageIds,
                    latestMessage = "Agent Inbox package is already in your library.",
                )
            }

            AgentInboxImportStatus.INVALID,
            AgentInboxImportStatus.REJECTED,
            -> {
                val updatedCandidates = uiState.agentInboxCandidates.map { candidate ->
                    if (candidate.packageFolderId == packageFolderId) {
                        val packageErrors = candidate.packageErrors +
                            result.packageErrors +
                            if (
                                result.status == AgentInboxImportStatus.REJECTED &&
                                result.packageErrors.isEmpty() &&
                                result.documentErrors.isNotEmpty()
                            ) {
                                setOf(AgentInboxPackageValidationError.LOCAL_IMPORT_REJECTED)
                            } else {
                                emptySet()
                            }
                        candidate.copy(
                            status = AgentInboxReviewStatus.INVALID,
                            duplicateContentId = null,
                            reviewedContentSha256 = null,
                            reviewedContentSizeBytes = null,
                            manifestErrors = candidate.manifestErrors + result.manifestErrors,
                            packageErrors = packageErrors,
                            importFailureDetail = result.failureDetail,
                        )
                    } else {
                        candidate
                    }
                }
                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.AGENT_INBOX_CANDIDATE_IMPORT_FAILED,
                        timestampMillis = nowMillis,
                        metadata = mapOf(
                            "importStatus" to result.status.name,
                            "validationErrorCount" to result.manifestErrors.size.toString(),
                            "packageErrorCount" to result.packageErrors.size.toString(),
                            "documentErrorCount" to result.documentErrors.size.toString(),
                            "priorityRequested" to result.requestedHighPriority.toString(),
                        ),
                    ),
                )
                uiState = uiState.copy(
                    isAgentInboxImporting = false,
                    agentInboxCandidates = updatedCandidates,
                    agentInboxPriorityAcceptedPackageIds = uiState.agentInboxPriorityAcceptedPackageIds -
                        packageFolderId,
                    latestMessage = if (
                        AgentInboxPackageValidationError.CONTENT_CHANGED_AFTER_REVIEW in result.packageErrors
                    ) {
                        "Agent Inbox package changed. Scan again before importing."
                    } else if (result.failureDetail != null) {
                        "Agent Inbox import failed: ${result.failureDetail.exceptionClass}."
                    } else {
                        "Agent Inbox package could not be imported."
                    },
                )
            }
        }
    }

    private fun logAgentInboxImportFailure(error: Throwable) {
        val detail = error.toAgentInboxImportFailureDetail()
        val message = buildString {
            append("Agent Inbox import failed with ")
            append(detail.exceptionClass)
            detail.message?.let { failureMessage ->
                append(": ")
                append(failureMessage)
            }
        }
        runCatching {
            Log.e(MAIN_VIEW_MODEL_LOG_TAG, message, error)
        }
    }

    private fun reportAgentInboxDriveFailure(message: String) {
        uiState = uiState.copy(
            isAgentInboxScanning = false,
            isAgentInboxImporting = false,
            isAgentInboxDriveFolderBrowserLoading = false,
            agentInboxDriveLastError = message,
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox connection failed.",
        )
        viewModelScope.launch {
            settingsRepository.saveAgentInboxDriveScanFailure(message)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_SCAN_FAILED,
                    timestampMillis = nowProvider(),
                    metadata = mapOf("reason" to "scan_failed"),
                ),
            )
        }
    }

    private fun reportAgentInboxDriveAccessLost(message: String) {
        uiState = uiState.copy(
            agentInboxDriveEnabled = false,
            agentInboxDriveFolderId = null,
            agentInboxDriveGrantMode = null,
            agentInboxDriveLastSuccessfulAtMillis = null,
            agentInboxDriveLastError = message,
            agentInboxDriveFolderDraft = "",
            agentInboxDriveFolderDraftError = null,
            isAgentInboxDriveFolderBrowserOpen = false,
            isAgentInboxDriveFolderBrowserLoading = false,
            agentInboxDriveFolderBrowserLocation = AgentInboxDriveFolderBrowserLocation.Root,
            agentInboxDriveFolderBrowserBackStack = emptyList(),
            agentInboxDriveFolderOptions = emptyList(),
            agentInboxDriveFolderBrowserHasMore = false,
            agentInboxDriveFolderBrowserError = null,
            isAgentInboxScanning = false,
            isAgentInboxImporting = false,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox folder access was lost.",
        )
        viewModelScope.launch {
            settingsRepository.clearAgentInboxDriveConnection()
            settingsRepository.saveAgentInboxDriveScanFailure(message)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.AGENT_INBOX_SCAN_FAILED,
                    timestampMillis = nowProvider(),
                    metadata = mapOf("reason" to "access_lost"),
                ),
            )
        }
    }

    private fun AgentInboxDriveHttpException.requiresAgentInboxReconnect(): Boolean {
        return statusCode == 401 || statusCode == 403 || statusCode == 404
    }

    fun toggleOnboardingPack(pack: EditorialPack) {
        val selected = uiState.onboardingSelection.selectedPackIds.toMutableSet()
        if (!selected.add(pack.id)) {
            selected.remove(pack.id)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(selectedPackIds = selected),
        )
    }

    fun completeOnboarding() {
        val selection = uiState.onboardingSelection
        if (!selection.isValid()) {
            uiState = uiState.copy(
                latestMessage = "Select at least 3 distracting apps, 3 topics, and 1 starter pack.",
            )
            return
        }

        viewModelScope.launch {
            settingsRepository.saveOnboardingSelection(selection)
            autosaveAccountLightProfileAfterPortableMutation()
            uiState = uiState.copy(latestMessage = null)
        }
    }

    fun openAddLink() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.AddLink,
            addLinkForm = AddLinkFormState(),
            addDocumentForm = AddDocumentFormState(),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
    }

    fun beginUserDocumentImportPreparation(): Int {
        documentImportPreparationRequestId += 1
        pendingDocumentImportBase = if (uiState.screen == MainScreen.AddDocument) {
            uiState.addDocumentForm.pendingMarkdownAttachmentBase()
        } else {
            null
        }
        uiState = uiState.copy(
            screen = MainScreen.AddDocument,
            addDocumentForm = AddDocumentFormState(isPreparing = true),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
        return documentImportPreparationRequestId
    }

    fun reportUserDocumentImportPreparationFailure(requestId: Int? = null) {
        if (requestId != null && requestId != documentImportPreparationRequestId) {
            return
        }
        documentImportPreparationRequestId += 1
        pendingDocumentImportBase = null
        uiState = uiState.copy(
            screen = MainScreen.AddDocument,
            addDocumentForm = AddDocumentFormState(),
            latestMessage = "The selected files could not be prepared. Try a smaller batch.",
        )
    }

    fun prepareUserDocumentImport(
        uri: String,
        displayName: String,
        mimeType: String?,
        openInputStream: () -> InputStream? = { null },
    ) {
        val requestId = ++documentImportPreparationRequestId
        pendingDocumentImportBase = null
        uiState = uiState.copy(
            screen = MainScreen.AddDocument,
            addDocumentForm = AddDocumentFormState(isPreparing = true),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
        viewModelScope.launch {
            val candidate = runCatching {
                withContext(documentWorkDispatcher) {
                    DocumentImportCandidateFactory.fromPickedDocument(
                        uri = uri,
                        displayName = displayName,
                        mimeType = mimeType,
                        openInputStream = openInputStream,
                    )
                }
            }.getOrElse {
                if (requestId == documentImportPreparationRequestId) {
                    reportUserDocumentImportPreparationFailure(requestId = requestId)
                }
                return@launch
            }
            if (requestId != documentImportPreparationRequestId) {
                return@launch
            }
            updateAddDocumentForm(
                form = AddDocumentFormState(
                    uri = candidate.uri,
                    displayName = candidate.displayName,
                    mimeType = mimeType,
                    title = candidate.title,
                    durationMinutes = candidate.durationMinutes,
                    candidates = listOf(candidate),
                ),
                screen = MainScreen.AddDocument,
            )
        }
    }

    fun prepareUserDocumentBatchImport(
        candidates: List<DocumentImportCandidate>,
        nowMillis: Long = nowProvider(),
        requestId: Int = documentImportPreparationRequestId,
    ) {
        if (requestId != documentImportPreparationRequestId) {
            return
        }
        val pickedCandidates = candidates
            .map(DocumentImportCandidate::cleaned)
            .distinctBy(DocumentImportCandidate::uri)
        val pendingBase = pendingDocumentImportBase
        val attachmentOnlySelection = pickedCandidates.isNotEmpty() &&
            pickedCandidates.all(DocumentImportCandidate::isMarkdownImageAttachmentCandidate)
        val cleanedCandidates = pickedCandidates
            .withMarkdownImageAttachments(
                baseCandidates = if (attachmentOnlySelection) {
                    pendingBase?.candidates.orEmpty()
                } else {
                    emptyList()
                },
            )
            .map(DocumentImportCandidate::cleaned)
            .distinctBy(DocumentImportCandidate::uri)
        pendingDocumentImportBase = null
        documentImportPreparationRequestId += 1
        if (cleanedCandidates.isEmpty()) {
            uiState = uiState.copy(latestMessage = "Choose local PDF, Markdown, or EPUB files first.")
            return
        }

        val primary = cleanedCandidates.first()
        updateAddDocumentForm(
            form = AddDocumentFormState(
                uri = primary.uri,
                displayName = primary.displayName,
                mimeType = primary.mimeType,
                title = primary.title,
                durationMinutes = primary.durationMinutes,
                candidates = cleanedCandidates,
                selectedTopics = if (attachmentOnlySelection) pendingBase?.selectedTopics.orEmpty() else emptySet(),
                markPriority = attachmentOnlySelection && pendingBase?.markPriority == true,
            ),
            screen = MainScreen.AddDocument,
        )
        viewModelScope.launch {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.BATCH_DOCUMENT_IMPORT_ATTEMPTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf(
                        "selectedCount" to cleanedCandidates.size.toString(),
                        "supportedCount" to cleanedCandidates.count { it.format != null }.toString(),
                        "unsupportedCount" to cleanedCandidates.count { it.format == null }.toString(),
                        "formats" to cleanedCandidates.map { it.format?.name ?: "UNSUPPORTED" }.sorted().joinToString(","),
                    ),
                ),
            )
        }
    }

    fun openHome() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Home,
            isReaderOpening = false,
            isManagingLibrary = false,
            selectedLibraryContentIds = emptySet(),
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            latestMessage = null,
        )
    }

    fun openLibrary() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Library,
            isReaderOpening = false,
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            latestMessage = null,
        )
    }

    fun toggleLibraryManageMode() {
        val willManage = !uiState.isManagingLibrary
        uiState = uiState.copy(
            isManagingLibrary = willManage,
            selectedLibraryContentIds = if (willManage) uiState.selectedLibraryContentIds else emptySet(),
            latestMessage = null,
        )
    }

    fun toggleLibraryContentSelection(content: ContentItem) {
        if (!uiState.isManagingLibrary) {
            return
        }
        if (!content.isUserManagedContent()) {
            uiState = uiState.copy(latestMessage = "Editorial pieces stay in the starter packs.")
            return
        }

        val selectedIds = uiState.selectedLibraryContentIds.toMutableSet()
        if (!selectedIds.add(content.id)) {
            selectedIds.remove(content.id)
        }
        uiState = uiState.copy(
            selectedLibraryContentIds = selectedIds,
            latestMessage = null,
        )
    }

    fun deleteSelectedLibraryContent(
        nowMillis: Long = nowProvider(),
        releaseDocumentPermission: (String) -> Unit = {},
    ) {
        val userContent = (uiState.userLinks + uiState.userDocuments).associateBy(ContentItem::id)
        val selectedItems = uiState.selectedLibraryContentIds.mapNotNull(userContent::get)
        if (selectedItems.isEmpty()) {
            uiState = uiState.copy(latestMessage = "Select saved links or files to delete.")
            return
        }

        uiState = uiState.copy(latestMessage = null)
        viewModelScope.launch {
            try {
                val deletedIds = selectedItems.mapTo(mutableSetOf(), ContentItem::id)
                selectedItems.filter { item -> item.sourceType == ContentSourceType.USER_LINK }
                    .forEach { item -> userLinkRepository.deleteLink(item.id) }
                selectedItems.filter { item -> item.sourceType == ContentSourceType.USER_DOCUMENT }
                    .forEach { item ->
                        userDocumentRepository.deleteDocument(item.id)
                        item.rights.sourceUrl?.takeIf(String::isNotBlank)?.let { uri ->
                            runCatching { releaseDocumentPermission(uri) }
                        }
                }
                readingProgressRepository.deleteProgressForContentIds(deletedIds)
                readingAnnotationRepository.deleteAnnotationsForContentIds(deletedIds, nowMillis = nowMillis)
                val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)

                val updatedPriorityIds = uiState.priorityContentIds - deletedIds
                if (updatedPriorityIds != uiState.priorityContentIds) {
                    settingsRepository.savePriorityContentIds(updatedPriorityIds)
                }
                selectedItems.forEach { item ->
                    recordEventDurably(
                        AnalyticsEvent(
                            type = AnalyticsEventType.USER_CONTENT_DELETED,
                            timestampMillis = nowMillis,
                            contentId = item.id,
                            metadata = item.analyticsMetadata() + mapOf(
                                "deletedSelectionCount" to selectedItems.size.toString(),
                            ),
                        ),
                    )
                }
                autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
                val preferences = uiState.preferences
                uiState = uiState.copy(
                    priorityContentIds = updatedPriorityIds,
                    preferences = preferences?.copy(priorityContentIds = updatedPriorityIds),
                    selectedLibraryContentIds = emptySet(),
                    isManagingLibrary = false,
                    latestMessage = deletedLibraryMessage(selectedItems.size)
                        .withAnnotationAutosaveResult(annotationAutosaveResult),
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(latestMessage = "The selected content could not be deleted.")
            }
        }
    }

    fun openProgress() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Progress,
            isReaderOpening = false,
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            latestMessage = null,
        )
    }

    fun openSettings() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Settings,
            isReaderOpening = false,
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            latestMessage = null,
        )
    }

    fun openAnnotationLibrary() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Annotations,
            isReaderOpening = false,
            isManagingLibrary = false,
            selectedLibraryContentIds = emptySet(),
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            latestMessage = null,
        )
    }

    fun openAnnotationTarget(annotationId: String) {
        val annotation = uiState.readingAnnotations.firstOrNull { candidate -> candidate.id == annotationId }
        if (annotation == null) {
            uiState = uiState.copy(latestMessage = "That annotation is no longer available.")
            return
        }
        val content = contentForAnnotation(annotation)
        if (content == null) {
            uiState = uiState.copy(latestMessage = "The source for that annotation is no longer in Library.")
            return
        }
        if (!content.usesRepositoryBody()) {
            uiState = uiState.copy(latestMessage = "That source cannot be opened in the reader.")
            return
        }
        openLibraryItem(
            content = content,
            origin = "annotation-library",
            startParagraphIndex = annotation.paragraphIndex,
            startSelector = annotation.selector.takeIf { selector -> selector.hasExplicitReaderTarget() },
        )
    }

    fun openLibraryItem(
        content: ContentItem,
        origin: String = "library",
        startParagraphIndex: Int? = null,
        startSelector: ReadingAnnotationSelector? = null,
    ) {
        if (content.availability == ContentAvailability.UNAVAILABLE) {
            uiState = uiState.copy(
                latestMessage = if (content.sourceType == ContentSourceType.USER_DOCUMENT) {
                    "Reattach this file before reading can continue."
                } else {
                    "This saved item is unavailable."
                },
            )
            return
        }
        val startedAtMillis = nowProvider()
        val requestId = ++readerOpenRequestId
        val opensPrivateReader = content.usesRepositoryBody()
        if (opensPrivateReader) {
            uiState = uiState.copy(
                isReaderOpening = true,
                latestMessage = null,
            )
        } else {
            uiState = uiState.copy(isReaderOpening = false)
        }
        viewModelScope.launch {
            val readerDocument = loadReaderDocumentForSession(
                content = content,
                sessionId = null,
                startedAtMillis = startedAtMillis,
                requestId = requestId,
            ) ?: return@launch
            if (requestId != readerOpenRequestId) {
                return@launch
            }
            val repairedContent = repairLegacyReadingTimeEstimateFromLoadedDocument(
                content = content,
                readerDocument = readerDocument,
                nowMillis = startedAtMillis,
            ) ?: content
            val existingProgress = unfinishedProgressFor(content.id)
            if (existingProgress != null) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.MANUAL_CONTINUE_STARTED,
                        timestampMillis = startedAtMillis,
                        contentId = content.id,
                        metadata = repairedContent.analyticsMetadata() + existingProgress.analyticsMetadata() + mapOf(
                            "origin" to origin,
                        ),
                    ),
                )
            }
            uiState = uiState.copy(
                currentInterventionId = null,
                currentInterventionShownAtMillis = null,
                currentOpenAnywayUnlockAvailableAtMillis = null,
                currentRecommendationSet = null,
                currentInterventionOrigin = null,
                currentInterventionMetadata = emptyMap(),
                currentInterventionSuppressionKey = null,
                currentInterventionBedtimeEnforced = false,
                currentContent = repairedContent,
                currentReaderDocument = readerDocument,
                currentContentBody = readerDocument.plainText,
                isReaderOpening = false,
                currentReadingProgress = existingProgress,
                currentReaderStartParagraphIndex = startParagraphIndex,
                currentReaderStartSelector = startSelector,
                currentSessionId = null,
                currentSessionStartedAtMillis = startedAtMillis,
                screen = screenForReplacement(repairedContent),
                latestMessage = null,
            )
        }
    }

    fun cancelAddLink() {
        invalidatePendingReaderOpen()
        invalidatePendingDocumentImportPreparation()
        uiState = uiState.copy(
            screen = MainScreen.Home,
            isReaderOpening = false,
            addLinkForm = AddLinkFormState(),
            addDocumentForm = AddDocumentFormState(),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
    }

    fun updateAddLinkUrl(url: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(url = url))
    }

    fun updateAddLinkTitle(title: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(title = title))
    }

    fun updateAddLinkDuration(durationMinutes: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(durationMinutes = durationMinutes))
    }

    fun toggleAddLinkTopic(topic: TopicTag) {
        val selectedTopics = uiState.addLinkForm.selectedTopics.toMutableSet()
        if (!selectedTopics.add(topic)) {
            selectedTopics.remove(topic)
        }
        updateAddLinkForm(uiState.addLinkForm.copy(selectedTopics = selectedTopics))
    }

    fun toggleAddLinkPriority() {
        updateAddLinkForm(uiState.addLinkForm.copy(markPriority = !uiState.addLinkForm.markPriority))
    }

    fun saveUserLink(nowMillis: Long = nowProvider()) {
        val form = uiState.addLinkForm
        val draft = form.toDraftOrNull()
        if (draft == null) {
            uiState = uiState.copy(
                addLinkForm = uiState.addLinkForm.copy(
                    validationErrors = uiState.addLinkForm.localValidationErrors(),
                    canSave = false,
                    isSaving = false,
                ),
                latestMessage = "This link needs a little cleanup before saving.",
            )
            return
        }

        uiState = uiState.copy(addLinkForm = uiState.addLinkForm.copy(isSaving = true))
        viewModelScope.launch {
            try {
                when (val result = userLinkRepository.addLink(draft = draft, nowMillis = nowMillis)) {
                    is AddUserLinkResult.Added -> {
                        val updatedPriorityIds = if (form.markPriority) {
                            val ids = uiState.priorityContentIds + result.item.id
                            settingsRepository.savePriorityContentIds(ids)
                            recordPrioritySetDuringAdd(
                                item = result.item,
                                nowMillis = nowMillis,
                                priorityContentIds = ids,
                            )
                            ids
                        } else {
                            uiState.priorityContentIds
                        }
                        recordEventDurably(
                            AnalyticsEvent(
                                type = AnalyticsEventType.USER_LINK_ADDED,
                                timestampMillis = nowMillis,
                                contentId = result.item.id,
                                metadata = result.item.analyticsMetadata() + mapOf(
                                    "durationMinutes" to result.item.durationMinutes.toString(),
                                    "topicCount" to result.item.topicTags.size.toString(),
                                ),
                            ),
                        )
                        autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
                        uiState = uiState.copy(
                            screen = MainScreen.AddLinkSuccess,
                            addLinkForm = AddLinkFormState(),
                            priorityContentIds = updatedPriorityIds,
                            preferences = uiState.preferences?.copy(priorityContentIds = updatedPriorityIds),
                            savedLinkConfirmation = result.item.toAddLinkConfirmation(priorityMarked = form.markPriority),
                            latestMessage = null,
                        )
                    }

                    is AddUserLinkResult.Rejected -> {
                        uiState = uiState.copy(
                            addLinkForm = uiState.addLinkForm.copy(
                                validationErrors = result.errors,
                                canSave = false,
                                isSaving = false,
                            ),
                            latestMessage = "This link needs a little cleanup before saving.",
                        )
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(
                    screen = MainScreen.AddLink,
                    addLinkForm = uiState.addLinkForm.copy(isSaving = false),
                    latestMessage = "The link could not be saved locally. Try again.",
                )
            }
        }
    }

    fun finishAddLinkSuccess() {
        uiState = uiState.copy(
            screen = MainScreen.Library,
            savedLinkConfirmation = null,
            latestMessage = "Saved for future replacement moments.",
        )
    }

    fun updateAddDocumentTitle(title: String) {
        updateAddDocumentForm(uiState.addDocumentForm.copy(title = title))
    }

    fun toggleAddDocumentTopic(topic: TopicTag) {
        val selectedTopics = uiState.addDocumentForm.selectedTopics.toMutableSet()
        if (!selectedTopics.add(topic)) {
            selectedTopics.remove(topic)
        }
        updateAddDocumentForm(uiState.addDocumentForm.copy(selectedTopics = selectedTopics))
    }

    fun toggleAddDocumentPriority() {
        updateAddDocumentForm(uiState.addDocumentForm.copy(markPriority = !uiState.addDocumentForm.markPriority))
    }

    fun saveUserDocument(
        nowMillis: Long = nowProvider(),
        persistReadPermission: (String) -> Unit = {},
    ) {
        val form = uiState.addDocumentForm
        if (form.isPreparing) {
            return
        }
        val candidates = form.documentCandidates()
        val supportedCandidates = candidates.filter { candidate -> candidate.format != null }
        val drafts = supportedCandidates.mapNotNull { candidate -> candidate.toDraftOrNull(form.selectedTopics) }
        if (drafts.isEmpty()) {
            uiState = uiState.copy(
                addDocumentForm = uiState.addDocumentForm.copy(
                    validationErrors = uiState.addDocumentForm.localValidationErrors(),
                    canSave = false,
                    isSaving = false,
                ),
                latestMessage = "This file needs a little cleanup before saving.",
            )
            return
        }

        uiState = uiState.copy(addDocumentForm = uiState.addDocumentForm.copy(isSaving = true))
        viewModelScope.launch {
            try {
                val addedItems = mutableListOf<ContentItem>()
                var rejectedCount = candidates.count { candidate -> candidate.format == null }
                drafts.forEach { draft ->
                    when (val result = userDocumentRepository.addDocument(draft = draft, nowMillis = nowMillis)) {
                        is AddUserDocumentResult.Added -> {
                            addedItems += result.item
                            runCatching { persistReadPermission(draft.uri) }
                            draft.imageAttachmentUris.values.forEach { attachmentUri ->
                                runCatching { persistReadPermission(attachmentUri) }
                            }
                            recordReadingTimeEstimateApplied(
                                item = result.item,
                                draft = draft,
                                candidate = candidates.firstOrNull { candidate -> candidate.uri == draft.uri },
                                nowMillis = nowMillis,
                            )
                            recordEventDurably(
                                AnalyticsEvent(
                                    type = AnalyticsEventType.USER_DOCUMENT_ADDED,
                                    timestampMillis = nowMillis,
                                    contentId = result.item.id,
                                    metadata = result.item.analyticsMetadata() + mapOf(
                                        "durationMinutes" to result.item.durationMinutes.toString(),
                                        "topicCount" to result.item.topicTags.size.toString(),
                                        "batchSelectedCount" to candidates.size.toString(),
                                    ),
                                ),
                            )
                        }

                        is AddUserDocumentResult.Rejected -> {
                            rejectedCount += 1
                        }
                    }
                }

                if (addedItems.isEmpty()) {
                    uiState = uiState.copy(
                        screen = MainScreen.AddDocument,
                        addDocumentForm = uiState.addDocumentForm.copy(
                            validationErrors = setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT),
                            canSave = false,
                            isSaving = false,
                        ),
                        latestMessage = "No selected files could be saved locally.",
                    )
                    return@launch
                }

                val updatedPriorityIds = if (form.markPriority) {
                    val ids = uiState.priorityContentIds + addedItems.map(ContentItem::id)
                    settingsRepository.savePriorityContentIds(ids)
                    addedItems.forEach { item ->
                        recordPrioritySetDuringAdd(
                            item = item,
                            nowMillis = nowMillis,
                            priorityContentIds = ids,
                        )
                    }
                    ids
                } else {
                    uiState.priorityContentIds
                }

                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.BATCH_DOCUMENT_IMPORT_COMPLETED,
                        timestampMillis = nowMillis,
                        metadata = mapOf(
                            "selectedCount" to candidates.size.toString(),
                            "savedCount" to addedItems.size.toString(),
                            "rejectedCount" to rejectedCount.toString(),
                            "priorityMarked" to form.markPriority.toString(),
                        ),
                    ),
                )
                autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)

                uiState = uiState.copy(
                    screen = MainScreen.AddLinkSuccess,
                    addDocumentForm = AddDocumentFormState(),
                    priorityContentIds = updatedPriorityIds,
                    preferences = uiState.preferences?.copy(priorityContentIds = updatedPriorityIds),
                    savedLinkConfirmation = addedItems.toAddLinkConfirmation(
                        skippedCount = rejectedCount,
                        priorityMarked = form.markPriority,
                    ),
                    latestMessage = null,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(
                    screen = MainScreen.AddDocument,
                    addDocumentForm = uiState.addDocumentForm.copy(isSaving = false),
                    latestMessage = "The file could not be saved locally. Try again.",
                )
            }
        }
    }

    fun triggerDebugIntervention(nowMillis: Long = System.currentTimeMillis()) {
        val targetApp = uiState.selectedTargetApp ?: return
        triggerIntervention(
            targetApp = targetApp,
            origin = InterventionOrigin.DEBUG,
            triggeredAtMillis = nowMillis,
            processingNowMillis = nowMillis,
        )
    }

    fun requestSystemInterception(
        targetAppPackage: String,
        nowMillis: Long = nowProvider(),
    ) {
        if (uiState.isLoadingSettings) {
            pendingSystemInterception = PendingSystemInterception.App(
                targetAppPackage = targetAppPackage,
                triggeredAtMillis = nowMillis,
            )
            return
        }

        val targetApp = findTargetApp(targetAppPackage) ?: return
        triggerIntervention(
            targetApp = targetApp,
            origin = InterventionOrigin.SYSTEM,
            triggeredAtMillis = nowMillis,
            processingNowMillis = nowProvider(),
        )
    }

    fun requestSystemWebsiteInterception(
        browserPackage: String,
        browserDisplayName: String,
        websiteRuleType: String,
        websiteRuleIncludesApex: Boolean,
        nowMillis: Long = nowProvider(),
    ) {
        if (uiState.isLoadingSettings) {
            pendingSystemInterception = PendingSystemInterception.Website(
                browserPackage = browserPackage,
                browserDisplayName = browserDisplayName,
                websiteRuleType = websiteRuleType,
                websiteRuleIncludesApex = websiteRuleIncludesApex,
                triggeredAtMillis = nowMillis,
            )
            return
        }

        val safeBrowserName = browserDisplayName.trim().takeIf(String::isNotBlank) ?: "Browser"
        val targetApp = DistractingApp(
            packageName = browserPackage,
            displayName = "$safeBrowserName website",
        )
        triggerIntervention(
            targetApp = targetApp,
            origin = InterventionOrigin.SYSTEM,
            triggeredAtMillis = nowMillis,
            processingNowMillis = nowProvider(),
            interventionMetadata = mapOf(
                "targetType" to WebsiteInterceptionResolver.TARGET_TYPE,
                "browserPackage" to browserPackage,
                "browserSupportStatus" to WebsiteInterceptionResolver.BROWSER_SUPPORT_VERIFIED_HOST,
                "websiteRuleType" to websiteRuleType,
                "websiteRuleIncludesApex" to websiteRuleIncludesApex.toString(),
            ),
            suppressionKey = WebsiteInterceptionResolver.suppressionKeyFor(browserPackage),
        )
    }

    fun triggerIntervention(
        targetApp: DistractingApp,
        origin: InterventionOrigin,
        triggeredAtMillis: Long = nowProvider(),
        processingNowMillis: Long = triggeredAtMillis,
        interventionMetadata: Map<String, String> = emptyMap(),
        suppressionKey: String = targetApp.packageName,
    ) {
        val preferences = uiState.preferences ?: return
        if (uiState.isLoadingSettings) {
            uiState = uiState.copy(latestMessage = "Local replacement state is still loading.")
            return
        }

        interventionJob?.cancel()
        interventionJob = viewModelScope.launch {
            maybeRecordInterceptionLatencyDegradation(
                origin = origin,
                targetApp = targetApp,
                triggeredAtMillis = triggeredAtMillis,
                shownAtMillis = processingNowMillis,
                interventionMetadata = interventionMetadata,
            )

            val bedtimeActive = bedtimeWindowIsActive(
                enabled = uiState.bedtimeEnabled,
                startMinutes = uiState.bedtimeStartMinutes,
                endMinutes = uiState.bedtimeEndMinutes,
                nowMillis = processingNowMillis,
            )
            if (
                origin == InterventionOrigin.SYSTEM &&
                InterceptionRuntimeGate.shouldSuppress(
                    targetAppPackage = targetApp.packageName,
                    targetKey = suppressionKey,
                    nowMillis = processingNowMillis,
                    bedtimeActive = bedtimeActive,
                )
            ) {
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    currentInterventionId = null,
                    currentInterventionShownAtMillis = null,
                    currentOpenAnywayUnlockAvailableAtMillis = null,
                    currentRecommendationSet = null,
                    activeDelayWindow = null,
                    activeDelaySuggestion = null,
                    currentInterventionOrigin = null,
                    currentInterventionMetadata = emptyMap(),
                    currentInterventionSuppressionKey = null,
                    currentInterventionBedtimeEnforced = false,
                    latestMessage = "${targetApp.displayName} is still unlocked.",
                    screen = MainScreen.Home,
                )
                return@launch
            }

            if (!bedtimeActive) {
                val delayInspection = delayGate.inspectDelay(targetApp = targetApp, nowMillis = processingNowMillis)
                if (delayInspection.activeWindow != null) {
                    recordDelayReturnDuringActiveWindow(targetApp = targetApp, nowMillis = processingNowMillis)
                    val activeDelayWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = processingNowMillis)
                    uiState = uiState.copy(
                        selectedTargetApp = targetApp,
                        activeDelayWindow = activeDelayWindow,
                        activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
                        latestMessage = "${targetApp.displayName} is paused for 15 minutes.",
                        screen = MainScreen.Home,
                        currentOpenAnywayUnlockAvailableAtMillis = null,
                        currentInterventionOrigin = null,
                        currentInterventionMetadata = emptyMap(),
                        currentInterventionSuppressionKey = null,
                        currentInterventionBedtimeEnforced = false,
                    )
                    return@launch
                }

                var delayReturnHandled = false
                delayInspection.expiredWindow?.let { expiredWindow ->
                    delayReturnHandled = true
                    recordDelayReturnAfterExpiry(expiredWindow = expiredWindow, nowMillis = processingNowMillis)
                    delayGate.consumeExpiredDelay(
                        targetApp = targetApp,
                        delayId = expiredWindow.id,
                        nowMillis = processingNowMillis,
                    )
                }

                if (!delayReturnHandled) {
                    recordReturnSignalIfNeeded(targetApp = targetApp, nowMillis = processingNowMillis)
                }
            }

            val interventionId = UUID.randomUUID().toString()
            val rawInventory = fullReplacementInventory()
            val filteredInventory = rawInventory.filter { item ->
                when (item.sourceType) {
                    ContentSourceType.MEDITATION -> true
                    ContentSourceType.USER_LINK -> item.availability != ContentAvailability.UNAVAILABLE
                    ContentSourceType.USER_DOCUMENT -> item.availability != ContentAvailability.UNAVAILABLE
                    ContentSourceType.EDITORIAL -> item.packId in preferences.selectedPackIds
                }
            }
            val inventoryDiagnostics = inventoryDiagnostics(
                rawInventory = rawInventory,
                eligibleInventory = filteredInventory,
                preferences = preferences,
                completedContentIds = uiState.completedContentIds,
                userLinks = uiState.userLinks,
                userDocuments = uiState.userDocuments,
            )
            val signals = buildRecommendationSignals(nowMillis = processingNowMillis)
            val recommendationSet = recommendationEngine.generate(
                targetApp = targetApp,
                preferences = preferences,
                inventory = filteredInventory,
                excludedContentIds = excludedContentIdsForRecommendation(),
                signals = signals,
                nowMillis = processingNowMillis,
            )

            if (recommendationSet == null) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.NO_RECOMMENDATION_AVAILABLE,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        metadata = interventionMetadata + inventoryDiagnostics,
                    ),
                )
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    currentInterventionId = null,
                    currentInterventionShownAtMillis = null,
                    currentOpenAnywayUnlockAvailableAtMillis = null,
                    currentRecommendationSet = null,
                    activeDelayWindow = null,
                    activeDelaySuggestion = null,
                    currentInterventionOrigin = null,
                    currentInterventionMetadata = emptyMap(),
                    currentInterventionSuppressionKey = null,
                    currentInterventionBedtimeEnforced = false,
                    latestMessage = "No replacement inventory is available yet.",
                    screen = MainScreen.Home,
                )
                return@launch
            }

            val backupIds = recommendationSet.backups.map(ContentItem::id)

            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.INTERVENTION_SHOWN,
                    timestampMillis = processingNowMillis,
                    interventionId = interventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = backupIds,
                    contentId = recommendationSet.primary.id,
                    metadata = interventionMetadata + recommendationSet.analyticsMetadata() + inventoryDiagnostics,
                ),
            )

            if (recommendationSet.inventoryShortage) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.INVENTORY_SHORTAGE,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = interventionMetadata + recommendationSet.analyticsMetadata() + inventoryDiagnostics,
                    ),
                )
            }
            val interventionMode = uiState.interventionMode
            val openAnywayDelayMillis = when {
                bedtimeActive -> BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS
                interventionMode == InterventionMode.FIRM -> FORM_INTERVENTION_UNLOCK_DELAY_MILLIS
                else -> null
            }
            val openAnywayAvailableAtMillis = openAnywayDelayMillis?.let { delayMillis ->
                processingNowMillis + delayMillis
            }
            if (bedtimeActive) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.BEDTIME_INTERVENTION_SHOWN,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = interventionMetadata + recommendationSet.analyticsMetadata() + mapOf(
                            "interventionMode" to interventionMode.name,
                            "bedtimeStartMinutes" to uiState.bedtimeStartMinutes.toString(),
                            "bedtimeEndMinutes" to uiState.bedtimeEndMinutes.toString(),
                            "openAnywayUnlockDelayMillis" to BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS.toString(),
                        ),
                    ),
                )
            } else if (interventionMode == InterventionMode.FIRM) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.FORM_INTERVENTION_SHOWN,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = interventionMetadata + recommendationSet.analyticsMetadata() + mapOf(
                            "interventionMode" to interventionMode.name,
                            "openAnywayUnlockDelayMillis" to FORM_INTERVENTION_UNLOCK_DELAY_MILLIS.toString(),
                        ),
                    ),
                )
            }
            unfinishedProgressFor(recommendationSet.primary.id)?.let { progress ->
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.UNFINISHED_CONTENT_RECOMMENDED_AS_PRIMARY,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = interventionMetadata + recommendationSet.primary.analyticsMetadata() + progress.analyticsMetadata() + mapOf(
                            "unfinishedContentCount" to preferences.unfinishedContentIds.size.toString(),
                        ),
                    ),
                )
            }

            uiState = uiState.copy(
                selectedTargetApp = targetApp,
                currentInterventionId = interventionId,
                currentInterventionShownAtMillis = processingNowMillis,
                currentOpenAnywayUnlockAvailableAtMillis = openAnywayAvailableAtMillis,
                currentRecommendationSet = recommendationSet,
                currentInterventionOrigin = origin,
                currentInterventionMetadata = interventionMetadata,
                currentInterventionSuppressionKey = suppressionKey,
                isBedtimeActive = bedtimeActive,
                currentInterventionBedtimeEnforced = bedtimeActive,
                activeDelayWindow = null,
                activeDelaySuggestion = null,
                latestMessage = null,
                screen = MainScreen.Intervention,
            )
        }
    }

    fun acceptPrimary() {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val interventionShownAtMillis = uiState.currentInterventionShownAtMillis ?: return
        val nowMillis = nowProvider()
        viewModelScope.launch {
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = recommendationSet.primary,
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.PRIMARY_ACCEPTED,
                    timestampMillis = nowMillis,
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = recommendationSet.primary.id,
                    metadata = uiState.currentInterventionMetadata + recommendationSet.primary.analyticsMetadata(),
                ),
            )
            recordFormInterventionCompleted(action = "primary", nowMillis = nowMillis, contentId = recommendationSet.primary.id)
            openReplacementSession(content = recommendationSet.primary, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    fun acceptBackup(content: ContentItem) {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val interventionShownAtMillis = uiState.currentInterventionShownAtMillis ?: return
        val nowMillis = nowProvider()
        viewModelScope.launch {
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = content,
                source = RecommendationSource.BACKUP,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.BACKUP_ACCEPTED,
                    timestampMillis = nowMillis,
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = content.id,
                    metadata = uiState.currentInterventionMetadata + content.analyticsMetadata(),
                ),
            )
            recordFormInterventionCompleted(action = "backup", nowMillis = nowMillis, contentId = content.id)
            openReplacementSession(content = content, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    fun delayFor15Minutes() {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val nowMillis = nowProvider()
        if (ensureCurrentInterventionBedtimeEnforced(nowMillis = nowMillis) || uiState.currentInterventionBedtimeEnforced) {
            return
        }
        viewModelScope.launch {
            val window = delayGate.storeDelayDurably(
                targetApp = targetApp,
                nowMillis = nowMillis,
                interventionId = interventionId,
                interventionShownAtMillis = uiState.currentInterventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
            )
            recordDelaySelectedDurably(window)
            recordFormInterventionCompleted(action = "delay", nowMillis = nowMillis)
            uiState = uiState.copy(
                screen = MainScreen.Home,
                currentInterventionId = null,
                currentInterventionShownAtMillis = null,
                currentOpenAnywayUnlockAvailableAtMillis = null,
                currentRecommendationSet = null,
                currentInterventionOrigin = null,
                currentInterventionMetadata = emptyMap(),
                currentInterventionSuppressionKey = null,
                currentInterventionBedtimeEnforced = false,
                activeDelayWindow = window,
                activeDelaySuggestion = activeDelaySuggestionFor(window),
                latestMessage = "${targetApp.displayName} paused for 15 minutes.",
            )
        }
    }

    fun recordFormInterventionUnlockAvailable(nowMillis: Long = nowProvider()) {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val availableAtMillis = uiState.currentOpenAnywayUnlockAvailableAtMillis ?: return
        recordEvent(
            AnalyticsEvent(
                type = if (uiState.currentInterventionBedtimeEnforced) {
                    AnalyticsEventType.BEDTIME_UNLOCK_ENABLED
                } else {
                    AnalyticsEventType.FORM_INTERVENTION_UNLOCK_ENABLED
                },
                timestampMillis = nowMillis,
                interventionId = interventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                metadata = uiState.currentInterventionMetadata + mapOf(
                    "interventionMode" to uiState.interventionMode.name,
                    "openAnywayUnlockAvailableAtMillis" to availableAtMillis.toString(),
                    "elapsedMillis" to (nowMillis - (uiState.currentInterventionShownAtMillis ?: nowMillis))
                        .coerceAtLeast(0L)
                        .toString(),
                ),
            ),
        )
    }

    fun abandonFormIntervention(reason: String = "back", nowMillis: Long = nowProvider()) {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        if (uiState.interventionMode == InterventionMode.FIRM && !uiState.currentInterventionBedtimeEnforced) {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.FORM_INTERVENTION_ABANDONED,
                    timestampMillis = nowMillis,
                    interventionId = interventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    metadata = uiState.currentInterventionMetadata + mapOf(
                        "reason" to reason,
                        "interventionMode" to uiState.interventionMode.name,
                        "openAnywayUnlockAvailableAtMillis" to uiState.currentOpenAnywayUnlockAvailableAtMillis.orEmptyString(),
                    ),
                ),
            )
        }
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentOpenAnywayUnlockAvailableAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentInterventionMetadata = emptyMap(),
            currentInterventionSuppressionKey = null,
            currentInterventionBedtimeEnforced = false,
            latestMessage = null,
        )
    }

    fun startActiveDelayAlternative() {
        val targetApp = uiState.selectedTargetApp ?: return
        val delayWindow = uiState.activeDelayWindow ?: return
        val nowMillis = nowProvider()
        val activeDelayWindow = if (delayWindow.targetAppPackage == targetApp.packageName) {
            delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis)
        } else {
            null
        }
        if (activeDelayWindow == null || activeDelayWindow.id != delayWindow.id || !activeDelayWindow.isActive(nowMillis)) {
            refreshActiveDelayWindow(nowMillis = nowMillis)
            return
        }
        val content = activeDelaySuggestionFor(activeDelayWindow, nowMillis = nowMillis)
        if (content == null) {
            uiState = uiState.copy(
                activeDelayWindow = activeDelayWindow,
                activeDelaySuggestion = null,
                latestMessage = "No paused alternative is available right now.",
            )
            return
        }
        val recommendationSet = recommendationSetForDelayWindow(delayWindow = activeDelayWindow, fallbackContent = content)
        val interventionId = activeDelayWindow.interventionId ?: "delay-${activeDelayWindow.id}"
        val interventionShownAtMillis = activeDelayWindow.interventionShownAtMillis ?: activeDelayWindow.startsAtMillis

        viewModelScope.launch {
            val source = if (content.id == recommendationSet.primary.id) {
                RecommendationSource.PRIMARY
            } else {
                RecommendationSource.BACKUP
            }
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = content,
                source = source,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.DELAY_ALTERNATIVE_STARTED,
                    timestampMillis = nowMillis,
                    semanticKey = AnalyticsSemanticKeys.delayAlternativeStarted(
                        delayId = activeDelayWindow.id,
                        sessionId = sessionId,
                    ),
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + mapOf(
                        "delayId" to activeDelayWindow.id,
                        "origin" to "active_delay_card",
                    ),
                ),
            )
            uiState = uiState.copy(
                currentInterventionId = interventionId,
                currentInterventionShownAtMillis = interventionShownAtMillis,
                currentOpenAnywayUnlockAvailableAtMillis = null,
                currentRecommendationSet = recommendationSet,
                currentInterventionOrigin = null,
                currentInterventionMetadata = emptyMap(),
                currentInterventionSuppressionKey = null,
                currentInterventionBedtimeEnforced = false,
            )
            openReplacementSession(content = content, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    private fun ensureCurrentInterventionBedtimeEnforced(nowMillis: Long): Boolean {
        if (uiState.screen != MainScreen.Intervention) return false
        if (uiState.currentInterventionBedtimeEnforced) return false
        val targetApp = uiState.selectedTargetApp ?: return false
        val recommendationSet = uiState.currentRecommendationSet ?: return false
        val bedtimeActiveNow = bedtimeWindowIsActive(
            enabled = uiState.bedtimeEnabled,
            startMinutes = uiState.bedtimeStartMinutes,
            endMinutes = uiState.bedtimeEndMinutes,
            nowMillis = nowMillis,
        )
        if (!bedtimeActiveNow) return false
        val bedtimeUnlockAvailableAtMillis = nowMillis + BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.BEDTIME_INTERVENTION_SHOWN,
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                contentId = recommendationSet.primary.id,
                metadata = uiState.currentInterventionMetadata + recommendationSet.analyticsMetadata() + mapOf(
                    "interventionMode" to uiState.interventionMode.name,
                    "bedtimeStartMinutes" to uiState.bedtimeStartMinutes.toString(),
                    "bedtimeEndMinutes" to uiState.bedtimeEndMinutes.toString(),
                    "openAnywayUnlockDelayMillis" to BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS.toString(),
                    "bedtimeActivatedAfterInterventionShown" to "true",
                ),
            ),
        )
        uiState = uiState.copy(
            isBedtimeActive = true,
            currentInterventionBedtimeEnforced = true,
            currentOpenAnywayUnlockAvailableAtMillis = bedtimeUnlockAvailableAtMillis,
            latestMessage = "Bedtime is active. Breathe for one minute before the emergency unlock.",
        )
        return true
    }

    fun refreshBedtimeInterventionBoundary(nowMillis: Long = nowProvider()) {
        ensureCurrentInterventionBedtimeEnforced(nowMillis = nowMillis)
    }

    fun openAnyway(): Boolean {
        val targetApp = uiState.selectedTargetApp ?: return false
        val recommendationSet = uiState.currentRecommendationSet
        val nowMillis = nowProvider()
        if (ensureCurrentInterventionBedtimeEnforced(nowMillis = nowMillis)) {
            return false
        }
        val openAnywayAvailableAtMillis = uiState.currentOpenAnywayUnlockAvailableAtMillis
        val isFirmMode = uiState.interventionMode == InterventionMode.FIRM
        if (openAnywayAvailableAtMillis != null && nowMillis < openAnywayAvailableAtMillis) {
            recordEvent(
                AnalyticsEvent(
                    type = if (uiState.currentInterventionBedtimeEnforced) {
                        AnalyticsEventType.BEDTIME_UNLOCK_BLOCKED
                    } else {
                        AnalyticsEventType.FORM_INTERVENTION_UNLOCK_BLOCKED
                    },
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet?.primary?.id,
                    backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
                    metadata = uiState.currentInterventionMetadata + mapOf(
                        "interventionMode" to uiState.interventionMode.name,
                        "openAnywayUnlockAvailableAtMillis" to openAnywayAvailableAtMillis.toString(),
                        "remainingMillis" to (openAnywayAvailableAtMillis - nowMillis).toString(),
                    ),
                ),
            )
            uiState = uiState.copy(
                latestMessage = if (uiState.currentInterventionBedtimeEnforced) {
                    "Breathe for one minute before the emergency unlock."
                } else {
                    "Take five seconds before opening ${targetApp.displayName}."
                },
            )
            return false
        }
        val shouldExitToTarget = uiState.currentInterventionOrigin == InterventionOrigin.SYSTEM
        val unlockMinutes = uiState.openAnywayUnlockMinutes
            .coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
        val unlockUntilMillis = nowMillis + unlockMinutes * 60_000L
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.OPEN_ANYWAY_SELECTED,
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet?.primary?.id,
                backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
                metadata = uiState.currentInterventionMetadata + mapOf(
                    "interventionMode" to uiState.interventionMode.name,
                    "openAnywayUnlockMinutes" to unlockMinutes.toString(),
                    "openAnywayUnlockUntilMillis" to unlockUntilMillis.toString(),
                    "formUnlockWaitMillis" to when {
                        uiState.currentInterventionBedtimeEnforced -> BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS.toString()
                        isFirmMode -> FORM_INTERVENTION_UNLOCK_DELAY_MILLIS.toString()
                        else -> "0"
                    },
                    "bedtimeActive" to uiState.currentInterventionBedtimeEnforced.toString(),
                ),
            ),
        )
        if (uiState.currentInterventionBedtimeEnforced) {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.BEDTIME_UNLOCK_USED,
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet?.primary?.id,
                    backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
                    metadata = uiState.currentInterventionMetadata + mapOf(
                        "interventionMode" to uiState.interventionMode.name,
                        "openAnywayUnlockAvailableAtMillis" to (openAnywayAvailableAtMillis?.toString() ?: "0"),
                        "openAnywayUnlockUntilMillis" to unlockUntilMillis.toString(),
                    ),
                ),
            )
        }
        if (isFirmMode && !uiState.currentInterventionBedtimeEnforced) {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.FORM_INTERVENTION_UNLOCK_USED,
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet?.primary?.id,
                    backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
                    metadata = uiState.currentInterventionMetadata + mapOf(
                        "interventionMode" to uiState.interventionMode.name,
                        "openAnywayUnlockAvailableAtMillis" to (openAnywayAvailableAtMillis?.toString() ?: "0"),
                        "openAnywayUnlockUntilMillis" to unlockUntilMillis.toString(),
                    ),
                ),
            )
        }
        recordFormInterventionCompleted(action = "open_anyway", nowMillis = nowMillis)
        if (shouldExitToTarget) {
            InterceptionRuntimeGate.suppressPackage(
                targetAppPackage = targetApp.packageName,
                untilMillis = unlockUntilMillis,
                allowedDuringBedtime = uiState.currentInterventionBedtimeEnforced,
                targetKey = uiState.currentInterventionSuppressionKey ?: targetApp.packageName,
            )
        }
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentOpenAnywayUnlockAvailableAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentInterventionMetadata = emptyMap(),
            currentInterventionSuppressionKey = null,
            currentInterventionBedtimeEnforced = false,
            isBedtimeActive = bedtimeWindowIsActive(
                enabled = uiState.bedtimeEnabled,
                startMinutes = uiState.bedtimeStartMinutes,
                endMinutes = uiState.bedtimeEndMinutes,
                nowMillis = nowMillis,
            ),
            activeDelaySuggestion = null,
            latestMessage = "${targetApp.displayName} unlocked for $unlockMinutes minutes.",
        )
        return shouldExitToTarget
    }

    fun saveCurrentReadingProgress(
        progressPercent: Int,
        lastVisibleParagraphIndex: Int,
        lastVisibleTextOffset: Int = 0,
        paragraphCount: Int,
        nowMillis: Long = nowProvider(),
    ) {
        val content = uiState.currentContent ?: return
        if (!content.usesRepositoryBody()) {
            return
        }
        if (hasCompletedProgressForActiveRead(content.id)) {
            return
        }
        val safeParagraphCount = paragraphCount.coerceAtLeast(1)
        val progressUpdatedAtMillis = nextReadingProgressUpdatedAtMillis(
            contentId = content.id,
            nowMillis = nowMillis,
        )
        val progress = ReadingProgress(
            contentId = content.id,
            progressPercent = progressPercent.coerceIn(1, 99),
            lastVisibleParagraphIndex = lastVisibleParagraphIndex.coerceIn(0, safeParagraphCount - 1),
            lastVisibleTextOffset = lastVisibleTextOffset.coerceAtLeast(0),
            paragraphCount = safeParagraphCount,
            updatedAtMillis = progressUpdatedAtMillis,
        )
        val sameVisiblePosition = uiState.currentReadingProgress?.sameVisiblePosition(progress) == true
        if (!sameVisiblePosition || uiState.currentReadingProgress?.updatedAtMillis != progress.updatedAtMillis) {
            val updatedReadingProgress = uiState.readingProgress.upsertReadingProgress(progress)
            uiState = uiState.copy(
                readingProgress = updatedReadingProgress,
                preferences = uiState.preferences?.copy(
                    unfinishedContentIds = updatedReadingProgress.unfinishedContentIds(),
                ),
                currentReadingProgress = progress,
            )
        }
        readingProgressRepository.cachePendingProgress(progress)
        val progressSaveJob = (progressPersistenceScope ?: viewModelScope).launch {
            readingProgressRepository.saveProgress(progress)
        }
        viewModelScope.launch {
            progressSaveJob.join()
            if (!sameVisiblePosition) {
                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.READING_PROGRESS_SAVED,
                        timestampMillis = progressUpdatedAtMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = uiState.currentSessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = content.analyticsMetadata() + progress.analyticsMetadata(),
                    ),
                )
            }
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = progressUpdatedAtMillis)
        }
    }

    fun saveCurrentReadingAnnotation(
        paragraphIndex: Int,
        quotedText: String,
        noteText: String,
        existingAnnotationId: String? = null,
        selector: ReadingAnnotationSelector = ReadingAnnotationSelector(),
        nowMillis: Long = nowProvider(),
    ) {
        val content = uiState.currentContent ?: return
        if (!content.usesRepositoryBody()) {
            return
        }
        val normalizedQuote = quotedText.trim().take(MAX_READING_ANNOTATION_QUOTE_LENGTH)
        val normalizedNote = noteText.trim()
        if (normalizedQuote.isBlank() || normalizedNote.isBlank()) {
            uiState = uiState.copy(latestMessage = "Write a note before saving.")
            return
        }
        viewModelScope.launch {
            readingAnnotationRepository.saveAnnotation(
	                draft = ReadingAnnotationDraft(
	                    id = existingAnnotationId,
	                    contentId = content.id,
	                    paragraphIndex = paragraphIndex,
	                    quotedText = normalizedQuote,
	                    noteText = normalizedNote,
	                    sourceTitle = content.title,
	                    sourceLabel = content.sourceLabel,
	                    sourceType = content.sourceType,
	                    sourceFormat = content.format,
	                    selector = selector,
	                ),
                nowMillis = nowMillis,
            )
            val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            val savedMessage = if (existingAnnotationId == null) {
                "Annotation saved."
            } else {
                "Annotation updated."
            }
            uiState = uiState.copy(
                latestMessage = savedMessage.withAnnotationAutosaveResult(annotationAutosaveResult),
            )
        }
    }

    fun deleteReadingAnnotation(
        annotationId: String,
        nowMillis: Long = nowProvider(),
    ) {
        if (uiState.readingAnnotations.none { annotation -> annotation.id == annotationId }) {
            uiState = uiState.copy(latestMessage = "That annotation is no longer available.")
            return
        }
        viewModelScope.launch {
            readingAnnotationRepository.deleteAnnotation(
                annotationId = annotationId,
                nowMillis = nowMillis,
            )
            val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                latestMessage = "Annotation deleted.".withAnnotationAutosaveResult(annotationAutosaveResult),
            )
        }
    }

    fun finishReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        val nowMillis = nowProvider()
        viewModelScope.launch {
            val completedProgress = completedProgressFor(content = content, completedAtMillis = nowMillis)
            readingProgressRepository.saveProgress(completedProgress)
            if (sessionId != null) {
                historyRepository.markCompleted(sessionId = sessionId, completedAtMillis = nowMillis)
            }
            deactivateCompletedContentOverride(contentId = content.id)
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.READER_COMPLETED,
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = sessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = content.id,
                    metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata() + completedProgress.analyticsMetadata(),
                ),
            )
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            uiState = uiState.copy(
                screen = MainScreen.Feedback,
                currentReadingProgress = completedProgress,
            )
        }
    }

    fun finishMeditationReset(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.MEDITATION_TIMER_COMPLETED,
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = sessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = content.id,
                    metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                ),
            )
            uiState = uiState.copy(screen = MainScreen.Feedback)
        }
    }

    fun skipReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.READER_SKIPPED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            clearActiveSession(
                latestMessage = "Replacement session skipped.",
            )
        }
    }

    fun skipMeditationReset(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.MEDITATION_TIMER_SKIPPED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            clearActiveSession(latestMessage = "Meditation reset skipped.")
        }
    }

    fun submitFeedback(fitRating: String, scrollRating: String) {
        val wasGoodFit = fitRating != FEEDBACK_FIT_NOT
        val helpedAvoidScrolling = scrollRating != FEEDBACK_SCROLL_NO
        val feedback = SessionFeedback(
            wasGoodFit = wasGoodFit,
            helpedAvoidScrolling = helpedAvoidScrolling,
            fitRating = fitRating,
            scrollRating = scrollRating,
            submittedAtMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            uiState.currentSessionId?.let { sessionId ->
                historyRepository.attachFeedback(sessionId = sessionId, feedback = feedback)
            }
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.FEEDBACK_SUBMITTED,
                    timestampMillis = feedback.submittedAtMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = uiState.currentSessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = uiState.currentContent?.id,
                    metadata = mapOf(
                        "goodFit" to wasGoodFit.toString(),
                        "helpedAvoidScrolling" to helpedAvoidScrolling.toString(),
                        "fitRating" to fitRating,
                        "scrollRating" to scrollRating,
                    ),
                ),
            )
            clearActiveSession(
                lastFeedback = feedback,
                screen = MainScreen.Progress,
                latestMessage = "Feedback captured for the replacement session.",
            )
        }
    }

    fun skipFeedback() {
        clearActiveSession(
            screen = MainScreen.Progress,
            latestMessage = "Feedback skipped for this session.",
        )
    }

    fun selectThemeMode(themeMode: AppThemeMode) {
        if (uiState.themeMode == themeMode) return
        uiState = uiState.copy(themeMode = themeMode)
        viewModelScope.launch {
            settingsRepository.saveThemeMode(themeMode)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun selectInterventionMode(mode: InterventionMode) {
        if (uiState.interventionMode == mode) return
        uiState = uiState.copy(interventionMode = mode)
        viewModelScope.launch {
            settingsRepository.saveInterventionMode(mode)
            autosaveAccountLightProfileAfterPortableMutation()
        }
    }

    fun dismissMessage() {
        uiState = uiState.copy(latestMessage = null)
    }

    fun exportAccountLightProfile(writeJson: suspend (String) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isAccountLightExporting = true,
                accountLightStatus = null,
                accountLightImportError = null,
            )
            try {
                val json = accountLightProfileExporter.exportSettingsOnlyProfileJson(nowMillis = nowProvider())
                writeJson(json)
                uiState = uiState.copy(
                    isAccountLightExporting = false,
                    accountLightStatus = "Portable profile exported.",
                    latestMessage = "Portable profile exported.",
                )
            } catch (exception: CancellationException) {
                uiState = uiState.copy(isAccountLightExporting = false)
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isAccountLightExporting = false,
                    accountLightImportError = "Export failed.",
                    latestMessage = "Portable profile export failed.",
                )
            }
        }
    }

    fun configureAccountLightProfileAutosave(
        uri: String,
        displayName: String,
        persistWritePermission: (String) -> Unit = {},
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedUri = uri.trim()
        if (normalizedUri.isBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a destination for profile backup.")
            return
        }
        val normalizedDisplayName = displayName.trim().ifBlank { "Profile backup folder" }
        viewModelScope.launch {
            val permissionError = runCatching { persistWritePermission(normalizedUri) }.exceptionOrNull()
            settingsRepository.saveProfileAutosaveDestination(
                uri = normalizedUri,
                displayName = normalizedDisplayName,
            )
            if (permissionError != null) {
                val message = permissionError.profileAutosaveErrorMessage()
                settingsRepository.saveProfileAutosaveFailure(message)
                uiState = uiState.copy(
                    profileAutosaveUri = normalizedUri,
                    profileAutosaveDisplayName = normalizedDisplayName,
                    profileAutosaveUsesLocalDefault = false,
                    profileAutosaveLastSuccessfulAtMillis = null,
                    profileAutosaveLastError = message,
                    latestMessage = "Profile backup needs folder permission.",
                )
                return@launch
            }
            val saved = autosaveAccountLightProfileTo(uri = normalizedUri, nowMillis = nowMillis)
            uiState = uiState.copy(
                profileAutosaveUri = normalizedUri,
                profileAutosaveDisplayName = normalizedDisplayName,
                profileAutosaveUsesLocalDefault = false,
                profileAutosaveLastSuccessfulAtMillis = if (saved) nowMillis else null,
                latestMessage = if (saved) {
                    "Profile backup destination changed."
                } else {
                    "Profile backup destination changed, but the first write failed."
                },
            )
        }
    }

    fun clearAccountLightProfileAutosave(releaseWritePermission: (String) -> Unit = {}) {
        val uri = uiState.profileAutosaveUri
        val wasUsingLocalDefault = uiState.profileAutosaveUsesLocalDefault
        viewModelScope.launch {
            if (!wasUsingLocalDefault) {
                uri?.takeIf(String::isNotBlank)?.let { configuredUri ->
                    runCatching { releaseWritePermission(configuredUri) }
                }
            }
            settingsRepository.clearProfileAutosaveDestination()
            val defaultUri = normalizedDefaultProfileAutosaveUri()
            val hasDefault = defaultUri != null
            uiState = uiState.copy(
                profileAutosaveUri = defaultUri,
                profileAutosaveDisplayName = defaultProfileAutosaveDisplayName.takeIf { hasDefault },
                profileAutosaveUsesLocalDefault = hasDefault,
                profileAutosaveLastSuccessfulAtMillis = null,
                profileAutosaveLastError = null,
                latestMessage = if (hasDefault) {
                    "Profile backup returned to the default folder."
                } else {
                    "Profile autosave disabled."
                },
            )
        }
    }

    fun retryAccountLightProfileAutosave(nowMillis: Long = nowProvider()) {
        val uri = uiState.profileAutosaveUri
        if (uri.isNullOrBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a destination for profile backup.")
            return
        }
        viewModelScope.launch {
            val saved = autosaveAccountLightProfileTo(uri = uri, nowMillis = nowMillis)
            uiState = uiState.copy(
                latestMessage = if (saved) {
                    "Profile backup saved."
                } else {
                    "Profile backup failed."
                },
            )
        }
    }

    fun previewDefaultAccountLightProfileImport() {
        loadDefaultAccountLightProfileImport(applyImmediately = false)
    }

    fun restoreDefaultAccountLightProfileFromOnboarding() {
        loadDefaultAccountLightProfileImport(applyImmediately = true)
    }

    private fun loadDefaultAccountLightProfileImport(applyImmediately: Boolean) {
        val uri = normalizedDefaultProfileAutosaveUri()
        if (uri == null) {
            uiState = uiState.copy(latestMessage = "No default profile backup location is available.")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(
                isAccountLightImporting = true,
                accountLightImportError = null,
                accountLightStatus = "Checking default profile backup...",
            )
            val rawJson = try {
                withContext(documentWorkDispatcher) {
                    accountLightProfileBackupReader.readProfileJson(
                        uri = uri,
                        fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
                    )
                }
            } catch (exception: CancellationException) {
                uiState = uiState.copy(isAccountLightImporting = false)
                throw exception
            } catch (exception: Exception) {
                null
            }
            if (rawJson.isNullOrBlank()) {
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = "No profile backup found at $defaultProfileAutosaveDisplayName.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    screen = if (uiState.hasCompletedOnboarding) uiState.screen else MainScreen.Onboarding,
                    latestMessage = "No profile backup found.",
                )
                return@launch
            }
            val plan = try {
                accountLightProfileImporter.validateImportProfileJson(rawJson)
            } catch (exception: AccountLightImportException) {
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = exception.message ?: "Default profile backup is invalid.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Default profile backup is invalid.",
                )
                return@launch
            } catch (exception: Exception) {
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = "Default profile backup is invalid.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Default profile backup is invalid.",
                )
                return@launch
            }
            if (!applyImmediately) {
                pendingAccountLightImportPlan = plan
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = plan.preview,
                    accountLightImportError = null,
                    accountLightStatus = "Default profile backup ready to import.",
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Review default profile backup before replacing local data.",
                )
                return@launch
            }
            try {
                accountLightProfileImporter.applyReplace(plan)
                val profileAutosaved = autosaveAccountLightProfileIfConfigured(nowMillis = nowProvider())
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = null,
                    accountLightStatus = "Default backup restored from $defaultProfileAutosaveDisplayName.",
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Portable profile restored.".withProfileAutosaveResult(profileAutosaved),
                )
            } catch (exception: CancellationException) {
                uiState = uiState.copy(isAccountLightImporting = false)
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportError = "Default backup restore failed before any settings were changed.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Default profile backup restore failed.",
                )
            }
        }
    }

    fun previewAccountLightImport(rawJson: String) {
        val plan = try {
            accountLightProfileImporter.validateImportProfileJson(rawJson)
        } catch (exception: AccountLightImportException) {
            pendingAccountLightImportPlan = null
            uiState = uiState.copy(
                accountLightImportPreview = null,
                accountLightImportError = exception.message ?: "Portable profile is invalid.",
                accountLightStatus = null,
                isAccountLightReplaceConfirming = false,
                latestMessage = "Portable profile import failed validation.",
            )
            return
        } catch (exception: Exception) {
            pendingAccountLightImportPlan = null
            uiState = uiState.copy(
                accountLightImportPreview = null,
                accountLightImportError = "Portable profile is invalid.",
                accountLightStatus = null,
                isAccountLightReplaceConfirming = false,
                latestMessage = "Portable profile import failed validation.",
            )
            return
        }
        pendingAccountLightImportPlan = plan
        uiState = uiState.copy(
            accountLightImportPreview = plan.preview,
            accountLightImportError = null,
            accountLightStatus = "Profile ready to import.",
            isAccountLightReplaceConfirming = false,
        )
    }

    fun reportAccountLightImportReadFailure() {
        pendingAccountLightImportPlan = null
        uiState = uiState.copy(
            accountLightImportPreview = null,
            accountLightImportError = "Could not read portable profile.",
            accountLightStatus = null,
            isAccountLightReplaceConfirming = false,
            latestMessage = "Portable profile import failed.",
        )
    }

    fun applyAccountLightMergeImport() {
        val plan = pendingAccountLightImportPlan ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isAccountLightImporting = true)
            try {
                val result = accountLightProfileImporter.applyMerge(plan)
                val profileAutosaved = autosaveAccountLightProfileIfConfigured(nowMillis = nowProvider())
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = null,
                    accountLightStatus = if (result.warningCount > 0) {
                        "Profile merged. Local settings were kept."
                    } else {
                        "Profile merged."
                    },
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Portable profile merged.".withProfileAutosaveResult(profileAutosaved),
                )
            } catch (exception: CancellationException) {
                uiState = uiState.copy(isAccountLightImporting = false)
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportError = "Import failed before any settings were changed.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Portable profile import failed.",
                )
            }
        }
    }

    fun requestAccountLightReplaceConfirmation() {
        if (pendingAccountLightImportPlan != null) {
            uiState = uiState.copy(isAccountLightReplaceConfirming = true)
        }
    }

    fun cancelAccountLightImport() {
        pendingAccountLightImportPlan = null
        uiState = uiState.copy(
            accountLightImportPreview = null,
            accountLightImportError = null,
            accountLightStatus = null,
            isAccountLightReplaceConfirming = false,
            isAccountLightImporting = false,
        )
    }

    fun confirmAccountLightReplaceImport() {
        val plan = pendingAccountLightImportPlan ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isAccountLightImporting = true)
            try {
                accountLightProfileImporter.applyReplace(plan)
                val profileAutosaved = autosaveAccountLightProfileIfConfigured(nowMillis = nowProvider())
                pendingAccountLightImportPlan = null
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportPreview = null,
                    accountLightImportError = null,
                    accountLightStatus = "Imported profile replaced local portable settings and library.",
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Portable profile restored.".withProfileAutosaveResult(profileAutosaved),
                )
            } catch (exception: CancellationException) {
                uiState = uiState.copy(isAccountLightImporting = false)
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isAccountLightImporting = false,
                    accountLightImportError = "Import failed before any settings were changed.",
                    accountLightStatus = null,
                    isAccountLightReplaceConfirming = false,
                    latestMessage = "Portable profile import failed.",
                )
            }
        }
    }

    private fun normalizedDefaultAnnotationExportUri(): String? {
        return defaultAnnotationExportUri?.trim()?.takeIf(String::isNotBlank)
    }

    private fun normalizedDefaultProfileAutosaveUri(): String? {
        return defaultProfileAutosaveUri?.trim()?.takeIf(String::isNotBlank)
    }

    private fun applySettings(settings: AppSettings) {
        val reactivatedCompletedContentIds = settings.reactivatedCompletedContentIds.trackedCompletedContentIds()
        val preferences = settings.toUserPreferences(
            supportedApps = supportedApps,
            fallbackPackIds = defaultSelectedPackIds,
        ).withReadingProgress(uiState.readingProgress)
        val availableTargetApps = if (settings.hasCompletedOnboarding) preferences.selectedApps else emptyList()
        val selectedTargetApp = if (settings.hasCompletedOnboarding) {
            uiState.selectedTargetApp.takeIf { candidate ->
                availableTargetApps.any { it.packageName == candidate?.packageName }
            } ?: availableTargetApps.firstOrNull()
        } else {
            null
        }
        val activeDelayWindow = selectedTargetApp?.takeIf { delayReady }?.let { delayGate.activeDelay(it) }
        val configuredAnnotationExportUri = settings.annotationExportUri?.takeIf(String::isNotBlank)
        val effectiveAnnotationExportUri = configuredAnnotationExportUri ?: normalizedDefaultAnnotationExportUri()
        val annotationExportUsesLocalDefault =
            configuredAnnotationExportUri == null && effectiveAnnotationExportUri != null
        val effectiveAnnotationExportDisplayName = settings.annotationExportDisplayName
            ?.takeIf(String::isNotBlank)
            ?: defaultAnnotationExportDisplayName.takeIf { annotationExportUsesLocalDefault }
        val configuredProfileAutosaveUri = settings.profileAutosaveUri?.takeIf(String::isNotBlank)
        val effectiveProfileAutosaveUri = configuredProfileAutosaveUri ?: normalizedDefaultProfileAutosaveUri()
        val profileAutosaveUsesLocalDefault =
            configuredProfileAutosaveUri == null && effectiveProfileAutosaveUri != null
        val effectiveProfileAutosaveDisplayName = settings.profileAutosaveDisplayName
            ?.takeIf(String::isNotBlank)
            ?: defaultProfileAutosaveDisplayName.takeIf { profileAutosaveUsesLocalDefault }
        val settingsNowMillis = nowProvider()
        val activeBedtimeNow = bedtimeWindowIsActive(
            enabled = settings.bedtimeEnabled,
            startMinutes = settings.bedtimeStartMinutes,
            endMinutes = settings.bedtimeEndMinutes,
            nowMillis = settingsNowMillis,
        )
        val agentInboxDriveFolderDraft = when {
            settings.agentInboxDriveFolderId.isNullOrBlank() -> uiState.agentInboxDriveFolderDraft
            settings.agentInboxDriveFolderId == uiState.agentInboxDriveFolderId &&
                uiState.agentInboxDriveFolderDraft.isNotBlank() -> uiState.agentInboxDriveFolderDraft
            else -> settings.agentInboxDriveFolderId
        }

        uiState = uiState.copy(
            hasCompletedOnboarding = settings.hasCompletedOnboarding,
            availableTargetApps = availableTargetApps,
            selectedTargetApp = selectedTargetApp,
            preferences = preferences.takeIf { settings.hasCompletedOnboarding },
            interventionMode = settings.interventionMode,
            themeMode = settings.themeMode,
            meditationDurationMinutes = settings.meditationDurationMinutes,
            readerFontScale = settings.readerFontScale,
            interfaceTextScale = settings.interfaceTextScale,
            contentPriority = settings.contentPriority,
            priorityContentIds = settings.priorityContentIds,
            reactivatedCompletedContentIds = reactivatedCompletedContentIds,
            openAnywayUnlockMinutes = settings.openAnywayUnlockMinutes,
            bedtimeEnabled = settings.bedtimeEnabled,
            bedtimeStartMinutes = settings.bedtimeStartMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES),
            bedtimeEndMinutes = settings.bedtimeEndMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES),
            isBedtimeActive = activeBedtimeNow,
            annotationExportUri = effectiveAnnotationExportUri,
            annotationExportDisplayName = effectiveAnnotationExportDisplayName,
            annotationExportUsesLocalDefault = annotationExportUsesLocalDefault,
            annotationExportLastSuccessfulAtMillis = settings.annotationExportLastSuccessfulAtMillis,
            annotationExportLastError = settings.annotationExportLastError,
            annotationDriveSyncEnabled = settings.annotationDriveSyncEnabled,
            annotationDriveFolderId = settings.annotationDriveFolderId,
            annotationDriveLastSuccessfulAtMillis = settings.annotationDriveLastSuccessfulAtMillis,
            annotationDriveLastError = settings.annotationDriveLastError,
            agentInboxDriveEnabled = settings.agentInboxDriveEnabled,
            agentInboxDriveFolderId = settings.agentInboxDriveFolderId,
            agentInboxDriveGrantMode = settings.agentInboxDriveGrantMode,
            agentInboxDriveLastSuccessfulAtMillis = settings.agentInboxDriveLastSuccessfulAtMillis,
            agentInboxDriveLastError = settings.agentInboxDriveLastError,
            agentInboxDriveFolderDraft = agentInboxDriveFolderDraft,
            profileAutosaveUri = effectiveProfileAutosaveUri,
            profileAutosaveDisplayName = effectiveProfileAutosaveDisplayName,
            profileAutosaveUsesLocalDefault = profileAutosaveUsesLocalDefault,
            profileAutosaveLastSuccessfulAtMillis = settings.profileAutosaveLastSuccessfulAtMillis,
            profileAutosaveLastError = settings.profileAutosaveLastError,
            websiteRules = settings.websiteRules,
            onboardingSelection = settings.toOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            permissionReadiness = interceptionMonitor.currentReadiness(),
            screen = when {
                !settings.hasCompletedOnboarding -> MainScreen.Onboarding
                uiState.screen == MainScreen.Onboarding -> MainScreen.Home
                uiState.screen == MainScreen.AddLink -> MainScreen.AddLink
                uiState.screen == MainScreen.AddDocument -> MainScreen.AddDocument
                uiState.screen == MainScreen.AddLinkSuccess -> MainScreen.AddLinkSuccess
                uiState.screen == MainScreen.Library -> MainScreen.Library
                uiState.screen == MainScreen.Annotations -> MainScreen.Annotations
                uiState.screen == MainScreen.Progress -> MainScreen.Progress
                uiState.screen == MainScreen.Settings -> MainScreen.Settings
                else -> uiState.screen
            },
        )
        ensureCurrentInterventionBedtimeEnforced(nowMillis = settingsNowMillis)
        if (reactivatedCompletedContentIds != settings.reactivatedCompletedContentIds) {
            viewModelScope.launch {
                settingsRepository.saveReactivatedCompletedContentIds(reactivatedCompletedContentIds)
            }
        }
        if (settings.hasCompletedOnboarding && delayReady) {
            selectedTargetApp?.let { targetApp ->
                viewModelScope.launch {
                    reconcilePersistedDelayAnalytics(targetApp = targetApp)
                }
            }
        }
    }

    private fun buildRecommendationSignals(nowMillis: Long): RecommendationSignals {
        val history = historyRepository.recentHistory(nowMillis = nowMillis)
        return RecommendationSignals(
            completedTopics = history.filter(ReplacementHistoryEntry::isCompletedReadingReplacement)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            skippedTopics = history.filter(ReplacementHistoryEntry::isSkipped)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            successfulPackIds = history.filter { entry ->
                entry.isCompletedReadingReplacement() && entry.feedbackHelpedAvoidScrolling != false
            }.mapTo(mutableSetOf(), ReplacementHistoryEntry::packId),
            timeOfDay = TimeOfDayBucket.from(nowMillis),
        )
    }

    private suspend fun recordReturnSignalIfNeeded(targetApp: DistractingApp, nowMillis: Long) {
        val signal = historyRepository.markReturnedToTarget(
            targetAppPackage = targetApp.packageName,
            returnedAtMillis = nowMillis,
        ) ?: return
        recordSessionReturnMetricsDurably(signal)
    }

    private fun maybeRecordInterceptionLatencyDegradation(
        origin: InterventionOrigin,
        targetApp: DistractingApp,
        triggeredAtMillis: Long,
        shownAtMillis: Long,
        interventionMetadata: Map<String, String> = emptyMap(),
    ) {
        if (origin != InterventionOrigin.SYSTEM) {
            return
        }
        val delayMillis = shownAtMillis - triggeredAtMillis
        if (delayMillis <= INTERVENTION_DEGRADED_THRESHOLD_MILLIS) {
            return
        }
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.INTERVENTION_DEGRADED_PERFORMANCE,
                timestampMillis = shownAtMillis,
                targetAppPackage = targetApp.packageName,
                metadata = interventionMetadata + mapOf(
                    "triggeredAtMillis" to triggeredAtMillis.toString(),
                    "shownAtMillis" to shownAtMillis.toString(),
                    "interceptionDelayMillis" to delayMillis.toString(),
                ),
            ),
        )
    }

    private suspend fun recordDelayReturnDuringActiveWindow(targetApp: DistractingApp, nowMillis: Long) {
        val activeWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis) ?: return
        val recordedWindow = delayGate.recordFirstReturnAttemptDurably(targetApp = targetApp, nowMillis = nowMillis)
            ?: activeWindow
        recordDelaySelectedDurably(recordedWindow)
        recordDelayReturnMetricsDurably(
            window = recordedWindow,
            eventTimestampMillis = recordedWindow.firstReturnAttemptAtMillis ?: nowMillis,
            origin = "active_delay",
        )
    }

    private suspend fun recordDelayReturnAfterExpiry(expiredWindow: DelayWindow, nowMillis: Long) {
        recordDelaySelectedDurably(expiredWindow)
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.RETURN_AFTER_DELAY_ENDED,
                timestampMillis = nowMillis,
                semanticKey = AnalyticsSemanticKeys.delayEnded(expiredWindow.id),
                interventionId = expiredWindow.interventionId,
                targetAppPackage = expiredWindow.targetAppPackage,
                primaryContentId = expiredWindow.primaryContentId,
                backupContentIds = expiredWindow.backupContentIds,
                contentId = expiredWindow.primaryContentId,
                metadata = mapOf(
                    "delayId" to expiredWindow.id,
                    "delayReturnOrigin" to "after_delay_expired",
                    "returnAttribution" to "delay",
                    "delayStartedAtMillis" to expiredWindow.startsAtMillis.toString(),
                    "delayEndedAtMillis" to expiredWindow.endsAtMillis.toString(),
                    "hadActiveDelayReturn" to (expiredWindow.firstReturnAttemptAtMillis != null).toString(),
                ),
            ),
        )
        if (expiredWindow.firstReturnAttemptAtMillis == null) {
            recordDelayReturnMetricsDurably(
                window = expiredWindow,
                eventTimestampMillis = nowMillis,
                origin = "after_delay_expired",
            )
        } else {
            recordDelayReturnMetricsDurably(
                window = expiredWindow,
                eventTimestampMillis = expiredWindow.firstReturnAttemptAtMillis,
                origin = "active_delay",
            )
        }
    }

    private suspend fun recordDelayReturnMetricsDurably(
        window: DelayWindow,
        eventTimestampMillis: Long,
        origin: String,
    ) {
        delayReturnMetricEvents(
            window = window,
            eventTimestampMillis = eventTimestampMillis,
            origin = origin,
        ).forEach { event ->
            recordEventDurably(event)
        }
    }

    private fun delayReturnMetricEvents(
        window: DelayWindow,
        eventTimestampMillis: Long,
        origin: String,
    ): List<AnalyticsEvent> {
        val anchorMillis = window.interventionShownAtMillis ?: window.startsAtMillis
        val delta = eventTimestampMillis - anchorMillis
        val metadata = mapOf(
            "delayId" to window.id,
            "delayReturnOrigin" to origin,
            "returnAttribution" to "delay",
            "delayStartedAtMillis" to window.startsAtMillis.toString(),
            "delayEndedAtMillis" to window.endsAtMillis.toString(),
        )
        val events = mutableListOf<AnalyticsEvent>()

        if (delta <= 15 * 60_000L) {
            events += AnalyticsEvent(
                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                timestampMillis = eventTimestampMillis,
                semanticKey = AnalyticsSemanticKeys.delayReturn(
                    delayId = window.id,
                    origin = origin,
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                ),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = metadata,
            )
        }

        if (delta <= 60 * 60_000L) {
            events += AnalyticsEvent(
                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                timestampMillis = eventTimestampMillis,
                semanticKey = AnalyticsSemanticKeys.delayReturn(
                    delayId = window.id,
                    origin = origin,
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                ),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = metadata,
            )
        }

        return events
    }

    private suspend fun recordSessionReturnMetricsDurably(signal: ReturnToTargetSignal) {
        if (signal.within15Minutes) {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                    timestampMillis = signal.returnedAtMillis,
                    semanticKey = AnalyticsSemanticKeys.sessionReturn(
                        sessionId = signal.sessionId,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                    ),
                    interventionId = signal.interventionId,
                    sessionId = signal.sessionId,
                    targetAppPackage = signal.targetAppPackage,
                    primaryContentId = signal.primaryContentId,
                    backupContentIds = signal.backupContentIds,
                    contentId = signal.contentId,
                    metadata = mapOf("returnAttribution" to "session"),
                ),
            )
        }

        if (signal.within60Minutes) {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                    timestampMillis = signal.returnedAtMillis,
                    semanticKey = AnalyticsSemanticKeys.sessionReturn(
                        sessionId = signal.sessionId,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                    ),
                    interventionId = signal.interventionId,
                    sessionId = signal.sessionId,
                    targetAppPackage = signal.targetAppPackage,
                    primaryContentId = signal.primaryContentId,
                    backupContentIds = signal.backupContentIds,
                    contentId = signal.contentId,
                    metadata = mapOf("returnAttribution" to "session"),
                ),
            )
        }
    }

    private suspend fun recordDelaySelectedDurably(window: DelayWindow) {
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.DELAY_SELECTED,
                timestampMillis = window.startsAtMillis,
                semanticKey = AnalyticsSemanticKeys.delaySelected(window.id),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = mapOf(
                    "delayId" to window.id,
                    "delayStartedAtMillis" to window.startsAtMillis.toString(),
                    "delayEndedAtMillis" to window.endsAtMillis.toString(),
                    "interventionShownAtMillis" to (window.interventionShownAtMillis?.toString() ?: ""),
                ),
            ),
        )
    }

    private suspend fun reconcilePersistedDelayAnalytics(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        val activeWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis) ?: return
        recordDelaySelectedDurably(activeWindow)
        activeWindow.firstReturnAttemptAtMillis?.let { firstReturnAtMillis ->
            recordDelayReturnMetricsDurably(
                window = activeWindow,
                eventTimestampMillis = firstReturnAtMillis,
                origin = "active_delay",
            )
        }
    }

    fun currentExternalLinkUrl(): String? {
        return uiState.currentContent?.externalUrl
    }

    fun currentExternalContentMimeType(): String? {
        return when (uiState.currentContent?.format) {
            ContentFormat.PDF -> "application/pdf"
            ContentFormat.EPUB -> "application/epub+zip"
            ContentFormat.MARKDOWN -> "text/markdown"
            else -> null
        }
    }

    fun recordExternalLinkOpened(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId ?: return
        content.externalUrl ?: return
        recordEvent(
            AnalyticsEvent(
                type = if (content.sourceType == ContentSourceType.USER_LINK) {
                    AnalyticsEventType.USER_LINK_FALLBACK_OPENED
                } else {
                    AnalyticsEventType.EXTERNAL_HANDOFF_OPENED
                },
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                sessionId = sessionId,
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                contentId = content.id,
                metadata = content.analyticsMetadata(),
            ),
        )
    }

    fun recordExternalLinkHandoffFailed(
        reason: String,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            recordEvent(
                AnalyticsEvent(
                    type = if (content.sourceType == ContentSourceType.USER_LINK) {
                        AnalyticsEventType.USER_LINK_HANDOFF_FAILED
                    } else {
                        AnalyticsEventType.EXTERNAL_HANDOFF_FAILED
                    },
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = sessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + mapOf("failureReason" to reason),
                ),
            )
            when (content.sourceType) {
                ContentSourceType.USER_LINK -> userLinkRepository.markUnavailable(contentId = content.id, nowMillis = nowMillis)
                ContentSourceType.USER_DOCUMENT -> userDocumentRepository.markUnavailable(contentId = content.id, nowMillis = nowMillis)
                else -> Unit
            }
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
            }
            autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
            clearActiveSession(
                latestMessage = content.handoffFailureMessage(),
            )
        }
    }

    private suspend fun openReplacementSession(content: ContentItem, sessionId: String, startedAtMillis: Long) {
        val requestId = ++readerOpenRequestId
        if (content.usesRepositoryBody()) {
            uiState = uiState.copy(
                isReaderOpening = true,
                latestMessage = null,
            )
        } else {
            uiState = uiState.copy(isReaderOpening = false)
        }
        val readerDocument = loadReaderDocumentForSession(
            content = content,
            sessionId = sessionId,
            startedAtMillis = startedAtMillis,
            requestId = requestId,
        ) ?: return
        if (requestId != readerOpenRequestId) {
            return
        }
        val repairedContent = repairLegacyReadingTimeEstimateFromLoadedDocument(
            content = content,
            readerDocument = readerDocument,
            nowMillis = startedAtMillis,
        ) ?: content
        val existingProgress = unfinishedProgressFor(content.id)
        uiState = uiState.copy(
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentOpenAnywayUnlockAvailableAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentInterventionMetadata = emptyMap(),
            currentInterventionSuppressionKey = null,
            currentInterventionBedtimeEnforced = false,
            currentContent = repairedContent,
            currentReaderDocument = readerDocument,
            currentContentBody = readerDocument.plainText,
            isReaderOpening = false,
            currentReadingProgress = existingProgress,
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            currentSessionId = sessionId,
            currentSessionStartedAtMillis = startedAtMillis,
            screen = screenForReplacement(repairedContent),
            latestMessage = null,
        )
    }

    private suspend fun loadReaderDocumentForSession(
        content: ContentItem,
        sessionId: String?,
        startedAtMillis: Long,
        requestId: Int,
    ): ReaderDocument? {
        if (!content.usesRepositoryBody()) {
            return ReaderDocument.fromPlainText("")
        }
        return try {
            withContext(documentWorkDispatcher) {
                contentRepository.readerDocument(content)
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            if (requestId != readerOpenRequestId) {
                return null
            }
            handleRepositoryBodyLoadFailure(
                content = content,
                sessionId = sessionId,
                failedAtMillis = startedAtMillis,
                error = error,
            )
            null
        }
    }

    private fun scheduleLegacyReadingTimeEstimateRepairs(
        documents: List<ContentItem>,
        progress: List<ReadingProgress>,
    ) {
        if (legacyReadingTimeBackgroundRepairCycleStarted) {
            return
        }
        val unfinishedProgressById = progress
            .filter(ReadingProgress::isUnfinished)
            .associateBy(ReadingProgress::contentId)
        val candidates = documents
            .asSequence()
            .filter { item -> item.needsLegacyReadingTimeEstimateRepair(unfinishedProgressById[item.id]) }
            .sortedByDescending { item -> unfinishedProgressById.getValue(item.id).updatedAtMillis }
            .filter { item -> item.id !in durationRepairAttemptedContentIds }
            .filter { item -> item.id !in durationRepairInFlightContentIds }
            .take(MAX_BACKGROUND_READING_TIME_REPAIR_SCAN_COUNT)
            .toList()
        if (candidates.isEmpty()) {
            return
        }
        legacyReadingTimeBackgroundRepairCycleStarted = true
        durationRepairInFlightContentIds += candidates.map(ContentItem::id)
        viewModelScope.launch {
            var repairedCount = 0
            try {
                for (item in candidates) {
                    if (repairedCount >= MAX_BACKGROUND_READING_TIME_REPAIR_COUNT) {
                        break
                    }
                    if (!durationRepairAttemptedContentIds.add(item.id)) {
                        continue
                    }
                    val estimate = try {
                        withContext(documentWorkDispatcher) {
                            ReadingTimeEstimator.estimateFromText(contentRepository.readerDocument(item).plainText)
                        }
                    } catch (error: Throwable) {
                        if (error is CancellationException) throw error
                        continue
                    }
                    val repaired = applyRecoveredReadingTimeEstimate(
                        content = item,
                        estimatedMinutes = estimate.minutes,
                        nowMillis = nowProvider(),
                        source = "background_continue_repair",
                    )
                    if (repaired != null) {
                        repairedCount += 1
                    }
                }
            } finally {
                durationRepairInFlightContentIds.removeAll(candidates.map(ContentItem::id).toSet())
            }
        }
    }

    private suspend fun repairLegacyReadingTimeEstimateFromLoadedDocument(
        content: ContentItem,
        readerDocument: ReaderDocument,
        nowMillis: Long,
    ): ContentItem? {
        if (!content.isLegacyReadingTimeEstimateCandidate()) {
            return null
        }
        // Full-document word-count runs off the main thread (reader-open path is on Dispatchers.Main),
        // mirroring the background repair above, to avoid a jank spike when opening a long document.
        val estimate = withContext(documentWorkDispatcher) {
            ReadingTimeEstimator.estimateFromText(readerDocument.plainText)
        }
        return applyRecoveredReadingTimeEstimate(
            content = content,
            estimatedMinutes = estimate.minutes,
            nowMillis = nowMillis,
            source = "reader_open_repair",
        )
    }

    private suspend fun applyRecoveredReadingTimeEstimate(
        content: ContentItem,
        estimatedMinutes: Int,
        nowMillis: Long,
        source: String,
    ): ContentItem? {
        if (estimatedMinutes <= content.durationMinutes) {
            return null
        }
        val updated = userDocumentRepository.updateEstimatedDuration(
            contentId = content.id,
            durationMinutes = estimatedMinutes,
            nowMillis = nowMillis,
        ) ?: return null
        if (!durationRepairEventRecordedContentIds.add(updated.id)) {
            return updated
        }
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.READING_TIME_ESTIMATE_APPLIED,
                timestampMillis = nowMillis,
                contentId = updated.id,
                metadata = updated.analyticsMetadata() + mapOf(
                    "estimateSource" to ReadingTimeEstimateSource.EXTRACTED_TEXT.name,
                    "repairSource" to source,
                    "previousDurationMinutes" to content.durationMinutes.toString(),
                    "durationMinutes" to updated.durationMinutes.toString(),
                ),
            ),
        )
        autosaveAccountLightProfileAfterPortableMutation(nowMillis = nowMillis)
        return updated
    }

    private suspend fun handleRepositoryBodyLoadFailure(
        content: ContentItem,
        sessionId: String?,
        failedAtMillis: Long,
        error: Throwable,
    ) {
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.USER_DOCUMENT_BODY_LOAD_FAILED,
                timestampMillis = failedAtMillis,
                interventionId = uiState.currentInterventionId,
                sessionId = sessionId,
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                contentId = content.id,
                metadata = content.analyticsMetadata() + mapOf(
                    "failureReason" to (error::class.simpleName ?: "body_load_failed"),
                ),
            ),
        )
        if (content.sourceType == ContentSourceType.USER_DOCUMENT) {
            userDocumentRepository.markUnavailable(contentId = content.id, nowMillis = failedAtMillis)
        }
        if (sessionId != null) {
            historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = failedAtMillis)
        }
        autosaveAccountLightProfileAfterPortableMutation(nowMillis = failedAtMillis)
        clearActiveSession(
            latestMessage = content.handoffFailureMessage(),
        )
    }

    private fun screenForReplacement(content: ContentItem): MainScreen {
        return when {
            content.usesExternalHandoff() -> MainScreen.ExternalHandoff
            content.usesMeditationTimer() -> MainScreen.MeditationTimer
            else -> MainScreen.Reader
        }
    }

    private fun fullReplacementInventory(): List<ContentItem> {
        return contentRepository.inventory() + meditationTimerContentItem(uiState.meditationDurationMinutes)
    }

    private fun contentForAnnotation(annotation: ReadingAnnotation): ContentItem? {
        return fullReplacementInventory()
            .firstOrNull { content -> content.id == annotation.contentId }
    }

    private fun ReadingAnnotationSelector.hasExplicitReaderTarget(): Boolean {
        return !sourceHref.isNullOrBlank() ||
            !sourceAnchor.isNullOrBlank() ||
            sourceBlockIndex > 0 ||
            textStartOffset > 0 ||
            textEndOffset > textStartOffset ||
            prefixText.isNotBlank() ||
            suffixText.isNotBlank()
    }

    private suspend fun autosaveReadingAnnotations(nowMillis: Long): AnnotationAutosaveResult {
        return AnnotationAutosaveResult(
            fileResult = exportReadingAnnotationsIfConfigured(nowMillis = nowMillis),
            driveResult = syncReadingAnnotationsToDriveIfConnected(nowMillis = nowMillis),
        )
    }

    private suspend fun exportReadingAnnotationsIfConfigured(nowMillis: Long): Boolean? {
        val uri = uiState.annotationExportUri?.takeIf(String::isNotBlank) ?: return null
        return exportReadingAnnotationsTo(uri = uri, nowMillis = nowMillis)
    }

    private suspend fun exportReadingAnnotationsTo(uri: String, nowMillis: Long): Boolean {
        return try {
            val contentById = fullReplacementInventory()
                .distinctBy(ContentItem::id)
                .associateBy(ContentItem::id)
            val files = readingAnnotationExportFormatter.formatJsonLdFiles(
                annotations = readingAnnotationRepository.readingAnnotations(),
                contentById = contentById,
            )
            readingAnnotationExportWriter.writeJsonLdFiles(uri = uri, files = files)
            settingsRepository.saveAnnotationExportSuccess(timestampMillis = nowMillis)
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            val message = if (uiState.annotationExportUsesLocalDefault && uri == normalizedDefaultAnnotationExportUri()) {
                "Retry or change annotation sync destination."
            } else {
                error.annotationExportErrorMessage()
            }
            settingsRepository.saveAnnotationExportFailure(message)
            false
        }
    }

    private fun autosaveAccountLightProfileAfterPortableMutation(nowMillis: Long = nowProvider()) {
        // Fire-and-forget: hand the request to the conflated collector so rapid successive mutations
        // (notably reading-progress saves) coalesce into a single rewrite instead of one per call.
        profileAutosaveRequests.trySend(nowMillis)
    }

    private suspend fun autosaveAccountLightProfileIfConfigured(nowMillis: Long): Boolean? {
        val uri = uiState.profileAutosaveUri?.takeIf(String::isNotBlank) ?: return null
        return autosaveAccountLightProfileTo(uri = uri, nowMillis = nowMillis)
    }

    private suspend fun autosaveAccountLightProfileTo(uri: String, nowMillis: Long): Boolean = profileAutosaveMutex.withLock {
        uiState = uiState.copy(isProfileAutosaving = true)
        try {
            val json = accountLightProfileExporter.exportSettingsOnlyProfileJson(nowMillis = nowMillis)
            accountLightProfileAutosaveWriter.writeProfileJson(
                uri = uri,
                fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
                json = json,
            )
            settingsRepository.saveProfileAutosaveSuccess(timestampMillis = nowMillis)
            uiState = uiState.copy(
                isProfileAutosaving = false,
                profileAutosaveLastSuccessfulAtMillis = nowMillis,
                profileAutosaveLastError = null,
            )
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            val message = if (uiState.profileAutosaveUsesLocalDefault && uri == normalizedDefaultProfileAutosaveUri()) {
                "Retry or change profile backup destination."
            } else {
                error.profileAutosaveErrorMessage()
            }
            settingsRepository.saveProfileAutosaveFailure(message)
            uiState = uiState.copy(
                isProfileAutosaving = false,
                profileAutosaveLastError = message,
            )
            false
        }
    }

    private suspend fun syncReadingAnnotationsToDriveIfConnected(nowMillis: Long): Boolean? =
        annotationDriveSyncMutex.withLock {
            syncReadingAnnotationsToDriveIfConnectedLocked(nowMillis = nowMillis)
        }

    private suspend fun syncReadingAnnotationsToDriveIfConnectedLocked(nowMillis: Long): Boolean? {
        if (!uiState.annotationDriveSyncEnabled) {
            return null
        }
        val cachedAccessToken = annotationDriveAccessToken?.takeIf(String::isNotBlank)
        val accessToken = runCatching { readingAnnotationDriveTokenProvider.driveAccessToken() }
            .onSuccess { token -> annotationDriveAccessToken = token }
            .getOrElse { cachedAccessToken }
        if (accessToken == null) {
            val message = "Open Settings and tap Save now to refresh Google Drive access."
            settingsRepository.saveAnnotationDriveSyncFailure(message)
            uiState = uiState.copy(annotationDriveLastError = message)
            return false
        }
        return syncReadingAnnotationsToDrive(accessToken = accessToken, nowMillis = nowMillis)
    }

    private suspend fun syncReadingAnnotationsToDrive(accessToken: String, nowMillis: Long): Boolean {
        return try {
            val contentById = fullReplacementInventory()
                .distinctBy(ContentItem::id)
                .associateBy(ContentItem::id)
            val files = readingAnnotationExportFormatter.formatJsonLdFiles(
                annotations = readingAnnotationRepository.readingAnnotations(),
                contentById = contentById,
            )
            val indexJson = readingAnnotationExportFormatter.formatIndexJson(files)
            val result = readingAnnotationDriveSyncClient.syncJsonLdFiles(
                ReadingAnnotationDriveSyncRequest(
                    accessToken = accessToken,
                    folderId = uiState.annotationDriveFolderId,
                    files = files,
                    indexJson = indexJson,
                ),
            )
            settingsRepository.saveAnnotationDriveSyncSuccess(
                timestampMillis = nowMillis,
                folderId = result.folderId,
            )
            uiState = uiState.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = result.folderId,
                annotationDriveLastSuccessfulAtMillis = nowMillis,
                annotationDriveLastError = null,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_SUCCEEDED,
                    timestampMillis = nowMillis,
                    metadata = mapOf(
                        "syncedFileCount" to result.syncedFileNames.size.toString(),
                        "sourceFileCount" to files.size.toString(),
                    ),
                ),
            )
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            val message = error.annotationDriveSyncErrorMessage()
            settingsRepository.saveAnnotationDriveSyncFailure(message)
            uiState = uiState.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveLastError = message,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_FAILED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("reason" to message),
                ),
            )
            false
        }
    }

    private fun excludedContentIdsForRecommendation(): Set<String> {
        return uiState.completedContentIds -
            uiState.reactivatedCompletedContentIds
    }

    private suspend fun deactivateCompletedContentOverride(contentId: String) {
        if (contentId !in uiState.reactivatedCompletedContentIds) {
            return
        }
        val updatedIds = uiState.reactivatedCompletedContentIds - contentId
        uiState = uiState.copy(reactivatedCompletedContentIds = updatedIds)
        settingsRepository.saveReactivatedCompletedContentIds(updatedIds)
    }

    private fun clearActiveSession(
        lastFeedback: SessionFeedback? = uiState.lastFeedback,
        screen: MainScreen = MainScreen.Home,
        latestMessage: String,
    ) {
        readerOpenRequestId += 1
        uiState = uiState.copy(
            screen = screen,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentOpenAnywayUnlockAvailableAtMillis = null,
            currentContent = null,
            currentReaderDocument = null,
            currentContentBody = "",
            isReaderOpening = false,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentInterventionMetadata = emptyMap(),
            currentInterventionSuppressionKey = null,
            currentInterventionBedtimeEnforced = false,
            currentSessionId = null,
            currentSessionStartedAtMillis = null,
            currentReadingProgress = null,
            currentReaderStartParagraphIndex = null,
            currentReaderStartSelector = null,
            lastFeedback = lastFeedback,
            latestMessage = latestMessage,
        )
    }

    private fun sessionDurationMetadata(nowMillis: Long): Map<String, String> {
        val startedAtMillis = uiState.currentSessionStartedAtMillis ?: return emptyMap()
        val durationSeconds = ((nowMillis - startedAtMillis) / 1000L).coerceAtLeast(0)
        return mapOf("sessionDurationSeconds" to durationSeconds.toString())
    }

    private suspend fun recordPrioritySetDuringAdd(
        item: ContentItem,
        nowMillis: Long,
        priorityContentIds: Set<String>,
    ) {
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.PRIORITY_SET_DURING_ADD,
                timestampMillis = nowMillis,
                contentId = item.id,
                metadata = item.analyticsMetadata() + mapOf(
                    "priorityContentCount" to priorityContentIds.size.toString(),
                    "source" to "add_flow",
                ),
            ),
        )
    }

    private suspend fun recordReadingTimeEstimateApplied(
        item: ContentItem,
        draft: UserDocumentDraft,
        candidate: DocumentImportCandidate?,
        nowMillis: Long,
    ) {
        val source = candidate?.estimateSource ?: return
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.READING_TIME_ESTIMATE_APPLIED,
                timestampMillis = nowMillis,
                contentId = item.id,
                metadata = item.analyticsMetadata() + mapOf(
                    "estimateSource" to source.name,
                    "durationMinutes" to draft.durationMinutes.toString(),
                    "displayName" to draft.displayName,
                    "wordCount" to candidate.estimatedWordCount.orZeroString(),
                ),
            ),
        )
    }

    private fun recordEvent(event: AnalyticsEvent) {
        analyticsTracker.record(event)
    }

    private fun recordFormInterventionCompleted(
        action: String,
        nowMillis: Long,
        contentId: String? = null,
    ) {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        if (uiState.interventionMode != InterventionMode.FIRM || uiState.currentInterventionBedtimeEnforced) return
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.FORM_INTERVENTION_COMPLETED,
                timestampMillis = nowMillis,
                interventionId = interventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                contentId = contentId,
                metadata = uiState.currentInterventionMetadata + mapOf(
                    "action" to action,
                    "interventionMode" to uiState.interventionMode.name,
                    "openAnywayUnlockAvailableAtMillis" to uiState.currentOpenAnywayUnlockAvailableAtMillis.orEmptyString(),
                ),
            ),
        )
    }

    private suspend fun recordEventDurably(event: AnalyticsEvent) {
        analyticsTracker.recordDurably(event)
    }

    private fun refreshActiveDelayWindow(nowMillis: Long = System.currentTimeMillis()) {
        val selectedTargetApp = uiState.selectedTargetApp ?: return
        if (!delayReady) {
            return
        }
        val activeDelayWindow = delayGate.activeDelay(targetApp = selectedTargetApp, nowMillis = nowMillis)
        val activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow)
        if (uiState.activeDelayWindow != activeDelayWindow || uiState.activeDelaySuggestion != activeDelaySuggestion) {
            uiState = uiState.copy(
                activeDelayWindow = activeDelayWindow,
                activeDelaySuggestion = activeDelaySuggestion,
            )
        }
    }

    private fun activeDelaySuggestionFor(
        delayWindow: DelayWindow?,
        nowMillis: Long = nowProvider(),
        excludedContentIds: Set<String> = excludedContentIdsForRecommendation(),
    ): ContentItem? {
        delayWindow ?: return null
        if (!delayWindow.isActive(nowMillis)) {
            return null
        }
        val candidateIds = (delayWindow.backupContentIds + listOfNotNull(delayWindow.primaryContentId)).toSet()
        if (candidateIds.isEmpty()) {
            return null
        }
        return fullReplacementInventory()
            .asSequence()
            .filter { item ->
                item.id in candidateIds &&
                    item.id !in excludedContentIds &&
                    item.availability != ContentAvailability.UNAVAILABLE
            }
            .minWithOrNull(compareBy<ContentItem> { it.durationMinutes }.thenBy { it.title })
    }

    private fun recommendationSetForDelayWindow(
        delayWindow: DelayWindow,
        fallbackContent: ContentItem,
    ): RecommendationSet {
        val excludedContentIds = excludedContentIdsForRecommendation()
        val candidatesById = fullReplacementInventory()
            .filter { item ->
                item.id !in excludedContentIds &&
                    item.availability != ContentAvailability.UNAVAILABLE
            }
            .associateBy(ContentItem::id)
        val primary = delayWindow.primaryContentId
            ?.let(candidatesById::get)
            ?: fallbackContent
        val backups = delayWindow.backupContentIds
            .mapNotNull(candidatesById::get)
            .filterNot { it.id == primary.id }
        return RecommendationSet(
            primary = primary,
            backups = backups,
            inventoryShortage = backups.size < 2,
            generatedAtMillis = delayWindow.startsAtMillis,
        )
    }

    private fun updateHydrationState() {
        val isReady = settingsLoaded && contentReady && analyticsReady && historyReady && readingProgressReady &&
            readingAnnotationReady && delayReady && userDocumentReady
        if (uiState.isLoadingSettings != !isReady) {
            uiState = uiState.copy(isLoadingSettings = !isReady)
        }
        if (isReady) {
            val pending = pendingSystemInterception ?: return
            pendingSystemInterception = null
            when (pending) {
                is PendingSystemInterception.App -> requestSystemInterception(
                    targetAppPackage = pending.targetAppPackage,
                    nowMillis = pending.triggeredAtMillis,
                )

                is PendingSystemInterception.Website -> requestSystemWebsiteInterception(
                    browserPackage = pending.browserPackage,
                    browserDisplayName = pending.browserDisplayName,
                    websiteRuleType = pending.websiteRuleType,
                    websiteRuleIncludesApex = pending.websiteRuleIncludesApex,
                    nowMillis = pending.triggeredAtMillis,
                )
            }
        }
    }

    private fun updateCompletedContentIds() {
        val completedIds = historyCompletedContentIds + readingProgressCompletedContentIds
        if (uiState.completedContentIds != completedIds) {
            val activeDelaySuggestion = activeDelaySuggestionFor(
                delayWindow = uiState.activeDelayWindow,
                excludedContentIds = completedIds - uiState.reactivatedCompletedContentIds,
            )
            uiState = uiState.copy(
                completedContentIds = completedIds,
                activeDelaySuggestion = activeDelaySuggestion,
            )
        }
    }

    private fun unfinishedProgressFor(contentId: String): ReadingProgress? {
        return uiState.readingProgress.firstOrNull { progress ->
            progress.contentId == contentId && progress.isUnfinished()
        }
    }

    private fun hasCompletedProgressForActiveRead(contentId: String): Boolean {
        val currentCompleted = uiState.currentReadingProgress
            ?.takeIf { progress -> progress.contentId == contentId }
            ?.isCompleted() == true
        if (currentCompleted) {
            return true
        }
        if (contentId in uiState.reactivatedCompletedContentIds) {
            return false
        }
        return uiState.readingProgress.any { progress ->
            progress.contentId == contentId && progress.isCompleted()
        }
    }

    private fun completedProgressFor(
        content: ContentItem,
        completedAtMillis: Long,
    ): ReadingProgress {
        val current = uiState.currentReadingProgress
            ?.takeIf { progress -> progress.contentId == content.id }
        val paragraphCount = current?.paragraphCount ?: 1
        return ReadingProgress(
            contentId = content.id,
            progressPercent = 100,
            lastVisibleParagraphIndex = (current?.lastVisibleParagraphIndex ?: (paragraphCount - 1))
                .coerceIn(0, paragraphCount - 1),
            lastVisibleTextOffset = current?.lastVisibleTextOffset?.coerceAtLeast(0) ?: 0,
            paragraphCount = paragraphCount,
            updatedAtMillis = completedAtMillis,
            completedAtMillis = completedAtMillis,
        )
    }

    private fun nextReadingProgressUpdatedAtMillis(
        contentId: String,
        nowMillis: Long,
    ): Long {
        val currentUpdatedAtMillis = uiState.currentReadingProgress
            ?.takeIf { progress -> progress.contentId == contentId }
            ?.updatedAtMillis
            ?: 0L
        val storedUpdatedAtMillis = uiState.readingProgress
            .firstOrNull { progress -> progress.contentId == contentId }
            ?.updatedAtMillis
            ?: 0L
        val floor = maxOf(
            lastReadingProgressUpdatedAtMillis,
            currentUpdatedAtMillis,
            storedUpdatedAtMillis,
        )
        val nextUpdatedAtMillis = maxOf(nowMillis, floor + 1)
        lastReadingProgressUpdatedAtMillis = nextUpdatedAtMillis
        return nextUpdatedAtMillis
    }

    internal fun closeForTests() {
        viewModelScope.cancel()
    }

    internal fun seedAgentInboxReviewForTests(
        candidates: List<AgentInboxReviewCandidate>,
        priorityAcceptedPackageIds: Set<String> = emptySet(),
        lastSuccessfulAtMillis: Long = 1_781_256_600_000L,
    ) {
        check(BuildConfig.DEBUG) { "Agent Inbox visual fixture state is only available in debug builds." }
        uiState = uiState.copy(
            screen = MainScreen.Settings,
            agentInboxDriveEnabled = true,
            agentInboxDriveFolderId = "visual-test-agent-inbox",
            agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
            agentInboxDriveLastSuccessfulAtMillis = lastSuccessfulAtMillis,
            agentInboxDriveLastError = null,
            isAgentInboxScanning = false,
            isAgentInboxImporting = false,
            agentInboxCandidates = candidates,
            agentInboxPriorityAcceptedPackageIds = priorityAcceptedPackageIds,
            agentInboxScanTruncated = false,
        )
    }

    internal fun seedAgentInboxDriveAccessLostForTests(
        message: String = "Agent Inbox folder access was lost. Choose the folder again.",
    ) {
        check(BuildConfig.DEBUG) { "Agent Inbox access-lost visual fixture state is only available in debug builds." }
        uiState = uiState.copy(
            screen = MainScreen.Settings,
            agentInboxDriveEnabled = false,
            agentInboxDriveFolderId = null,
            agentInboxDriveGrantMode = null,
            agentInboxDriveLastSuccessfulAtMillis = null,
            agentInboxDriveLastError = message,
            agentInboxDriveFolderDraft = "",
            agentInboxDriveFolderDraftError = null,
            isAgentInboxScanning = false,
            isAgentInboxImporting = false,
            agentInboxCandidates = emptyList(),
            agentInboxPriorityAcceptedPackageIds = emptySet(),
            agentInboxScanTruncated = false,
            latestMessage = "Agent Inbox folder access was lost.",
        )
    }

    internal fun setAgentInboxDriveClientForTests(client: AgentInboxDriveClient) {
        check(BuildConfig.DEBUG) { "Agent Inbox Drive client override is only available in debug builds." }
        agentInboxDriveClient = client
    }

    private fun findTargetApp(targetAppPackage: String): DistractingApp? {
        return uiState.availableTargetApps.firstOrNull { it.packageName == targetAppPackage }
    }

    private fun updateAddLinkForm(form: AddLinkFormState) {
        uiState = uiState.copy(
            addLinkForm = form.copy(
                validationErrors = form.visibleValidationErrors(),
                canSave = form.localValidationErrors().isEmpty(),
                isSaving = false,
            ),
            savedLinkConfirmation = null,
        )
    }

    private fun updateAddDocumentForm(
        form: AddDocumentFormState,
        screen: MainScreen = uiState.screen,
    ) {
        val validationErrors = form.visibleValidationErrors()
        uiState = uiState.copy(
            screen = screen,
            addDocumentForm = form.copy(
                validationErrors = validationErrors,
                canSave = !form.isPreparing && form.localValidationErrors().isEmpty(),
                isPreparing = form.isPreparing,
                isSaving = false,
            ),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
    }

    private fun invalidatePendingReaderOpen() {
        readerOpenRequestId += 1
    }

    private fun invalidatePendingDocumentImportPreparation() {
        documentImportPreparationRequestId += 1
        pendingDocumentImportBase = null
    }
}

private data class PendingDocumentImportBase(
    val candidates: List<DocumentImportCandidate>,
    val selectedTopics: Set<TopicTag>,
    val markPriority: Boolean,
)

class MainViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
            contentRepository = appContainer.contentRepository,
            userLinkRepository = appContainer.userLinkRepository,
            userDocumentRepository = appContainer.userDocumentRepository,
            settingsRepository = appContainer.settingsRepository,
            recommendationEngine = appContainer.recommendationEngine,
            delayGate = appContainer.delayGate,
            analyticsTracker = appContainer.analyticsTracker,
            historyRepository = appContainer.historyRepository,
            readingProgressRepository = appContainer.readingProgressRepository,
            readingAnnotationRepository = appContainer.readingAnnotationRepository,
            readingAnnotationExportWriter = appContainer.readingAnnotationExportWriter,
            readingAnnotationDriveSyncClient = appContainer.readingAnnotationDriveSyncClient,
            readingAnnotationDriveTokenProvider = appContainer.readingAnnotationDriveTokenProvider,
            agentInboxDriveClient = appContainer.agentInboxDriveClient,
            agentInboxPackageImporter = appContainer.agentInboxPackageImporter,
            accountLightProfileExporter = appContainer.accountLightProfileExporter,
            accountLightProfileImporter = appContainer.accountLightProfileImporter,
            accountLightProfileAutosaveWriter = appContainer.accountLightProfileAutosaveWriter,
            accountLightProfileBackupReader = appContainer.accountLightProfileAutosaveWriter,
            progressPersistenceScope = appContainer.appScope,
            defaultAnnotationExportUri = appContainer.defaultAnnotationExportUri,
            defaultProfileAutosaveUri = appContainer.defaultProfileAutosaveUri,
            defaultProfileAutosaveDisplayName = appContainer.defaultProfileAutosaveDisplayName,
            interceptionMonitor = appContainer.interceptionMonitor,
        ) as T
    }
}

private const val LOCAL_ANNOTATION_EXPORT_DISPLAY_NAME = "App storage - Annotation sync"
private const val LOCAL_PROFILE_BACKUP_DISPLAY_NAME = "App storage - Profile backup"

private fun defaultOnboardingSelection(
    supportedApps: List<DistractingApp>,
    starterPacks: List<EditorialPack>,
): OnboardingSelection {
    val defaultSocialPackages = listOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.reddit.frontpage",
    )
    val defaultSelectedPackages = defaultSocialPackages
        .filterTo(mutableSetOf()) { candidate ->
            supportedApps.any { app -> app.packageName == candidate }
        }
        .ifEmpty {
            supportedApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName)
    }
    return OnboardingSelection(
        selectedAppPackages = defaultSelectedPackages,
        preferredTopics = defaultPrototypeTopics(),
        preferredDurationBucket = DurationBucket.FOCUS,
        selectedPackIds = defaultStarterPackIds(starterPacks),
    )
}

private fun defaultStarterPackIds(starterPacks: List<EditorialPack>): Set<String> {
    val selected = starterPacks.take(1).mapTo(mutableSetOf(), EditorialPack::id)
    if (starterPacks.any { pack -> pack.id == ATTENTION_CLASSICS_PACK_ID }) {
        selected += ATTENTION_CLASSICS_PACK_ID
    }
    if (starterPacks.any { pack -> pack.id == PUBLIC_DOMAIN_EXPANSION_PACK_ID }) {
        selected += PUBLIC_DOMAIN_EXPANSION_PACK_ID
    }
    if (starterPacks.any { pack -> pack.id == LINK_ONLY_MODERN_PACK_ID }) {
        selected += LINK_ONLY_MODERN_PACK_ID
    }
    SPRINT9_PACK_IDS
        .filterTo(selected) { packId -> starterPacks.any { pack -> pack.id == packId } }
    return selected
}

private fun AppSettings.toUserPreferences(
    supportedApps: List<DistractingApp>,
    fallbackPackIds: Set<String>,
): UserPreferences {
    val supportedByPackage = supportedApps.associateBy(DistractingApp::packageName)
    val selectedApps = selectedAppPackages.mapNotNull { packageName ->
        supportedByPackage[packageName] ?: SupportedCatalog.findByPackage(packageName)
    }
        .ifEmpty {
            if (hasCompletedOnboarding) {
                emptyList()
            } else {
                supportedApps.take(3)
            }
        }
    val selectedTopics = preferredTopics.ifEmpty { defaultPrototypeTopics() }
    val packs = selectedPackIds.ifEmpty { fallbackPackIds }
    return UserPreferences(
        selectedApps = selectedApps,
        preferredTopics = selectedTopics,
        preferredDurationBucket = preferredDurationBucket,
        selectedPackIds = packs,
        meditationDurationMinutes = meditationDurationMinutes,
        contentPriority = contentPriority,
        priorityContentIds = priorityContentIds,
    )
}

private fun RecommendationSet.withMeditationDuration(meditation: ContentItem): RecommendationSet {
    return copy(
        primary = primary.replaceIfMeditation(meditation),
        backups = backups.map { item -> item.replaceIfMeditation(meditation) },
    )
}

private fun UserPreferences.withReadingProgress(progress: List<ReadingProgress>): UserPreferences {
    return copy(unfinishedContentIds = progress.unfinishedContentIds())
}

private fun List<ReadingProgress>.unfinishedContentIds(): Set<String> {
    return filter(ReadingProgress::isUnfinished)
        .mapTo(mutableSetOf(), ReadingProgress::contentId)
}

private fun List<ReadingProgress>.upsertReadingProgress(progress: ReadingProgress): List<ReadingProgress> {
    return filterNot { current -> current.contentId == progress.contentId }
        .plus(progress)
        .sortedByDescending(ReadingProgress::updatedAtMillis)
}

private fun List<ReadingProgress>.withActiveProgressOverride(
    activeProgress: ReadingProgress?,
): List<ReadingProgress> {
    return if (activeProgress == null) {
        this
    } else {
        upsertReadingProgress(activeProgress)
    }
}

private fun ReadingProgress.sameVisiblePosition(other: ReadingProgress): Boolean {
    return contentId == other.contentId &&
        progressPercent == other.progressPercent &&
        lastVisibleParagraphIndex == other.lastVisibleParagraphIndex &&
        lastVisibleTextOffset == other.lastVisibleTextOffset &&
        paragraphCount == other.paragraphCount &&
        completedAtMillis == other.completedAtMillis
}

private fun ReadingProgress.analyticsMetadata(): Map<String, String> {
    return mapOf(
        "progressPercent" to progressPercent.toString(),
        "lastVisibleParagraphIndex" to lastVisibleParagraphIndex.toString(),
        "lastVisibleTextOffset" to lastVisibleTextOffset.toString(),
        "paragraphCount" to paragraphCount.toString(),
        "completed" to isCompleted().toString(),
        "updatedAtMillis" to updatedAtMillis.toString(),
        "completedAtMillis" to (completedAtMillis?.toString() ?: ""),
    )
}

private fun AgentInboxReviewCandidate.agentInboxAnalyticsMetadata(): Map<String, String> {
    return buildMap {
        put("importStatus", status.name)
        put("priorityRequested", requestsHighPriority.toString())
        put("validationErrorCount", (manifestErrors.size + packageErrors.size).toString())
        manifest?.format?.let { format -> put("format", format.name) }
    }
}

private fun ContentItem.needsLegacyReadingTimeEstimateRepair(progress: ReadingProgress?): Boolean {
    return progress?.isUnfinished() == true && isLegacyReadingTimeEstimateCandidate()
}

private fun ContentItem.isLegacyReadingTimeEstimateCandidate(): Boolean {
    return sourceType == ContentSourceType.USER_DOCUMENT &&
        usesRepositoryBody() &&
        durationMinutes <= ReadingTimeEstimator.MAX_SESSION_MINUTES
}

private fun ContentItem.replaceIfMeditation(meditation: ContentItem): ContentItem {
    return if (usesMeditationTimer()) meditation else this
}

private fun ContentItem.toAddLinkConfirmation(priorityMarked: Boolean = false): AddLinkConfirmation {
    return AddLinkConfirmation(
        title = title,
        host = when (sourceType) {
            ContentSourceType.USER_DOCUMENT -> sourceLabel.orEmpty().ifBlank { "your file" }
            else -> externalUrl?.hostLabel().orEmpty().ifBlank { "your link" }
        },
        durationMinutes = durationMinutes,
        topicLabel = topicTags.firstOrNull()?.displayName().orEmpty().ifBlank { "Reading" },
        priorityMarked = priorityMarked,
    )
}

private fun List<ContentItem>.toAddLinkConfirmation(
    skippedCount: Int,
    priorityMarked: Boolean,
): AddLinkConfirmation {
    if (size == 1) {
        return single().toAddLinkConfirmation(priorityMarked = priorityMarked).copy(skippedCount = skippedCount)
    }
    val firstTopic = flatMap { item -> item.topicTags }.firstOrNull()?.displayName().orEmpty().ifBlank { "Reading" }
    return AddLinkConfirmation(
        title = "$size files saved",
        host = "Batch import",
        durationMinutes = maxOf(1, maxOfOrNull(ContentItem::durationMinutes) ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES),
        topicLabel = firstTopic,
        savedCount = size,
        skippedCount = skippedCount,
        priorityMarked = priorityMarked,
    )
}

private fun defaultDocumentDuration(mimeType: String?, displayName: String): String {
    return when (UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType)) {
        ContentFormat.MARKDOWN -> ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString()
        ContentFormat.PDF -> ReadingTimeEstimator.DEFAULT_PDF_MINUTES.toString()
        ContentFormat.EPUB -> ReadingTimeEstimator.MAX_SESSION_MINUTES.toString()
        else -> ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString()
    }
}

private fun ContentItem.handoffFailureMessage(): String {
    return when (sourceType) {
        ContentSourceType.USER_LINK -> "This saved link could not be opened and was removed from future recommendations."
        ContentSourceType.USER_DOCUMENT -> "This saved file could not be opened and was removed from future recommendations."
        else -> "This external recommendation could not be opened."
    }
}

private fun String.hostLabel(): String {
    return runCatching {
        val normalized = if (startsWith("http://") || startsWith("https://")) this else "https://$this"
        java.net.URI.create(normalized).host.orEmpty().removePrefix("www.")
    }.getOrDefault("")
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

private fun defaultPrototypeTopics(): Set<TopicTag> = setOf(
    TopicTag.ATTENTION,
    TopicTag.PRACTICAL,
    TopicTag.SCIENCE,
)

private const val ATTENTION_CLASSICS_PACK_ID = "attention-classics-v1"
private const val PUBLIC_DOMAIN_EXPANSION_PACK_ID = "public-domain-expansion-v2"
private const val LINK_ONLY_MODERN_PACK_ID = "link-only-modern-v1"
private val SPRINT9_PACK_IDS = setOf(
    "attention_practical_agency_v1",
    "embodied_calm_v1",
    "wonder_science_v1",
    "long_view_history_v1",
    "creativity_play_v1",
)

private fun AppSettings.toOnboardingSelection(
    supportedApps: List<DistractingApp>,
    starterPacks: List<EditorialPack>,
): OnboardingSelection {
    return if (hasCompletedOnboarding) {
        OnboardingSelection(
            selectedAppPackages = selectedAppPackages,
            preferredTopics = preferredTopics,
            preferredDurationBucket = preferredDurationBucket,
            selectedPackIds = selectedPackIds,
        )
    } else {
        defaultOnboardingSelection(
            supportedApps = supportedApps,
            starterPacks = starterPacks,
        )
    }
}

private fun unavailablePermissionReadiness(): PermissionReadiness {
    return PermissionReadiness(
        overlayStatus = PermissionStatus.MISSING,
        accessibilityStatus = PermissionStatus.UNAVAILABLE_IN_BUILD,
        interceptionReady = false,
        summary = "System interception is not active in this build.",
    )
}

private const val ACTIVE_DELAY_REFRESH_INTERVAL_MILLIS = 1_000L
private const val INTERVENTION_DEGRADED_THRESHOLD_MILLIS = 2_000L
internal const val FORM_INTERVENTION_UNLOCK_DELAY_MILLIS = 5_000L
private const val MIN_SELECTED_DISTRACTING_APPS = 3
private const val MAX_READING_ANNOTATION_QUOTE_LENGTH = 1_200
private const val PROGRESS_HISTORY_WINDOW_DAYS = 31
private const val MAX_BACKGROUND_READING_TIME_REPAIR_COUNT = 3
private const val MAX_BACKGROUND_READING_TIME_REPAIR_SCAN_COUNT = 10
private const val FEEDBACK_FIT_NOT = "not"
private const val FEEDBACK_SCROLL_NO = "no"

private sealed class PendingSystemInterception {
    data class App(
        val targetAppPackage: String,
        val triggeredAtMillis: Long,
    ) : PendingSystemInterception()

    data class Website(
        val browserPackage: String,
        val browserDisplayName: String,
        val websiteRuleType: String,
        val websiteRuleIncludesApex: Boolean,
        val triggeredAtMillis: Long,
    ) : PendingSystemInterception()
}

private fun AddLinkFormState.toDraftOrNull(): UserLinkDraft? {
    val duration = durationMinutes.toIntOrNull() ?: return null
    if (localValidationErrors().isNotEmpty()) {
        return null
    }
    return UserLinkDraft(
        url = url,
        title = title,
        durationMinutes = duration,
        topicTags = selectedTopics,
    )
}

private fun AddDocumentFormState.toDraftOrNull(): UserDocumentDraft? {
    return documentCandidates()
        .singleOrNull()
        ?.toDraftOrNull(selectedTopics)
        ?.takeIf { localValidationErrors().isEmpty() }
}

private fun AddLinkFormState.localValidationErrors(): Set<UserLinkValidationError> {
    val errors = mutableSetOf<UserLinkValidationError>()
    val trimmedUrl = url.trim()
    val duration = durationMinutes.toIntOrNull()
    errors += UserLinkValidator.validateUrl(trimmedUrl).errors
    if (title.isBlank()) {
        errors += UserLinkValidationError.BLANK_TITLE
    }
    if (duration == null || duration !in 1..60) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (selectedTopics.isEmpty()) {
        errors += UserLinkValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.localValidationErrors(): Set<UserDocumentValidationError> {
    val candidates = documentCandidates()
    if (candidates.isEmpty()) {
        return setOf(UserDocumentValidationError.EMPTY_URI)
    }
    val supported = candidates.filter { candidate -> candidate.format != null }
    if (supported.isEmpty()) {
        return setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT)
    }
    val errors = mutableSetOf<UserDocumentValidationError>()
    supported.forEach { candidate ->
        errors += candidate.validationErrors(selectedTopics)
    }
    return errors
}

private fun AddLinkFormState.visibleValidationErrors(): Set<UserLinkValidationError> {
    val errors = mutableSetOf<UserLinkValidationError>()
    val trimmedUrl = url.trim()
    val duration = durationMinutes.trim()
    val validUrl = UserLinkValidator.validateUrl(trimmedUrl).isValid
    val validDuration = duration.toIntOrNull()?.let { it in 1..60 } == true
    if (trimmedUrl.isBlank() && (title.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.EMPTY_URL
    } else if (trimmedUrl.isNotBlank()) {
        errors += UserLinkValidator.validateUrl(trimmedUrl).errors
    }
    if (duration.isNotBlank() && !validDuration) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (duration.isBlank() && (trimmedUrl.isNotBlank() || title.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (title.isBlank() && (validUrl || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.BLANK_TITLE
    }
    if (selectedTopics.isEmpty() && validUrl && title.isNotBlank() && validDuration) {
        errors += UserLinkValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.visibleValidationErrors(): Set<UserDocumentValidationError> {
    val candidates = documentCandidates()
    if (candidates.isEmpty()) {
        return setOf(UserDocumentValidationError.EMPTY_URI)
    }
    val errors = mutableSetOf<UserDocumentValidationError>()
    if (candidates.all { candidate -> candidate.format == null }) {
        errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
    }
    val editableCandidate = candidates.singleOrNull()
    if (editableCandidate != null && editableCandidate.uri.isBlank()) {
        errors += UserDocumentValidationError.EMPTY_URI
    }
    if (editableCandidate != null) {
        if (editableCandidate.format == null) {
            errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
        }
        if (editableCandidate.title.isBlank() && (editableCandidate.uri.isNotBlank() || selectedTopics.isNotEmpty())) {
            errors += UserDocumentValidationError.BLANK_TITLE
        }
        if (selectedTopics.isEmpty() && editableCandidate.uri.isNotBlank() && editableCandidate.title.isNotBlank()) {
            errors += UserDocumentValidationError.NO_TOPICS
        }
    } else if (selectedTopics.isEmpty() && candidates.any { candidate -> candidate.format != null }) {
        errors += UserDocumentValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.documentCandidates(): List<DocumentImportCandidate> {
    if (candidates.isNotEmpty()) {
        return if (candidates.size == 1) {
            listOf(
                candidates.first().copy(
                    uri = uri.ifBlank { candidates.first().uri },
                    displayName = displayName.ifBlank { candidates.first().displayName },
                    mimeType = mimeType ?: candidates.first().mimeType,
                    title = title,
                    durationMinutes = durationMinutes,
                ).cleaned(),
            )
        } else {
            candidates.map(DocumentImportCandidate::cleaned)
        }
    }
    if (uri.isBlank()) {
        return emptyList()
    }
    return listOf(
        DocumentImportCandidate(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            title = title,
            durationMinutes = durationMinutes,
            format = UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType),
        ).cleaned(),
    )
}

private fun AddDocumentFormState.pendingMarkdownAttachmentBase(): PendingDocumentImportBase? {
    val currentCandidates = documentCandidates()
    if (currentCandidates.none { candidate -> candidate.format == ContentFormat.MARKDOWN }) {
        return null
    }
    return PendingDocumentImportBase(
        candidates = currentCandidates,
        selectedTopics = selectedTopics,
        markPriority = markPriority,
    )
}

private fun DocumentImportCandidate.cleaned(): DocumentImportCandidate {
    val cleanedName = displayName.trim().ifBlank { "Untitled document" }
    val cleanedTitle = title.trim().ifBlank {
        cleanedName.substringBeforeLast('.', cleanedName).trim().ifBlank { cleanedName }
    }
    val detectedFormat = format ?: UserDocumentValidator.detectFormat(displayName = cleanedName, mimeType = mimeType)
    val cleanedDuration = durationMinutes.trim().ifBlank {
        defaultDocumentDuration(mimeType = mimeType, displayName = cleanedName)
    }
    return copy(
        uri = uri.trim(),
        displayName = cleanedName,
        title = cleanedTitle,
        durationMinutes = cleanedDuration,
        format = detectedFormat,
        imageAttachmentUris = imageAttachmentUris.sanitizedImageAttachmentUris(),
    )
}

private fun DocumentImportCandidate.validationErrors(selectedTopics: Set<TopicTag>): Set<UserDocumentValidationError> {
    val duration = durationMinutes.toIntOrNull() ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES
    return UserDocumentValidator.validate(
        UserDocumentDraft(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            title = title,
            durationMinutes = duration,
            topicTags = selectedTopics,
        ),
    ).errors
}

private fun DocumentImportCandidate.toDraftOrNull(selectedTopics: Set<TopicTag>): UserDocumentDraft? {
    val duration = durationMinutes.toIntOrNull() ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES
    if (validationErrors(selectedTopics).isNotEmpty()) {
        return null
    }
    return UserDocumentDraft(
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        title = title,
        durationMinutes = duration,
        topicTags = selectedTopics,
        imageAttachmentUris = if (format == ContentFormat.MARKDOWN) {
            imageAttachmentUris.sanitizedImageAttachmentUris()
        } else {
            emptyMap()
        },
    )
}

private fun Map<String, String>.sanitizedImageAttachmentUris(): Map<String, String> {
    return entries
        .mapNotNull { (rawKey, rawUri) ->
            val key = rawKey.trim()
            val uri = rawUri.trim()
            if (key.isBlank() || uri.isBlank()) {
                null
            } else {
                key to uri
            }
        }
        .distinctBy { (key, _) -> key }
        .toMap()
}

private fun RecommendationSet.analyticsMetadata(): Map<String, String> {
    return primary.analyticsMetadata(prefix = "primary") +
        backups.flatMapIndexed { index, content ->
            content.analyticsMetadata(prefix = "backup${index + 1}").entries
        }.associate { it.toPair() }
}

private fun Set<String>.trackedCompletedContentIds(): Set<String> {
    return this - MEDITATION_TIMER_CONTENT_ID
}

private fun ReplacementHistoryEntry.isCompletedReadingReplacement(): Boolean {
    return isCompleted() && contentId != MEDITATION_TIMER_CONTENT_ID
}

private fun inventoryDiagnostics(
    rawInventory: List<ContentItem>,
    eligibleInventory: List<ContentItem>,
    preferences: UserPreferences,
    completedContentIds: Set<String>,
    userLinks: List<ContentItem>,
    userDocuments: List<ContentItem>,
): Map<String, String> {
    val unavailableUserLinkIds = (rawInventory + userLinks)
        .asSequence()
        .filter { item ->
            item.sourceType == ContentSourceType.USER_LINK &&
                item.availability == ContentAvailability.UNAVAILABLE
        }
        .map(ContentItem::id)
        .toSet()
    val unavailableUserDocumentIds = (rawInventory + userDocuments)
        .asSequence()
        .filter { item ->
            item.sourceType == ContentSourceType.USER_DOCUMENT &&
                item.availability == ContentAvailability.UNAVAILABLE
        }
        .map(ContentItem::id)
        .toSet()
    return mapOf(
        "selectedPackCount" to preferences.selectedPackIds.size.toString(),
        "selectedPackIds" to preferences.selectedPackIds.sorted().joinToString(","),
        "eligibleInventoryCount" to eligibleInventory.size.toString(),
        "eligibleEditorialCount" to eligibleInventory.count { it.sourceType == ContentSourceType.EDITORIAL }.toString(),
        "eligibleMeditationCount" to eligibleInventory.count { it.sourceType == ContentSourceType.MEDITATION }.toString(),
        "eligibleUserLinkCount" to eligibleInventory.count { it.sourceType == ContentSourceType.USER_LINK }.toString(),
        "eligibleUserDocumentCount" to eligibleInventory.count { it.sourceType == ContentSourceType.USER_DOCUMENT }.toString(),
        "unavailableUserLinkCount" to unavailableUserLinkIds.size.toString(),
        "unavailableUserDocumentCount" to unavailableUserDocumentIds.size.toString(),
        "completedContentCount" to completedContentIds.size.toString(),
        "priorityContentCount" to preferences.priorityContentIds.size.toString(),
        "unfinishedContentCount" to preferences.unfinishedContentIds.size.toString(),
    )
}

private fun ContentItem.analyticsMetadata(prefix: String? = null): Map<String, String> {
    fun key(name: String): String = prefix?.let { "${it}_$name" } ?: name
    return buildMap {
        put(key("sourceType"), sourceType.name)
        put(key("availability"), availability.name)
        put(key("format"), format.name)
        put(key("packId"), packId)
        put(key("rightsClass"), rights.rightsClass.name)
        put(key("renderMode"), rights.renderMode.name)
    }
}

private fun ContentItem.isUserManagedContent(): Boolean {
    return sourceType == ContentSourceType.USER_LINK || sourceType == ContentSourceType.USER_DOCUMENT
}

private fun deletedLibraryMessage(count: Int): String {
    return if (count == 1) "Deleted 1 saved item." else "Deleted $count saved items."
}

private fun Int?.orZeroString(): String = (this ?: 0).toString()

private fun Long?.orEmptyString(): String = this?.toString() ?: ""

private object EmptyUserLinkRepository : UserLinkRepository {
    override fun userLinks(): List<ContentItem> = emptyList()

    override fun observeUserLinks() = flowOf(emptyList<ContentItem>())

    override suspend fun addLink(
        draft: UserLinkDraft,
        nowMillis: Long,
    ): AddUserLinkResult = AddUserLinkResult.Rejected(setOf(UserLinkValidationError.UNSUPPORTED_SCHEME))

    override suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long,
    ) = Unit

    override suspend fun deleteLink(contentId: String) = Unit
}

private object EmptyUserDocumentRepository : UserDocumentRepository {
    override fun userDocuments(): List<ContentItem> = emptyList()

    override fun observeUserDocuments() = flowOf(emptyList<ContentItem>())

    override suspend fun addDocument(
        draft: UserDocumentDraft,
        nowMillis: Long,
    ): AddUserDocumentResult = AddUserDocumentResult.Rejected(setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT))

    override suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long,
    ) = Unit

    override suspend fun deleteDocument(contentId: String) = Unit
}

private object EmptyReadingProgressRepository : ReadingProgressRepository {
    override fun readingProgress(): List<ReadingProgress> = emptyList()

    override fun observeReadingProgress() = flowOf(emptyList<ReadingProgress>())

    override suspend fun saveProgress(progress: ReadingProgress) = Unit

    override suspend fun deleteProgress(contentId: String) = Unit
}

private object EmptyReadingAnnotationRepository : ReadingAnnotationRepository {
    override fun readingAnnotations(): List<ReadingAnnotation> = emptyList()

    override fun observeReadingAnnotations() = flowOf(emptyList<ReadingAnnotation>())

    override suspend fun saveAnnotation(
        draft: com.qualityalternative.app.domain.model.ReadingAnnotationDraft,
        nowMillis: Long,
    ): ReadingAnnotation = error("Reading annotations are not available in this build.")

    override suspend fun deleteAnnotation(
        annotationId: String,
        nowMillis: Long,
    ) = Unit
}

private object NoOpReadingAnnotationExportWriter : ReadingAnnotationExportWriter {
    override suspend fun writeMarkdown(uri: String, markdown: String) = Unit
}

private object NoOpReadingAnnotationDriveSyncClient : ReadingAnnotationDriveSyncClient {
    override suspend fun syncJsonLdFiles(
        request: ReadingAnnotationDriveSyncRequest,
    ) = error("Google Drive sync is not available in this build.")
}

private object NoOpReadingAnnotationDriveTokenProvider : ReadingAnnotationDriveTokenProvider {
    override suspend fun driveAccessToken(): String = error("Google Drive authorization is not available in this build.")
}

private object NoOpAgentInboxDriveClient : AgentInboxDriveClient {
    override suspend fun listFolders(
        request: AgentInboxDriveFolderListRequest,
    ) = error("Agent Inbox Drive is not available in this build.")

    override suspend fun scanPackages(
        request: AgentInboxDriveScanRequest,
    ) = error("Agent Inbox Drive is not available in this build.")

    override suspend fun downloadFile(
        accessToken: String,
        fileId: String,
        maxBytes: Long,
        expectedBytes: Long?,
    ): ByteArray =
        error("Agent Inbox Drive is not available in this build.")
}

private object NoOpAgentInboxDocumentStore : AgentInboxDocumentStore {
    override suspend fun writeDocument(
        packageFolderId: String,
        contentFileName: String,
        verifiedContentSha256: String,
        format: ContentFormat,
        bytes: ByteArray,
        imageAttachments: List<com.qualityalternative.app.data.AgentInboxImageAttachmentWrite>,
    ) = error("Agent Inbox document store is not available in this build.")

    override suspend fun deleteDocument(stored: StoredAgentInboxDocument) = Unit
}

private object NoOpAccountLightProfileAutosaveWriter : AccountLightProfileAutosaveWriter {
    override suspend fun writeProfileJson(uri: String, fileName: String, json: String) = Unit
}

private object NoOpAccountLightProfileBackupReader : AccountLightProfileBackupReader {
    override suspend fun readProfileJson(uri: String, fileName: String): String? = null
}

private data class AnnotationAutosaveResult(
    val fileResult: Boolean?,
    val driveResult: Boolean?,
)

private fun String.withAnnotationAutosaveResult(result: AnnotationAutosaveResult): String {
    val successes = listOfNotNull(
        "file".takeIf { result.fileResult == true },
        "Drive".takeIf { result.driveResult == true },
    )
    val failures = listOfNotNull(
        "file".takeIf { result.fileResult == false },
        "Drive".takeIf { result.driveResult == false },
    )
    return when {
        successes.isEmpty() && failures.isEmpty() -> this
        failures.isEmpty() -> removeSuffix(".") + " and autosaved."
        successes.isEmpty() -> this + " Autosave failed."
        else -> this + " Autosaved to ${successes.joinToString(" and ")}; ${failures.joinToString(" and ")} failed."
    }
}

private fun String.withProfileAutosaveResult(result: Boolean?): String {
    return when (result) {
        true -> removeSuffix(".") + " and autosaved."
        false -> this + " Profile autosave failed."
        null -> this
    }
}

private fun Throwable.annotationExportErrorMessage(): String {
    return "Choose the file again or retry."
}

private fun Throwable.annotationDriveSyncErrorMessage(): String {
    return "Google Drive sync failed. Retry from Settings."
}

private fun Throwable.profileAutosaveErrorMessage(): String {
    return "Choose the folder again or retry."
}
