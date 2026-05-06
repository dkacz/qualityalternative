package com.qualityalternative.app.domain.model

data class ReadingProgress(
    val contentId: String,
    val progressPercent: Int,
    val lastVisibleParagraphIndex: Int,
    val paragraphCount: Int,
    val updatedAtMillis: Long,
    val completedAtMillis: Long? = null,
    val lastVisibleTextOffset: Int = 0,
) {
    fun isUnfinished(): Boolean {
        return completedAtMillis == null && progressPercent in 1..99
    }

    fun isCompleted(): Boolean = completedAtMillis != null
}
