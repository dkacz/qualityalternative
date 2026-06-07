package com.qualityalternative.app.interception

import org.junit.Assert.assertEquals
import org.junit.Test

class VerifiedBrowserHostAdapterTest {
    @Test
    fun readHostFromSnapshot_acceptsOnlyChromeAddressBarText() {
        val root = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/title",
                    text = "example.com",
                ),
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "https://News.Example.com/deep/path?query=private",
                ),
                BrowserNodeSnapshot(
                    packageName = "android",
                    viewIdResourceName = "android:id/content",
                    text = "private page body",
                ),
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Verified(host = "news.example.com"),
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = root,
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_acceptsDeepChromeToolbarAddressBar() {
        val root = nestedChromeToolbarNode(
            depth = 11,
            leaf = BrowserNodeSnapshot(
                packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                viewIdResourceName = "com.android.chrome:id/url_bar",
                text = "https://example.org/",
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Verified(host = "example.org"),
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = root,
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_ignoresTitleOrBodyWithoutVerifiedAddressNode() {
        val root = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/title",
                    text = "news.example.com",
                ),
                BrowserNodeSnapshot(
                    packageName = "android",
                    viewIdResourceName = "android:id/content",
                    text = "https://news.example.com/private",
                ),
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = root,
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_rejectsSearchTextAndIpAddressBarText() {
        val searchRoot = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "news example search",
                ),
            ),
        )
        val ipRoot = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "8.8.8.8",
                ),
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = searchRoot,
            ),
        )
        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = ipRoot,
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_rejectsHiddenOrEditedAddressBarText() {
        val hiddenRoot = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "news.example.com",
                    visibleToUser = false,
                ),
            ),
        )
        val focusedEditableRoot = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "news.example.com",
                    focused = true,
                    editable = true,
                ),
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = hiddenRoot,
            ),
        )
        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = focusedEditableRoot,
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_rejectsUnsupportedBrowsers() {
        assertEquals(
            VerifiedBrowserHostResult.UnsupportedBrowser,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = "org.mozilla.firefox",
                root = BrowserNodeSnapshot(
                    packageName = "org.mozilla.firefox",
                    viewIdResourceName = "org.mozilla.firefox:id/url_bar",
                    text = "example.com",
                ),
            ),
        )
    }

    @Test
    fun readHostFromSnapshot_rejectsPackageMismatchedChromeResourceNames() {
        val nonChromeRootWithChromeAddressNode = BrowserNodeSnapshot(
            packageName = "org.mozilla.firefox",
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "example.com",
                ),
            ),
        )
        val chromeRootWithNonChromeAddressNode = BrowserNodeSnapshot(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            children = listOf(
                BrowserNodeSnapshot(
                    packageName = "org.mozilla.firefox",
                    viewIdResourceName = "com.android.chrome:id/url_bar",
                    text = "example.com",
                ),
            ),
        )

        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = nonChromeRootWithChromeAddressNode,
            ),
        )
        assertEquals(
            VerifiedBrowserHostResult.Unreadable,
            VerifiedBrowserHostAdapter.readHostFromSnapshot(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                root = chromeRootWithNonChromeAddressNode,
            ),
        )
    }

    private fun nestedChromeToolbarNode(depth: Int, leaf: BrowserNodeSnapshot): BrowserNodeSnapshot {
        return if (depth <= 0) {
            leaf
        } else {
            BrowserNodeSnapshot(
                packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                viewIdResourceName = "com.android.chrome:id/container_$depth",
                children = listOf(nestedChromeToolbarNode(depth - 1, leaf)),
            )
        }
    }
}
