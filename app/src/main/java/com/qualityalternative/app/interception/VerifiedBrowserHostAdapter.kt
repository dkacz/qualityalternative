package com.qualityalternative.app.interception

import android.view.accessibility.AccessibilityNodeInfo
import com.qualityalternative.app.data.WebsiteRuleDraftResult
import com.qualityalternative.app.data.WebsiteRuleNormalizer

object VerifiedBrowserHostAdapter {
    const val CHROME_PACKAGE = "com.android.chrome"

    private val supportedBrowserPackages = setOf(CHROME_PACKAGE)
    private val chromeAddressBarResourceNames = setOf(
        "url_bar",
        "search_box_text",
    )

    fun readHostFromWindow(
        browserPackage: String,
        root: AccessibilityNodeInfo?,
    ): VerifiedBrowserHostResult {
        if (browserPackage !in supportedBrowserPackages) {
            return VerifiedBrowserHostResult.UnsupportedBrowser
        }
        val snapshot = root?.toBrowserNodeSnapshot() ?: return VerifiedBrowserHostResult.Unreadable
        return readHostFromSnapshot(browserPackage = browserPackage, root = snapshot)
    }

    fun readHostFromSnapshot(
        browserPackage: String,
        root: BrowserNodeSnapshot?,
    ): VerifiedBrowserHostResult {
        if (browserPackage !in supportedBrowserPackages) {
            return VerifiedBrowserHostResult.UnsupportedBrowser
        }
        root ?: return VerifiedBrowserHostResult.Unreadable
        if (root.packageName != browserPackage) {
            return VerifiedBrowserHostResult.Unreadable
        }
        val host = root.depthFirst()
            .filter { node -> node.isChromeAddressBarNode(browserPackage) }
            .mapNotNull { node -> node.text?.let(::normalizedObservedHostOrNull) }
            .firstOrNull()
            ?: return VerifiedBrowserHostResult.Unreadable
        return VerifiedBrowserHostResult.Verified(host = host)
    }

    private fun BrowserNodeSnapshot.isChromeAddressBarNode(browserPackage: String): Boolean {
        if (packageName != browserPackage) {
            return false
        }
        if (!visibleToUser || (focused && editable)) {
            return false
        }
        val idName = viewIdResourceName?.substringAfter(":id/", missingDelimiterValue = viewIdResourceName)
            ?: return false
        return idName in chromeAddressBarResourceNames
    }

    private fun normalizedObservedHostOrNull(raw: String): String? {
        val candidate = raw
            .trim()
            .replace("\u200B", "")
            .takeIf(String::isNotBlank)
            ?: return null
        if (candidate.any { it.isWhitespace() }) return null
        return when (val result = WebsiteRuleNormalizer.normalize(candidate, wildcard = false)) {
            is WebsiteRuleDraftResult.Valid -> result.host
            is WebsiteRuleDraftResult.Invalid -> null
        }
    }

    private fun BrowserNodeSnapshot.depthFirst(): Sequence<BrowserNodeSnapshot> = sequence {
        yield(this@depthFirst)
        children.forEach { child -> yieldAll(child.depthFirst()) }
    }
}

sealed class VerifiedBrowserHostResult {
    data class Verified(val host: String) : VerifiedBrowserHostResult()
    data object Unreadable : VerifiedBrowserHostResult()
    data object UnsupportedBrowser : VerifiedBrowserHostResult()
}

data class BrowserNodeSnapshot(
    val packageName: String? = null,
    val viewIdResourceName: String? = null,
    val text: String? = null,
    val visibleToUser: Boolean = true,
    val focused: Boolean = false,
    val editable: Boolean = false,
    val children: List<BrowserNodeSnapshot> = emptyList(),
)

private fun AccessibilityNodeInfo.toBrowserNodeSnapshot(
    maxDepth: Int = 14,
    maxNodes: Int = 512,
): BrowserNodeSnapshot {
    var remainingNodes = maxNodes

    fun snapshot(node: AccessibilityNodeInfo, depth: Int): BrowserNodeSnapshot {
        remainingNodes -= 1
        if (depth >= maxDepth || remainingNodes <= 0) {
            return BrowserNodeSnapshot(
                packageName = node.packageName?.toString(),
                viewIdResourceName = node.viewIdResourceName,
                text = node.takeIf(AccessibilityNodeInfo::isVisibleToUser)?.text?.toString(),
                visibleToUser = node.isVisibleToUser,
                focused = node.isFocused,
                editable = node.isEditable,
            )
        }
        val children = buildList {
            for (index in 0 until node.childCount) {
                if (remainingNodes <= 0) break
                node.getChild(index)?.let { child ->
                    add(snapshot(child, depth + 1))
                }
            }
        }
        return BrowserNodeSnapshot(
            packageName = node.packageName?.toString(),
            viewIdResourceName = node.viewIdResourceName,
            text = node.takeIf(AccessibilityNodeInfo::isVisibleToUser)?.text?.toString(),
            visibleToUser = node.isVisibleToUser,
            focused = node.isFocused,
            editable = node.isEditable,
            children = children,
        )
    }

    return snapshot(this, depth = 0)
}
