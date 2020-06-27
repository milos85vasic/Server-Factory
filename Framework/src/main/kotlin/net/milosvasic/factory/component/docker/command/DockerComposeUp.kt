package net.milosvasic.factory.component.docker.command

import net.milosvasic.factory.component.docker.DockerCommand
import net.milosvasic.factory.terminal.TerminalCommand

class DockerComposeUp

@Throws(IllegalArgumentException::class, IllegalStateException::class)
constructor(path: String, flags: String) : TerminalCommand(

        "${DockerCommand.COMPOSE.obtain()} -f $path ${DockerCommand.UP.obtain()} $flags"
)