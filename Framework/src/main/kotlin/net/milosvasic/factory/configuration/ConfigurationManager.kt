package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.busy.Busy
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.common.initialization.Initialization
import net.milosvasic.factory.configuration.recipe.ConfigurationRecipe
import net.milosvasic.factory.configuration.recipe.FileConfigurationRecipe
import net.milosvasic.factory.configuration.recipe.RawJsonConfigurationRecipe
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.log
import net.milosvasic.factory.validation.JsonValidator
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

object ConfigurationManager : Initialization {

    private const val DIRECTORY_DEFINITIONS = "Definitions"

    private val busy = Busy()
    private var loaded = AtomicBoolean()
    private val loading = AtomicBoolean()
    private var configuration: Configuration? = null
    private var recipe: ConfigurationRecipe<*>? = null
    private var configurationFactory: ConfigurationFactory<*>? = null
    private var configurations = mutableMapOf<SoftwareConfigurationType, MutableList<SoftwareConfiguration>>()

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun initialize() {

        checkInitialized()
        BusyWorker.busy(busy)
        if (configurationFactory == null) {

            throw IllegalStateException("Configuration factory was not provided")
        }
        if (recipe == null) {

            throw IllegalStateException("Configuration recipe was not provided")
        }
        recipe?.let { rcp ->

            configuration = configurationFactory?.obtain(rcp)
            nullConfigurationCheck()
            BusyWorker.free(busy)
        }
    }

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun load() {

        checkNotInitialized()
        nullConfigurationCheck()
        if (loaded.get()) {

            throw IllegalStateException("Configuration is already loaded")
        }
        if (loading.get()) {
            return
        }
        loading.set(true)
        configuration?.let { config ->
            config.enabled?.let { enabled ->
                if (!enabled) {

                    throw IllegalStateException("Configuration is not enabled")
                }
            }
            config.getConfigurationMap().forEach { (type, items) ->
                items?.let {

                    val path = FilePathBuilder()
                            .addContext(DIRECTORY_DEFINITIONS)
                            .addContext(type.label)
                            .getPath()

                    val home = System.getProperty("user.home")
                    val homePath = FilePathBuilder()
                            .addContext(home)
                            .addContext(path)
                            .getPath()

                    var directory = File(path)
                    if (directory.absolutePath == homePath) {
                        directory = File(directory.absolutePath.replace(home, "/usr/local/bin"))
                    }
                    findDefinitions(type, directory, it)

                    it.forEach { item ->
                        val configurationPath = Configuration.getConfigurationFilePath(item)
                        val obtainedConfiguration = SoftwareConfiguration.obtain(configurationPath)
                        if (obtainedConfiguration.isEnabled()) {

                            val variables = obtainedConfiguration.variables
                            config.mergeVariables(variables)

                            val configurationItems = getConfigurationItems(type)
                            configurationItems.add(obtainedConfiguration)
                        } else {

                            log.w("Disabled ${type.label.toLowerCase()} configuration: $configurationPath")
                        }
                    }
                }
            }

            printVariableNode(config.variables)
        }
        loaded.set(true)
        loading.set(false)
    }

    @Throws(IllegalStateException::class)
    fun getConfiguration(): Configuration {

        checkNotInitialized()
        configuration?.let {
            return it
        }
        throw IllegalStateException("No configuration available")
    }

    @Synchronized
    override fun isInitialized(): Boolean {

        return configuration != null
    }

    fun isLoaded() = loaded.get()

    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setConfigurationRecipe(recipe: ConfigurationRecipe<*>) {

        checkInitialized()
        notLoadConfigurationCheck()
        when (recipe) {
            is FileConfigurationRecipe -> {

                val path = recipe.data.absolutePath
                val validator = ConfigurationPathValidator()
                validator.validate(path)
            }
            is RawJsonConfigurationRecipe -> {

                val json = recipe.data
                val validator = JsonValidator()
                validator.validate(json)
            }
            else -> {

                throw IllegalArgumentException("Unsupported recipe type: ${recipe::class.simpleName}")
            }
        }

        this.recipe = recipe
    }

    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setConfigurationFactory(factory: ConfigurationFactory<*>) {

        checkInitialized()
        notLoadConfigurationCheck()
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

    fun getConfigurationItems(type: SoftwareConfigurationType): MutableList<SoftwareConfiguration> {

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
                    log.i("${type.label} definition found: $definition")
                    collection.add(definition)
                }
            }
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
                            log.d("Configuration variable:$printablePrefix$name -> $nodeValue")
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

    @Throws(IllegalStateException::class)
    private fun nullConfigurationCheck() {

        if (configuration == null) {
            throw IllegalStateException("Configuration was not initialised")
        }
    }

    @Throws(IllegalStateException::class)
    private fun loadConfigurationCheck() {

        if (!loaded.get()) {
            throw IllegalStateException("Configuration has not been loaded")
        }
    }

    @Throws(IllegalStateException::class)
    private fun notLoadConfigurationCheck() {

        if (loaded.get()) {
            throw IllegalStateException("Configuration has been already loaded")
        }
    }
}