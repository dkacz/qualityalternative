package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DelayInspection
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.LocalProfileIdentity
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.model.ReaderDocument
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ContentRepository {
    fun starterPacks(): List<EditorialPack>
    fun inventory(): List<ContentItem>
    fun contentBody(item: ContentItem): String
    fun readerDocument(item: ContentItem): ReaderDocument = ReaderDocument.fromPlainText(contentBody(item))
    fun isReady(): Boolean = true
    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

sealed class AddUserLinkResult {
    data class Added(val item: ContentItem) : AddUserLinkResult()

    data class Rejected(val errors: Set<UserLinkValidationError>) : AddUserLinkResult()
}

sealed class AddUserDocumentResult {
    data class Added(val item: ContentItem) : AddUserDocumentResult()

    data class Rejected(val errors: Set<UserDocumentValidationError>) : AddUserDocumentResult()
}

interface UserLinkRepository {
    fun userLinks(): List<ContentItem>

    fun observeUserLinks(): Flow<List<ContentItem>> = flowOf(userLinks())

    suspend fun addLink(
        draft: UserLinkDraft,
        nowMillis: Long = System.currentTimeMillis(),
    ): AddUserLinkResult

    suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long = System.currentTimeMillis(),
    )

    suspend fun deleteLink(contentId: String)

    suspend fun importPortableLinks(
        links: List<ContentItem>,
        replaceExisting: Boolean,
        nowMillis: Long = System.currentTimeMillis(),
    ) = Unit

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface UserDocumentRepository {
    fun userDocuments(): List<ContentItem>

    fun observeUserDocuments(): Flow<List<ContentItem>> = flowOf(userDocuments())

    suspend fun addDocument(
        draft: UserDocumentDraft,
        nowMillis: Long = System.currentTimeMillis(),
    ): AddUserDocumentResult

    suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long = System.currentTimeMillis(),
    )

    suspend fun deleteDocument(contentId: String)

    suspend fun importPortableDocuments(
        documents: List<ContentItem>,
        replaceExisting: Boolean,
        nowMillis: Long = System.currentTimeMillis(),
    ) = Unit

    fun contentBody(item: ContentItem): String = item.description

    fun readerDocument(item: ContentItem): ReaderDocument = ReaderDocument.fromPlainText(contentBody(item))

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface SettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>
    fun supportedDistractingApps(): List<DistractingApp>
    suspend fun ensureLocalProfileIdentity(nowMillis: Long = System.currentTimeMillis()): LocalProfileIdentity
    suspend fun replacePortableSettings(settings: AppSettings, profileIdentity: LocalProfileIdentity? = null)
    suspend fun saveOnboardingSelection(selection: OnboardingSelection)
    suspend fun saveSelectedAppPackages(packages: Set<String>)
    suspend fun savePreferredDurationBucket(bucket: DurationBucket)
    suspend fun saveThemeMode(themeMode: AppThemeMode)
    suspend fun saveMeditationDurationMinutes(minutes: Int)
    suspend fun saveReaderFontScale(scale: Double)
    suspend fun saveContentPriority(priority: ContentPriority)
    suspend fun savePriorityContentIds(contentIds: Set<String>)
    suspend fun saveReactivatedCompletedContentIds(contentIds: Set<String>)
    suspend fun saveOpenAnywayUnlockMinutes(minutes: Int)
    suspend fun saveAnnotationExportDestination(uri: String, displayName: String)
    suspend fun clearAnnotationExportDestination()
    suspend fun saveAnnotationExportSuccess(timestampMillis: Long)
    suspend fun saveAnnotationExportFailure(errorMessage: String)
    suspend fun saveAnnotationDriveSyncConnection(folderId: String?)
    suspend fun clearAnnotationDriveSyncConnection()
    suspend fun saveAnnotationDriveSyncSuccess(timestampMillis: Long, folderId: String)
    suspend fun saveAnnotationDriveSyncFailure(errorMessage: String)
}

interface RecommendationEngine {
    fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        excludedContentIds: Set<String>,
        signals: RecommendationSignals,
        nowMillis: Long = System.currentTimeMillis(),
    ): RecommendationSet?
}

interface InterceptionMonitor {
    fun isAvailable(): Boolean
    fun currentReadiness(): PermissionReadiness
}

interface DelayGate {
    /**
     * Returns the current delay state for an intervention attempt without consuming expired delay
     * provenance. The trigger path must explicitly consume an expired window after it finishes
     * any required analytics or follow-up work.
     */
    fun inspectDelay(targetApp: DistractingApp, nowMillis: Long = System.currentTimeMillis()): DelayInspection

    /**
     * Returns the currently active delay window without consuming expired provenance.
     */
    fun activeDelay(targetApp: DistractingApp, nowMillis: Long = System.currentTimeMillis()): DelayWindow?

