package com.qualityalternative.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveAnnotationSyncMergerTest {
    @Test
    fun annotationMergeKeepsRemoteOnlyItemsAndPrefersNewerOnConflict() {
        val local = annotationCollection(
            items = listOf(
                item(id = "a1", modified = "2026-02-01T00:00:00Z", note = "local-newer"),
                item(id = "a2", modified = "2026-02-01T00:00:00Z", note = "local-only"),
            ),
        )
        val remote = annotationCollection(
            items = listOf(
                item(id = "a1", modified = "2026-01-01T00:00:00Z", note = "remote-older"),
                item(id = "a3", modified = "2026-01-15T00:00:00Z", note = "remote-only"),
            ),
        )

        val merged = JSONObject(DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remote))
        val notesById = merged.itemsById()

        assertEquals(setOf("a1", "a2", "a3"), notesById.keys)
        assertEquals("local-newer", notesById["a1"])
        assertEquals("local-only", notesById["a2"])
        assertEquals("remote-only", notesById["a3"])
        assertEquals(3, merged.getInt("total"))
    }

    @Test
    fun annotationMergeKeepsRemoteWhenRemoteModifiedIsNewer() {
        val local = annotationCollection(
            items = listOf(item(id = "a1", modified = "2026-01-01T00:00:00Z", note = "local-older")),
        )
        val remote = annotationCollection(
            items = listOf(item(id = "a1", modified = "2026-03-01T00:00:00Z", note = "remote-newer")),
        )

        val merged = JSONObject(DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remote))

        assertEquals("remote-newer", merged.itemsById()["a1"])
        assertEquals(1, merged.getInt("total"))
    }

    @Test
    fun annotationMergeFallsBackToLocalWhenRemoteIsBlankOrUnparseable() {
        val local = annotationCollection(items = listOf(item(id = "a1", modified = "2026-01-01T00:00:00Z", note = "local")))

        assertEquals(local, DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remoteJson = null))
        assertEquals(local, DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remoteJson = "   "))
        assertEquals(local, DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remoteJson = "not-json"))
    }

    @Test
    fun indexMergeUnionsFilesAndPrefersLocalMetadataOnFileNameClash() {
        val local = """{"type":"QualityAlternativeAnnotationExportIndex","files":[
            {"contentId":"c1","sourceTitle":"Local Title","fileName":"a.jsonld"},
            {"contentId":"c2","sourceTitle":"Only Local","fileName":"b.jsonld"}
        ]}"""
        val remote = """{"type":"QualityAlternativeAnnotationExportIndex","files":[
            {"contentId":"c1","sourceTitle":"Remote Title","fileName":"a.jsonld"},
            {"contentId":"c3","sourceTitle":"Only Remote","fileName":"c.jsonld"}
        ]}"""

        val merged = JSONObject(DriveAnnotationSyncMerger.mergeIndexJson(local, remote))
        val files = merged.getJSONArray("files")
        val byFileName = (0 until files.length()).associate { index ->
            val entry = files.getJSONObject(index)
            entry.getString("fileName") to entry.getString("sourceTitle")
        }

        assertEquals(setOf("a.jsonld", "b.jsonld", "c.jsonld"), byFileName.keys)
        assertEquals("Local Title", byFileName["a.jsonld"])
        assertEquals("Only Local", byFileName["b.jsonld"])
        assertEquals("Only Remote", byFileName["c.jsonld"])
    }

    @Test
    fun annotationMergeKeepsLocalWhenTimestampsTie() {
        val local = annotationCollection(items = listOf(item(id = "a1", modified = "2026-01-01T00:00:00Z", note = "local")))
        val remote = annotationCollection(items = listOf(item(id = "a1", modified = "2026-01-01T00:00:00Z", note = "remote")))

        val merged = JSONObject(DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(local, remote))

        assertEquals("local", merged.itemsById()["a1"])
        assertNull(merged.itemsById()["missing"])
        assertTrue(merged.getJSONArray("items").length() == 1)
    }

    private fun annotationCollection(items: List<String>): String {
        return """{"@context":"http://www.w3.org/ns/anno.jsonld",
            "id":"urn:quality-alternative:annotation-collection:c1",
            "type":"AnnotationCollection","label":"Annotations","total":${items.size},
            "source":{"id":"quality-alternative://content/c1"},
            "items":[${items.joinToString(",")}]}"""
    }

    private fun item(id: String, modified: String, note: String): String {
        return """{"id":"urn:quality-alternative:annotation:$id","type":"Annotation",
            "modified":"$modified",
            "body":[{"type":"TextualBody","value":"$note"}]}"""
    }

    private fun JSONObject.itemsById(): Map<String, String> {
        val items = getJSONArray("items")
        return (0 until items.length()).associate { index ->
            val item = items.getJSONObject(index)
            val rawId = item.getString("id").substringAfterLast(':')
            val note = item.getJSONArray("body").getJSONObject(0).getString("value")
            rawId to note
        }
    }
}
