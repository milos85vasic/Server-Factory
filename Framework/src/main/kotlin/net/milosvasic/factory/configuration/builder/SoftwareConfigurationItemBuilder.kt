package net.milosvasic.factory.configuration.builder

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.Build
import net.milosvasic.factory.configuration.InstallationStepDefinition
import net.milosvasic.factory.configuration.SoftwareConfigurationItem
import net.milosvasic.factory.platform.Platform
import net.milosvasic.factory.validation.parameters.StringArgumentValidation

class SoftwareConfigurationItemBuilder : Build<SoftwareConfigurationItem> {

    private var name = String.EMPTY
    private var version = String.EMPTY
    private val installationSteps = mutableMapOf<String, List<InstallationStepDefinition>>()

    fun setName(name: String): SoftwareConfigurationItemBuilder {

        this.name = name
        return this
    }

    fun setVersion(version: String): SoftwareConfigurationItemBuilder {

        this.version = version
        return this
    }

    fun addInstallationStep(platform: Platform, definition: InstallationStepDefinition)
            : SoftwareConfigurationItemBuilder {

        // TODO:
        return this
    }

    @Throws(IllegalArgumentException::class)
    override fun build(): SoftwareConfigurationItem {

        val validator = StringArgumentValidation()
        if (!validator.validate(name)) {

            throw IllegalArgumentException("Empty name provided")
        }
        if (!validator.validate(version)) {

            throw IllegalArgumentException("Empty version provided")
        }
        return SoftwareConfigurationItem(name, version, installationSteps)
    }
}