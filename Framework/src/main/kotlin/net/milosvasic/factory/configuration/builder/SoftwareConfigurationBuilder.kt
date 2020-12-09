package net.milosvasic.factory.configuration.builder

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.Build
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.SoftwareConfigurationItem
import net.milosvasic.factory.platform.Platform
import net.milosvasic.factory.validation.Validator
import net.milosvasic.factory.validation.parameters.EmptyArgumentException

class SoftwareConfigurationBuilder : Build<SoftwareConfiguration> {

    private var enabled = true
    private var platform = Platform.UNKNOWN
    private var configuration = String.EMPTY
    private var software = mutableListOf<SoftwareConfigurationItem>()

    fun setEnabled(enabled: Boolean): SoftwareConfigurationBuilder {

        this.enabled = enabled
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setConfiguration(configuration: String): SoftwareConfigurationBuilder {

        if (Validator.Arguments.validateNotEmpty(configuration)) {

            this.configuration = configuration
            return this
        }
        throw EmptyArgumentException()
    }

    fun setPlatform(platform: Platform): SoftwareConfigurationBuilder {

        // TODO: Validate
        this.platform = platform
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun addSoftware(builder: SoftwareBuilder): SoftwareConfigurationBuilder {

        this.software.addAll(builder.build())
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setSoftware(builder: SoftwareBuilder): SoftwareConfigurationBuilder {

        this.software.clear()
        return addSoftware(builder)
    }

    @Throws(IllegalArgumentException::class)
    override fun build(): SoftwareConfiguration {

        // TODO: Validate
        val configuration = SoftwareConfiguration()
        configuration.enabled = true
        configuration.configuration = "Deployment dependencies"
        configuration.setPlatform(platform.platformName)
        configuration.software = software
        configuration.setPlatform(Platform.CENTOS.platformName)
        return configuration
    }
}