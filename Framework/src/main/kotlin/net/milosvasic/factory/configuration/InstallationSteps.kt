package net.milosvasic.factory.configuration

import net.milosvasic.factory.os.OSType

data class InstallationSteps(val os: OSType, val items: List<InstallationStepDefinition>)