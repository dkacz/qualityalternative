package com.qualityalternative.app.data

import android.content.Context
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.ContentRepository
import org.json.JSONArray
import org.json.JSONObject

class AssetContentRepository(
    private val context: Context,
) : ContentRepository {
    private val cachedPacks: List<EditorialPack> by lazy {
        val root = JSONObject(loadAsset("editorial/starter_packs.json"))
        root.getJSONArray("packs").toEditorialPacks()
    }

    override fun starterPacks(): List<EditorialPack> = cachedPacks

    override fun inventory(): List<ContentItem> = cachedPacks.flatMap(EditorialPack::items)

    override fun contentBody(item: ContentItem): String = loadAsset(item.bodyAssetPath)

    private fun loadAsset(path: String): String = context.assets.open(path).bufferedReader().use { it.readText() }

    private fun JSONArray.toEditorialPacks(): List<EditorialPack> = buildList {
        for (index in 0 until length()) {
            val pack = getJSONObject(index)
            val packId = pack.getString("id")
            add(
                EditorialPack(
                    id = packId,
                    title = pack.getString("title"),
                    description = pack.getString("description"),
                    items = pack.getJSONArray("items").toContentItems(packId = packId),
                ),
            )
        }
    }

    private fun JSONArray.toContentItems(packId: String): List<ContentItem> = buildList {
        for (index in 0 until length()) {
            val item = getJSONObject(index)
            add(
                ContentItem(
                    id = item.getString("id"),
                    packId = packId,
                    title = item.getString("title"),
                    description = item.getString("description"),
                    durationMinutes = item.getInt("durationMinutes"),
                    format = ContentFormat.valueOf(item.getString("format")),
                    topicTags = item.getJSONArray("topics").toTopicTags(),
                    bodyAssetPath = item.getString("bodyAssetPath"),
                ),
            )
        }
    }

    private fun JSONArray.toTopicTags(): Set<TopicTag> = buildSet {
        for (index in 0 until length()) {
            add(TopicTag.valueOf(getString(index)))
        }
    }
}
