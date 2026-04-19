package com.qualityalternative.app.interception

object InterceptionRuntimeGate {
    private val suppressedPackages = mutableMapOf<String, Long>()

    @Synchronized
    fun shouldSuppress(targetAppPackage: String, nowMillis: Long): Boolean {
        suppressedPackages.entries.removeAll { (_, untilMillis) -> nowMillis >= untilMillis }
        val suppressedUntil = suppressedPackages[targetAppPackage] ?: return false
        return nowMillis < suppressedUntil
    }

    @Synchronized
    fun suppressPackage(targetAppPackage: String, untilMillis: Long) {
        suppressedPackages[targetAppPackage] = untilMillis
    }

    @Synchronized
    fun clearAll() {
        suppressedPackages.clear()
    }
}
