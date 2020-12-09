package net.milosvasic.factory.configuration

import net.milosvasic.factory.common.Validation
import net.milosvasic.factory.validation.Validator

class SoftwareConfigurationValidator : Validation<SoftwareConfiguration> {

    @Throws(IllegalArgumentException::class)
    override fun validate(vararg what: SoftwareConfiguration): Boolean {

        Validator.Arguments.validateNotEmpty(*what)
        what.forEach {

            // TODO:
        }
        return true
    }
}