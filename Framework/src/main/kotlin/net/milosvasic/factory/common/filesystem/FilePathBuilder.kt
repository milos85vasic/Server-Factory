package net.milosvasic.factory.common.filesystem

import net.milosvasic.factory.common.path.PathBuilder
import java.io.File
import java.nio.file.InvalidPathException

class FilePathBuilder : PathBuilder<String, String, String>() {

    private val builder = StringBuilder()

    override val separator: String
        get() = File.separator

    @Throws(InvalidPathException::class)
    override fun build(): String {

        val validator = FilePathValidator()
        if (validator.validate(this)) {

            contexts.forEachIndexed { index, context ->

                builder.append(context)
                if (index != contexts.lastIndex) {
                    builder.append(separator)
                }
            }
        }
        return builder.toString()
    }

    fun getElements() = contexts.toList()
}