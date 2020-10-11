package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.definition.Definition

class FilesystemDefinitionProvider : DefinitionProvider {

    override fun load(definition: Definition): Boolean {

        return false
    }
}