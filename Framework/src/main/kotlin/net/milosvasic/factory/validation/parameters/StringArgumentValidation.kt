package net.milosvasic.factory.validation.parameters

import net.milosvasic.factory.common.Validation

class StringArgumentValidation : Validation<String> {

    override fun validate(vararg what: String): Boolean {

        what.forEach {
            if (it.isEmpty() || it.isBlank()) {

                return false
            }
        }
        return true
    }
}