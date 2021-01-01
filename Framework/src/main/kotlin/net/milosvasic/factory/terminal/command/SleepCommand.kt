package net.milosvasic.factory.terminal.command

import net.milosvasic.factory.terminal.TerminalCommand

class SleepCommand(duration: Int) : TerminalCommand(Commands.sleep(duration))