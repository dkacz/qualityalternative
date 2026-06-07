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
import java.util.UUID

class MainActivity : ComponentActivity() {
    internal val mainViewModel: MainViewModel by viewModels {
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
        // MainActivity is the exported launcher, so any app can send it an explicit intent with this
        // action. The token is generated per process and only our own AccessibilityService (same
        // process) can read it, so a forged external intent is rejected before triggering an
        // intervention with an attacker-chosen package.
        if (intent.getStringExtra(EXTRA_LAUNCH_TOKEN) != SYSTEM_INTERVENTION_TOKEN) {
            return
        }
        val targetAppPackage = intent.getStringExtra(EXTRA_TARGET_APP_PACKAGE) ?: return
        val triggeredAtMillis = intent.getLongExtra(
            EXTRA_TRIGGERED_AT_MILLIS,
            System.currentTimeMillis(),
        )
        when (intent.getStringExtra(EXTRA_TARGET_KIND)) {
            TARGET_KIND_WEBSITE -> mainViewModel.requestSystemWebsiteInterception(
                browserPackage = targetAppPackage,
                browserDisplayName = intent.getStringExtra(EXTRA_BROWSER_DISPLAY_NAME) ?: "Browser",
                websiteRuleType = intent.getStringExtra(EXTRA_WEBSITE_RULE_TYPE) ?: "",
                websiteRuleIncludesApex = intent.getBooleanExtra(EXTRA_WEBSITE_RULE_INCLUDES_APEX, false),
                nowMillis = triggeredAtMillis,
            )

            else -> mainViewModel.requestSystemInterception(
                targetAppPackage = targetAppPackage,
                nowMillis = triggeredAtMillis,
            )
        }
    }

    companion object {
        private const val ACTION_SYSTEM_INTERVENTION =
            "com.qualityalternative.app.action.SYSTEM_INTERVENTION"
        private const val EXTRA_TARGET_KIND = "extra_target_kind"
        private const val EXTRA_TARGET_APP_PACKAGE = "extra_target_app_package"
        private const val EXTRA_BROWSER_DISPLAY_NAME = "extra_browser_display_name"
        private const val EXTRA_WEBSITE_RULE_TYPE = "extra_website_rule_type"
        private const val EXTRA_WEBSITE_RULE_INCLUDES_APEX = "extra_website_rule_includes_apex"
        private const val EXTRA_TRIGGERED_AT_MILLIS = "extra_triggered_at_millis"
        private const val EXTRA_LAUNCH_TOKEN = "extra_launch_token"
        private const val TARGET_KIND_APP = "app"
        private const val TARGET_KIND_WEBSITE = "website"

        // Per-process secret shared only between in-process components (the AccessibilityService that
        // builds the intent and this activity that consumes it). Not derivable by another app.
        private val SYSTEM_INTERVENTION_TOKEN = UUID.randomUUID().toString()

        fun createSystemInterceptionIntent(
            context: Context,
            targetAppPackage: String,
            triggeredAtMillis: Long = System.currentTimeMillis(),
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = ACTION_SYSTEM_INTERVENTION
                putExtra(EXTRA_TARGET_KIND, TARGET_KIND_APP)
                putExtra(EXTRA_TARGET_APP_PACKAGE, targetAppPackage)
                putExtra(EXTRA_TRIGGERED_AT_MILLIS, triggeredAtMillis)
                putExtra(EXTRA_LAUNCH_TOKEN, SYSTEM_INTERVENTION_TOKEN)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            }
        }

        fun createWebsiteInterceptionIntent(
            context: Context,
            browserPackage: String,
            browserDisplayName: String,
            websiteRuleType: String,
            websiteRuleIncludesApex: Boolean,
            triggeredAtMillis: Long = System.currentTimeMillis(),
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = ACTION_SYSTEM_INTERVENTION
                putExtra(EXTRA_TARGET_KIND, TARGET_KIND_WEBSITE)
                putExtra(EXTRA_TARGET_APP_PACKAGE, browserPackage)
                putExtra(EXTRA_BROWSER_DISPLAY_NAME, browserDisplayName)
                putExtra(EXTRA_WEBSITE_RULE_TYPE, websiteRuleType)
                putExtra(EXTRA_WEBSITE_RULE_INCLUDES_APEX, websiteRuleIncludesApex)
                putExtra(EXTRA_TRIGGERED_AT_MILLIS, triggeredAtMillis)
                putExtra(EXTRA_LAUNCH_TOKEN, SYSTEM_INTERVENTION_TOKEN)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            }
        }
    }
}
