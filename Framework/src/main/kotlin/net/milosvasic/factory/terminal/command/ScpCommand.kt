package net.milosvasic.factory.terminal.command

import net.milosvasic.factory.remote.Remote
import net.milosvasic.factory.terminal.TerminalCommand

class ScpCommand
@Throws(IllegalStateException::class)
constructor(what: String, where: String, remote: Remote) :

        @Throws(IllegalStateException::class)
        TerminalCommand(Commands.scp(what, where, remote))