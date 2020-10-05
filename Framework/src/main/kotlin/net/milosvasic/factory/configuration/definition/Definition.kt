package net.milosvasic.factory.configuration.definition

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
}