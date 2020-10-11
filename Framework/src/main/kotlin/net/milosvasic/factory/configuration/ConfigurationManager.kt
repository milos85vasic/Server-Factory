package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.busy.Busy
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.common.initialization.Initialization
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.configuration.group.Group
import net.milosvasic.factory.configuration.group.GroupValidator
import net.milosvasic.factory.configuration.recipe.ConfigurationRecipe
import net.milosvasic.factory.configuration.recipe.FileConfigurationRecipe
import net.milosvasic.factory.configuration.recipe.RawJsonConfigurationRecipe
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.log
import net.milosvasic.factory.os.OperatingSystem
import net.milosvasic.factory.validation.JsonValidator
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

object ConfigurationManager : Initialization {

    private const val DIRECTORY_DEFINITIONS = Definition.DIRECTORY_ROOT

    // TODO: MSF-284 - Make sure that this is default value for the installation location that will be provided
    //  by application execution arguments that will originally be provided through installation script.
    private const val DIRECTORY_INSTALLATION_LOCATION = "/usr/local/bin"

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
    fun load(operatingSystem: OperatingSystem) {

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

            initializeSystemVariables(config)

            val definitionsHomePath = FilePathBuilder()
                    .addContext(DIRECTORY_DEFINITIONS)
                    .getPath()

            val definitionsDirectory = File(definitionsHomePath)
            config.uses?.forEach { use ->

                log.v("Required dependency: $use")
            }

//            val groups = definitionsDirectory.list()
//            groups?.forEach { group ->
//
//                val groupPath = FilePathBuilder()
//                        .addContext(DIRECTORY_DEFINITIONS)
//                        .addContext(group)
//                        .getPath()
//
//                val groupFile = File(groupPath)
//                if (groupFile.isDirectory) {
//
//                    val groupDetailsPath = FilePathBuilder()
//                            .addContext(DIRECTORY_DEFINITIONS)
//                            .addContext(group)
//                            .addContext(Repository.REPOSITORY_DETAILS_FILE)
//                            .getPath()
//
//                    val wrapped = Group(group)
//                    val validator = GroupValidator()
//                    val groupDetailsFile = File(groupDetailsPath)
//
//                    if (groupDetailsFile.exists() && validator.validate(wrapped)) {
//
//                        log.i("Definitions group: $group")
//                        config.getConfigurationMap().forEach { (type, items) ->
//                            items?.let {
//
//                                val path = FilePathBuilder()
//                                        .addContext(DIRECTORY_DEFINITIONS)
//                                        .addContext(group)
//                                        .addContext(type.label)
//                                        .getPath()
//
//                                val home = System.getProperty("user.home")
//                                val homePath = FilePathBuilder()
//                                        .addContext(home)
//                                        .addContext(path)
//                                        .getPath()
//
//                                var directory = File(path)
//                                if (directory.absolutePath == homePath) {
//
//                                    val replaced = directory.absolutePath.replace(home, DIRECTORY_INSTALLATION_LOCATION)
//                                    directory = File(replaced)
//                                }
//                                findDefinitions(type, directory, it)
//
//                                it.forEach { item ->
//                                    val os = operatingSystem.getType().osName
//                                    val configurationPath = Configuration.getConfigurationFilePath(item)
//                                    val obtainedConfiguration = SoftwareConfiguration.obtain(configurationPath, os)
//                                    if (obtainedConfiguration.isEnabled()) {
//
//                                        val variables = obtainedConfiguration.variables
//                                        config.mergeVariables(variables)
//
//                                        val configurationItems = getConfigurationItems(type)
//                                        configurationItems.add(obtainedConfiguration)
//
//                                        log.i("${type.label} definition file: $item")
//                                        obtainedConfiguration.definition?.let { definition ->
//
//                                            log.i("${type.label} definition: $definition")
//                                        }
//                                    } else {
//
//                                        log.w("Disabled ${type.label.toLowerCase()} configuration: $configurationPath")
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//
//                        log.v("Skipping '$group', it is not a valid group directory")
//                    }
//                }
//            }

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
    private fun notLoadConfigurationCheck() {

        if (loaded.get()) {
            throw IllegalStateException("Configuration has been already loaded")
        }
    }

    private fun initializeSystemVariables(config: Configuration) {

        var node: Node? = null
        config.variables?.let {
            node = it
        }
        if (node == null) {
            node = Node()
        }

        val keyHome = Key.Home.key()
        val ctxSystem = Context.System.context()
        val ctxInstallation = Context.Installation.context()

        // TODO: MSG-283 - Prevent nodes overwriting if user has already defined it
        val systemHome = getHomeDirectory()
        val systemHomeNode = Node(name = keyHome, value = systemHome.absolutePath)
        val installationHomeNode = Node(name = keyHome, value = DIRECTORY_INSTALLATION_LOCATION)
        val installationVariables = mutableListOf(installationHomeNode)
        val installationNode = Node(name = ctxInstallation, children = installationVariables)
        val systemVariables = mutableListOf(systemHomeNode, installationNode)
        val systemNode = Node(name = ctxSystem, children = systemVariables)
        node?.children?.add(systemNode)
    }

    private fun getHomeDirectory(): File {

        val home = System.getProperty("user.home")
        val homePath = FilePathBuilder().addContext(home).getPath()
        var systemHome = File("")
        if (systemHome.absolutePath == homePath) {
            systemHome = File(DIRECTORY_INSTALLATION_LOCATION)
        }
        return systemHome
    }
}