package net.milosvasic.factory.test.implementation

import net.milosvasic.factory.remote.Remote
import net.milosvasic.factory.remote.ssh.SSHCommand
import net.milosvasic.factory.terminal.TerminalCommand

class StubSSHCommand
@Throws(IllegalStateException::class)
constructor(remote: Remote, command: TerminalCommand) :
        @Throws(IllegalStateException::class)
        SSHCommand(remote, command, sshCommand = command.command)
