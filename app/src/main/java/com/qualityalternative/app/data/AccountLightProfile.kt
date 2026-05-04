package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.LocalProfileIdentity
import com.qualityalternative.app.domain.model.MAX_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MAX_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MIN_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MIN_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.SettingsRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val ACCOUNT_LIGHT_SCHEMA_VERSION = 1
const val ACCOUNT_LIGHT_PROFILE_FORMAT = "quality-alternative-account-light"
const val ACCOUNT_LIGHT_PACKAGE_NAME = "com.qualityalternative.app"
const val ACCOUNT_LIGHT_PROFILE_FILE_NAME = "quality-alternative-profile.json"

@Serializable
data class AccountLightProfileFile(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val app: AccountLightApp,
    val profile: AccountLightProfileIdentityDto,
    val settings: AccountLightSettings,
    val library: AccountLightLibrary = AccountLightLibrary(),
    val reading: AccountLightReading = AccountLightReading(),
    val annotations: AccountLightAnnotations = AccountLightAnnotations(),
    val sync: AccountLightSync = AccountLightSync(),
    val warnings: List<AccountLightWarning> = emptyList(),
) {
    init {
        require(schemaVersion == ACCOUNT_LIGHT_SCHEMA_VERSION) { "Unsupported Account Light schema version." }
        require(exportedAtMillis >= 0L) { "exportedAtMillis must be non-negative." }
    }
}

@Serializable
data class AccountLightApp(
    val profileFormat: String = ACCOUNT_LIGHT_PROFILE_FORMAT,
    val packageName: String = ACCOUNT_LIGHT_PACKAGE_NAME,
    val appVersionName: String,
    val appVersionCode: Int,
) {
    init {
        require(profileFormat == ACCOUNT_LIGHT_PROFILE_FORMAT) { "Invalid Account Light profile format." }
        require(packageName == ACCOUNT_LIGHT_PACKAGE_NAME && packageName.matches(PackageNameRegex)) {
            "Invalid Account Light package name."
        }
        require(appVersionName.isNotBlank()) { "appVersionName must be non-blank." }
        require(appVersionCode >= 1) { "appVersionCode must be positive." }
    }
}

@Serializable
data class AccountLightProfileIdentityDto(
    val profileId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val displayName: String? = null,
) {
    init {
        require(profileId.matches(ProfileIdRegex)) { "Invalid Account Light profileId." }
        require(createdAtMillis >= 0L) { "createdAtMillis must be non-negative." }
        require(updatedAtMillis >= createdAtMillis) { "updatedAtMillis must be >= createdAtMillis." }
        require(displayName == null || displayName.trim().length <= 80) { "displayName is too long." }
    }
}

@Serializable
data class AccountLightSettings(
    val hasCompletedOnboarding: Boolean,
    val selectedAppPackages: List<String>,
    val preferredTopics: List<String>,
    val preferredDurationBucket: String,
    val selectedPackIds: List<String>,
    val themeMode: String,
    val meditationDurationMinutes: Int,
    val readerFontScale: Double,
    val contentPriority: String,
    val priorityContentIds: List<String>,
    val reactivatedCompletedContentIds: List<String>,
    val openAnywayUnlockMinutes: Int,
) {
    init {
        require(selectedAppPackages.all { it.matches(PackageNameRegex) }) {
            "selectedAppPackages contains an invalid package name."
        }
        require(preferredTopics.all { it in TopicTag.entries.map(TopicTag::name) }) {
            "preferredTopics contains an invalid topic."
        }
        require(preferredDurationBucket in DurationBucket.entries.map(DurationBucket::name)) {
            "preferredDurationBucket is invalid."
        }
        require(selectedPackIds.all { it.isNotBlank() }) { "selectedPackIds must be non-blank." }
        require(themeMode in AppThemeMode.entries.map(AppThemeMode::name)) { "themeMode is invalid." }
        require(meditationDurationMinutes in 1..60) { "meditationDurationMinutes is outside the portable range." }
        require(readerFontScale in MIN_READER_FONT_SCALE..MAX_READER_FONT_SCALE) {
            "readerFontScale is outside the portable range."
        }
        require(contentPriority in ContentPriority.entries.map(ContentPriority::name)) { "contentPriority is invalid." }
        require(priorityContentIds.all { it.matches(ContentIdRegex) }) {
            "priorityContentIds contains an invalid content id."
        }
        require(reactivatedCompletedContentIds.all { it.matches(ContentIdRegex) }) {
            "reactivatedCompletedContentIds contains an invalid content id."
        }
        require(openAnywayUnlockMinutes in MIN_OPEN_ANYWAY_UNLOCK_MINUTES..MAX_OPEN_ANYWAY_UNLOCK_MINUTES) {
            "openAnywayUnlockMinutes is outside the portable range."
        }
    }
}