    suspend fun consumeExpiredDelay(
        targetApp: DistractingApp,
        delayId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean

    fun storeDelay(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 15,
        interventionId: String? = null,
        interventionShownAtMillis: Long? = null,
        primaryContentId: String? = null,
        backupContentIds: List<String> = emptyList(),
    ): DelayWindow

    suspend fun storeDelayDurably(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 15,
        interventionId: String? = null,
        interventionShownAtMillis: Long? = null,
        primaryContentId: String? = null,
        backupContentIds: List<String> = emptyList(),
    ): DelayWindow {
        return storeDelay(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
    }

    fun recordFirstReturnAttempt(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ): DelayWindow?

    suspend fun recordFirstReturnAttemptDurably(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ): DelayWindow? {
        return recordFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis)
    }

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface AnalyticsTracker {
    fun record(event: AnalyticsEvent)
    suspend fun recordDurably(event: AnalyticsEvent) {
        record(event)
    }
    fun allEvents(): List<AnalyticsEvent>
    fun observeEvents(): Flow<List<AnalyticsEvent>> = flowOf(allEvents())
    fun isReady(): Boolean = true
    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface HistoryRepository {
    fun recentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): List<ReplacementHistoryEntry>
    fun observeRecentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): Flow<List<ReplacementHistoryEntry>> =
        flowOf(recentHistory(nowMillis = nowMillis, windowDays = windowDays))

    fun observeCompletedContentIds(): Flow<Set<String>> =
        flowOf(
            recentHistory().filter(ReplacementHistoryEntry::isCompleted)
                .mapTo(mutableSetOf(), ReplacementHistoryEntry::contentId),
        )

    suspend fun recordAcceptedSession(
        targetApp: DistractingApp,
        interventionId: String,
        interventionShownAtMillis: Long,
        primaryContentId: String,
        backupContentIds: List<String>,
        content: ContentItem,
        source: RecommendationSource,
        acceptedAtMillis: Long = System.currentTimeMillis(),
    ): String

    suspend fun updateAcceptedSessionContent(sessionId: String, content: ContentItem)

    suspend fun markCompleted(sessionId: String, completedAtMillis: Long = System.currentTimeMillis())

    suspend fun markSkipped(sessionId: String, skippedAtMillis: Long = System.currentTimeMillis())

    suspend fun attachFeedback(sessionId: String, feedback: SessionFeedback)

    suspend fun markReturnedToTarget(
        targetAppPackage: String,
        returnedAtMillis: Long = System.currentTimeMillis(),
    ): ReturnToTargetSignal?

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface ReadingProgressRepository {
    fun readingProgress(): List<ReadingProgress>

    fun observeReadingProgress(): Flow<List<ReadingProgress>> = flowOf(readingProgress())

    fun observeCompletedContentIds(): Flow<Set<String>> =
        flowOf(
            readingProgress().filter(ReadingProgress::isCompleted)
                .mapTo(mutableSetOf(), ReadingProgress::contentId),
        )

    suspend fun saveProgress(progress: ReadingProgress)

    suspend fun deleteProgress(contentId: String)

    suspend fun deleteProgressForContentIds(contentIds: Set<String>) {
        contentIds.forEach { contentId -> deleteProgress(contentId) }
    }

    suspend fun replaceReadingProgress(progress: List<ReadingProgress>) {
        deleteProgressForContentIds(readingProgress().mapTo(mutableSetOf(), ReadingProgress::contentId))
        progress.forEach { item -> saveProgress(item) }
    }

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface ReadingAnnotationRepository {
    fun readingAnnotations(): List<ReadingAnnotation>

    fun observeReadingAnnotations(): Flow<List<ReadingAnnotation>> = flowOf(readingAnnotations())

    fun observeAnnotationsForContent(contentId: String): Flow<List<ReadingAnnotation>> =
        flowOf(readingAnnotations().filter { annotation -> annotation.contentId == contentId })

    suspend fun saveAnnotation(
        draft: ReadingAnnotationDraft,
        nowMillis: Long = System.currentTimeMillis(),
    ): ReadingAnnotation

    suspend fun deleteAnnotation(
        annotationId: String,
        nowMillis: Long = System.currentTimeMillis(),
    )

    suspend fun deleteAnnotationsForContentIds(
        contentIds: Set<String>,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        contentIds.forEach { contentId ->
            readingAnnotations()
                .filter { annotation -> annotation.contentId == contentId }
                .forEach { annotation -> deleteAnnotation(annotation.id, nowMillis) }
        }
    }

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface ReadingAnnotationExportWriter {
    suspend fun writeMarkdown(uri: String, markdown: String)

    suspend fun writeJsonLdFiles(uri: String, files: List<ReadingAnnotationExportFile>) {
        val singleSourcePayload = files.singleOrNull()?.jsonLd
            ?: error("Annotation export requires a folder destination for per-source JSON-LD files.")
        writeMarkdown(uri = uri, markdown = singleSourcePayload)
    }
}
