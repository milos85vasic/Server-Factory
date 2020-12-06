package net.milosvasic.factory.application.server_factory

import net.milosvasic.factory.*
import net.milosvasic.factory.common.Application
import net.milosvasic.factory.common.busy.Busy
import net.milosvasic.factory.common.busy.BusyDelegation
import net.milosvasic.factory.common.busy.BusyException
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.common.initialization.Initializer
import net.milosvasic.factory.common.initialization.Termination
import net.milosvasic.factory.common.obtain.Obtain
import net.milosvasic.factory.component.database.manager.DatabaseManager
import net.milosvasic.factory.component.docker.Docker
import net.milosvasic.factory.component.docker.DockerInitializationFlowCallback
import net.milosvasic.factory.component.installer.Installer
import net.milosvasic.factory.component.installer.InstallerInitializationFlowCallback
import net.milosvasic.factory.component.installer.step.deploy.Deploy
import net.milosvasic.factory.configuration.*
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.execution.TaskExecutor
import net.milosvasic.factory.execution.flow.FlowBuilder
import net.milosvasic.factory.execution.flow.callback.DieOnFailureCallback
import net.milosvasic.factory.execution.flow.callback.FlowCallback
import net.milosvasic.factory.execution.flow.callback.TerminationCallback
import net.milosvasic.factory.execution.flow.implementation.CommandFlow
import net.milosvasic.factory.execution.flow.implementation.InstallationFlow
import net.milosvasic.factory.execution.flow.implementation.initialization.InitializationFlow
import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.operation.OperationResultListener
import net.milosvasic.factory.platform.HostInfoDataHandler
import net.milosvasic.factory.platform.HostNameDataHandler
import net.milosvasic.factory.platform.OperatingSystem
import net.milosvasic.factory.remote.Connection
import net.milosvasic.factory.remote.ConnectionProvider
import net.milosvasic.factory.remote.ssh.SSH
import net.milosvasic.factory.terminal.TerminalCommand
import net.milosvasic.factory.terminal.command.*
import java.awt.HeadlessException
import java.awt.MouseInfo
import java.awt.Robot
import java.util.concurrent.ConcurrentLinkedQueue


abstract class ServerFactory(private val builder: ServerFactoryBuilder) : Application, BusyDelegation {

    protected var configuration: Configuration? = null

    private val busy = Busy()
    private var runStartedAt = 0L
    private val executor = TaskExecutor.instantiate(5)
    private val terminators = ConcurrentLinkedQueue<Termination>()
    private val connectionPool = mutableMapOf<String, Connection>()
    private var configurations = mutableListOf<SoftwareConfiguration>()
    private val terminationOperation = ServerFactoryTerminationOperation()
    private val subscribers = ConcurrentLinkedQueue<OperationResultListener>()
    private val initializationOperation = ServerFactoryInitializationOperation()

    private var connectionProvider: ConnectionProvider = object : ConnectionProvider {

        @Throws(IllegalArgumentException::class)
        override fun obtain(): Connection {
            configuration?.let { config ->

                val key = config.remote.toString()
                connectionPool[key]?.let {
                    return it
                }
                val connection = SSH(config.remote)
                connectionPool[key] = connection
                return connection
            }
            throw IllegalArgumentException("No valid configuration available for creating a connection")
        }
    }

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun initialize() {
        checkInitialized()
        busy()
        try {

            tag = getLogTag()
            builder.getLogger()?.let {

                compositeLogger.addLogger(it)
            }
            try {

                ConfigurationManager.setConfigurationRecipe(builder.getRecipe())
                ConfigurationManager.setConfigurationFactory(getConfigurationFactory())
                ConfigurationManager.setInstallationLocation(builder.getInstallationLocation())
                ConfigurationManager.initialize()

                configuration = ConfigurationManager.getConfiguration()
                if (configuration == null) {

                    throw IllegalStateException("Configuration is null")
                }

                val ssh = getConnection()

                val callback = object : FlowCallback {
                    override fun onFinish(success: Boolean) {
                        if (success) {
                            try {

                                ConfigurationManager.load(ssh.getRemoteOS())
                                configuration?.let { config ->

                                    val configurationItems = ConfigurationManager.getConfigurationItems()
                                    configurations.addAll(configurationItems)
                                    log.v(config.name)
                                }
                                notifyInit()
                            } catch (e: IllegalStateException) {

                                notifyInit(e)
                            } catch (e: IllegalArgumentException) {

                                notifyInit(e)
                            }
                        } else {

                            val e = IllegalStateException("Initialization failure")
                            notifyInit(e)
                        }
                    }
                }

                getCommandFlow(ssh, DieOnFailureCallback())
                        .onFinish(callback)
                        .run()
            } catch (e: IllegalArgumentException) {

                notifyInit(e)
            } catch (e: IllegalStateException) {

                notifyInit(e)
            } catch (e: RuntimeException) {

                notifyInit(e)
            }
        } catch (e: IllegalArgumentException) {

            notifyInit(e)
        }
    }

