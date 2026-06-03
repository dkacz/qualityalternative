package com.qualityalternative.app.data

import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject

/**
 * Non-destructive merge of the local annotation export with whatever is already on Drive, so a sync
 * from a second device does not blindly overwrite the annotations a first device uploaded.
 *
 * The merge is structural and preserves fields it does not understand: items the remote has but the
 * local export does not are kept verbatim, items the local has but the remote does not are added, and
 * when both sides carry the same item id the newer `modified` timestamp wins (ties favour local, the
 * most recent intent on this device). The index file is merged the same way, keyed by file name.
 */
internal object DriveAnnotationSyncMerger {
    fun mergeAnnotationCollectionJson(localJson: String, remoteJson: String?): String {
        val remote = remoteJson.parseObjectOrNull() ?: return localJson
        val local = localJson.parseObjectOrNull() ?: return localJson
        val merged = mergeKeyedArray(
            base = local,
            remote = remote,
            arrayKey = "items",
            idKey = "id",
            pickNewer = ::pickNewerAnnotation,
        )
        merged.put("total", merged.optJSONArray("items")?.length() ?: 0)
        return merged.toString()
    }

    fun mergeIndexJson(localJson: String, remoteJson: String?): String {
        val remote = remoteJson.parseObjectOrNull() ?: return localJson
        val local = localJson.parseObjectOrNull() ?: return localJson
        // Local is authoritative for index entry metadata, so on a file-name clash the local entry wins.
        val merged = mergeKeyedArray(
            base = local,
            remote = remote,
            arrayKey = "files",
            idKey = "fileName",
            pickNewer = { _, localEntry -> localEntry },
        )
        return merged.toString()
    }

    private fun mergeKeyedArray(
        base: JSONObject,
        remote: JSONObject,
        arrayKey: String,
        idKey: String,
        pickNewer: (remoteEntry: JSONObject, localEntry: JSONObject) -> JSONObject,
    ): JSONObject {
        val localEntries = base.optJSONArray(arrayKey).objects()
        val remoteEntries = remote.optJSONArray(arrayKey).objects()
        val byId = LinkedHashMap<String, JSONObject>()
        // Seed with remote so remote-only entries survive, preserving their original order first.
        remoteEntries.forEach { entry -> entry.identity(idKey)?.let { byId[it] = entry } }
        localEntries.forEach { entry ->
            val id = entry.identity(idKey) ?: return@forEach
            val existing = byId[id]
            byId[id] = if (existing == null) entry else pickNewer(existing, entry)
        }
        return JSONObject(base.toString()).put(arrayKey, JSONArray(byId.values.toList()))
    }

    private fun pickNewerAnnotation(remoteEntry: JSONObject, localEntry: JSONObject): JSONObject {
        val remoteModified = remoteEntry.optString("modified").toInstantOrNull()
        val localModified = localEntry.optString("modified").toInstantOrNull()
        return when {
            remoteModified == null -> localEntry
            localModified == null -> remoteEntry
            remoteModified.isAfter(localModified) -> remoteEntry
            else -> localEntry
        }
    }

    private fun JSONObject.identity(idKey: String): String? = optString(idKey).takeIf(String::isNotBlank)

    private fun JSONArray?.objects(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optJSONObject(index) }
    }

    private fun String?.parseObjectOrNull(): JSONObject? {
        val raw = this?.takeIf(String::isNotBlank) ?: return null
        return runCatching { JSONObject(raw) }.getOrNull()
    }

    private fun String.toInstantOrNull(): Instant? {
        return takeIf(String::isNotBlank)?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }
    }
}
