package com.qualityalternative.app

import android.app.Application
import com.qualityalternative.app.data.AppContainer

class QualityAlternativeApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}
