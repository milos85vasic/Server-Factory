package net.milosvasic.factory.configuration

import net.milosvasic.factory.os.OSType

data class SoftwareConfigurationItem(

    val name: String,
    val version: String,
    private val installationSteps: Map<String, List<InstallationStepDefinition>>
) {

    @Throws(IllegalArgumentException::class)
    fun getInstallationSteps(osName: String): List<InstallationStepDefinition> {

        val os = OSType.getByValue(osName)
        installationSteps[os.osName]?.let {
            return it
        }
        os.fallback.forEach {

            installationSteps[it.osName]?.let { items ->
                return items
            }
        }
        return listOf()
    }

    fun hasInstallationSteps(forWhat: String) = installationSteps.containsKey(forWhat)
}