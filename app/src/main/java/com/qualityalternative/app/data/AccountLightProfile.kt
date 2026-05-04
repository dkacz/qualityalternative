package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DEFAULT_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.LocalProfileIdentity
import com.qualityalternative.app.domain.model.MAX_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MAX_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MIN_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MIN_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

const val ACCOUNT_LIGHT_SCHEMA_VERSION = 1
const val ACCOUNT_LIGHT_PROFILE_FORMAT = "quality-alternative-account-light"
const val ACCOUNT_LIGHT_PACKAGE_NAME = "com.qualityalternative.app"
const val ACCOUNT_LIGHT_PROFILE_FILE_NAME = "quality-alternative-profile.json"

fun accountLightTimestampedBackupFileName(nowMillis: Long = System.currentTimeMillis()): String {
    val formatter = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return "quality-alternative-profile-${formatter.format(Date(nowMillis.coerceAtLeast(0L)))}.json"
}

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
        require(schemaVersion == ACCOUNT_LIGHT_SCHEMA_VERSION) { "Unsupported portable profile schema version." }
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
        require(profileFormat == ACCOUNT_LIGHT_PROFILE_FORMAT) { "Invalid portable profile format." }
        require(packageName == ACCOUNT_LIGHT_PACKAGE_NAME && packageName.matches(PackageNameRegex)) {
            "Invalid portable profile package name."
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
        require(profileId.matches(ProfileIdRegex)) { "Invalid portable profileId." }
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
) {
    init {
        require(contentId.matches(ContentIdRegex) && contentId.startsWith("user-link-")) {
            "userLinks contentId must use the user-link prefix."
        }
    }
}

@Serializable
data class AccountLightUserDocument(
    val contentId: String,
) {
    init {
        require(contentId.matches(ContentIdRegex) && contentId.startsWith("user-document-")) {
            "userDocuments contentId must use the user-document prefix."
        }
    }
}

@Serializable
data class AccountLightReading(
    val progress: List<AccountLightReadingProgress> = emptyList(),
)

