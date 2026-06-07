package com.qualityalternative.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.qualityalternative.app.domain.model.CustomTargetAppCandidate
import com.qualityalternative.app.domain.model.CustomTargetAppEligibility
import com.qualityalternative.app.domain.model.DistractingApp

class AndroidInstalledAppTargetCatalog(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    private val candidates: List<CustomTargetAppCandidate> by lazy {
        buildCandidates()
    }

    fun candidates(): List<CustomTargetAppCandidate> = candidates

    private fun buildCandidates(): List<CustomTargetAppCandidate> {
        val homePackages = launcherPackages()
        return launchableActivities()
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                val label = info.loadLabel(packageManager)?.toString()?.trim()?.takeIf(String::isNotBlank)
                    ?: packageName
                val app = DistractingApp(packageName = packageName, displayName = label)
                CustomTargetAppCandidate(
                    app = app,
                    eligibility = eligibilityFor(packageName = packageName, homePackages = homePackages),
                    exclusionReason = exclusionReasonFor(packageName = packageName, homePackages = homePackages),
                )
            }
            .distinctBy { candidate -> candidate.app.packageName }
            .filterNot { candidate -> SupportedCatalog.distractingApps.any { it.packageName == candidate.app.packageName } }
            .sortedWith(
                compareBy<CustomTargetAppCandidate> { !it.isEligible }
                    .thenBy { it.app.displayName.lowercase() }
                    .thenBy { it.app.packageName },
            )
    }

    private fun launchableActivities(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
    }

    private fun launcherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val launchers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return launchers.mapNotNullTo(mutableSetOf()) { info ->
            info.activityInfo?.packageName?.takeIf(String::isNotBlank)
        }
    }

    private fun eligibilityFor(packageName: String, homePackages: Set<String>): CustomTargetAppEligibility {
        return InstalledAppTargetEligibilityPolicy.eligibilityFor(
            packageName = packageName,
            appPackageName = appContext.packageName,
            homePackages = homePackages,
        )
    }

    private fun exclusionReasonFor(packageName: String, homePackages: Set<String>): String? {
        return when (eligibilityFor(packageName = packageName, homePackages = homePackages)) {
            CustomTargetAppEligibility.ELIGIBLE -> null
            CustomTargetAppEligibility.EXCLUDED_SELF -> "Quality Alternative cannot interrupt itself."
            CustomTargetAppEligibility.EXCLUDED_LAUNCHER -> "Launcher and Home apps stay available for safety."
            CustomTargetAppEligibility.EXCLUDED_SETTINGS_OR_PERMISSION ->
                "Settings and permission screens stay available so setup cannot trap you."
            CustomTargetAppEligibility.EXCLUDED_PHONE_OR_EMERGENCY ->
                "Phone, emergency, and safety flows are never interruption targets."
            CustomTargetAppEligibility.EXCLUDED_INSTALLER ->
                "Installers and app stores stay available during install or update flows."
            CustomTargetAppEligibility.EXCLUDED_DOCUMENTS_OR_FILE_PICKER ->
                "Files and document pickers stay available for imports, exports, and profile setup."
            CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL ->
                "System-critical packages are excluded from custom targets."
            CustomTargetAppEligibility.EXCLUDED_NOT_LAUNCHABLE ->
                "Only launchable apps can be selected."
        }
    }
}
