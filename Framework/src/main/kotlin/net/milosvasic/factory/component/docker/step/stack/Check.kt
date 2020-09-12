package net.milosvasic.factory.component.docker.step.stack

import net.milosvasic.factory.component.installer.step.CommandInstallationStep
import net.milosvasic.factory.terminal.TerminalCommand

open class Check(
        containerName: String,
        checkCommand: TerminalCommand = CheckCommand(containerName, 120)

) : CommandInstallationStep(checkCommand)