@Serializable
data class AccountLightReadingProgress(
    val contentId: String,
) {
    init {
        require(contentId.matches(ContentIdRegex)) { "reading progress contentId is invalid." }
    }
}

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
) {
    init {
        require(contentId.matches(ContentIdRegex)) { "annotation sidecar contentId is invalid." }
        require(sourceTitle.isSafePortableTitle()) {
            "annotation sidecar sourceTitle is not portable."
        }
        require(jsonLdFileName.isSafePortableAnnotationSidecarFileName()) {
            "annotation sidecar file name is invalid."
        }
        require(sha256 == null || sha256.matches(Sha256Regex)) { "annotation sidecar sha256 is invalid." }
        require(updatedAtMillis >= 0L) { "annotation sidecar updatedAtMillis must be non-negative." }
    }
}

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
        require(
            message == null ||
                (message.length <= 160 && !message.containsUnsafePortableValue() && !message.containsUnsafeWarningMessage()),
        ) {
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
            throw IllegalArgumentException("Invalid portable profile JSON.", exception)
        }
        require(profile.schemaVersion == ACCOUNT_LIGHT_SCHEMA_VERSION) {
            "Unsupported portable profile schema version ${profile.schemaVersion}."
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

enum class AccountLightImportMode {
    MERGE,
    REPLACE,
}

enum class AccountLightImportErrorCode {
    MALFORMED_JSON,
    MISSING_TOP_LEVEL_SECTION,
    UNSUPPORTED_SCHEMA_VERSION,
    INVALID_PROFILE,
}

class AccountLightImportException(
    val code: AccountLightImportErrorCode,
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

data class AccountLightImportPreview(
    val profileId: String,
    val appVersionName: String,
    val exportedAtMillis: Long,
    val importedAppCount: Int,
    val unsupportedAppCount: Int,
    val importedTopicCount: Int,
    val importedPackCount: Int,
    val warningCount: Int,
    val warningSummaries: List<String> = emptyList(),
)

data class AccountLightImportPlan(
    val profile: AccountLightProfileFile,
    val preview: AccountLightImportPreview,
    val generatedWarnings: List<AccountLightWarning> = emptyList(),
) {
    val allWarnings: List<AccountLightWarning>
        get() = profile.warnings + generatedWarnings
}

data class AccountLightImportApplyResult(
    val mode: AccountLightImportMode,
    val settingsApplied: Boolean,
    val warningCount: Int,
)

class AccountLightProfileImporter(
    private val settingsRepository: SettingsRepository,
    private val supportedApps: List<DistractingApp> = settingsRepository.supportedDistractingApps(),
    private val knownContentIdsProvider: () -> Set<String> = { emptySet() },
    private val codec: AccountLightProfileCodec = AccountLightProfileCodec(),
    private val json: Json = Json { ignoreUnknownKeys = false },
) {
    fun validateImportProfileJson(rawJson: String): AccountLightImportPlan {
        val root = parseRootObject(rawJson)
        val missingSection = RequiredTopLevelKeys.firstOrNull { key -> key !in root }
        if (missingSection != null) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.MISSING_TOP_LEVEL_SECTION,
                message = "Portable profile is missing $missingSection.",
            )
        }
        val schemaVersionPrimitive = root["schemaVersion"]?.jsonPrimitive
            ?: throw AccountLightImportException(
                code = AccountLightImportErrorCode.INVALID_PROFILE,
                message = "Portable profile has an invalid schema version.",
            )
        if (schemaVersionPrimitive.isString) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.INVALID_PROFILE,
                message = "Portable profile has an invalid schema version.",
            )
        }
        val schemaVersion = schemaVersionPrimitive.intOrNull
            ?: throw AccountLightImportException(
                code = AccountLightImportErrorCode.INVALID_PROFILE,
                message = "Portable profile has an invalid schema version.",
            )
        if (schemaVersion != ACCOUNT_LIGHT_SCHEMA_VERSION) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.UNSUPPORTED_SCHEMA_VERSION,
                message = "Unsupported portable profile schema version $schemaVersion.",
            )
        }
        try {
            validateRequiredShape(root)
        } catch (exception: IllegalArgumentException) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.INVALID_PROFILE,
                message = exception.message ?: "Portable profile is invalid.",
                cause = exception,
            )
        }
        val profile = try {
            codec.decode(rawJson)
        } catch (exception: IllegalArgumentException) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.INVALID_PROFILE,
                message = exception.message ?: "Portable profile is invalid.",
                cause = exception,
            )
        }
        val supportedPackages = supportedApps.mapTo(mutableSetOf(), DistractingApp::packageName)
        val unsupportedAppCount = profile.settings.selectedAppPackages.distinct().count { packageName ->
            packageName !in supportedPackages
        }
        val generatedWarnings = unknownFieldWarnings(root) +
            unsupportedAppWarnings(unsupportedAppCount) +
            duplicateScalarWarnings(profile.settings)
        val allWarnings = profile.warnings + generatedWarnings
        return AccountLightImportPlan(
            profile = profile,
            generatedWarnings = generatedWarnings,
            preview = AccountLightImportPreview(
                profileId = profile.profile.profileId,
                appVersionName = profile.app.appVersionName,
                exportedAtMillis = profile.exportedAtMillis,
                importedAppCount = profile.settings.selectedAppPackages.size,
                unsupportedAppCount = unsupportedAppCount,
                importedTopicCount = profile.settings.preferredTopics.size,
                importedPackCount = profile.settings.selectedPackIds.size,
                warningCount = allWarnings.size,
                warningSummaries = allWarnings.map { warning ->
                    warning.message ?: warning.code
                }.distinct().take(5),
            ),
        )
    }

    suspend fun applyMerge(plan: AccountLightImportPlan): AccountLightImportApplyResult {
        return AccountLightImportApplyResult(
            mode = AccountLightImportMode.MERGE,
            settingsApplied = false,
            warningCount = plan.allWarnings.size + 1,
        )
    }

    suspend fun applyReplace(plan: AccountLightImportPlan): AccountLightImportApplyResult {
        val supportedPackages = supportedApps.mapTo(mutableSetOf(), DistractingApp::packageName)
        settingsRepository.replacePortableSettings(
            settings = plan.profile.settings.toPortableAppSettings(supportedPackages = supportedPackages),
            profileIdentity = LocalProfileIdentity(
                profileId = plan.profile.profile.profileId,
                createdAtMillis = plan.profile.profile.createdAtMillis,
            ),
        )
        return AccountLightImportApplyResult(
            mode = AccountLightImportMode.REPLACE,
            settingsApplied = true,
            warningCount = plan.allWarnings.size,
        )
    }

    private fun parseRootObject(rawJson: String): JsonObject {
        return try {
            json.parseToJsonElement(rawJson).jsonObject
        } catch (exception: Exception) {
            throw AccountLightImportException(
                code = AccountLightImportErrorCode.MALFORMED_JSON,
                message = "Portable profile is not valid JSON.",
                cause = exception,
            )
        }
    }

    private fun validateRequiredShape(root: JsonObject) {
        root.requireObject("app", "app").validateRequiredKeys(
            path = "app",
            required = RequiredAppKeys,
            allowed = RequiredAppKeys,
        )
        root.requireObject("profile", "profile").validateRequiredKeys(
            path = "profile",
            required = RequiredProfileKeys,
            allowed = RequiredProfileKeys,
        )
        root.requireObject("settings", "settings").validateRequiredKeys(
            path = "settings",
            required = RequiredSettingsKeys,
            allowed = RequiredSettingsKeys,
        )
        root.requireObject("library", "library").validateLibraryShape()
        val allowedContentIds = root.importedLibraryContentIds() + knownContentIdsProvider()
        root.requireObject("settings", "settings").validateSettingsReferences(allowedContentIds)
        root.requireObject("reading", "reading").validateReadingShape(
            allowedContentIds = allowedContentIds,
        )
        root.requireObject("annotations", "annotations").validateAnnotationsShape()
        root.requireObject("sync", "sync").validateSyncShape()
        root.requireArray("warnings", "warnings").forEachIndexed { index, element ->
            element.requireObject("warnings[$index]").validateRequiredKeys(
                path = "warnings[$index]",
                required = RequiredWarningKeys,
                allowed = RequiredWarningKeys,
            )
        }
    }

    private fun unknownFieldWarnings(root: JsonObject): List<AccountLightWarning> {
        return findUnknownFieldWarnings(root)
            .sortedBy(UnknownFieldWarning::path)
            .map { warning ->
                AccountLightWarning(
                    code = "UNKNOWN_FIELD_IGNORED",
                    severity = "INFO",
                    section = warning.section,
                    message = "An unknown ${warning.section.toUnknownFieldMessageSection()} field was ignored",
                )
            }
    }

    private fun unsupportedAppWarnings(unsupportedAppCount: Int): List<AccountLightWarning> {
        return List(unsupportedAppCount) {
            AccountLightWarning(
                code = "UNSUPPORTED_LOCAL_APP_PACKAGE",
                severity = "WARNING",
                section = "settings",
                message = "An imported app package is not supported by this build and will stay inactive.",
            )
        }
    }

    private fun duplicateScalarWarnings(settings: AccountLightSettings): List<AccountLightWarning> {
        return listOfNotNull(
            settings.selectedAppPackages.duplicateScalarWarning("selectedAppPackages"),
            settings.preferredTopics.duplicateScalarWarning("preferredTopics"),
            settings.selectedPackIds.duplicateScalarWarning("selectedPackIds"),
            settings.priorityContentIds.duplicateScalarWarning("priorityContentIds"),
            settings.reactivatedCompletedContentIds.duplicateScalarWarning("reactivatedCompletedContentIds"),
        )
    }
}

