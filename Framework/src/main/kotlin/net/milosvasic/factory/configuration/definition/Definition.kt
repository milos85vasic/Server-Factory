package net.milosvasic.factory.configuration.definition

import net.milosvasic.factory.common.filesystem.FilePathBuilder

data class Definition(

        val name: String,
        val group: String = "",
        val type: String = DefinitionType.Unknown.type,
        val version: String = DefinitionType.Unknown.type,
) {

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

        return "${builder.getPath()}:$version"
    }
}