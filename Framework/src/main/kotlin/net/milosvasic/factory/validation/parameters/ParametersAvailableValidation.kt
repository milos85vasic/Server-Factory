package net.milosvasic.factory.validation.parameters

class ParametersAvailableValidation <T> : ParametersValidation<T> {

    @Throws(ArgumentsExpectedException::class)
    override fun validate(vararg what: T): Boolean {

        if (what.isEmpty()) {
            throw ArgumentsExpectedException()
        }
        return true
    }
}