private data class UnknownFieldWarning(
    val path: String,
    val section: String,
)

private fun String.toUnknownFieldMessageSection(): String {
    return when (this) {
        "library.userLinks" -> "saved link"
        "library.userDocuments" -> "document"
        "reading.progress" -> "reading progress"
        else -> replace('.', ' ').substringBefore(' ')
    }
}

private fun findUnknownFieldWarnings(root: JsonObject): List<UnknownFieldWarning> {
    return buildList {
        addUnknownKeys(root, RequiredTopLevelKeys, "profile", "unknown")
        root["app"]?.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredAppKeys, "app", "app") }
        root["profile"]?.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredProfileKeys, "profile", "profile") }
        root["settings"]?.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredSettingsKeys, "settings", "settings") }
        root["library"]?.jsonObjectOrNull()?.let { library ->
            addUnknownKeys(library, RequiredLibraryKeys, "library", "unknown")
            library["userLinks"]?.jsonArrayOrNull()?.forEachIndexed { index, element ->
                element.jsonObjectOrNull()?.let { addUnknownKeys(it, AllowedUserLinkKeys, "library.userLinks[$index]", "library.userLinks") }
            }
            library["userDocuments"]?.jsonArrayOrNull()?.forEachIndexed { index, element ->
                element.jsonObjectOrNull()?.let { document ->
                    addUnknownKeys(document, AllowedUserDocumentKeys, "library.userDocuments[$index]", "library.userDocuments")
                    document["documentFingerprint"]?.jsonObjectOrNull()?.let {
                        addUnknownKeys(
                            it,
                            RequiredDocumentFingerprintKeys,
                            "library.userDocuments[$index].documentFingerprint",
                            "library.userDocuments",
                        )
                    }
                    document["sourceHint"]?.jsonObjectOrNull()?.let {
                        addUnknownKeys(
                            it,
                            AllowedSourceHintKeys,
                            "library.userDocuments[$index].sourceHint",
                            "library.userDocuments",
                        )
                    }
                }
            }
        }
        root["reading"]?.jsonObjectOrNull()?.let { reading ->
            addUnknownKeys(reading, RequiredReadingKeys, "reading", "reading.progress")
            reading["progress"]?.jsonArrayOrNull()?.forEachIndexed { index, element ->
                element.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredReadingProgressKeys, "reading.progress[$index]", "reading.progress") }
            }
        }
        root["annotations"]?.jsonObjectOrNull()?.let { annotations ->
            addUnknownKeys(annotations, RequiredAnnotationsKeys, "annotations", "annotations")
            annotations["export"]?.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredAnnotationExportKeys, "annotations.export", "annotations") }
            annotations["driveSync"]?.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredAnnotationDriveSyncKeys, "annotations.driveSync", "annotations") }
            annotations["sidecarIndex"]?.jsonArrayOrNull()?.forEachIndexed { index, element ->
                element.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredAnnotationSidecarKeys, "annotations.sidecarIndex[$index]", "annotations") }
            }
        }
        root["sync"]?.jsonObjectOrNull()?.let { sync ->
            addUnknownKeys(sync, RequiredSyncKeys, "sync", "sync")
            sync["profileAutosave"]?.jsonObjectOrNull()?.let {
                addUnknownKeys(it, RequiredProfileAutosaveKeys, "sync.profileAutosave", "sync")
            }
        }
        root["warnings"]?.jsonArrayOrNull()?.forEachIndexed { index, element ->
            element.jsonObjectOrNull()?.let { addUnknownKeys(it, RequiredWarningKeys, "warnings[$index]", "unknown") }
        }
    }
}

private fun MutableList<UnknownFieldWarning>.addUnknownKeys(
    source: JsonObject,
    allowed: Set<String>,
    path: String,
    section: String,
) {
    (source.keys - allowed).forEach { key ->
        add(UnknownFieldWarning(path = "$path.$key", section = section))
    }
}

