package com.qualityalternative.app.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.ReadingAnnotationRepository
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncClient
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")
private val Context.delayGateDataStore by preferencesDataStore(name = "delay_gate")

class AppContainer(context: Context) {
    private val appJob = SupervisorJob()
    private val appScope = CoroutineScope(appJob + Dispatchers.IO)
    private val database = QualityAlternativeDatabase.build(context)

    val analyticsTracker: AnalyticsTracker = RoomAnalyticsTracker(
        dao = database.analyticsEventDao(),
        scope = appScope,
    )
    val userLinkRepository: UserLinkRepository = RoomUserLinkRepository(
        dao = database.userLinkDao(),
        scope = appScope,
    )
    val userDocumentRepository: UserDocumentRepository = RoomUserDocumentRepository(
        dao = database.userDocumentDao(),
        scope = appScope,
        bodyLoader = AndroidUserDocumentBodyLoader(context = context),
    )
    val contentRepository: ContentRepository = CompositeContentRepository(
        editorialRepository = AssetContentRepository(context = context),
        userLinkRepository = userLinkRepository,
        userDocumentRepository = userDocumentRepository,
    )
    val historyRepository: HistoryRepository = RoomHistoryRepository(
        dao = database.replacementSessionDao(),
        scope = appScope,
    )
    val readingProgressRepository: ReadingProgressRepository = RoomReadingProgressRepository(
        dao = database.readingProgressDao(),
        scope = appScope,
    )
    val readingAnnotationRepository: ReadingAnnotationRepository = RoomReadingAnnotationRepository(
        dao = database.readingAnnotationDao(),
        analyticsTracker = analyticsTracker,
        scope = appScope,
    )
    val readingAnnotationExportWriter: ReadingAnnotationExportWriter = AndroidReadingAnnotationExportWriter(
        context = context,
    )
    val accountLightProfileAutosaveWriter = AndroidAccountLightProfileAutosaveWriter(
        context = context,
    )
    val readingAnnotationDriveSyncClient: ReadingAnnotationDriveSyncClient = AndroidGoogleDriveAnnotationSyncClient()
    val readingAnnotationDriveTokenProvider: ReadingAnnotationDriveTokenProvider = AndroidGoogleDriveTokenProvider(
        context = context,
    )
    val settingsRepository: SettingsRepository = PreferencesSettingsRepository(
        dataStore = context.appSettingsDataStore,
    )
    val accountLightProfileExporter: AccountLightProfileExporter = AccountLightProfileExporter(
        settingsRepository = settingsRepository,
        appVersionName = BuildConfig.VERSION_NAME,
        appVersionCode = BuildConfig.VERSION_CODE,
        userLinkRepository = userLinkRepository,
        userDocumentRepository = userDocumentRepository,
        readingProgressRepository = readingProgressRepository,
    )
    val accountLightProfileImporter: AccountLightProfileImporter = AccountLightProfileImporter(
        settingsRepository = settingsRepository,
        userLinkRepository = userLinkRepository,
        userDocumentRepository = userDocumentRepository,
        readingProgressRepository = readingProgressRepository,
        knownContentIdsProvider = { contentRepository.inventory().mapTo(mutableSetOf(), ContentItem::id) },
    )
    val delayGate: DelayGate = PreferencesDelayGate(
        dataStore = context.delayGateDataStore,
        scope = appScope,
    )
    val interceptionMonitor: InterceptionMonitor = AndroidInterceptionMonitor(context = context)
    val recommendationEngine: RecommendationEngine = DefaultRecommendationEngine()

    suspend fun resetPersistentStateForTests() {
        database.clearAllTables()
        (settingsRepository as PreferencesSettingsRepository).clearForTests()
        (delayGate as PreferencesDelayGate).clearForTests()
        InterceptionRuntimeGate.clearAll()
    }

    fun closeForTests() {
        runBlocking {
            appJob.cancelAndJoin()
        }
        database.close()
    }
}
