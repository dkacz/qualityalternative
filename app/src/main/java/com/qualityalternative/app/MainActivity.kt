package com.qualityalternative.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qualityalternative.app.ui.MainViewModel
import com.qualityalternative.app.ui.MainViewModelFactory
import com.qualityalternative.app.ui.QualityAlternativeApp

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as QualityAlternativeApplication).appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QualityAlternativeApp(viewModel = mainViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPermissionReadiness()
    }
}