private fun JsonObject.validateLibraryShape() {
    validateRequiredKeys(path = "library", required = RequiredLibraryKeys, allowed = RequiredLibraryKeys)
    val userLinkIds = mutableSetOf<String>()
    requireArray("userLinks", "library.userLinks").forEachIndexed { index, element ->
        val path = "library.userLinks[$index]"
        val link = element.requireObject(path)
        link.validateRequiredKeys(path = path, required = RequiredUserLinkKeys, allowed = AllowedUserLinkKeys)
        val contentId = link.requireString("contentId", path)
        require(contentId.matches(ContentIdRegex) && contentId.startsWith("user-link-")) {
            "$path.contentId must use the user-link prefix."
        }
        require(userLinkIds.add(contentId)) { "$path.contentId is duplicated." }
        val normalizedUrl = link.requireString("normalizedUrl", path)
        val urlValidation = UserLinkValidator.validateUrl(normalizedUrl)
        require(urlValidation.isValid && urlValidation.normalizedUrl == normalizedUrl) {
            "$path.normalizedUrl must be a normalized manual-link URL."
        }
        link.requireNonBlankString("title", path, maxLength = 200)
            .also { title ->
                require(title.isSafePortableTitle()) { "$path.title is not portable." }
            }
        link.requireString("description", path, maxLength = 1000)
        link.requireInt("durationMinutes", path, 1..240)
        link.requireEnumArray("topicTags", path, TopicTag.entries.mapTo(mutableSetOf(), TopicTag::name), minSize = 1)
        link.requireEnum("availability", path, AvailabilityValues)
        val createdAtMillis = link.requireLong("createdAtMillis", path, minimum = 0L)
        val updatedAtMillis = link.requireLong("updatedAtMillis", path, minimum = 0L)
        require(updatedAtMillis >= createdAtMillis) { "$path.updatedAtMillis must be >= createdAtMillis." }
        link.requireNullableString("sourceLabel", path, maxLength = 120, required = false)
    }

    val userDocumentIds = mutableSetOf<String>()
    requireArray("userDocuments", "library.userDocuments").forEachIndexed { index, element ->
        val path = "library.userDocuments[$index]"
        val document = element.requireObject(path)
        document.validateRequiredKeys(path = path, required = RequiredUserDocumentKeys, allowed = AllowedUserDocumentKeys)
        val contentId = document.requireString("contentId", path)
        require(contentId.matches(ContentIdRegex) && contentId.startsWith("user-document-")) {
            "$path.contentId must use the user-document prefix."
        }
        require(userDocumentIds.add(contentId)) { "$path.contentId is duplicated." }
        document.requireNonBlankString("displayName", path, maxLength = 240)
            .also { displayName ->
                require(displayName.isSafePortableSourceHint()) { "$path.displayName is not portable." }
            }
        document.requireNullableString("mimeType", path, maxLength = 120, required = true)
        document.requireEnum("documentFormat", path, DocumentFormatValues)
        document.requireNonBlankString("title", path, maxLength = 200)
            .also { title ->
                require(title.isSafePortableTitle()) { "$path.title is not portable." }
            }
        document.requireString("description", path, maxLength = 1000)
        document.requireInt("durationMinutes", path, 1..240)
        document.requireEnumArray("topicTags", path, TopicTag.entries.mapTo(mutableSetOf(), TopicTag::name), minSize = 1)
        document.requireEnum("availability", path, AvailabilityValues)
        val documentImportState = document.requireEnum("documentImportState", path, DocumentImportStateValues)
        val fingerprintStrategy = document
            .requireObject("documentFingerprint", path)
            .validateDocumentFingerprint("$path.documentFingerprint")
        require(fingerprintStrategy != "UNVERIFIED_METADATA_ONLY" || documentImportState == "MISSING_FILE_NEEDS_REATTACH") {
            "$path.documentImportState must be MISSING_FILE_NEEDS_REATTACH for UNVERIFIED_METADATA_ONLY."
        }
        document["sourceHint"]?.let { sourceHint ->
            sourceHint.requireObject("$path.sourceHint").validateSourceHint("$path.sourceHint")
        }
        val createdAtMillis = document.requireLong("createdAtMillis", path, minimum = 0L)
        val updatedAtMillis = document.requireLong("updatedAtMillis", path, minimum = 0L)
        require(updatedAtMillis >= createdAtMillis) { "$path.updatedAtMillis must be >= createdAtMillis." }
    }
}

private fun JsonObject.validateDocumentFingerprint(path: String): String {
    validateRequiredKeys(
        path = path,
        required = RequiredDocumentFingerprintKeys,
        allowed = RequiredDocumentFingerprintKeys,
    )
    val strategy = requireEnum("strategy", path, DocumentFingerprintStrategyValues)
    val sha256 = requireNullableString("sha256", path, maxLength = 64, required = true)?.also { sha256 ->
        require(sha256.matches(Sha256Regex)) { "$path.sha256 must be lowercase SHA-256 hex." }
    }
    val sizeBytes = requireNullableLong("sizeBytes", path, minimum = 0L, required = true)
    requireNonBlankString("normalizedTitle", path, maxLength = 200).also { normalizedTitle ->
        require(normalizedTitle.isNormalizedPortableTitle()) { "$path.normalizedTitle is not normalized." }
    }
    requireEnum("format", path, ContentFormatValues)
    when (strategy) {
        "SHA256_BYTES" -> {
            require(sha256 != null) { "$path.sha256 is required for SHA256_BYTES." }
            require(sizeBytes != null) { "$path.sizeBytes is required for SHA256_BYTES." }
        }
        "TEXT_SAMPLE_SHA256" -> {
            require(sha256 != null) { "$path.sha256 is required for TEXT_SAMPLE_SHA256." }
            require(sizeBytes == null) { "$path.sizeBytes must be null for TEXT_SAMPLE_SHA256." }
        }
        "UNVERIFIED_METADATA_ONLY" -> {
            require(sha256 == null) { "$path.sha256 must be null for UNVERIFIED_METADATA_ONLY." }
            require(sizeBytes == null) { "$path.sizeBytes must be null for UNVERIFIED_METADATA_ONLY." }
        }
    }
    return strategy
}

private fun JsonObject.validateSourceHint(path: String) {
    validateRequiredKeys(path = path, required = emptySet(), allowed = AllowedSourceHintKeys)
    requireNullableString("lastKnownDisplayName", path, maxLength = 240, required = false)?.let { displayName ->
        require(displayName.isSafePortableSourceHint()) { "$path.lastKnownDisplayName is not portable." }
    }
    requireNullableString("providerLabel", path, maxLength = 120, required = false)?.let { providerLabel ->
        require(providerLabel.isSafePortableProviderLabel()) { "$path.providerLabel is not portable." }
    }
}

