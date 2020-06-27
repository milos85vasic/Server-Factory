package net.milosvasic.factory.common.filesystem

import net.milosvasic.factory.common.path.PathBuilder
import java.io.File
import java.nio.file.InvalidPathException

class FilePathBuilder : PathBuilder<String, String, String>() {

    override val separator: String
        get() = File.separator

    fun addContext(file: File): FilePathBuilder {
        addContext(file.absolutePath)
        return this
    }

    @Throws(InvalidPathException::class)
    override fun build(): String {

        val validator = FilePathValidator()
        if (validator.validate(this)) {

            return getPath()
        }
        throw InvalidPathException(getPath(), "Path is invalid")
    }

    fun getElements() = contexts.toList()

    fun getPath(): String {

        val builder = StringBuilder()
        contexts.forEachIndexed { index, context ->

            builder.append(context)
            if (index != contexts.lastIndex) {
                builder.append(separator)
            }
        }
        return builder.toString()
    }
}