    @Throws(IllegalStateException::class)
    override fun terminate() {
        checkNotInitialized()
        if (!busy.isBusy()) {
            throw IllegalStateException("Server factory is not running")
        }
        try {
            terminators.forEach {
                it.terminate()
            }
            configuration = null
            configurations.clear()
            notifyTerm()
        } catch (e: IllegalStateException) {
            notifyTerm(e)
        }
    }

    @Synchronized
    override fun isInitialized(): Boolean {
        try {
            ConfigurationManager.getConfiguration()
            return true
        } catch (e: IllegalStateException) {
            log.w(e)
        }
        return false
    }

    @Throws(IllegalStateException::class)
    override fun run() {
        checkNotInitialized()
        busy()
        if (configuration == null) {
            throw IllegalStateException("Configuration is null")
        }
        log.i("Server factory started")
        runStartedAt = System.currentTimeMillis()
        try {

            val ssh = getConnection()
            val docker = instantiateDocker(ssh)
            val installer = instantiateInstaller(ssh)
            val databaseManager = getDatabaseManager(ssh)

            terminators.add(docker)
            terminators.add(installer)
            terminators.add(databaseManager)

            val terminationFlow = getTerminationFlow(ssh)
            val loadDbsFlow = databaseManager.loadDatabasesFlow().connect(terminationFlow)
            val dockerFlow = getDockerFlow(docker, loadDbsFlow)
            val dockerInitFlow = getDockerInitFlow(docker, dockerFlow)
            val installationFlow = getInstallationFlow(installer, dockerInitFlow) ?: dockerInitFlow
            val initializers = listOf<Initializer>(installer, databaseManager)
            val initFlow = getInitializationFlow(initializers, installationFlow)

            initFlow.run()
        } catch (e: IllegalArgumentException) {

            fail(e)
        } catch (e: IllegalStateException) {

            fail(e)
        }
    }

    override fun onStop() {

        val duration = getDuration()
        log.i("Server factory finished in: $duration")
    }

    @Synchronized
    @Throws(BusyException::class)
    override fun busy() {
        BusyWorker.busy(busy)
    }

    @Synchronized
    override fun free() {
        BusyWorker.free(busy)
    }

