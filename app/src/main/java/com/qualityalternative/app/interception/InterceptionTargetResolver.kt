package com.qualityalternative.app.interception

import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.DistractingApp

object InterceptionTargetResolver {
    fun resolve(
        foregroundPackage: String,
        foregroundClass: String?,
        selectedPackages: Set<String>,
        appPackage: String,
    ): DistractingApp? {
        return when {
            foregroundPackage == appPackage -> {
                FixtureTargetRegistry.findByComponent(foregroundClass)
                    ?.takeIf { it.packageName in selectedPackages }
            }

            foregroundPackage in selectedPackages -> {
                SupportedCatalog.findByPackage(foregroundPackage)
            }

            else -> null
        }
    }
}
