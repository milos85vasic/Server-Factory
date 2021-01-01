package net.milosvasic.factory.configuration

import net.milosvasic.factory.platform.Platform

data class InstallationSteps(val platform: Platform, val items: List<InstallationStepDefinition>)