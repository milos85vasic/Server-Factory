package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.busy.Busy
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.common.initialization.Initialization
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.log
import java.io.File
import java.util.concurrent.LinkedBlockingQueue

object ConfigurationManager : Initialization {

    private val busy = Busy()
    private var configurationPath = String.EMPTY
    private var configuration: Configuration? = null
    private var configurationFactory: ConfigurationFactory<*>? = null
    private val softwareConfigurations = mutableListOf<SoftwareConfiguration>()
    private val containersConfigurations = mutableListOf<SoftwareConfiguration>()

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun initialize() {
        checkInitialized()
        BusyWorker.busy(busy)
        val file = File(configurationPath)
        if (configurationFactory == null) {

            throw IllegalStateException("Configuration factory was not provided")
        }
        configuration = configurationFactory?.obtain(file)
        configuration?.let { config ->
            config.enabled?.let { enabled ->
                if (!enabled) {

                    throw IllegalStateException("Configuration is not enabled")
                }
            }
            config.software?.let {

                val path = FilePathBuilder()
                        .addContext("Definitions")
                        .addContext("Software")
                        .getPath()

                val directory = File(path)
                findDefinitions(directory, it)
            }
            config.containers?.let {

                val path = FilePathBuilder()
                        .addContext("Definitions")
                        .addContext("Containers")
                        .getPath()

                val directory = File(path)
                findDefinitions(directory, it)
            }
            config.software?.forEach {
                val path = Configuration.getConfigurationFilePath(it)
                val softwareConfiguration = SoftwareConfiguration.obtain(path)
                if (softwareConfiguration.enabled) {

                    val variables = softwareConfiguration.variables
                    config.mergeVariables(variables)
                    softwareConfigurations.add(softwareConfiguration)
                } else {

                    log.w("Disabled software configuration: $path")
                }
            }
            config.containers?.forEach {
                val path = Configuration.getConfigurationFilePath(it)
                val containerConfiguration = SoftwareConfiguration.obtain(path)
                if (containerConfiguration.enabled) {

                    val variables = containerConfiguration.variables
                    config.mergeVariables(variables)
                    containersConfigurations.add(containerConfiguration)
                } else {

                    log.w("Disabled container configuration: $path")
                }
            }
            printVariableNode(config.variables)
        }
        if (configuration == null) {

            throw IllegalStateException("Configuration was not initialised")
        }
        BusyWorker.free(busy)
    }

    @Throws(IllegalStateException::class)
    fun getConfiguration(): Configuration {
        checkNotInitialized()
        configuration?.let {
            return it
        }
        throw IllegalStateException("No configuration available")
    }

    fun getSoftwareConfiguration() = softwareConfigurations

    fun getContainerConfiguration() = containersConfigurations

    @Synchronized
    override fun isInitialized(): Boolean {
        return configuration != null
    }

    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setConfigurationPath(path: String) {
        checkInitialized()
        val validator = ConfigurationPathValidator()
        if (validator.validate(path)) {
            configurationPath = path
        }
    }

    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setConfigurationFactory(factory: ConfigurationFactory<*>) {
        checkInitialized()
        configurationFactory = factory
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkInitialized() {
        if (isInitialized()) {
            throw IllegalStateException("Configuration manager has been already initialized")
        }
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkNotInitialized() {
        if (!isInitialized()) {
            throw IllegalStateException("Configuration manager has not been initialized")
        }
    }

    private fun printVariableNode(variableNode: Node?, prefix: String = String.EMPTY) {
        val prefixEnd = "-> "
        variableNode?.let { node ->
            if (node.value != String.EMPTY) {
                val printablePrefix = if (prefix != String.EMPTY) {
                    " $prefix $prefixEnd"
                } else {
                    " "
                }
                node.value.let { value ->
                    val nodeValue = Variable.parse(value)
                    node.name.let { name ->
                        if (name != String.EMPTY) {
                            log.v("Configuration variable:$printablePrefix$name -> $nodeValue")
                        }
                    }
                }
            }
            node.children.forEach { child ->
                var nextPrefix = prefix
                if (nextPrefix != String.EMPTY && !nextPrefix.endsWith(prefixEnd)) {
                    nextPrefix += " $prefixEnd"
                }
                nextPrefix += node.name
                printVariableNode(child, nextPrefix)
            }
        }
    }

    private fun findDefinitions(directory: File, collection: LinkedBlockingQueue<String>) {

        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {

                findDefinitions(file, collection)
            } else {
                if (file.name == Configuration.DEFAULT_CONFIGURATION_FILE) {

                    val definition = file.absolutePath
                    log.v("Software definition found: $definition")
                    collection.add(definition)
                }
            }
        }
    }
}