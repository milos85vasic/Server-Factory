package net.milosvasic.factory.component.database.manager

import net.milosvasic.factory.common.Registration
import net.milosvasic.factory.common.busy.BusyWorker
import net.milosvasic.factory.common.initialization.Initializer
import net.milosvasic.factory.common.initialization.Termination
import net.milosvasic.factory.common.obtain.Instantiate
import net.milosvasic.factory.common.obtain.ObtainParametrized
import net.milosvasic.factory.component.database.*
import net.milosvasic.factory.execution.flow.callback.FlowCallback
import net.milosvasic.factory.execution.flow.implementation.initialization.InitializationFlow
import net.milosvasic.factory.log
import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.remote.Connection
import net.milosvasic.factory.validation.Validator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class DatabaseManager(entryPoint: Connection) :
        BusyWorker<DatabaseManager>(entryPoint),
        ObtainParametrized<DatabaseRequest, Database>,
        Registration<DatabaseRegistration>,
        Initializer,
        Termination {

    private val initialized = AtomicBoolean()
    private var registration: DatabaseRegistration? = null
    private val operation = DatabaseRegistrationOperation()
    private val databases = ConcurrentHashMap<Type, MutableMap<String, Database>>()

    companion object : Instantiate<DatabaseManager?> {

        private var manager: DatabaseManager? = null

        @Throws(IllegalStateException::class)
        override fun instantiate(): DatabaseManager? {

            if (manager == null) {

                throw IllegalStateException("Database manager is not initialized")
            }
            return manager
        }
    }

    private val initFlowCallback = object : FlowCallback {
        override fun onFinish(success: Boolean) {

            registration?.let {

                val db = it.database
                val name = db.name
                val type = db.type

                if (success) {

                    var dbs = databases[type]
                    if (dbs == null) {
                        dbs = mutableMapOf()
                        databases[type] = dbs
                    }
                    dbs[name] = it.database
                    log.i("$type database initialized: '$name'")
                } else {

                    log.e("Database initialization failed for ${type.type} database")
                }
                val result = OperationResult(operation, success)
                it.callback.onOperationPerformed(result)
            }
            registration = null
            free()
        }
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun initialize() {
        checkInitialized()
        busy()

        manager?.let {

            initialized.set(true)
            free()
            log.i("Database manager has been initialized")
            notify(true)
            return
        }

        // TODO: Command flow with data handler and onFinish callback
        manager = this
        initialized.set(true)
        free()
        log.i("Database manager has been initialized")
        notify(true)
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun register(what: DatabaseRegistration) {
        busy()

        registration = what
        val db = what.database
        InitializationFlow()
                .width(db)
                .onFinish(initFlowCallback)
                .run()
    }

    @Throws(IllegalArgumentException::class)
    override fun unRegister(what: DatabaseRegistration) {

        val db = what.database
        val name = db.name
        val type = db.type

        if (databases[type]?.get(name) == what.database) {
            databases[type]?.remove(name)?.terminate()

            val result = OperationResult(operation, true)
            what.callback.onOperationPerformed(result)
        } else {

            log.e("Database instance is not registered: $databases")
            val result = OperationResult(operation, false)
            what.callback.onOperationPerformed(result)
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun obtain(vararg param: DatabaseRequest): Database {

        Validator.Arguments.validateSingle(param)
        val request = param[0]
        val type = request.type
        val name = request.name
        databases[type]?.get(name)?.let {
            return it
        }
        throw IllegalArgumentException("No database registered for the type: ${type.type}")
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkInitialized() {
        if (isInitialized()) {
            throw IllegalStateException("Installer has been already initialized")
        }
    }

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun checkNotInitialized() {
        if (!isInitialized()) {
            throw IllegalStateException("Installer has not been initialized")
        }
    }

    override fun isInitialized() = initialized.get()

    @Synchronized
    @Throws(IllegalStateException::class)
    override fun terminate() {
        busy()
        log.v("Shutting down: $this")
        val pairs = mutableListOf<Pair<Type, String>>()
        val iterator = databases.keys.iterator()
        while (iterator.hasNext()) {
            val type = iterator.next()
            val keyIterator = databases[type]?.keys?.iterator()
            keyIterator?.let {
                while (it.hasNext()) {
                    val name = it.next()
                    val pair = Pair(type, name)
                    pairs.add(pair)
                }
            }
        }
        pairs.forEach { pair ->
            unRegister(pair.first, pair.second)
        }
        free()
    }

    private fun unRegister(type: Type, name: String) {
        databases[type]?.remove(name)?.terminate()
    }

    override fun notify(success: Boolean) {
        val operation = DatabaseManagerInitializationOperation()
        val result = OperationResult(operation, true)
        notify(result)
    }

    override fun onSuccessResult() {
        free(true)
    }

    override fun onFailedResult() {
        free(false)
    }
}