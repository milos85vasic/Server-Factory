package net.milosvasic.factory.configuration

import net.milosvasic.factory.platform.Platform

data class SoftwareConfigurationItem(

    val name: String,
    val version: String,
    private val installationSteps: Map<String, List<InstallationStepDefinition>>
) {

    @Throws(IllegalArgumentException::class)
    fun getInstallationSteps(platformName: String): InstallationSteps {

        val platform = Platform.getByValue(platformName)
        installationSteps[platform.platformName]?.let {
            return InstallationSteps(platform, it)
        }
        platform.getFallback().forEach {

            installationSteps[it.platformName]?.let { items ->
                return InstallationSteps(it, items)
            }
        }
        return InstallationSteps(Platform.UNKNOWN, listOf())
    }

    fun hasInstallationSteps(forWhat: String): Boolean {

        val platform = Platform.getByValue(forWhat)
        installationSteps[platform.platformName]?.let {
            return true
        }
        platform.getFallback().forEach {

            installationSteps[it.platformName]?.let {
                return true
            }
        }
        return false
    }
}