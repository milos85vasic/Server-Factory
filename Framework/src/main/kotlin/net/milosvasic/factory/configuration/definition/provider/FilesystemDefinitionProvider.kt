package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.Configuration
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.log
import net.milosvasic.factory.os.OperatingSystem
import java.io.File

class FilesystemDefinitionProvider(configuration: Configuration, operatingSystem: OperatingSystem) :
        DefinitionProvider(configuration, operatingSystem) {

    private var configurations = mutableListOf<SoftwareConfiguration>()

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun load(definition: Definition): MutableList<SoftwareConfiguration> {

        val definitionHome = definition.getHome()
        if (definitionHome.exists()) {

            log.i("Definition dependency found: $definition")
            val items = mutableListOf<String>()
            findDefinitions(definitionHome, items)
            items.forEach { item ->
                val os = operatingSystem.getType().osName
                val configurationPath = Configuration.getConfigurationFilePath(item)
                val obtainedConfiguration = SoftwareConfiguration.obtain(configurationPath, os)
                if (obtainedConfiguration.isEnabled()) {

                    val variables = obtainedConfiguration.variables
                    configuration.mergeVariables(variables)
                    configurations.add(obtainedConfiguration)
                } else {

                    log.w("Disabled configuration: $configurationPath")
                }
            }
        } else {

            throw IllegalArgumentException("Definition not found: $definition")
        }
        return configurations
    }

    private fun findDefinitions(

            directory: File,
            collection: MutableList<String>
    ) {

        log.v("Searching for definitions: ${directory.absolutePath}")
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {

                findDefinitions(file, collection)
            } else {
                if (file.name == Configuration.DEFAULT_CONFIGURATION_FILE) {

                    val definition = file.absolutePath
                    collection.add(definition)
                }
            }
        }
    }
}