package com.qualityalternative.app.fixture

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qualityalternative.app.MainActivity
import com.qualityalternative.app.interception.FixtureTargetRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FixtureDistractorOneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Fixture Feed One",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Internal distractor fixture for interception automation tests.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        triggerInterceptionWhenRequested()
    }

    private fun triggerInterceptionWhenRequested() {
        if (!intent.getBooleanExtra(EXTRA_TRIGGER_INTERCEPTION, false)) {
            return
        }
        lifecycleScope.launch {
            delay(AUTOMATION_TRIGGER_DELAY_MILLIS)
            startActivity(
                MainActivity.createSystemInterceptionIntent(
                    context = this@FixtureDistractorOneActivity,
                    targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
    }

    companion object {
        const val EXTRA_TRIGGER_INTERCEPTION = "trigger_interception"
        private const val AUTOMATION_TRIGGER_DELAY_MILLIS = 250L
    }
}
