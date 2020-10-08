package net.milosvasic.factory.configuration.definition

import net.milosvasic.factory.common.filesystem.FilePathBuilder

data class Definition(

        val name: String,
        val group: String = "",
        val type: String = DefinitionType.Unknown.type,
        val version: String = DefinitionType.Unknown.type,
) {

    companion object {

        const val VERSION_SEPARATOR = ":"
        val PATH_SEPARATOR = FilePathBuilder().separator

        @Throws(IllegalArgumentException::class)
        fun fromString(string: String): Definition {

            val validator = DefinitionValidator()
            if (validator.validate(string)) {

                val params = string.split(VERSION_SEPARATOR)
                val path = params[0]
                val version = params[1]
                val pathParams = path.split(PATH_SEPARATOR)

                return Definition(pathParams[2], pathParams[0], pathParams[1], version)
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

    override fun toString(): String {

        val builder = FilePathBuilder()
                .addContext(group)
                .addContext(getType().type)
                .addContext(name)

        return "${builder.getPath()}$VERSION_SEPARATOR$version"
    }
}