package net.milosvasic.factory.terminal.command

import net.milosvasic.factory.terminal.TerminalCommand

class IpAddressObtainCommand(host: String) : TerminalCommand(Commands.getIpAddress(host))