private fun JsonObject.validateSettingsReferences(allowedContentIds: Set<String>) {
    validateContentIdReferences("priorityContentIds", "settings.priorityContentIds", allowedContentIds)
    validateContentIdReferences("reactivatedCompletedContentIds", "settings.reactivatedCompletedContentIds", allowedContentIds)
}

private fun JsonObject.validateContentIdReferences(key: String, path: String, allowedContentIds: Set<String>) {
    requireArray(key, path).forEachIndexed { index, element ->
        val contentId = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString.not() }?.content
            ?: throw IllegalArgumentException("$path[$index] must be a string.")
        require(contentId.matches(ContentIdRegex)) { "$path[$index] is invalid." }
        require(contentId in allowedContentIds) { "$path[$index] must reference imported or locally known content." }
    }
}

private fun JsonObject.validateReadingShape(allowedContentIds: Set<String>) {
    validateRequiredKeys(path = "reading", required = RequiredReadingKeys, allowed = RequiredReadingKeys)
    val progressIds = mutableSetOf<String>()
    requireArray("progress", "reading.progress").forEachIndexed { index, element ->
        val path = "reading.progress[$index]"
        val progress = element.requireObject(path)
        progress.validateRequiredKeys(path = path, required = RequiredReadingProgressKeys, allowed = RequiredReadingProgressKeys)
        val contentId = progress.requireString("contentId", path)
        require(contentId.matches(ContentIdRegex)) { "$path.contentId is invalid." }
        require(contentId in allowedContentIds) {
            "$path.contentId must reference imported or locally known content."
        }
        require(progressIds.add(contentId)) { "$path.contentId is duplicated." }
        val progressPercent = progress.requireInt("progressPercent", path, 0..100)
        val lastVisibleParagraphIndex = progress.requireInt("lastVisibleParagraphIndex", path, 0..Int.MAX_VALUE)
        val paragraphCount = progress.requireInt("paragraphCount", path, 1..Int.MAX_VALUE)
        progress.requireLong("updatedAtMillis", path, minimum = 0L)
        val completedAtMillis = progress.requireNullableLong("completedAtMillis", path, minimum = 0L, required = true)
        require(lastVisibleParagraphIndex < paragraphCount) {
            "$path.lastVisibleParagraphIndex must be < paragraphCount."
        }
        require((completedAtMillis != null && progressPercent == 100) || (completedAtMillis == null && progressPercent in 0..99)) {
            "$path progress completion fields are inconsistent."
        }
    }
}

private fun JsonObject.validateAnnotationsShape() {
    validateRequiredKeys(path = "annotations", required = RequiredAnnotationsKeys, allowed = RequiredAnnotationsKeys)
    requireObject("export", "annotations.export").apply {
        validateRequiredKeys(
            path = "annotations.export",
            required = RequiredAnnotationExportKeys,
            allowed = RequiredAnnotationExportKeys,
        )
        requireNullableString("destinationDisplayName", "annotations.export", maxLength = 120, required = true)?.let {
            require(it.isSafePortableDisplayName()) { "annotations.export.destinationDisplayName is not portable." }
        }
        requireNullableLong("lastSuccessfulAtMillis", "annotations.export", minimum = 0L, required = true)
    }
    requireObject("driveSync", "annotations.driveSync").apply {
        validateRequiredKeys(
            path = "annotations.driveSync",
            required = RequiredAnnotationDriveSyncKeys,
            allowed = RequiredAnnotationDriveSyncKeys,
        )
        requireBoolean("wasEnabledOnSourceDevice", "annotations.driveSync")
        requireNullableString("folderDisplayName", "annotations.driveSync", maxLength = 120, required = true)?.let {
            require(it.isSafePortableDisplayName()) { "annotations.driveSync.folderDisplayName is not portable." }
        }
        requireNullableLong("lastSuccessfulAtMillis", "annotations.driveSync", minimum = 0L, required = true)
    }
    val sidecarIds = mutableSetOf<String>()
    requireArray("sidecarIndex", "annotations.sidecarIndex").forEachIndexed { index, element ->
        val path = "annotations.sidecarIndex[$index]"
        val sidecar = element.requireObject(path)
        sidecar.validateRequiredKeys(
            path = path,
            required = RequiredAnnotationSidecarKeys,
            allowed = RequiredAnnotationSidecarKeys,
        )
        val contentId = sidecar.requireString("contentId", path)
        require(contentId.matches(ContentIdRegex)) { "$path.contentId is invalid." }
        require(sidecarIds.add(contentId)) { "$path.contentId is duplicated." }
        sidecar.requireNonBlankString("sourceTitle", path, maxLength = 240)
        val fileName = sidecar.requireString("jsonLdFileName", path)
        require(fileName.isSafePortableAnnotationSidecarFileName()) {
            "$path.jsonLdFileName is invalid."
        }
        sidecar.requireNullableString("sha256", path, maxLength = 64, required = true)?.let { sha256 ->
            require(sha256.matches(Sha256Regex)) { "$path.sha256 must be lowercase SHA-256 hex." }
        }
        sidecar.requireLong("updatedAtMillis", path, minimum = 0L)
    }
}