@Serializable
data class AccountLightLibrary(
    val userLinks: List<AccountLightUserLink> = emptyList(),
    val userDocuments: List<AccountLightUserDocument> = emptyList(),
)

@Serializable
data class AccountLightUserLink(
    val contentId: String,
)

@Serializable
data class AccountLightUserDocument(
    val contentId: String,
)

@Serializable
data class AccountLightReading(
    val progress: List<AccountLightReadingProgress> = emptyList(),
)

@Serializable
data class AccountLightReadingProgress(
    val contentId: String,
)

@Serializable
data class AccountLightAnnotations(
    val export: AccountLightAnnotationExport = AccountLightAnnotationExport(),
    val driveSync: AccountLightAnnotationDriveSync = AccountLightAnnotationDriveSync(),
    val sidecarIndex: List<AccountLightAnnotationSidecar> = emptyList(),
)

@Serializable
data class AccountLightAnnotationExport(
    val destinationDisplayName: String? = null,
    val lastSuccessfulAtMillis: Long? = null,
) {
    init {
        require(destinationDisplayName == null || destinationDisplayName.isSafePortableDisplayName()) {
            "destinationDisplayName is not safe for export."
        }
        require(lastSuccessfulAtMillis == null || lastSuccessfulAtMillis >= 0L) {
            "lastSuccessfulAtMillis must be non-negative."
        }
    }
}

@Serializable
data class AccountLightAnnotationDriveSync(
    val wasEnabledOnSourceDevice: Boolean = false,
    val folderDisplayName: String? = null,
    val lastSuccessfulAtMillis: Long? = null,
) {
    init {
        require(folderDisplayName == null || folderDisplayName.isSafePortableDisplayName()) {
            "folderDisplayName is not safe for export."
        }
        require(lastSuccessfulAtMillis == null || lastSuccessfulAtMillis >= 0L) {
            "lastSuccessfulAtMillis must be non-negative."
        }
    }
}

@Serializable
data class AccountLightAnnotationSidecar(
    val contentId: String,
    val sourceTitle: String,
    val jsonLdFileName: String,
    val sha256: String? = null,
    val updatedAtMillis: Long,
)

@Serializable
data class AccountLightSync(
    val profileAutosave: AccountLightProfileAutosave = AccountLightProfileAutosave(),
)

@Serializable
data class AccountLightProfileAutosave(
    val provider: String = "NONE",
    val destinationDisplayName: String? = null,
    val lastSuccessfulAtMillis: Long? = null,
    val activationStateOnImport: String = "REQUIRES_LOCAL_SELECTION",
) {
    init {
        require(provider in AutosaveProviders) { "profileAutosave.provider is invalid." }
        require(destinationDisplayName == null || destinationDisplayName.isSafePortableDisplayName()) {
            "profileAutosave.destinationDisplayName is not safe for export."
        }
        require(lastSuccessfulAtMillis == null || lastSuccessfulAtMillis >= 0L) {
            "profileAutosave.lastSuccessfulAtMillis must be non-negative."
        }
        require(activationStateOnImport == "REQUIRES_LOCAL_SELECTION") {
            "profileAutosave.activationStateOnImport is invalid."
        }
    }
}

