package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.Configuration
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.SoftwareConfigurationType
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.log
import net.milosvasic.factory.os.OperatingSystem
import java.io.File
import java.util.concurrent.LinkedBlockingQueue

class FilesystemDefinitionProvider(configuration: Configuration, operatingSystem: OperatingSystem) :
        DefinitionProvider(configuration, operatingSystem) {

    private var configurations = mutableMapOf<SoftwareConfigurationType, MutableList<SoftwareConfiguration>>()

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun load(definition: Definition):
            MutableMap<SoftwareConfigurationType, MutableList<SoftwareConfiguration>> {

        val definitionHome = definition.getHome()
        if (definitionHome.exists()) {

            log.w("Definition found: $definition")
            val type = SoftwareConfigurationType.STACKS
            val items = configuration.getConfigurationMap()[type]
            items?.let {

                findDefinitions(type, definitionHome, it)
                it.forEach { item ->
                    val os = operatingSystem.getType().osName
                    val configurationPath = Configuration.getConfigurationFilePath(item)
                    val obtainedConfiguration = SoftwareConfiguration.obtain(configurationPath, os)
                    if (obtainedConfiguration.isEnabled()) {

                        val variables = obtainedConfiguration.variables
                        configuration.mergeVariables(variables)

                        val configurationItems = getConfigurationItems(type)
                        configurationItems.add(obtainedConfiguration)

                        log.i("${type.label} definition file: $item")
                        obtainedConfiguration.definition?.let { definition ->

                            log.i("${type.label} definition: $definition")
                        }
                    } else {

                        log.w("Disabled ${type.label.toLowerCase()} configuration: $configurationPath")
                    }
                }
            }
        } else {

            throw IllegalArgumentException("Definition not found: $definition")
        }
        return configurations
    }

    private fun getConfigurationItems(type: SoftwareConfigurationType): MutableList<SoftwareConfiguration> {

        var configurationItems = configurations[type]
        if (configurationItems == null) {
            configurationItems = mutableListOf()
            configurations[type] = configurationItems
        }
        return configurationItems
    }

    private fun findDefinitions(
            type: SoftwareConfigurationType,
            directory: File,
            collection: LinkedBlockingQueue<String>
    ) {

        log.v("Searching for definitions: ${directory.absolutePath}")
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {

                findDefinitions(type, file, collection)
            } else {
                if (file.name == Configuration.DEFAULT_CONFIGURATION_FILE) {

                    val definition = file.absolutePath
                    collection.add(definition)
                }
            }
        }
    }
}