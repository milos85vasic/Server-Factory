package net.milosvasic.factory.common.filesystem

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.Validation
import net.milosvasic.factory.validation.Validator
import net.milosvasic.factory.validation.parameters.SingleParameterExpectedException
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class FilePathValidator : Validation<FilePathBuilder> {

    private val forbidden = listOf(" ", "\n", "\t")

    @Throws(InvalidPathException::class, SingleParameterExpectedException::class)
    override fun validate(vararg what: FilePathBuilder): Boolean {

        Validator.Arguments.validateSingle(what)
        val builder = what[0]
        builder.getElements().forEach {

            if (it == String.EMPTY) {

                return false
            }
            if (it.contains(builder.separator)) {

                return false
            }

            forbidden.forEach { forbid ->
                if (it.contains(forbid)) {
                    return false
                }
            }
            Paths.get(it)
        }
        return true
    }
}