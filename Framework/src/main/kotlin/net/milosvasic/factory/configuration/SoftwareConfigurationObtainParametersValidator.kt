package net.milosvasic.factory.configuration

import net.milosvasic.factory.common.Validation

class SoftwareConfigurationObtainParametersValidator : Validation<String> {

    override fun validate(vararg what: String) = what.size == 2
}