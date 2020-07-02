package net.milosvasic.factory.component.docker.connection

import net.milosvasic.factory.remote.Remote
import net.milosvasic.factory.remote.ssh.SSH
import net.milosvasic.factory.terminal.TerminalCommand

class DockerServiceConnection(
        private val service: String,
        remote: Remote

) : SSH(remote) {

    override fun filterCommand(command: TerminalCommand): TerminalCommand {

        val filtered = super.filterCommand(command)
        return DockerServiceCommand(service, filtered.command)
    }
}