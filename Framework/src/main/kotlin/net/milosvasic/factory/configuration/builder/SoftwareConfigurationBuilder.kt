package net.milosvasic.factory.configuration.builder

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.Build
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.SoftwareConfigurationItem
import net.milosvasic.factory.configuration.SoftwareConfigurationValidator
import net.milosvasic.factory.platform.Platform
import net.milosvasic.factory.platform.PlatformValidator
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

    @Throws(IllegalArgumentException::class)
    fun setPlatform(platform: Platform): SoftwareConfigurationBuilder {

        val platformValidator = PlatformValidator()
        if (platformValidator.validate(platform)) {

            this.platform = platform
            return this
        }
        throw IllegalArgumentException("Invalid platform: ${platform.platformName}")
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

        val softwareConfiguration = SoftwareConfiguration()
        softwareConfiguration.enabled = true
        softwareConfiguration.software = software
        softwareConfiguration.configuration = configuration
        softwareConfiguration.setPlatform(platform.platformName)
        val validator = SoftwareConfigurationValidator()
        if (validator.validate(softwareConfiguration)) {

            return softwareConfiguration
        }
        throw IllegalArgumentException("Invalid software configuration: $softwareConfiguration")
    }
}