private fun JsonObject.validateSyncShape() {
    validateRequiredKeys(path = "sync", required = RequiredSyncKeys, allowed = RequiredSyncKeys)
    requireObject("profileAutosave", "sync.profileAutosave").apply {
        validateRequiredKeys(
            path = "sync.profileAutosave",
            required = RequiredProfileAutosaveKeys,
            allowed = RequiredProfileAutosaveKeys,
        )
        requireEnum("provider", "sync.profileAutosave", AutosaveProviders)
        requireNullableString("destinationDisplayName", "sync.profileAutosave", maxLength = 120, required = true)?.let {
            require(it.isSafePortableDisplayName()) { "sync.profileAutosave.destinationDisplayName is not portable." }
        }
        requireNullableLong("lastSuccessfulAtMillis", "sync.profileAutosave", minimum = 0L, required = true)
        require(requireString("activationStateOnImport", "sync.profileAutosave") == "REQUIRES_LOCAL_SELECTION") {
            "sync.profileAutosave.activationStateOnImport is invalid."
        }
    }
}

private fun JsonObject.validateRequiredKeys(path: String, required: Set<String>, allowed: Set<String>) {
    val missing = required.firstOrNull { key -> key !in this }
    require(missing == null) { "$path is missing required field $missing." }
    val unknown = keys - allowed
    require(unknown.isEmpty() || allowed.isNotEmpty()) { "$path has unknown fields." }
}

private fun JsonObject.requireObject(key: String, path: String): JsonObject {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing required field $key.")
    return element.requireObject("$path.$key")
}

private fun JsonElement.requireObject(path: String): JsonObject {
    return jsonObjectOrNull() ?: throw IllegalArgumentException("$path must be an object.")
}

private fun JsonObject.requireArray(key: String, path: String): JsonArray {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing.")
    return element.jsonArrayOrNull() ?: throw IllegalArgumentException("$path must be an array.")
}

private fun JsonObject.requireString(key: String, path: String, maxLength: Int? = null): String {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing required field $key.")
    val value = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString.not() }?.content
        ?: throw IllegalArgumentException("$path.$key must be a string.")
    require(maxLength == null || value.length <= maxLength) { "$path.$key is too long." }
    return value
}

private fun JsonObject.requireNonBlankString(key: String, path: String, maxLength: Int): String {
    val value = requireString(key, path, maxLength)
    require(value.trim().isNotBlank()) { "$path.$key must be non-blank." }
    return value
}

private fun JsonObject.requireNullableString(
    key: String,
    path: String,
    maxLength: Int,
    required: Boolean,
): String? {
    val element = this[key]
    if (element == null) {
        require(!required) { "$path is missing required field $key." }
        return null
    }
    if (element.isJsonNull()) return null
    val value = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString.not() }?.content
        ?: throw IllegalArgumentException("$path.$key must be a string or null.")
    require(value.length <= maxLength) { "$path.$key is too long." }
    return value
}

private fun JsonObject.requireBoolean(key: String, path: String): Boolean {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing required field $key.")
    val primitive = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString }
        ?: throw IllegalArgumentException("$path.$key must be a boolean.")
    return primitive.booleanOrNull
        ?: throw IllegalArgumentException("$path.$key must be a boolean.")
}

private fun JsonObject.requireInt(key: String, path: String, range: IntRange): Int {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing required field $key.")
    val primitive = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString }
        ?: throw IllegalArgumentException("$path.$key must be an integer.")
    val value = primitive.intOrNull
        ?: throw IllegalArgumentException("$path.$key must be an integer.")
    require(value in range) { "$path.$key is outside the supported range." }
    return value
}

private fun JsonObject.requireLong(key: String, path: String, minimum: Long): Long {
    val element = this[key] ?: throw IllegalArgumentException("$path is missing required field $key.")
    val primitive = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString }
        ?: throw IllegalArgumentException("$path.$key must be an integer.")
    val value = primitive.longOrNull
        ?: throw IllegalArgumentException("$path.$key must be an integer.")
    require(value >= minimum) { "$path.$key is outside the supported range." }
    return value
}

private fun JsonObject.requireNullableLong(key: String, path: String, minimum: Long, required: Boolean): Long? {
    val element = this[key]
    if (element == null) {
        require(!required) { "$path is missing required field $key." }
        return null
    }
    if (element.isJsonNull()) return null
    val primitive = element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString }
        ?: throw IllegalArgumentException("$path.$key must be an integer or null.")
    val value = primitive.longOrNull
        ?: throw IllegalArgumentException("$path.$key must be an integer or null.")
    require(value >= minimum) { "$path.$key is outside the supported range." }
    return value
}

private fun JsonObject.requireEnum(key: String, path: String, allowed: Set<String>): String {
    val value = requireString(key, path)
    require(value in allowed) { "$path.$key is invalid." }
    return value
}