@Serializable
data class AccountLightWarning(
    val code: String,
    val severity: String,
    val section: String,
    val contentId: String? = null,
    val message: String? = null,
) {
    init {
        require(code in WarningCodes) { "warning code is invalid." }
        require(severity in WarningSeverities) { "warning severity is invalid." }
        require(section in WarningSections) { "warning section is invalid." }
        require(contentId == null || contentId.matches(ContentIdRegex)) { "warning contentId is invalid." }
        require(message == null || (message.length <= 160 && !message.containsUnsafePortableValue())) {
            "warning message must not include unsafe exported values."
        }
    }
}

class AccountLightProfileCodec(
    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = true
    },
) {
    fun encode(profile: AccountLightProfileFile): String {
        return json.encodeToString(profile) + "\n"
    }

    fun decode(rawJson: String): AccountLightProfileFile {
        val profile = try {
            json.decodeFromString<AccountLightProfileFile>(rawJson)
        } catch (exception: SerializationException) {
            throw IllegalArgumentException("Invalid Account Light profile JSON.", exception)
        }
        require(profile.schemaVersion == ACCOUNT_LIGHT_SCHEMA_VERSION) {
            "Unsupported Account Light schema version ${profile.schemaVersion}."
        }
        return profile
    }
}

class AccountLightProfileExporter(
    private val settingsRepository: SettingsRepository,
    private val appVersionName: String,
    private val appVersionCode: Int,
    private val codec: AccountLightProfileCodec = AccountLightProfileCodec(),
) {
    suspend fun exportSettingsOnlyProfileJson(nowMillis: Long = System.currentTimeMillis()): String {
        val exportedAtMillis = nowMillis.coerceAtLeast(0L)
        val identity = settingsRepository.ensureLocalProfileIdentity(nowMillis = exportedAtMillis)
        val settings = settingsRepository.observeAppSettings().first()
        val profile = AccountLightProfileFile(
            schemaVersion = ACCOUNT_LIGHT_SCHEMA_VERSION,
            exportedAtMillis = exportedAtMillis,
            app = AccountLightApp(
                appVersionName = appVersionName,
                appVersionCode = appVersionCode,
            ),
            profile = identity.toDto(updatedAtMillis = exportedAtMillis),
            settings = settings.toAccountLightSettings(),
            annotations = settings.toAccountLightAnnotations(),
            warnings = settings.toAccountLightWarnings(),
        )
        return codec.encode(profile)
    }
}

private fun LocalProfileIdentity.toDto(updatedAtMillis: Long): AccountLightProfileIdentityDto {
    val safeUpdatedAtMillis = updatedAtMillis.coerceAtLeast(createdAtMillis)
    return AccountLightProfileIdentityDto(
        profileId = profileId,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = safeUpdatedAtMillis,
        displayName = null,
    )
}

private fun AppSettings.toAccountLightSettings(): AccountLightSettings {
    return AccountLightSettings(
        hasCompletedOnboarding = hasCompletedOnboarding,
        selectedAppPackages = selectedAppPackages.sorted(),
        preferredTopics = preferredTopics.map { it.name }.sorted(),
        preferredDurationBucket = preferredDurationBucket.name,
        selectedPackIds = selectedPackIds.sorted(),
        themeMode = themeMode.name,
        meditationDurationMinutes = meditationDurationMinutes,
        readerFontScale = readerFontScale.toPortableReaderFontScale(),
        contentPriority = contentPriority.name,
        priorityContentIds = priorityContentIds.toPortableContentIds(),
        reactivatedCompletedContentIds = reactivatedCompletedContentIds.toPortableContentIds(),
        openAnywayUnlockMinutes = openAnywayUnlockMinutes,
    )
}

private fun AppSettings.toAccountLightAnnotations(): AccountLightAnnotations {
    return AccountLightAnnotations(
        export = AccountLightAnnotationExport(
            destinationDisplayName = annotationExportDisplayName.toPortableDisplayNameOrNull(),
            lastSuccessfulAtMillis = annotationExportLastSuccessfulAtMillis,
        ),
        driveSync = AccountLightAnnotationDriveSync(
            wasEnabledOnSourceDevice = annotationDriveSyncEnabled,
            folderDisplayName = null,
            lastSuccessfulAtMillis = annotationDriveLastSuccessfulAtMillis,
        ),
        sidecarIndex = emptyList(),
    )
}

