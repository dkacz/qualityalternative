package com.qualityalternative.app.interception

object InterceptionRuntimeGate {
    private val suppressedPackages = mutableMapOf<String, SuppressionWindow>()

    @Synchronized
    fun shouldSuppress(
        targetAppPackage: String,
        nowMillis: Long,
        bedtimeActive: Boolean = false,
        targetKey: String = targetAppPackage,
    ): Boolean {
        suppressedPackages.entries.removeAll { (_, window) -> nowMillis >= window.untilMillis }
        val suppression = suppressedPackages[targetKey] ?: return false
        if (bedtimeActive && !suppression.allowedDuringBedtime) return false
        return nowMillis < suppression.untilMillis
    }

    @Synchronized
    fun suppressPackage(
        targetAppPackage: String,
        untilMillis: Long,
        allowedDuringBedtime: Boolean = false,
        targetKey: String = targetAppPackage,
    ) {
        suppressedPackages[targetKey] = SuppressionWindow(
            untilMillis = untilMillis,
            allowedDuringBedtime = allowedDuringBedtime,
        )
    }

    @Synchronized
    fun clearAll() {
        suppressedPackages.clear()
    }
}

private data class SuppressionWindow(
    val untilMillis: Long,
    val allowedDuringBedtime: Boolean,
)
