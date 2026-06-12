package com.qualityalternative.app.interception

import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.DistractingApp

object InterceptionTargetResolver {
    fun resolve(
        foregroundPackage: String,
        foregroundClass: String?,
        selectedPackages: Set<String>,
        knownTargets: List<DistractingApp> = emptyList(),
        appPackage: String,
        enableFixtureTargets: Boolean = BuildConfig.DEBUG,
    ): DistractingApp? {
        val targetByPackage = knownTargets.associateBy(DistractingApp::packageName)
        return when {
            foregroundPackage == appPackage -> {
                FixtureTargetRegistry.findByComponent(foregroundClass, enabled = enableFixtureTargets)
                    ?.takeIf { it.packageName in selectedPackages }
            }

            foregroundPackage in selectedPackages -> {
                targetByPackage[foregroundPackage]
                    ?: SupportedCatalog.findByPackage(
                        packageName = foregroundPackage,
                        includeFixtures = enableFixtureTargets,
                    )
            }

            else -> null
        }
    }
}
