package net.milosvasic.factory.test.implementation

import net.milosvasic.factory.component.database.manager.DatabaseManager
import net.milosvasic.factory.execution.flow.implementation.CommandFlow
import net.milosvasic.factory.remote.Connection
import net.milosvasic.factory.terminal.command.EchoCommand

class StubDatabaseManager(entryPoint: Connection) : DatabaseManager(entryPoint) {

    override fun loadDatabasesFlow() = CommandFlow()
            .width(entryPoint)
            .perform(EchoCommand("Stub database manager"))
}