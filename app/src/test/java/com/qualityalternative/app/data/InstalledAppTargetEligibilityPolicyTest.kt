package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.CustomTargetAppEligibility
import org.junit.Assert.assertEquals
import org.junit.Test

class InstalledAppTargetEligibilityPolicyTest {
    private val appPackageName = "com.qualityalternative.app"
    private val homePackages = setOf("com.android.launcher3")

    @Test
    fun eligibilityFor_excludesSafetyCriticalPackageFamilies() {
        val cases = mapOf(
            appPackageName to CustomTargetAppEligibility.EXCLUDED_SELF,
            "com.android.launcher3" to CustomTargetAppEligibility.EXCLUDED_LAUNCHER,
            "com.android.settings" to CustomTargetAppEligibility.EXCLUDED_SETTINGS_OR_PERMISSION,
            "com.google.android.permissioncontroller" to CustomTargetAppEligibility.EXCLUDED_SETTINGS_OR_PERMISSION,
            "com.android.phone" to CustomTargetAppEligibility.EXCLUDED_PHONE_OR_EMERGENCY,
            "com.google.android.dialer" to CustomTargetAppEligibility.EXCLUDED_PHONE_OR_EMERGENCY,
            "com.android.packageinstaller" to CustomTargetAppEligibility.EXCLUDED_INSTALLER,
            "com.android.vending" to CustomTargetAppEligibility.EXCLUDED_INSTALLER,
            "com.android.documentsui" to CustomTargetAppEligibility.EXCLUDED_DOCUMENTS_OR_FILE_PICKER,
            "com.google.android.documentsui" to CustomTargetAppEligibility.EXCLUDED_DOCUMENTS_OR_FILE_PICKER,
            "com.google.android.apps.docs" to CustomTargetAppEligibility.EXCLUDED_DOCUMENTS_OR_FILE_PICKER,
            "android" to CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL,
            "com.android.systemui" to CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL,
            "com.android.systemui.plugin" to CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL,
            "com.google.android.gms" to CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL,
        )

        cases.forEach { (packageName, expected) ->
            assertEquals(
                packageName,
                expected,
                InstalledAppTargetEligibilityPolicy.eligibilityFor(
                    packageName = packageName,
                    appPackageName = appPackageName,
                    homePackages = homePackages,
                ),
            )
        }
    }

    @Test
    fun eligibilityFor_allowsOrdinaryLaunchablePackage() {
        assertEquals(
            CustomTargetAppEligibility.ELIGIBLE,
            InstalledAppTargetEligibilityPolicy.eligibilityFor(
                packageName = "com.example.deepwork",
                appPackageName = appPackageName,
                homePackages = homePackages,
            ),
        )
    }

    @Test
    fun eligibilityFor_excludesKnownOemSafetyAppsWithoutBlanketOemBlock() {
        val cases = mapOf(
            "com.samsung.android.permissioncontroller" to CustomTargetAppEligibility.EXCLUDED_SETTINGS_OR_PERMISSION,
            "com.samsung.android.dialer" to CustomTargetAppEligibility.EXCLUDED_PHONE_OR_EMERGENCY,
            "com.samsung.android.calendar" to CustomTargetAppEligibility.ELIGIBLE,
        )

        cases.forEach { (packageName, expected) ->
            assertEquals(
                packageName,
                expected,
                InstalledAppTargetEligibilityPolicy.eligibilityFor(
                    packageName = packageName,
                    appPackageName = appPackageName,
                    homePackages = homePackages,
                ),
            )
        }
    }
}