    fun isBusy() = busy.isBusy()

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkInitialized() {
        if (ConfigurationManager.isInitialized()) {
            throw IllegalStateException("Server factory has been already initialized")
        }
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkNotInitialized() {
        if (!ConfigurationManager.isInitialized()) {
            throw IllegalStateException("Server factory has not been initialized")
        }
    }

    override fun subscribe(what: OperationResultListener) {
        subscribers.add(what)
    }

    override fun unsubscribe(what: OperationResultListener) {
        subscribers.remove(what)
    }

    @Synchronized
    override fun notify(data: OperationResult) {
        val iterator = subscribers.iterator()
        while (iterator.hasNext()) {
            val listener = iterator.next()
            listener.onOperationPerformed(data)
        }
    }

    fun setConnectionProvider(provider: ConnectionProvider) {
        connectionProvider = provider
    }

    protected abstract fun getConfigurationFactory(): ConfigurationFactory<*>

    @Throws(IllegalArgumentException::class)
    protected fun getConnection(): Connection {
        return connectionProvider.obtain()
    }

    protected open fun getLogTag() = tag

    protected open fun instantiateDocker(ssh: Connection) = Docker(ssh)

    protected open fun instantiateInstaller(ssh: Connection) = Installer(ssh)

    protected open fun getHostInfoCommand(): TerminalCommand = HostInfoCommand()

    protected open fun getDatabaseManager(ssh: Connection) = DatabaseManager(ssh)

    protected open fun getHostInfoDataHandler(os: OperatingSystem) = HostInfoDataHandler(os)

    protected open fun getHostNameSetCommand(hostname: String): TerminalCommand = HostNameSetCommand(hostname)

    private fun notifyInit() {

        free()
        val result = OperationResult(initializationOperation, true)
        notify(result)
        keepAlive()
    }

    @Synchronized
    private fun notifyInit(e: Exception) {

        free()
        log.e(e)
        val result = OperationResult(initializationOperation, false)
        notify(result)
    }

    private fun notifyTerm() {

        free()
        onStop()
        val result = OperationResult(terminationOperation, true)
        notify(result)
    }

    @Synchronized
    private fun notifyTerm(e: Exception) {

        free()
        onStop()
        log.e(e)
        val result = OperationResult(terminationOperation, false)
        notify(result)
    }

    private fun getInstallationFlow(installer: Installer, dockerInitFlow: InitializationFlow): InstallationFlow? {

        val items = getConfigurationItems(SoftwareConfigurationType.SOFTWARE)
        if (items.isEmpty()) {
            return null
        }
        val installFlow = InstallationFlow(installer)
        val dieCallback = DieOnFailureCallback()
        items.forEach {

            installFlow.width(it)
        }
        return installFlow
                .connect(dockerInitFlow)
                .onFinish(dieCallback)
    }

    private fun getDockerFlow(docker: Docker, terminationFlow: FlowBuilder<*, *, *>): InstallationFlow {

        val dockerFlow = InstallationFlow(docker)
        val items = getConfigurationItems(SoftwareConfigurationType.DOCKER)
        items.forEach { softwareConfiguration ->
            softwareConfiguration.software?.forEach { software ->

                val configuration = SoftwareConfiguration(

                        softwareConfiguration.definition,
                        softwareConfiguration.uses,
                        softwareConfiguration.overrides,
                        softwareConfiguration.configuration,
                        softwareConfiguration.variables,
                        mutableListOf(software),
                        softwareConfiguration.includes
                )
                val platformName = getConnection().getRemoteOS().getPlatform().platformName
                configuration.setPlatform(platformName)
                dockerFlow.width(configuration)
            }
        }

        dockerFlow.connect(terminationFlow)
        return dockerFlow
    }

    protected open fun getTerminationFlow(connection: Connection): FlowBuilder<*, *, *> {

        return CommandFlow()
                .width(connection.getTerminal())
                .perform(EchoCommand("Finishing"))
                .onFinish(TerminationCallback(this))
    }

    protected open fun getCoreUtilsDeploymentFlow(

        what: String,
        where: String,
        ssh: Connection

    ) = Deploy(what, where)
        .setConnection(ssh)
        .getFlow()

    protected open fun getIpAddressObtainCommand(os: OperatingSystem) =
        object : Obtain<TerminalCommand> {

            override fun obtain() = IpAddressObtainCommand(os.getHostname())
        }

    private fun getDockerInitFlow(docker: Docker, dockerFlow: InstallationFlow): InitializationFlow {

        val initCallback = DockerInitializationFlowCallback()
        return InitializationFlow()
                .width(docker)
                .connect(dockerFlow)
                .onFinish(initCallback)
    }

    @Throws(IllegalArgumentException::class)
    private fun getInitializationFlow(
            initializers: List<Initializer>,
            nextFlow: FlowBuilder<*, *, *>

    ): InitializationFlow {

        if (initializers.isEmpty()) {

            throw IllegalArgumentException("Initializers are not provided")
        }

        val initCallback = InstallerInitializationFlowCallback()
        val flow = InitializationFlow()
        initializers.forEach {
            flow.width(it)
        }
        flow.connect(nextFlow).onFinish(initCallback)
        return flow
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    private fun getCommandFlow(ssh: Connection, dieCallback: DieOnFailureCallback): CommandFlow {

        val os = ssh.getRemoteOS()
        val hostname = getHostname()
        val host = ssh.getRemote().getHost()
        val terminal = ssh.getTerminal()
        val pingCommand = PingCommand(host)
        val hostNameCommand = HostNameCommand()
        val hostInfoCommand = getHostInfoCommand()
        val testCommand = EchoCommand("Hello")

        /*
         * {
         *  "type": "deploy",
         *  "value": "{{SYSTEM.HOME}}/Core/Utils:{{SERVER.SERVER_HOME}}/Utils"
         * }
         */
        val systemHomePath = PathBuilder()
                .addContext(Context.System)
                .setKey(Key.Home)
                .build()

        val systemHome = Variable.get(systemHomePath)

        val what = FilePathBuilder()
                .addContext(systemHome)
                .addContext(Commands.DIRECTORY_CORE)
                .addContext(Commands.DIRECTORY_UTILS)
                .build()

        val whereRootPath = PathBuilder()
                .addContext(Context.Server)
                .setKey(Key.ServerHome)
                .build()

        val whereRoot = Variable.get(whereRootPath)

        val where = FilePathBuilder()
                .addContext(whereRoot)
                .addContext(Commands.DIRECTORY_UTILS)
                .build()

        val getIpCommand = getIpAddressObtainCommand(os)

        val coreUtilsDeployment = getCoreUtilsDeploymentFlow(what, where, ssh)
                .perform(hostInfoCommand, getHostInfoDataHandler(os))
                .perform(hostNameCommand, HostNameDataHandler(os))
                .perform(getIpCommand)

        if (hostname != String.EMPTY) {

            coreUtilsDeployment.perform(getHostNameSetCommand(hostname), HostNameDataHandler(os, hostname))
        }

        val flow = CommandFlow()
                .width(terminal)
                .perform(pingCommand)
                .width(ssh)
                .perform(testCommand)
                .connect(coreUtilsDeployment)

        return flow.onFinish(dieCallback)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    private fun getHostname(): String {
        val path = PathBuilder()
                .addContext(Context.Server)
                .setKey(Key.Hostname)
                .build()

        val hostname = Variable.get(path)
        if (hostname == String.EMPTY) {

            throw IllegalArgumentException("Empty hostname obtained for the server")
        }
        return hostname
    }

    private fun getConfigurationItems(type: SoftwareConfigurationType): MutableList<SoftwareConfiguration> {

        val typeDocker = SoftwareConfigurationType.DOCKER
        val items = mutableListOf<SoftwareConfiguration>()
        configurations.forEach { item ->
            item.software?.let { softwareItems ->
                softwareItems.forEach { softwareItem ->
                    when (type) {
                        typeDocker -> {
                            if (softwareItem.hasInstallationSteps(type.label)) {
                                if (!items.contains(item)) {
                                    items.add(item)
                                }
                            }
                        }
                        else -> {
                            if (!softwareItem.hasInstallationSteps(typeDocker.label)) {
                                if (!items.contains(item)) {
                                    items.add(item)
                                }
                            }
                        }
                    }
                }
            }
        }
        return items
    }

    private fun getDuration(): String {

        val duration = System.currentTimeMillis() - runStartedAt
        val seconds = (duration / 1000).toInt() % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60)) % 24

        val minutesFormatted = if (minutes == 0L) {

            if (hours == 0L) {
                ""
            } else {
                String.format("%02d", minutes) + " min. : "
            }
        } else {

            String.format("%02d", minutes) + " min. : "
        }

        val hoursFormatted = if (hours == 0L) {
            ""
        } else {
            String.format("%02d", hours) + " hr. : "
        }

        val secondsFormatted = String.format("%02d", seconds) + " sec."

        return "$hoursFormatted$minutesFormatted$secondsFormatted"
    }

    private fun keepAlive() = executor.execute {

        log.i("Keep alive: START")
        var count = 0
        val robot = Robot()
        while (isInitialized()) {

            count++
            try {

                val pointerInfo = MouseInfo.getPointerInfo()
                val location = pointerInfo.location
                val x = location.getX().toInt() + 1
                val y = location.getY().toInt() + 1
                robot.delay(60 * 1000)
                if (isInitialized()) {

                    robot.mouseMove(x, y)
                    log.v("Keep alive, count: $count")
                }
            } catch (e: HeadlessException) {

                log.e(e)
            }
        }
    }
}