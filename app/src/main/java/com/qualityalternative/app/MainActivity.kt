package com.qualityalternative.app

import android.content.Context
import android.content.Intent
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
            QualityAlternativeApp(
                viewModel = mainViewModel,
                onExitToTarget = ::finish,
            )
        }

        handleLaunchIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPermissionReadiness()
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.action != ACTION_SYSTEM_INTERVENTION) {
            return
        }
        val targetAppPackage = intent.getStringExtra(EXTRA_TARGET_APP_PACKAGE) ?: return
        mainViewModel.requestSystemInterception(targetAppPackage = targetAppPackage)
    }

    companion object {
        private const val ACTION_SYSTEM_INTERVENTION =
            "com.qualityalternative.app.action.SYSTEM_INTERVENTION"
        private const val EXTRA_TARGET_APP_PACKAGE = "extra_target_app_package"

        fun createSystemInterceptionIntent(
            context: Context,
            targetAppPackage: String,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = ACTION_SYSTEM_INTERVENTION
                putExtra(EXTRA_TARGET_APP_PACKAGE, targetAppPackage)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            }
        }
    }
}
