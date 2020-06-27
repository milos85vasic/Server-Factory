package net.milosvasic.factory.common.filesystem

import net.milosvasic.factory.common.Validation
import net.milosvasic.factory.validation.Validator
import net.milosvasic.factory.validation.parameters.SingleParameterExpectedException
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class FilePathValidator : Validation<FilePathBuilder> {

    private val builder = StringBuilder()

    @Throws(InvalidPathException::class, SingleParameterExpectedException::class)
    override fun validate(vararg what: FilePathBuilder): Boolean {

        Validator.Arguments.validateSingle(what)
        val builder = what[0]
        builder.getElements().forEach {

            Paths.get(it)
        }
        return true
    }
}