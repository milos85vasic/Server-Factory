package net.milosvasic.factory.component.docker.connection

import net.milosvasic.factory.component.docker.DockerCommand
import net.milosvasic.factory.terminal.TerminalCommand

class DockerServiceCommand(
        service: String,
        serviceCommand: String

) : TerminalCommand(

        "${DockerCommand.DOCKER.name} exec -ti $service sh -c \"$serviceCommand\""
)