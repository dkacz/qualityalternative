package com.qualityalternative.app.domain.model

object AnalyticsSemanticKeys {
    fun delaySelected(delayId: String): String = "delay:selected:$delayId"

    fun delayEnded(delayId: String): String = "delay:ended:$delayId"

    fun delayReturn(delayId: String, origin: String, type: AnalyticsEventType): String {
        return "delay:return:$delayId:$origin:${type.name}"
    }

    fun sessionReturn(sessionId: String, type: AnalyticsEventType): String {
        return "session:return:$sessionId:${type.name}"
    }
}
