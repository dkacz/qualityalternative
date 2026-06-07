package com.qualityalternative.app.interception

import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.DistractingApp

object InterceptionTargetResolver {
    fun resolve(
        foregroundPackage: String,
        foregroundClass: String?,
        selectedPackages: Set<String>,
        knownTargets: List<DistractingApp> = emptyList(),
        appPackage: String,
    ): DistractingApp? {
        val targetByPackage = knownTargets.associateBy(DistractingApp::packageName)
        return when {
            foregroundPackage == appPackage -> {
                FixtureTargetRegistry.findByComponent(foregroundClass)
                    ?.takeIf { it.packageName in selectedPackages }
            }

            foregroundPackage in selectedPackages -> {
                targetByPackage[foregroundPackage]
                    ?: SupportedCatalog.findByPackage(foregroundPackage)
            }

            else -> null
        }
    }
}
