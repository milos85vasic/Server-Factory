package net.milosvasic.factory.test.implementation

import net.milosvasic.factory.LOCALHOST
import net.milosvasic.factory.operation.command.CommandConfiguration
import net.milosvasic.factory.remote.Remote
import net.milosvasic.factory.remote.ssh.SSH
import net.milosvasic.factory.terminal.TerminalCommand

class StubSSH : SSH(
        Remote(host = LOCALHOST, port = 0, account = System.getProperty("user.name"))
) {

    @Synchronized
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun execute(what: TerminalCommand) {

        val command = StubSSHCommand(getRemote(), what)
        getTerminal().execute(command)
    }

    @Synchronized
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    override fun execute(data: TerminalCommand, obtainOutput: Boolean) {

        val command = StubSSHCommand(getRemote(), data)
        command.configuration[CommandConfiguration.OBTAIN_RESULT] = obtainOutput
        getTerminal().execute(command)
    }
}