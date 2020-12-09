package net.milosvasic.factory.validation

import net.milosvasic.factory.validation.parameters.*

object Validator {

    object Arguments {

        @Throws(SingleParameterExpectedException::class)
        fun validateSingle(vararg params: Any) {

            val validation = SingleParameterValidation<Any>()
            validation.validate(*params)
        }

        @Throws(NoArgumentsExpectedException::class)
        fun validateEmpty(vararg params: Any) {

            val validation = NoParameterValidation<Any>()
            validation.validate(*params)
        }

        @Throws(ArgumentsExpectedException::class)
        fun validateNotEmpty(vararg params: Any) {

            val validation = ParametersAvailableValidation<Any>()
            validation.validate(*params)
        }

        fun validateNotEmpty(param: String): Boolean {

            val validation = StringArgumentValidation()
            return validation.validate(param)
        }
    }
}