package com.qualityalternative.app.interception

class ForegroundAppDetectionPolicy(
    private val duplicateSuppressionMillis: Long = DEFAULT_DUPLICATE_SUPPRESSION_MILLIS,
) {
    private var lastSeenPackage: String? = null
    private var lastSeenAtMillis: Long = 0L

    fun shouldLog(
        packageName: String,
        selectedPackages: Set<String>,
        nowMillis: Long,
    ): Boolean {
        val isDuplicate = lastSeenPackage == packageName &&
            nowMillis - lastSeenAtMillis < duplicateSuppressionMillis

        lastSeenPackage = packageName
        lastSeenAtMillis = nowMillis

        return packageName in selectedPackages && !isDuplicate
    }

    private companion object {
        const val DEFAULT_DUPLICATE_SUPPRESSION_MILLIS = 1_500L
    }
}
