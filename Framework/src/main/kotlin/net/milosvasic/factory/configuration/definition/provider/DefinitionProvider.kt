package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.definition.Definition

interface DefinitionProvider {

    fun load(definition: Definition): Boolean
}