private fun AppSettings.toAccountLightWarnings(): List<AccountLightWarning> {
    return buildList {
        priorityContentIds.invalidPortableContentIdsWarning(section = "settings")?.let(::add)
        reactivatedCompletedContentIds
            .invalidPortableContentIdsWarning(section = "settings")
            ?.let(::add)
        if (!annotationExportDisplayName.isNullOrBlank() && annotationExportDisplayName.toPortableDisplayNameOrNull() == null) {
            add(
                AccountLightWarning(
                    code = "CONFLICT_RETAINED_LOCAL_VALUE",
                    severity = "WARNING",
                    section = "annotations",
                    message = "A stored annotation export display name was omitted because it is not portable.",
                ),
            )
        }
    }
}

private fun Double.toPortableReaderFontScale(): Double {
    return (coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE) * 100.0).roundToInt() / 100.0
}

private fun Collection<String>.toPortableContentIds(): List<String> {
    return filter { it.matches(ContentIdRegex) }
        .distinct()
        .sorted()
}

private fun Collection<String>.invalidPortableContentIdsWarning(section: String): AccountLightWarning? {
    return if (any { !it.matches(ContentIdRegex) }) {
        AccountLightWarning(
            code = "CONFLICT_RETAINED_LOCAL_VALUE",
            severity = "WARNING",
            section = section,
            message = "One or more stored content references were omitted because they are not portable Account Light content ids.",
        )
    } else {
        null
    }
}

private fun String?.toPortableDisplayNameOrNull(): String? {
    val value = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    return value.takeIf { it.length <= 120 && it.isSafePortableDisplayName() }
}

private fun String.isSafePortableDisplayName(): Boolean {
    return isNotBlank() &&
        length <= 120 &&
        !containsUnsafePortableValue() &&
        all { char ->
            char.isLetterOrDigit() || char.isWhitespace() || char in PortableDisplayNamePunctuation
        }
}

private fun String.containsUnsafePortableValue(): Boolean {
    val value = lowercase()
    return value.contains("content://") ||
        value.contains("file://") ||
        value.contains("oauth") ||
        value.contains("token") ||
        contains("/") ||
        contains("\\") ||
        contains("@") ||
        contains(":")
}

private val ProfileIdRegex = Regex("^qa-local-[0-9a-fA-F-]{36}$")
private val ContentIdRegex = Regex(
    "^(user-link|user-document)-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$" +
        "|^(editorial|meditation)-[a-z0-9][a-z0-9._-]{2,120}$",
)
private val PackageNameRegex = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$")
private val PortableDisplayNamePunctuation = setOf(' ', '.', '_', '-', '(', ')')
private val WarningCodes = setOf(
    "UNKNOWN_FIELD_IGNORED",
    "DUPLICATE_SCALAR_DEDUPED",
    "DOCUMENT_FINGERPRINT_UNVERIFIED",
    "DOCUMENT_FILE_MISSING_ON_IMPORT",
    "IMPORTED_SETTINGS_NOT_APPLIED",
    "AUTOSAVE_REQUIRES_LOCAL_SELECTION",
    "DRIVE_REAUTHORIZATION_REQUIRED",
    "ANNOTATION_SIDECAR_MISSING",
    "UNSUPPORTED_LOCAL_APP_PACKAGE",
    "CONFLICT_RETAINED_LOCAL_VALUE",
)
private val WarningSeverities = setOf("INFO", "WARNING")
private val WarningSections = setOf(
    "app",
    "profile",
    "settings",
    "library.userLinks",
    "library.userDocuments",
    "reading.progress",
    "annotations",
    "sync",
    "unknown",
)
private val AutosaveProviders = setOf("NONE", "ANDROID_DOCUMENT_TREE", "GOOGLE_DRIVE")
