package net.milosvasic.factory.configuration.definition

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable
import java.io.File

data class Definition(

        val name: String,
        val group: String = String.EMPTY,
        val type: String = DefinitionType.Unknown.type,
        val version: String = DefinitionType.Unknown.type,
) {

    companion object {

        const val VERSION_SEPARATOR = ":"
        const val CURRENT_DEFINITION = "this"
        const val DIRECTORY_ROOT = "Definitions"
        val PATH_SEPARATOR = FilePathBuilder().separator

        @Throws(IllegalArgumentException::class)
        fun fromString(string: String): Definition {

            val validator = DefinitionValidator()
            if (validator.validate(string)) {

                val params = string.split(VERSION_SEPARATOR)
                if (params.size == 1) {

                    throw IllegalArgumentException("Not supported: $string")
                } else {

                    val path = params[0]
                    val version = params[1]
                    val pathParams = path.split(PATH_SEPARATOR)

                    return Definition(pathParams[2], pathParams[0], pathParams[1], version)
                }
            }
            throw IllegalArgumentException("Invalid parameter: $string")
        }
    }

    fun getType(): DefinitionType {

        DefinitionType.values().forEach { definitionType ->
            if (type == definitionType.type) {
                return definitionType
            }
        }
        return DefinitionType.Unknown
    }

    @Throws(IllegalStateException::class)
    fun getHome(): File {

        val path = PathBuilder()
                .addContext(Context.System)
                .setKey(Key.Home)
                .build()

        val systemHome = Variable.get(path)
        val builder = FilePathBuilder()
                .addContext(systemHome)
                .addContext(DIRECTORY_ROOT)
                .addContext(group)
                .addContext(type)
                .addContext(name)
                .addContext(version)

        return File(builder.getPath())
    }

    override fun toString(): String {

        val builder = FilePathBuilder()
                .addContext(group)
                .addContext(getType().type)
                .addContext(name)

        return "${builder.getPath()}$VERSION_SEPARATOR$version"
    }
}