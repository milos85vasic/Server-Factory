package net.milosvasic.factory.component.installer.step.deploy

import net.milosvasic.factory.common.Validation
import net.milosvasic.factory.validation.Validator

class DeployValidator : Validation<String> {

    @Throws(IllegalArgumentException::class)
    override fun validate(vararg what: String): Boolean {

        Validator.Arguments.validateSingle(what)
        val arg = what[0]
        val split = arg.split(Deploy.DELIMITER_FROM_TO)
        if (split.isEmpty()) {
            throw IllegalArgumentException("No delimited parameters available in form: 'from:to'")
        }
        if (split.size != 2 && split.size != 3) {
            throw IllegalArgumentException("No valid delimited parameters available in form: 'from:to'")
        }
        return true
    }
}