package net.milosvasic.factory.application.server_factory

import net.milosvasic.factory.*
import net.milosvasic.factory.common.Application
import net.milosvasic.factory.common.busy.Busy
import net.milosvasic.factory.common.busy.BusyDelegation
import net.milosvasic.factory.common.busy.BusyException
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.initialization.Initializer
import net.milosvasic.factory.common.initialization.Termination
import net.milosvasic.factory.component.database.manager.DatabaseManager
import net.milosvasic.factory.component.docker.Docker
import net.milosvasic.factory.component.docker.DockerInitializationFlowCallback
import net.milosvasic.factory.component.installer.Installer
import net.milosvasic.factory.component.installer.InstallerInitializationFlowCallback
import net.milosvasic.factory.configuration.*
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.execution.flow.FlowBuilder
import net.milosvasic.factory.execution.flow.callback.DieOnFailureCallback
import net.milosvasic.factory.execution.flow.callback.TerminationCallback
import net.milosvasic.factory.execution.flow.implementation.CommandFlow
import net.milosvasic.factory.execution.flow.implementation.InstallationFlow
import net.milosvasic.factory.execution.flow.implementation.initialization.InitializationFlow
import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.operation.OperationResultListener
import net.milosvasic.factory.os.HostInfoDataHandler
import net.milosvasic.factory.os.HostNameDataHandler
import net.milosvasic.factory.remote.Connection
import net.milosvasic.factory.remote.ConnectionProvider
import net.milosvasic.factory.remote.ssh.SSH
import net.milosvasic.factory.terminal.TerminalCommand
import net.milosvasic.factory.terminal.command.*
import java.util.concurrent.ConcurrentLinkedQueue

abstract class ServerFactory(private val builder: ServerFactoryBuilder) : Application, BusyDelegation {

    protected var configuration: Configuration? = null

    private val busy = Busy()
    private val terminators = ConcurrentLinkedQueue<Termination>()
    private val terminationOperation = ServerFactoryTerminationOperation()
    private val subscribers = ConcurrentLinkedQueue<OperationResultListener>()
    private val initializationOperation = ServerFactoryInitializationOperation()
    private var configurations = mutableMapOf<SoftwareConfigurationType, MutableList<SoftwareConfiguration>>()

    private var connectionProvider: ConnectionProvider = object : ConnectionProvider {

        @Throws(IllegalArgumentException::class)
        override fun obtain(): Connection {
            configuration?.let { config ->
                return SSH(config.remote)
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
                ConfigurationManager.initialize()

                configuration = ConfigurationManager.getConfiguration()
                if (configuration == null) {
                    throw IllegalStateException("Configuration is null")
                }
                notifyInit()
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
            SoftwareConfigurationType.values().forEach { type ->

                val items = getConfigurationItems(type)
                items.clear()
            }
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
            val commandFlow = getCommandFlow(ssh, initFlow)

            commandFlow.run()
        } catch (e: IllegalArgumentException) {

            fail(e)
        } catch (e: IllegalStateException) {

            fail(e)
        }
    }

    override fun onStop() {
        log.i("Server factory finished")
        try {
            terminate()
        } catch (e: IllegalStateException) {
            log.e(e)
        }
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

    protected open fun getHostNameSetCommand(hostname: String): TerminalCommand = HostNameSetCommand(hostname)

    private fun notifyInit() {

        free()
        val result = OperationResult(initializationOperation, true)
        notify(result)
    }

    private fun notifyTerm() {
        free()
        val result = OperationResult(terminationOperation, true)
        notify(result)
    }

    @Synchronized
    private fun notifyInit(e: Exception) {
        free()
        log.e(e)
        val result = OperationResult(initializationOperation, false)
        notify(result)
    }

    @Synchronized
    private fun notifyTerm(e: Exception) {
        free()
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
        val items = getConfigurationItems(SoftwareConfigurationType.CONTAINERS)
        items.forEach { softwareConfiguration ->
            softwareConfiguration.software?.forEach { software ->
                dockerFlow.width(
                        SoftwareConfiguration(
                                docker.getOperatingSystem(),
                                softwareConfiguration.overrides,
                                softwareConfiguration.configuration,
                                softwareConfiguration.variables,
                                mutableListOf(software),
                                softwareConfiguration.includes
                        )
                )
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
    private fun getCommandFlow(ssh: Connection, initFlow: InitializationFlow): CommandFlow {

        val os = ssh.getRemoteOS()
        val hostname = getHostname()
        val host = ssh.getRemote().host
        val terminal = ssh.getTerminal()
        val pingCommand = PingCommand(host)
        val hostNameCommand = HostNameCommand()
        val hostInfoCommand = getHostInfoCommand()
        val testCommand = EchoCommand("Hello")
        val dieCallback = DieOnFailureCallback()

        val hostInfoDataHandler = object : HostInfoDataHandler(os) {

            override fun onData(data: OperationResult?) {
                super.onData(data)
                try {

                    ConfigurationManager.load()
                    configuration?.let { config ->
                        SoftwareConfigurationType.values().forEach { type ->

                            val configurationItems = ConfigurationManager.getConfigurationItems(type)
                            getConfigurationItems(type).addAll(configurationItems)
                        }
                        log.v(config.name)
                    }
                } catch (e: IllegalStateException) {

                    fail(e)
                }
            }
        }

        val flow = CommandFlow()
                .width(terminal)
                .perform(pingCommand)
                .width(ssh)
                .perform(testCommand)
                .perform(hostInfoCommand, hostInfoDataHandler)
                .perform(hostNameCommand, HostNameDataHandler(os))

        if (hostname != String.EMPTY) {

            flow.perform(getHostNameSetCommand(hostname), HostNameDataHandler(os, hostname))
        }

        return flow
                .onFinish(dieCallback)
                .connect(initFlow)
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

        var configurationItems = configurations[type]
        if (configurationItems == null) {
            configurationItems = mutableListOf()
            configurations[type] = configurationItems
        }
        return configurationItems
    }
}