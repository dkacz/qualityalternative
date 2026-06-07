package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.CustomTargetAppEligibility

object InstalledAppTargetEligibilityPolicy {
    fun eligibilityFor(
        packageName: String,
        appPackageName: String,
        homePackages: Set<String>,
    ): CustomTargetAppEligibility {
        return when {
            packageName == appPackageName -> CustomTargetAppEligibility.EXCLUDED_SELF
            packageName in homePackages -> CustomTargetAppEligibility.EXCLUDED_LAUNCHER
            packageName in SettingsOrPermissionPackages -> CustomTargetAppEligibility.EXCLUDED_SETTINGS_OR_PERMISSION
            packageName in PhoneOrEmergencyPackages -> CustomTargetAppEligibility.EXCLUDED_PHONE_OR_EMERGENCY
            packageName in InstallerPackages -> CustomTargetAppEligibility.EXCLUDED_INSTALLER
            packageName in DocumentsOrFilePickerPackages -> CustomTargetAppEligibility.EXCLUDED_DOCUMENTS_OR_FILE_PICKER
            packageName in SystemCriticalPackages -> CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL
            packageName.startsWith("com.android.systemui") -> CustomTargetAppEligibility.EXCLUDED_SYSTEM_CRITICAL
            else -> CustomTargetAppEligibility.ELIGIBLE
        }
    }

    val SettingsOrPermissionPackages = setOf(
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.samsung.android.permissioncontroller",
    )

    val PhoneOrEmergencyPackages = setOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.google.android.apps.safetycenter",
        "com.android.emergency",
    )

    val InstallerPackages = setOf(
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.google.android.apps.packageinstaller",
        "com.android.vending",
    )

    val DocumentsOrFilePickerPackages = setOf(
        "com.android.documentsui",
        "com.google.android.documentsui",
        "com.google.android.apps.docs",
    )

    val SystemCriticalPackages = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.gms",
        "com.google.android.gsf",
    )
}
