package net.milosvasic.factory.component.installer.step.certificate

import net.milosvasic.factory.terminal.TerminalCommand
import net.milosvasic.factory.terminal.command.Commands
import java.nio.file.InvalidPathException

class GeneratePrivateKeyCommand

@Throws(InvalidPathException::class)
constructor(path: String, name: String) : TerminalCommand(

        Commands.generatePrivateKey(path, name)
)