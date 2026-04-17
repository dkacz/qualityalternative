package com.qualityalternative.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qualityalternative.app.ui.MainViewModel
import com.qualityalternative.app.ui.MainViewModelFactory
import com.qualityalternative.app.ui.QualityAlternativeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val application = application as QualityAlternativeApplication

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(application.appContainer),
            )
            QualityAlternativeApp(viewModel = viewModel)
        }
    }
}
