package net.milosvasic.factory.configuration

import net.milosvasic.factory.os.OSType

data class SoftwareConfigurationItem(

    val name: String,
    val version: String,
    private val installationSteps: Map<String, List<InstallationStepDefinition>>
) {

    @Throws(IllegalArgumentException::class)
    fun getInstallationSteps(osName: String): InstallationSteps {

        val os = OSType.getByValue(osName)
        installationSteps[os.osName]?.let {
            return InstallationSteps(os, it)
        }
        os.getFallback().forEach {

            installationSteps[it.osName]?.let { items ->
                return InstallationSteps(it, items)
            }
        }
        return InstallationSteps(OSType.UNKNOWN, listOf())
    }

    fun hasInstallationSteps(forWhat: String): Boolean {

        val os = OSType.getByValue(forWhat)
        installationSteps[os.osName]?.let {
            return true
        }
        os.getFallback().forEach {

            installationSteps[it.osName]?.let {
                return true
            }
        }
        return false
    }
}