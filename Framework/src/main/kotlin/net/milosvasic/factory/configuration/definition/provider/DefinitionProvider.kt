package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.Configuration
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.platform.OperatingSystem

abstract class DefinitionProvider(

        protected val configuration: Configuration,
        protected val operatingSystem: OperatingSystem
) {

    abstract fun load(definition: Definition): MutableList<SoftwareConfiguration>
}