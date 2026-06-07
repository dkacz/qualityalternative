package com.qualityalternative.app.interception

object AccessibilityInterceptionPlanner {
    fun shouldEvaluateAppTargetAfterWebsiteResult(result: InterceptionProcessingResult): Boolean {
        return result == InterceptionProcessingResult.Suppressed
    }
}

enum class InterceptionProcessingResult {
    Handled,
    Suppressed,
    Duplicate,
    NotReady,
}