private fun JsonObject.requireEnumArray(key: String, path: String, allowed: Set<String>, minSize: Int = 0): List<String> {
    val values = requireArray(key, "$path.$key").mapIndexed { index, element ->
        element.jsonPrimitiveOrNull()?.takeUnless { primitive -> primitive.isString.not() }?.content
            ?: throw IllegalArgumentException("$path.$key[$index] must be a string.")
    }
    require(values.size >= minSize) { "$path.$key must include at least $minSize values." }
    values.forEach { value ->
        require(value in allowed) { "$path.$key contains an invalid value." }
    }
    return values
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? {
    return runCatching { jsonObject }.getOrNull()
}

private fun JsonElement.jsonArrayOrNull(): JsonArray? {
    return runCatching { jsonArray }.getOrNull()
}

private fun JsonElement.jsonPrimitiveOrNull() = runCatching { jsonPrimitive }.getOrNull()

private fun JsonElement.isJsonNull(): Boolean {
    return this.toString() == "null"
}

private fun JsonObject.importedLibraryContentIds(): Set<String> {
    val library = this["library"]?.jsonObjectOrNull() ?: return emptySet()
    val linkIds = library["userLinks"]
        ?.jsonArrayOrNull()
        .orEmpty()
        .mapNotNull { element -> element.jsonObjectOrNull()?.get("contentId")?.jsonPrimitiveOrNull()?.content }
    val documentIds = library["userDocuments"]
        ?.jsonArrayOrNull()
        .orEmpty()
        .mapNotNull { element -> element.jsonObjectOrNull()?.get("contentId")?.jsonPrimitiveOrNull()?.content }
    return (linkIds + documentIds).toSet()
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

private fun AccountLightSettings.toPortableAppSettings(supportedPackages: Set<String>? = null): AppSettings {
    return AppSettings(
        hasCompletedOnboarding = hasCompletedOnboarding,
        selectedAppPackages = selectedAppPackages
            .filter { packageName -> supportedPackages == null || packageName in supportedPackages }
            .toSet(),
        preferredTopics = preferredTopics.mapTo(mutableSetOf()) { TopicTag.valueOf(it) },
        preferredDurationBucket = DurationBucket.valueOf(preferredDurationBucket),
        selectedPackIds = selectedPackIds.toSet(),
        themeMode = AppThemeMode.valueOf(themeMode),
        meditationDurationMinutes = meditationDurationMinutes,
        contentPriority = ContentPriority.valueOf(contentPriority),
        priorityContentIds = priorityContentIds.toSet(),
        reactivatedCompletedContentIds = reactivatedCompletedContentIds.toSet(),
        openAnywayUnlockMinutes = openAnywayUnlockMinutes,
        readerFontScale = readerFontScale.takeIf { it in MIN_READER_FONT_SCALE..MAX_READER_FONT_SCALE }
            ?: DEFAULT_READER_FONT_SCALE,
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
            message = "One or more stored content references were omitted because they are not portable profile content ids.",
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

private fun String.isSafePortableSourceHint(): Boolean {
    val value = trim()
    return value.isNotBlank() &&
        value.length <= 240 &&
        !value.containsUnsafePortableValue() &&
        value.all { char ->
            char.isLetterOrDigit() || char.isWhitespace() || char in PortableDisplayNamePunctuation
        }
}

private fun String.isSafePortableProviderLabel(): Boolean {
    val value = trim()
    val lower = value.lowercase()
    return value.isNotBlank() &&
        value.length <= 120 &&
        value.isSafePortableSourceHint() &&
        "." !in value &&
        lower !in ProviderLabelBlocklist &&
        !ProviderLabelBlocklist.any { blocked -> lower.contains(blocked) }
}

private fun String.containsUnsafePortableValue(): Boolean {
    val value = lowercase(Locale.US)
    return value.contains("content://") ||
        value.contains("file://") ||
        value.contains("oauth") ||
        value.contains("token") ||
        UnsafePortableValueTerms.any { unsafeTerm -> value.contains(unsafeTerm) } ||
        UnsafePortableValuePatterns.any { unsafePattern -> unsafePattern.containsMatchIn(this) } ||
        contains("/") ||
        contains("\\") ||
        contains("@") ||
        contains(":")
}

private fun String.containsUnsafeWarningMessage(): Boolean {
    return UnsafeWarningMessagePatterns.any { unsafePattern -> unsafePattern.containsMatchIn(this) }
}

private fun String.containsUnsafePortableValueWithoutReverseDns(): Boolean {
    val value = lowercase(Locale.US)
    return value.contains("content://") ||
        value.contains("file://") ||
        value.contains("oauth") ||
        value.contains("token") ||
        UnsafePortableValueTerms.any { unsafeTerm -> value.contains(unsafeTerm) } ||
        contains("/") ||
        contains("\\") ||
        contains("@") ||
        contains(":")
}

private fun String.isSafePortableFileName(): Boolean {
    return isNotBlank() &&
        length <= 160 &&
        !containsUnsafePortableValue() &&
        all { char ->
            char.isLetterOrDigit() || char in setOf('.', '_', '-')
        }
}

private fun String.isSafePortableAnnotationSidecarFileName(): Boolean {
    val suffix = ".annotations.jsonld"
    val baseName = removeSuffix(suffix)
    return isNotBlank() &&
        length <= 160 &&
        endsWith(suffix) &&
        baseName.isNotBlank() &&
        !containsUnsafePortableValueWithoutReverseDns() &&
        all { char ->
            char.isLetterOrDigit() || char in setOf('.', '_', '-')
        }
}

private fun String.isSafePortableTitle(): Boolean {
    return isNotBlank() &&
        length <= 240 &&
        !containsUnsafePortableValue()
}

private fun String.isNormalizedPortableTitle(): Boolean {
    val value = this
    return value.isNotBlank() &&
        value == value.trim() &&
        value == value.lowercase(Locale.US) &&
        value == value.replace(Regex("\\s+"), " ") &&
        !value.endsWith(".pdf") &&
        !value.endsWith(".epub") &&
        !value.endsWith(".html") &&
        !value.endsWith(".htm") &&
        !value.endsWith(".md") &&
        !value.endsWith(".markdown") &&
        value.isSafePortableTitle()
}

private fun List<String>.duplicateScalarWarning(fieldName: String): AccountLightWarning? {
    return if (size != distinct().size) {
        AccountLightWarning(
            code = "DUPLICATE_SCALAR_DEDUPED",
            severity = "INFO",
            section = "settings",
            message = "$fieldName had duplicate values and was deduplicated.",
        )
    } else {
        null
    }
}

private val ProfileIdRegex = Regex("^qa-local-[0-9a-fA-F-]{36}$")
private val Sha256Regex = Regex("^[0-9a-f]{64}$")
private val ContentIdRegex = Regex(
    "^(user-link|user-document)-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$" +
        "|^(editorial|meditation)-[a-z0-9][a-z0-9._-]{2,120}$",
)
private val PackageNameRegex = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$")
private val PortableDisplayNamePunctuation = setOf(' ', '.', '_', '-', '(', ')')
private val ProviderLabelBlocklist = setOf(
    "com.android",
    "com.google",
    "providers",
    "documents",
    "storage",
    "media",
    "drive",
    "externalstorage",
)
private val UnsafePortableValueTerms = setOf(
    "android.provider",
    "com.android.providers",
    "com.android.providers.media.documents",
    "com.android.externalstorage.documents",
    "com.google.android.apps.docs",
    "com.google.android.apps.docs.storage",
    "providers.media.documents",
    "providers.downloads.documents",
    "providers.media",
    "externalstorage.documents",
)
private val UnsafePortableValuePatterns = listOf(
    Regex("\\b[a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z0-9_]+){2,}\\b"),
)
private val UnsafeWarningMessagePatterns = listOf(
    Regex("\\b[A-Z][A-Za-z0-9_]*(?:Exception|Error|Throwable)\\b"),
    Regex("\\b(?:exception|stack\\s*trace|stacktrace|traceback)\\b", RegexOption.IGNORE_CASE),
    Regex("\\bat\\s+[^\\s]+\\.(?:kt|java)(?::|\\s+line\\s+)\\d+\\b", RegexOption.IGNORE_CASE),
)
private val RequiredAppKeys = setOf("profileFormat", "packageName", "appVersionName", "appVersionCode")
private val RequiredProfileKeys = setOf("profileId", "createdAtMillis", "updatedAtMillis", "displayName")
private val RequiredSettingsKeys = setOf(
    "hasCompletedOnboarding",
    "selectedAppPackages",
    "preferredTopics",
    "preferredDurationBucket",
    "selectedPackIds",
    "themeMode",
    "meditationDurationMinutes",
    "readerFontScale",
    "contentPriority",
    "priorityContentIds",
    "reactivatedCompletedContentIds",
    "openAnywayUnlockMinutes",
)
private val RequiredLibraryKeys = setOf("userLinks", "userDocuments")
private val RequiredUserLinkKeys = setOf(
    "contentId",
    "normalizedUrl",
    "title",
    "description",
    "durationMinutes",
    "topicTags",
    "availability",
    "createdAtMillis",
    "updatedAtMillis",
)
private val AllowedUserLinkKeys = RequiredUserLinkKeys + setOf("sourceLabel")
private val RequiredUserDocumentKeys = setOf(
    "contentId",
    "displayName",
    "mimeType",
    "documentFormat",
    "title",
    "description",
    "durationMinutes",
    "topicTags",
    "availability",
    "documentImportState",
    "documentFingerprint",
    "createdAtMillis",
    "updatedAtMillis",
)
private val AllowedUserDocumentKeys = RequiredUserDocumentKeys + setOf("sourceHint")
private val RequiredDocumentFingerprintKeys = setOf(
    "strategy",
    "sha256",
    "sizeBytes",
    "normalizedTitle",
    "format",
)
private val AllowedSourceHintKeys = setOf("lastKnownDisplayName", "providerLabel")
private val RequiredReadingKeys = setOf("progress")
private val RequiredReadingProgressKeys = setOf(
    "contentId",
    "progressPercent",
    "lastVisibleParagraphIndex",
    "paragraphCount",
    "updatedAtMillis",
    "completedAtMillis",
)
private val RequiredAnnotationsKeys = setOf("export", "driveSync", "sidecarIndex")
private val RequiredAnnotationExportKeys = setOf("destinationDisplayName", "lastSuccessfulAtMillis")
private val RequiredAnnotationDriveSyncKeys = setOf(
    "wasEnabledOnSourceDevice",
    "folderDisplayName",
    "lastSuccessfulAtMillis",
)
private val RequiredAnnotationSidecarKeys = setOf(
    "contentId",
    "sourceTitle",
    "jsonLdFileName",
    "sha256",
    "updatedAtMillis",
)
private val RequiredSyncKeys = setOf("profileAutosave")
private val RequiredProfileAutosaveKeys = setOf(
    "provider",
    "destinationDisplayName",
    "lastSuccessfulAtMillis",
    "activationStateOnImport",
)
private val RequiredWarningKeys = setOf("code", "severity", "section", "contentId", "message")
private val AvailabilityValues = setOf("AVAILABLE", "UNAVAILABLE", "NEEDS_FALLBACK")
private val DocumentFormatValues = setOf("MARKDOWN", "PDF", "EPUB")
private val DocumentImportStateValues = setOf(
    "AVAILABLE_ON_THIS_DEVICE",
    "MISSING_FILE_NEEDS_REATTACH",
    "EXTERNAL_HANDOFF_ONLY",
)
private val DocumentFingerprintStrategyValues = setOf(
    "SHA256_BYTES",
    "TEXT_SAMPLE_SHA256",
    "UNVERIFIED_METADATA_ONLY",
)
private val ContentFormatValues = setOf("MARKDOWN", "HTML", "PDF", "EPUB")
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
private val RequiredTopLevelKeys = setOf(
    "schemaVersion",
    "exportedAtMillis",
    "app",
    "profile",
    "settings",
    "library",
    "reading",
    "annotations",
    "sync",
    "warnings",
)
