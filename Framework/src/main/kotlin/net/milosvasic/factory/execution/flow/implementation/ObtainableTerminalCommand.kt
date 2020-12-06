package net.milosvasic.factory.execution.flow.implementation

import net.milosvasic.factory.common.DataHandler
import net.milosvasic.factory.common.obtain.Obtain

import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.terminal.TerminalCommand

class ObtainableTerminalCommand(

    val obtainable: Obtain<TerminalCommand>,
    val dataHandler: DataHandler<OperationResult>
) : TerminalCommand("")