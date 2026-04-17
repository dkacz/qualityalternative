package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.InterceptionMonitor

class StubInterceptionMonitor : InterceptionMonitor {
    override fun isAvailable(): Boolean = false
}
